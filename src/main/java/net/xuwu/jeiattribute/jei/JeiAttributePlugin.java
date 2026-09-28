package net.xuwu.jeiattribute.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.xuwu.jeiattribute.JeiAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@JeiPlugin
public final class JeiAttributePlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(JeiAttribute.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        List<AttributeIngredient> attributes = BuiltInRegistries.ATTRIBUTE.keySet().stream()
                .sorted()
                .map(AttributeIngredient::new)
                .toList();
        registration.register(
                AttributeIngredientType.INSTANCE,
                attributes,
                new AttributeIngredientHelper(),
                new AttributeIngredientRenderer());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new AttributePageCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Map<ResourceLocation, List<ItemStack>> sourceItems = new TreeMap<>();

        boolean curiosLoaded = ModList.get().isLoaded("curios");
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            Set<ResourceLocation> attributeIds = new TreeSet<>();

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                stack.getAttributeModifiers(slot).keySet().forEach(attribute -> {
                    ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
                    if (id != null) {
                        attributeIds.add(id);
                    }
                });
            }

            if (curiosLoaded) {
                attributeIds.addAll(CuriosAttributeSupport.getAttributeIds(stack));
            }

            for (ResourceLocation attributeId : attributeIds) {
                sourceItems.computeIfAbsent(attributeId, ignored -> new ArrayList<>()).add(stack.copy());
            }
        }

        List<AttributePage> pages = new ArrayList<>();
        for (ResourceLocation attributeId : BuiltInRegistries.ATTRIBUTE.keySet().stream().sorted().toList()) {
            pages.add(new AttributePage(
                    new AttributeIngredient(attributeId),
                    sourceItems.getOrDefault(attributeId, List.of())));
        }

        registration.addRecipes(AttributePageCategory.TYPE, pages);
    }
}
