package com.schism.core.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Schism;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class ScrollbarWidget extends IconWidget
{
    protected final boolean horizontal;
    protected Vec2 overflow;
    protected final Placement scrollHandlePlacement;
    protected final DraggableWidget scrollHandle;

    /**
     * A scrollbar widget which consists of a bar and a contained draggable.
     * @param drawHelper The draw helper to render with.
     * @param placement The placement to position and size this widget with.
     * @param horizontal If true, this scroll bar will work horizontally, if false, vertically.
     * @param scrollAction A draggable callback to call as this scroll bar's handle is dragged.
     */
    public ScrollbarWidget(DrawHelper drawHelper, Placement placement, boolean horizontal, DraggableWidget.DraggableCallback scrollAction)
    {
        super(drawHelper, placement, new ResourceLocation(Schism.NAMESPACE, "textures/gui/scroll_bar.png"));
        this.horizontal = horizontal;
        this.overflow = Vec2.ONE;
        this.scrollHandlePlacement = new Placement(Vec3.ZERO, new Vec2(4, 0));
        this.scrollHandle = new DraggableWidget(
                drawHelper,
                this.scrollHandlePlacement,
                new ResourceLocation(Schism.NAMESPACE, "textures/gui/scroll_handle.png"),
                new Vec2(0, 1),
                scrollAction
        );
    }

    @Override
    public ScrollbarWidget setColor(Color color)
    {
        super.setColor(color);
        this.scrollHandle.setColor(color);
        return this;
    }

    /**
     * Sets the percentage of overflow that this must scroll for.
     * Anything at or below 1.0 results in no scrollbar, anything above creates a scrollbar sized accordingly.
     * @param overflow The overflow to set.
     * @return The current instance.
     */
    public ScrollbarWidget setOverflow(Vec2 overflow)
    {
        this.overflow = overflow;
        return this;
    }

    /**
     * Sets the offset normal of the content that this scrollbar is for.
     * @param normal The normal, ex y 0.25 would mean that the content is scrolled down by 25%.
     * @return The current instance.
     */
    public ScrollbarWidget setNormal(Vec2 normal)
    {
        if (!this.horizontal && this.overflow.y() > 1) {
            float handleHeight = (this.drawSize().y() / this.overflow.y()) - 2;
            float handleBoundsY = this.drawSize().y() - 2;
            this.scrollHandle.setOffset(new Vec3(0, (handleBoundsY - handleHeight) * normal.y(), 0));
            return this;
        }

        if (this.horizontal && this.overflow.x() > 1) {
            float handleWidth = (this.drawSize().x() / this.overflow.x()) - 2;
            float handleBoundsX = this.drawSize().x() - 2;
            this.scrollHandle.setOffset(new Vec3((handleBoundsX - handleWidth) * normal.x(), 0, 0));
        }
        return this;
    }

    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        if (this.overflow.x() <= 1 && this.overflow.y() <= 1) {
            return;
        }

        super.draw(poseStack, widgetState, mouseInside);

        // Vertical:
        if (!this.horizontal && this.overflow.y() > 1) {
            this.scrollHandlePlacement.setSize(new Vec2(this.drawSize().x() - 2, (this.drawSize().y() / this.overflow.y()) - 2));
            this.scrollHandlePlacement.setPosition(this.drawPosition().add(1, 1, 0));
            this.scrollHandle.setBounds(Vec2.ZERO, new Vec2(0, this.drawSize().y() - 2)).draw(poseStack, widgetState, mouseInside);
        }
    }
}
