package net.xuwu.jeiattribute.jei;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

final class CuriosAttributeSupport {
    private static final UUID VIEWER_ID = UUID.fromString("2a642c7f-7f21-4d44-b11f-4545ef1b1681");

    private CuriosAttributeSupport() {
    }

    @SuppressWarnings("removal")
    static Set<ResourceLocation> getAttributeIds(ItemStack stack) {
        Set<ResourceLocation> result = new TreeSet<>();
        Map<String, ?> slots = CuriosApi.getItemStackSlots(stack);
        for (String slotId : slots.keySet()) {
            SlotContext context = new SlotContext(slotId, null, -1, false, true);
            CuriosApi.getAttributeModifiers(context, VIEWER_ID, stack).keySet().forEach(holder -> addId(result, holder));
        }
        return result;
    }

    private static void addId(Set<ResourceLocation> result, Attribute attribute) {
        ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
        if (id != null) {
            result.add(id);
        }
    }
}
