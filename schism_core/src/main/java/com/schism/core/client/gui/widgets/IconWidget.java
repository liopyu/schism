package com.schism.core.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.util.Vec2;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class IconWidget extends AbstractWidget
{
    protected final ResourceLocation texture;
    protected Vec2 uv;
    protected final List<Component> tooltipComponents;
    protected Color color;

    /**
     * A widget that displays a texture with tooltip support.
     * @param drawHelper The draw helper to render with.
     * @param placement The placement to position and size this widget with.
     * @param texture The texture to use.
     */
    public IconWidget(DrawHelper drawHelper, Placement placement, ResourceLocation texture)
    {
        super(drawHelper, placement);
        this.texture = texture;
        this.uv = new Vec2(1, 1);
        this.tooltipComponents = new ArrayList<>();
        this.color = Color.WHITE;
    }

    /**
     * Changes the uv coordinates of this icon.
     * @param uv The uv coordinates to change to.
     * @return The current instance.
     */
    public IconWidget setUV(Vec2 uv)
    {
        this.uv = uv;
        return this;
    }

    /**
     * Changes the color of this icon.
     * @param color The color to change to.
     * @return The current instance.
     */
    public IconWidget setColor(Color color)
    {
        this.color = color;
        return this;
    }

    /**
     * Gets the color of this icon.
     * @param widgetState The input state to get the color for.
     * @return The color of this icon.
     */
    public Color color(WidgetState widgetState)
    {
        return this.color;
    }

    /**
     * Adds a text component to the tooltip this icon displays when hovered.
     * @param component The text component to add to the tooltip.
     * @return The current instance.
     */
    public IconWidget addTooltip(Component component)
    {
        this.tooltipComponents.add(component);
        return this;
    }

    /**
     * Sets the tooltip this icon displays when hovered.
     * @param components The text components to display in the tooltip, can be empty to disable the tooltip.
     * @return The current instance.
     */
    public IconWidget setTooltip(List<Component> components)
    {
        this.tooltipComponents.clear();
        this.tooltipComponents.addAll(components);
        return this;
    }

    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        mouseInside = mouseInside && this.inside(widgetState.mousePosition);
        this.drawHelper.drawTexture(poseStack, this.texture, this.drawPosition(), this.drawSize(), this.uv, this.color(widgetState));
        if (mouseInside) {
            widgetState.tooltipComponents.addAll(this.tooltipComponents);
        }
    }
}
