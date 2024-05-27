package com.schism.core.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Schism;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.database.AbstractDefinition;
import com.schism.core.util.Vec2;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

public class NotificationWidget extends AbstractWidget
{
    protected final AbstractDefinition definition;
    protected final String event;
    protected final String value;
    protected final ResourceLocation texture;
    protected int ticksRemaining;

    /**
     * A widget that displays a notification panel.
     * @param drawHelper The draw helper to render with.
     * @param placement The placement to position and size this widget with.
     * @param definition The definition to display info from.
     * @param event The event string to display.
     * @param value The value string to display.
     */
    public NotificationWidget(DrawHelper drawHelper, Placement placement, AbstractDefinition definition, String event, String value)
    {
        super(drawHelper, placement);
        this.definition = definition;
        this.event = event;
        this.value = value;
        this.texture = new ResourceLocation(Schism.NAMESPACE, "textures/gui/notification/" + definition.type() + ".png");
        this.ticksRemaining = 100;
    }

    /**
     * Decreases the time remaining for this notification.
     * @return True if this notification has expired and needs to be removed.
     */
    public boolean tick()
    {
        return this.ticksRemaining-- <= 0;
    }

    @Override
    public Vec2 drawSize()
    {
        float iconSize = 28;
        int textWidth = (int)(this.baseSize().x() - iconSize - 6);

        Component title = Component.translatable("schism.notification." + this.definition.type() + "." + this.event);
        Component subject = Component.translatable("schism." + this.definition.type() + "." + this.definition.subject());
        TextComponent value = new TextComponent(this.value);

        int textHeight = this.drawHelper.wrappedHeight(title, textWidth) + 2;
        textHeight += this.drawHelper.wrappedHeight(subject, textWidth) + 2;
        textHeight += this.drawHelper.wrappedHeight(value, textWidth) + 2;

        return this.baseSize().max(0, textHeight);
    }

    /**
     * Renders this notification.
     */
    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        float padding = 4;
        float iconSize = 28;
        float textLeft = this.drawPosition().x() + iconSize + 6;
        float textTop = this.drawPosition().y() + padding + 2;
        int textWidth = (int)(this.drawSize().x() - iconSize - 6);

        // Panel:
        this.drawHelper.drawTexture(poseStack, this.texture, this.drawPosition(), this.drawSize(), Vec2.ONE);

        // Icon:
        if (this.definition.icon().isPresent()) {
            this.drawHelper.drawTexture(poseStack, this.definition.icon().get(), this.drawPosition().add(padding, padding + 2, 0), new Vec2(iconSize, iconSize), Vec2.ONE);
        }

        // Title:
        Component title = Component.translatable("schism.notification." + this.definition.type() + "." + this.event);
        textTop += this.drawHelper.drawTextWrapped(poseStack, title, new Vec2(textLeft, textTop), textWidth, 0xDDFFEE, false);

        // Subject:
        textTop += 2;
        Component subject = Component.translatable("schism." + this.definition.type() + "." + this.definition.subject());
        textTop += this.drawHelper.drawTextWrapped(poseStack, subject, new Vec2(textLeft, textTop), textWidth, 0xCCDDEE, false);

        // Value:
        textTop += 2;
        TextComponent value = new TextComponent(this.value);
        this.drawHelper.drawTextWrapped(poseStack, value, new Vec2(textLeft, textTop), textWidth, 0xEEDDCC, false);
    }
}
