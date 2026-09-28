package net.xuwu.jeiattribute.jei;

import mezz.jei.api.ingredients.IIngredientType;

public final class AttributeIngredientType implements IIngredientType<AttributeIngredient> {
    public static final AttributeIngredientType INSTANCE = new AttributeIngredientType();

    private AttributeIngredientType() {
    }

    @Override
    public Class<? extends AttributeIngredient> getIngredientClass() {
        return AttributeIngredient.class;
    }

    @Override
    public String getUid() {
        return "jei_attribute:attribute";
    }
}
