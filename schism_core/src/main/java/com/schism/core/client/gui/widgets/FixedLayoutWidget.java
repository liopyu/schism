package com.schism.core.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;

public class FixedLayoutWidget<WidgetT extends AbstractWidget> extends LayoutWidget<WidgetT>
{
    protected Vec2 sizeMax;
    protected boolean scroll;
    protected Vec3 scrollPosition;
    protected final Placement scrollBarPlacement;
    protected final ScrollbarWidget scrollBar;

    /**
     * A layout widget that displays nested widgets with a fixed size and scrollbar support.
     * @param drawHelper The draw helper to render with.
     * @param placement The placement to position and size this widget with.
     */
    public FixedLayoutWidget(DrawHelper drawHelper, Placement placement, Vec3 direction, Vec3 spacing, Vec2 sizeMax, boolean scroll)
    {
        super(drawHelper, placement, direction, spacing);
        this.scroll = scroll;
        this.sizeMax = sizeMax;
        this.scrollPosition = Vec3.ZERO;

        this.scrollBarPlacement = new Placement(placement.position(), new Vec2(6, this.sizeMax.y()));
        this.scrollBar = new ScrollbarWidget(drawHelper, this.scrollBarPlacement, false, this::setScrollPositionNormal);
    }

    /**
     * Sets the maximum size of this layout before clipping and scrolling (if enabled).
     * @param sizeMax The maximum size of this layout before clipping and scrolling (if enabled).
     * @return The current instance.
     */
    public FixedLayoutWidget<WidgetT> setSizeMax(Vec2 sizeMax)
    {
        this.sizeMax = sizeMax;
        return this;
    }

    /**
     * Sets the content offset with a relative normal value.
     * @param normal The normal to set the offset by.
     */
    public void setScrollPositionNormal(Vec2 normal)
    {
        this.scrollPosition = new Vec3(
                (this.sizeMax.x() - this.drawSize().x()) * normal.x(),
                (this.sizeMax.y() - this.drawSize().y()) * normal.y(),
                0
        );
    }

    @Override
    public boolean inside(Vec2 position)
    {
        Vec3 layoutPosition = this.basePosition().add(-this.drawSize().x() * this.center.x(), -this.drawSize().y() * this.center.y(), 0);
        if (position.x() < layoutPosition.x()) {
            return false;
        }
        if (position.x() > layoutPosition.x() + this.sizeMax.x()) {
            return false;
        }
        if (position.y() < layoutPosition.y()) {
            return false;
        }
        if (position.y() > layoutPosition.y() + this.sizeMax.y()) {
            return false;
        }
        return true;
    }

    @Override
    protected Vec3 drawPosition()
    {
        return super.drawPosition().add(this.scrollPosition);
    }

    /**
     * Gets the size of all contents within this layout.
     * @return The total size of this layout's contents.
     */
    public Vec2 contentsSize()
    {
        Vec2 contentsSize = super.contentsSize();
        return new Vec2(
                Math.max(contentsSize.x(), this.sizeMax.x()),
                Math.max(contentsSize.y(), this.sizeMax.y())
        );
    }

    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        Vec3 position = this.basePosition().add(-this.drawSize().x() * this.center.x(), -this.drawSize().y() * this.center.y(), 0);
        this.drawHelper.scissorStart(position, this.sizeMax);
        super.draw(poseStack, widgetState, mouseInside && this.inside(widgetState.mousePosition));
        this.drawHelper.scissorStop();

        // Scrolling:
        if (this.scroll && this.inside(widgetState.mouseActionPosition)) {
            this.scrollPosition = new Vec3(
                    Math.max(Math.min(this.scrollPosition.x() + (widgetState.mouseScrollDirection * this.direction.x() * 16), 0), -(Math.max(this.drawSize().x(), this.sizeMax.x()) - (this.sizeMax.x() * this.direction.x()))),
                    Math.max(Math.min(this.scrollPosition.y() + (widgetState.mouseScrollDirection * this.direction.y() * 16), 0), -(Math.max(this.drawSize().y(), this.sizeMax.y()) - (this.sizeMax.y() * this.direction.y()))),
                    Math.max(Math.min(this.scrollPosition.z() + (widgetState.mouseScrollDirection * this.direction.z() * 16), 0), -100)
            );
            this.scrollBar.setNormal(new Vec2(
                    -(float)this.scrollPosition.x() / Math.max(this.drawSize().x() - (this.sizeMax.x() * this.direction.x()), 1),
                    -(float)this.scrollPosition.y() / Math.max(this.drawSize().y() - (this.sizeMax.y() * this.direction.y()), 1)
            ));
        }

        // Scroll Bar:
        this.scrollBarPlacement.setSize(new Vec2(this.scrollBar.drawSize().x(), this.sizeMax.y()));
        this.scrollBarPlacement.setPosition(position.add(this.sizeMax.x(), 0, 0));
        this.scrollBar.setOverflow(new Vec2(this.drawSize().x() / Math.max(this.sizeMax.x(), 1), this.drawSize().y() / Math.max(this.sizeMax.y(), 1)))
                .draw(poseStack, widgetState, mouseInside);
    }
}
