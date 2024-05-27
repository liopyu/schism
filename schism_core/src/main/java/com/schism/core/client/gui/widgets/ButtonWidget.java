package com.schism.core.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.function.Supplier;

public class ButtonWidget extends IconWidget
{
    protected Runnable action;

    protected ResourceLocation foregroundTexture;
    protected Vec2 foregroundSize;
    protected Color foregroundColor;
    protected Supplier<Boolean> selectedSupplier;

    protected ItemStack itemStack;
    protected float itemStackScale;

    /**
     * A widget that displays text.
     * @param drawHelper The draw helper to render with.
     * @param placement The placement to position and size this widget with.
     * @param texture The texture to use.
     * @param action The runnable to call when the button is clicked.
     */
    public ButtonWidget(DrawHelper drawHelper, Placement placement, ResourceLocation texture, Runnable action)
    {
        super(drawHelper, placement, texture);
        this.action = action;
        this.foregroundColor = this.color;
        this.selectedSupplier = () -> false;

        this.itemStack = null;
        this.itemStackScale = 1;
    }

    /**
     * Sets if this button is selected or not which forces it to stay lit up.
     * @param selectedSupplier A supplier to run to determine the selected state of the button.
     * @return The current instance.
     */
    public ButtonWidget setSelectedSupplier(Supplier<Boolean> selectedSupplier)
    {
        this.selectedSupplier = selectedSupplier;
        return this;
    }

    /**
     * Sets a foreground texture to display on this button.
     * @param texture The texture to use for the foreground.
     * @param size The size of the foreground, this will be centered over the button position.
     * @return The current instance.
     */
    public ButtonWidget setForeground(ResourceLocation texture, Vec2 size)
    {
        this.foregroundTexture = texture;
        this.foregroundSize = size;
        return this;
    }

    /**
     * Sets an item stack to display (above both the background and foreground).
     * @param itemStack The item stack to display (can be null no no item stack).
     * @param scale A render scale to apply to the item stack relative to inventory gui icons, etc.
     * @return The current instance.
     */
    public ButtonWidget setItemStack(ItemStack itemStack, float scale)
    {
        this.itemStack = itemStack;
        this.itemStackScale = scale;
        return this;
    }

    @Override
    public IconWidget setColor(Color color)
    {
        this.foregroundColor = color;
        return super.setColor(color);
    }

    /**
     * Overrides the foreground color of this icon.
     * @param color The color to change to.
     * @return The current instance.
     */
    public IconWidget setForegroundColor(Color color)
    {
        this.foregroundColor = color;
        return this;
    }

    @Override
    public Color color(WidgetState widgetState)
    {
        if (this.selectedSupplier.get() || this.inside(widgetState.mousePosition)) {
            return super.color(widgetState);
        }
        return super.color(widgetState).darker();
    }

    /**
     * Gets the foreground color of this icon.
     * @param widgetState The input state to get the foreground color for.
     * @return The color of this icon's foreground texture.
     */
    public Color foregroundColor(WidgetState widgetState)
    {
        if (this.selectedSupplier.get() || this.inside(widgetState.mousePosition)) {
            return this.foregroundColor;
        }
        return this.foregroundColor.darker();
    }

    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        if (this.foregroundTexture != null) {
            Vec3 foregroundPosition =  this.drawPosition().add((this.drawSize().x() - this.foregroundSize.x()) / 2, (this.drawSize().y() - this.foregroundSize.y()) / 2, 1);
            this.drawHelper.drawTexture(poseStack, this.foregroundTexture, foregroundPosition, this.foregroundSize, this.uv, this.foregroundColor(widgetState));
        }

        if (this.itemStack != null) {
            Vec3 itemStackPosition = this.drawPosition().add(this.drawSize().x() / 4, this.drawSize().y() / 4, 100);
            this.drawHelper.drawItemStack(poseStack, this.itemStack, itemStackPosition, this.itemStackScale);
        }

        mouseInside = mouseInside && this.inside(widgetState.mousePosition);
        super.draw(poseStack, widgetState, mouseInside);

        if (widgetState.mouseReleased == 0 && mouseInside) {
            this.action.run();
        }
    }
}
