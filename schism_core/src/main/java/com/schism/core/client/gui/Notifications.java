package com.schism.core.client.gui;

import com.schism.core.Schism;
import com.schism.core.client.gui.widgets.LayoutWidget;
import com.schism.core.client.gui.widgets.NotificationWidget;
import com.schism.core.database.AbstractDefinition;
import net.minecraft.client.Minecraft;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Notifications
{
    private static Notifications INSTANCE = new Notifications();

    protected final DrawHelper drawHelper;
    protected final WidgetState widgetState;
    protected final Placement layoutPlacement;
    protected final LayoutWidget<NotificationWidget> layout;
    protected final Placement notificationPlacement;

    /**
     * A gui overlay that shows creature debug information about a targeted entity.
     * @return The Creature Debug Overlay singleton instance.
     */
    public static Notifications get()
    {
        if (INSTANCE == null) {
            INSTANCE = new Notifications();
        }
        return INSTANCE;
    }

    public Notifications()
    {
        this.drawHelper = new DrawHelper();
        this.widgetState = new WidgetState();
        this.layoutPlacement = new Placement();
        this.layout = new LayoutWidget<>(this.drawHelper, this.layoutPlacement, new Vec3(0, 1, 0), new Vec3(0, 4, 0));
        this.layout.setCenter(new Vec2(0, 0.5F));
        this.notificationPlacement = new Placement(new Vec2(128, 40));
    }

    /**
     * Creates a new notification and adds it to the list for display.
     * @param definition The definition the notification should display.
     * @param event The event that happened.
     * @param value Additional text to include, this does not get translated.
     */
    public void notify(AbstractDefinition definition, String event, String value)
    {
        this.layout.add(new NotificationWidget(this.drawHelper, this.notificationPlacement, definition, event, value));
        GuiSounds.get().play(Schism.NAMESPACE, "notification." + definition.type());
    }

    /**
     * Called during the client tick event.
     * @param event The client tick event.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientUpdate(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().screen != null) {
            return;
        }
        this.layout.removeIf(NotificationWidget::tick);
    }

    /**
     * Called during the game gui overlay event.
     * @param event The overlay event.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onGameOverlayText(RenderGuiOverlayEvent event)
    {
        this.layoutPlacement.setPosition(new Vec3(2, (float)event.getWindow().getGuiScaledHeight() / 2, 0));
        this.layout.draw(event.getGuiGraphics().pose(), this.widgetState.beforeDraw(false, 0, 0), false);
        this.widgetState.drawTooltip(this.drawHelper, event.getGuiGraphics().pose()).afterDraw();
    }
}
