package com.schism.core.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;

public abstract class AbstractWidget
{
    protected final DrawHelper drawHelper;
    protected final Placement placement;
    protected Vec3 offset;
    protected Vec2 center;

    /**
     * A gui widget.
     * @param drawHelper The draw helper to render with.
     * @param placement The placement to position and size this widget with.
     */
    public AbstractWidget(DrawHelper drawHelper, Placement placement)
    {
        this.drawHelper = drawHelper;
        this.placement = placement;
        this.offset = Vec3.ZERO;
        this.center = Vec2.ZERO;
    }

    /**
     * Gets the current base position of this gui widget. Use drawPosition for the exact position.
     * @return The position of this gui widget.
     */
    public Vec3 basePosition()
    {
        return this.placement.position();
    }

    /**
     * Gets the current base size of this gui widget. Use drawSize for the actual size.
     * @return The size of this gui widget.
     */
    public Vec2 baseSize()
    {
        return this.placement.size();
    }

    /**
     * Sets the center of this component, this is a fraction of it's size, ex: 0.5, 0.5 for centering on the position.
     * @param center The relative center offsets to use.
     * @return The current instance.
     */
    public AbstractWidget setCenter(Vec2 center)
    {
        this.center = center;
        return this;
    }

    /**
     * Gets the current offset of this gui widget, this is added to the position when rendering.
     * @return The offset of this gui widget.
     */
    public Vec3 offset()
    {
        return this.offset;
    }

    /**
     * Sets the offset of this component.
     * @param offset The offset to use.
     * @return The current instance.
     */
    public AbstractWidget setOffset(Vec3 offset)
    {
        this.offset = offset;
        return this;
    }

    /**
     * Gets the size this widget should be drawn at.
     * @return The position this widget is to be drawn at.
     */
    public Vec2 drawSize()
    {
        return this.baseSize();
    }

    /**
     * Gets the position this widget should be drawn at taking its center into account.
     * @return The position this widget is to be drawn at.
     */
    protected Vec3 drawPosition()
    {
        return this.placement.position().add(
                this.offset.x() - (this.drawSize().x() * this.center.x()),
                this.offset.y() - (this.drawSize().y() * this.center.y()),
                this.offset.z()
        );
    }

    /**
     * Draws this component.
     * @param poseStack The pose stack to draw with.
     * @param widgetState The gui input state which contains information about mouse input, etc.
     * @param mouseInside True if the widget/screen/overlay drawing this widget has the mouse cursor inside it's content area.
     */
    public abstract void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside);

    /**
     * Determines if the provided position is inside this icon.
     * @param position The position to check.
     * @return True if the position is inside this icon, false if it is outside.
     */
    public boolean inside(Vec2 position)
    {
        if (position.x() < this.drawPosition().x()) {
            return false;
        }
        if (position.x() > this.drawPosition().x() + this.drawSize().x()) {
            return false;
        }
        if (position.y() < this.drawPosition().y()) {
            return false;
        }
        if (position.y() > this.drawPosition().y() + this.drawSize().y()) {
            return false;
        }
        return true;
    }
}
