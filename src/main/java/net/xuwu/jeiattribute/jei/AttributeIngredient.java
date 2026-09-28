package net.xuwu.jeiattribute.jei;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.client.resources.language.I18n;

public record AttributeIngredient(ResourceLocation id) {
    public static final Codec<AttributeIngredient> CODEC =
            ResourceLocation.CODEC.xmap(AttributeIngredient::new, AttributeIngredient::id);

    public static AttributeIngredient of(Attribute attribute) {
        return new AttributeIngredient(BuiltInRegistries.ATTRIBUTE.getKey(attribute));
    }

    public Attribute attribute() {
        return BuiltInRegistries.ATTRIBUTE.getOptional(id).orElse(null);
    }

    public Component name() {
        Attribute attribute = attribute();
        return attribute == null ? Component.literal(id.toString()) : Component.translatable(attribute.getDescriptionId());
    }

    public Component description() {
        Attribute attribute = attribute();
        if (attribute == null) {
            return Component.translatable("jei_attribute.description.empty");
        }
        String key = attribute.getDescriptionId() + ".desc";
        return I18n.exists(key) ? Component.translatable(key) : Component.translatable("jei_attribute.description.empty");
    }
}
