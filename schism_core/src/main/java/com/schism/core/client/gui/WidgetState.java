package com.schism.core.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.util.Vec2;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WidgetState
{
    public boolean mouseContained = true;

    public Vec2 mousePosition = Vec2.ZERO;
    public Vec2 mouseActionPosition = Vec2.ZERO;

    public Vec2 mouseDelta = Vec2.ZERO;
    public double mouseScrollDirection = 0;

    public int mouseClicked = -1;
    public int mouseReleased = -1;
    public int mouseDragged = -1;

    public List<Component> tooltipComponents = new ArrayList<>();

    /**
     * Should be called before a gui has drawn content, sets up relevant input states.
     * @return The current instance.
     */
    public WidgetState beforeDraw(boolean mouseContained, int mouseX, int mouseY)
    {
        this.mouseContained = mouseContained;
        this.mousePosition = new Vec2(mouseX, mouseY);
        return this;
    }

    /**
     * Should be called after a gui has drawn content, clears relevant input states.
     * @return The current instance.
     */
    public WidgetState afterDraw()
    {
        this.mouseActionPosition = Vec2.ZERO;
        this.mouseDelta = Vec2.ZERO;
        this.mouseScrollDirection = 0;
        this.mouseClicked = -1;
        this.mouseReleased = -1;
        this.mouseDragged = -1;
        this.tooltipComponents.clear();
        return this;
    }

    /**
     * Should be called when a mouse button clicked event is triggered.
     * @param mouseX The x position of the mouse cursor when the event happened.
     * @param mouseY The y position of the mouse cursor when the event happened.
     * @param button The button id that was clicked. -1 to clear the state.
     * @return The current instance.
     */
    public WidgetState mouseClicked(double mouseX, double mouseY, int button)
    {
        this.mouseActionPosition = new Vec2((float)mouseX, (float)mouseY);
        this.mouseClicked = button;
        return this;
    }

    /**
     * Should be called when a mouse button released event is triggered.
     * @param mouseX The x position of the mouse cursor when the event happened.
     * @param mouseY The y position of the mouse cursor when the event happened.
     * @param button The button id that was released. -1 to clear the state.
     * @return The current instance.
     */
    public WidgetState mouseReleased(double mouseX, double mouseY, int button)
    {
        this.mouseActionPosition = new Vec2((float)mouseX, (float)mouseY);
        this.mouseReleased = button;
        return this;
    }

    /**
     * Should be called when a mouse dragged event is triggered.
     * @param mouseX The x position of the mouse cursor when the event happened.
     * @param mouseY The y position of the mouse cursor when the event happened.
     * @param button The button id that was dragged. -1 to clear the state.
     * @param deltaX The x distance the cursor was dragged since last tick.
     * @param deltaY The yx distance the cursor was dragged since last tick.
     * @return The current instance.
     */
    public WidgetState mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        this.mouseActionPosition = new Vec2((float)mouseX, (float)mouseY);
        this.mouseDelta = new Vec2((float)deltaX, (float)deltaY);
        this.mouseDragged = button;
        return this;
    }

    /**
     * Should be called when a mouse scrolled event is triggered.
     * @param mouseX The x position of the mouse cursor when the event happened.
     * @param mouseY The y position of the mouse cursor when the event happened.
     * @param direction The scroll direction. 0 to clear state.
     * @return The current instance.
     */
    public WidgetState mouseScrolled(double mouseX, double mouseY, double direction)
    {
        this.mouseActionPosition = new Vec2((float)mouseX, (float)mouseY);
        this.mouseScrollDirection = direction;
        return this;
    }

    /**
     * Draws any tooltip components as a tooltip. Requires a screen to be set on the provided draw helper.
     * @param drawHelper The draw helper to draw with.
     * @param poseStack The post stack to draw on.
     * @return The current instance.
     */
    public WidgetState drawTooltip(DrawHelper drawHelper, PoseStack poseStack)
    {
        if (drawHelper.screen().isPresent() && !this.tooltipComponents.isEmpty()) {
            drawHelper.screen().get().renderTooltip(poseStack, this.tooltipComponents, Optional.empty(), (int)this.mousePosition.x(), (int)this.mousePosition.y());
        }
        return this;
    }
}
