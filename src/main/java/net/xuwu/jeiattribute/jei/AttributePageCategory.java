package net.xuwu.jeiattribute.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.xuwu.jeiattribute.JeiAttribute;

import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

public final class AttributePageCategory implements IRecipeCategory<AttributePage> {
    public static final RecipeType<AttributePage> TYPE = RecipeType.create(
            JeiAttribute.MOD_ID, "attribute", AttributePage.class);

    private static final int WIDTH = 180;
    private static final int HEIGHT = 132;

    private final IDrawable background;
    private final IDrawable icon;
    private final AttributeIngredientRenderer ingredientRenderer = new AttributeIngredientRenderer();

    public AttributePageCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(Items.NETHER_STAR));
    }

    @Override
    public RecipeType<AttributePage> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei_attribute.category.attribute");
    }

    @Override
    @SuppressWarnings("removal")
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AttributePage page, IFocusGroup focuses) {
        builder.addInputSlot(4, 4)
                .setCustomRenderer(AttributeIngredientType.INSTANCE, ingredientRenderer)
                .addIngredient(AttributeIngredientType.INSTANCE, page.attribute());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT)
                .addIngredient(AttributeIngredientType.INSTANCE, page.attribute());

        if (page.hasSourceItems()) {
            builder.addOutputSlot(156, 4).addItemStacks(page.sourceItems());
            // Keep each source item searchable from JEI's Uses view as well.
            builder.addInvisibleIngredients(RecipeIngredientRole.INPUT).addItemStacks(page.sourceItems());
        }
    }

    @Override
    public void draw(AttributePage page, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, page.attribute().name(), 4, 36, 0xFFFFFFFF, false);

        if (page.hasSourceItems()) {
            Component sourceItemsLabel = Component.translatable("jei_attribute.source_items");
            graphics.drawString(font, sourceItemsLabel,
                    WIDTH - font.width(sourceItemsLabel) - 4, 22, 0xFFFFFFFF, false);
        }

        List<FormattedCharSequence> descriptionLines = font.split(page.attribute().description(), WIDTH - 8);
        int visibleDescriptionLines = Math.min(descriptionLines.size(), 4);
        for (int i = 0; i < visibleDescriptionLines; i++) {
            graphics.drawString(font, descriptionLines.get(i), 4, 49 + i * 10, 0xFF000000, false);
        }

        Component playerValue = getPlayerData(page.attribute()).value();
        List<FormattedCharSequence> valueLines = font.split(playerValue, WIDTH - 8);
        int valueY = 54 + visibleDescriptionLines * 10;
        for (int i = 0; i < valueLines.size(); i++) {
            graphics.drawString(font, valueLines.get(i), 4, valueY + i * 10, 0xFFAAAAAA, false);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, AttributePage page, IRecipeSlotsView slots, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        PlayerAttributeData data = getPlayerData(page.attribute());
        if (data.details().isEmpty()) {
            return;
        }

        int descriptionLines = Math.min(font.split(page.attribute().description(), WIDTH - 8).size(), 4);
        int valueY = 54 + descriptionLines * 10;
        List<FormattedCharSequence> valueLines = font.split(data.value(), WIDTH - 8);
        int valueWidth = valueLines.stream().mapToInt(font::width).max().orElse(0);
        int valueHeight = valueLines.size() * 10;
        if (mouseX >= 4 && mouseX < 4 + valueWidth && mouseY >= valueY && mouseY < valueY + valueHeight) {
            data.details().forEach(tooltip::add);
        }
    }

    private static PlayerAttributeData getPlayerData(AttributeIngredient ingredient) {
        Player player = Minecraft.getInstance().player;
        Attribute attribute = BuiltInRegistries.ATTRIBUTE.getOptional(ingredient.id()).orElse(null);
        if (player == null || attribute == null) {
            return new PlayerAttributeData(Component.translatable("jei_attribute.player.unavailable"), List.of());
        }

        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return new PlayerAttributeData(Component.translatable("jei_attribute.player.unavailable"), List.of());
        }

        List<Double> additions = new java.util.ArrayList<>();
        List<Double> multiplyBases = new java.util.ArrayList<>();
        List<Double> multiplyTotals = new java.util.ArrayList<>();
        double multiplyTotalFactor = 1.0;
        for (AttributeModifier modifier : instance.getModifiers()) {
            switch (modifier.getOperation()) {
                case ADDITION -> additions.add(modifier.getAmount());
                case MULTIPLY_BASE -> multiplyBases.add(modifier.getAmount());
                case MULTIPLY_TOTAL -> {
                    multiplyTotals.add(modifier.getAmount());
                    multiplyTotalFactor *= 1.0 + modifier.getAmount();
                }
            }
        }

        double addition = additions.stream().mapToDouble(Double::doubleValue).sum();
        double multiplyBase = multiplyBases.stream().mapToDouble(Double::doubleValue).sum();
        String additionEquation = sumEquation(additions);
        String multiplyBaseEquation = sumEquation(multiplyBases);
        String multiplyTotalEquation = productEquation(multiplyTotals);

        Component value = Component.translatable(
                "jei_attribute.player.value",
                format(instance.getValue()),
                format(instance.getBaseValue()),
                format(addition),
                format(multiplyBase),
                format(multiplyTotalFactor));
        List<Component> details = List.of(
                Component.translatable("jei_attribute.player.tooltip.base", format(instance.getBaseValue())),
                Component.translatable("jei_attribute.player.tooltip.addition", additionEquation, format(addition)),
                Component.translatable("jei_attribute.player.tooltip.multiply_base",
                        multiplyBaseEquation, format(multiplyBase), format(1.0 + multiplyBase)),
                Component.translatable("jei_attribute.player.tooltip.multiply_total",
                        multiplyTotalEquation, format(multiplyTotalFactor)),
                Component.translatable("jei_attribute.player.tooltip.result",
                        format(instance.getBaseValue()), format(addition), format(multiplyBase),
                        format(multiplyTotalFactor), format(instance.getValue())));
        return new PlayerAttributeData(value, details);
    }

    private static String sumEquation(List<Double> values) {
        if (values.isEmpty()) {
            return "0.00";
        }
        StringJoiner equation = new StringJoiner(" + ");
        values.forEach(value -> equation.add(format(value)));
        return equation.toString();
    }

    private static String productEquation(List<Double> values) {
        if (values.isEmpty()) {
            return "1.00";
        }
        StringJoiner equation = new StringJoiner(" × ");
        values.forEach(value -> equation.add("(1 + " + format(value) + ")"));
        return equation.toString();
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private record PlayerAttributeData(Component value, List<Component> details) {
    }
}
