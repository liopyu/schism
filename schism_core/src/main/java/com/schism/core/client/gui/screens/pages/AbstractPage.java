package com.schism.core.client.gui.screens.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.client.gui.widgets.AbstractWidget;
import net.minecraft.resources.ResourceLocation;
import com.schism.core.util.Vec2;

public abstract class AbstractPage extends AbstractWidget
{
    protected final ResourceLocation backgroundTexture;

    public AbstractPage(DrawHelper drawHelper, Placement placement, ResourceLocation backgroundTexture)
    {
        super(drawHelper, placement);
        this.backgroundTexture = backgroundTexture;
    }

    /**
     * Gets the placement of this widget.
     * @return The placement of this widget.
     */
    public Placement placement()
    {
        return this.placement;
    }

    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        this.drawHelper.drawTexture(poseStack, this.backgroundTexture, this.drawPosition(), this.drawSize(), Vec2.ONE);
    }
}
