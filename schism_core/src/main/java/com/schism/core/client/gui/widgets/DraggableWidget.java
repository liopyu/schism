package com.schism.core.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;
import net.minecraft.resources.ResourceLocation;

public class DraggableWidget extends ButtonWidget
{
    protected Runnable clickAction;
    protected DraggableCallback dragAction;

    protected Vec2 direction;
    protected Vec2 boundsMin;
    protected Vec2 boundsMax;
    protected boolean dragging;
    protected Vec2 dragOffset;

    /**
     * A draggable widget like a button but with functionality for being dragged by the mouse.
     * @param drawHelper The draw helper to render with.
     * @param placement The placement to position and size this widget with.
     * @param texture The button texture.
     * @param direction The direction scale of where the draggable can be dragged, ex: (1.0, 1.0) allows for full xy movement, (0.0, 1.0) for y only, etc.
     * @param dragAction A draggable callback to call as this draggable button is moved around.
     */
    public DraggableWidget(DrawHelper drawHelper, Placement placement, ResourceLocation texture, Vec2 direction, DraggableCallback dragAction)
    {
        super(drawHelper, placement, texture, () -> {});
        this.clickAction = () -> {};
        this.dragAction = dragAction;

        this.direction = direction;
        this.boundsMin = Vec2.ZERO;
        this.boundsMax = placement.size();
        this.offset = Vec3.ZERO;
        this.dragOffset = Vec2.ZERO;
    }

    public interface DraggableCallback
    {
        /**
         * Called whenever the draggable is dragged, returns the normal offset of its position.
         * @param normal The normal of the current drag offset, ex y 0.25 would be dragged down by 25% of its height.
         */
        void call(Vec2 normal);
    }

    /**
     * Sets the minimum and maximum xy drag position bounds.
     * @param boundsMin The min bounds.
     * @param boundsMax The max bounds.
     * @return The current instance.
     */
    public DraggableWidget setBounds(Vec2 boundsMin, Vec2 boundsMax)
    {
        this.boundsMin = boundsMin;
        this.boundsMax = boundsMax;
        return this;
    }

    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        mouseInside = mouseInside && this.inside(widgetState.mousePosition);
        super.draw(poseStack, widgetState, mouseInside);

        // Drag Start:
        if (!this.dragging && mouseInside && widgetState.mouseClicked == 0) {
            this.dragging = true;
            this.dragOffset = new Vec2(
                    widgetState.mousePosition.x() - this.drawPosition().x(),
                    widgetState.mousePosition.y() - this.drawPosition().y()
            );
            this.clickAction.run();
        }

        // Drag:
        if (this.dragging && widgetState.mouseDragged == 0) {
            Vec2 draggedOffset = new Vec2(
                    (widgetState.mousePosition.x() - this.dragOffset.x() - this.drawPosition().x()) * this.direction.x(),
                    (widgetState.mousePosition.y() - this.dragOffset.y() - this.drawPosition().y()) * this.direction.y()
            );
            Vec2 boundsMax = this.boundsMax.add(new Vec2(-this.drawSize().x(), -this.drawSize().y()));
            this.setOffset(new Vec3(
                    Math.min(Math.max(draggedOffset.x(), this.boundsMin.x()), boundsMax.x() * this.direction.x()),
                    Math.min(Math.max(draggedOffset.y(), this.boundsMin.y()), boundsMax.y() * this.direction.y()),
                    0
            ));
            this.dragAction.call(new Vec2(this.offset.x() / boundsMax.x(), this.offset.y() / boundsMax.y()));
        }

        // Drag Stop:
        if (widgetState.mouseReleased == 0) {
            if (this.dragging && !mouseInside) { // Button already calls this when mouseInside is true.
                this.action.run();
            }
            this.dragging = false;
        }
    }
}
