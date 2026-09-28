package net.xuwu.jeiattribute.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record AttributePage(AttributeIngredient attribute, List<ItemStack> sourceItems) {
    public AttributePage {
        sourceItems = sourceItems.stream().map(ItemStack::copy).toList();
    }

    public boolean hasSourceItems() {
        return !sourceItems.isEmpty();
    }
}
