package net.xuwu.jeiattribute.jei;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;

public final class AttributeIngredientHelper implements IIngredientHelper<AttributeIngredient> {
    @Override
    public IIngredientType<AttributeIngredient> getIngredientType() {
        return AttributeIngredientType.INSTANCE;
    }

    @Override
    public String getDisplayName(AttributeIngredient ingredient) {
        return ingredient.name().getString();
    }

    @Override
    @SuppressWarnings("removal")
    public String getUniqueId(AttributeIngredient ingredient, UidContext context) {
        return ingredient.id().toString();
    }

    @Override
    public ResourceLocation getResourceLocation(AttributeIngredient ingredient) {
        return ingredient.id();
    }

    @Override
    public AttributeIngredient copyIngredient(AttributeIngredient ingredient) {
        return ingredient;
    }

    @Override
    public String getErrorInfo(AttributeIngredient ingredient) {
        return ingredient.id().toString();
    }
}
