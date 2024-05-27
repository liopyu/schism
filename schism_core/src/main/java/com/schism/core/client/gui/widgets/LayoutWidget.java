package com.schism.core.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class LayoutWidget<WidgetT extends AbstractWidget> extends AbstractWidget
{
    protected final List<WidgetT> widgets;
    protected Vec3 direction;
    protected Vec3 spacing;
    protected Vec2 flex;

    /**
     * A widget that contains and renders nested widgets in an automatic layout.
     * @param drawHelper The draw helper ot render with.
     * @param placement The placement to position and size this widget with.
     * @param direction The layout xyz direction when positioning nested widgets.
     * @param spacing Spacing applied between nested widgets.
     */
    public LayoutWidget(DrawHelper drawHelper, Placement placement, Vec3 direction, Vec3 spacing)
    {
        super(drawHelper, placement);
        this.widgets = new ArrayList<>();
        this.direction = direction;
        this.spacing = spacing;
        this.center = Vec2.ZERO;
        this.flex = Vec2.ZERO;
    }

    @Override
    public Vec2 drawSize()
    {
        Vec2 drawSize = this.widgets.stream()
                .map(WidgetT::drawSize)
                .map(size -> new Vec2(
                        (size.x() * this.direction.x()) + this.spacing.x(),
                        (size.y() * this.direction.y()) + this.spacing.y()
                ))
                .reduce(Vec2.ZERO, Vec2::add);
        Vec2 contentsSize = this.contentsSize();
        return new Vec2(Math.max(drawSize.x(), contentsSize.x()), Math.max(drawSize.y(), contentsSize.y()));
    }

    /**
     * Gets the size of all contents within this layout.
     * @return The total size of this layout's contents.
     */
    public Vec2 contentsSize()
    {
        return this.widgets.stream().map(WidgetT::drawSize).reduce(Vec2.ZERO, Vec2::max);
    }

    /**
     * Adds a gui component to this layout.
     * @param guiComponent The gui component to add.
     * @return The current instance.
     */
    public LayoutWidget<WidgetT> add(WidgetT guiComponent)
    {
        this.widgets.add(guiComponent);
        return this;
    }

    /**
     * Removes a gui component from this layout.
     * @param guiComponent The gui component to remove.
     * @return The current instance.
     */
    public LayoutWidget<WidgetT> remove(WidgetT guiComponent)
    {
        this.widgets.remove(guiComponent);
        return this;
    }

    /**
     * Removes any gui components that match the provided predicate filter.
     * @param filter The filter predicate.
     * @return The current instance.
     */
    public LayoutWidget<WidgetT> removeIf(Predicate<? super WidgetT> filter)
    {
        this.widgets.removeIf(filter);
        return this;
    }

    /**
     * Removes all gui components.
     * @return The current instance.
     */
    public LayoutWidget<WidgetT> clear()
    {
        this.widgets.clear();
        return this;
    }

    /**
     * Sets the flexible padding applied to components in this layout that are smaller than other components such as automatically centering them.
     * @param flex The flex percentage to apply to smaller components.
     * @return The current instance.
     */
    public LayoutWidget<WidgetT> setFlex(Vec2 flex)
    {
        this.flex = flex;
        return this;
    }

    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        mouseInside = mouseInside && this.inside(widgetState.mousePosition);
        Vec3 position = this.drawPosition();
        Vec2 contentsSize = this.contentsSize();
        for (WidgetT guiComponent : this.widgets) {
            Vec3 componentPosition = position.add(
                    (contentsSize.x() - guiComponent.drawSize().x()) * this.flex.x(),
                    (contentsSize.y() - guiComponent.drawSize().y()) * this.flex.y(),
                    0
            );
            guiComponent.setOffset(componentPosition).draw(poseStack, widgetState, mouseInside);
            position = position.add(
                    (guiComponent.drawSize().x() * this.direction.x()) + this.spacing.x(),
                    guiComponent.drawSize().y() * this.direction.y() + this.spacing.y(),
                    this.direction.z()
            );
        }
    }
}
