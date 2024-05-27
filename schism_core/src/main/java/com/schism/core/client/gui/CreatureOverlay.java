package com.schism.core.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Schism;
import com.schism.core.client.KeyHandler;
import com.schism.core.client.gui.components.CreatureElementStats;
import com.schism.core.client.gui.components.CreaturePietyStats;
import com.schism.core.client.gui.widgets.AbstractWidget;
import com.schism.core.client.gui.widgets.ButtonWidget;
import com.schism.core.client.gui.widgets.LayoutWidget;
import com.schism.core.client.gui.screens.JournalScreen;
import com.schism.core.client.gui.widgets.TextWidget;
import com.schism.core.creatures.Creatures;
import com.schism.core.creatures.ICreature;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;

public class CreatureOverlay
{
    private static CreatureOverlay INSTANCE = new CreatureOverlay();

    protected final DrawHelper drawHelper = new DrawHelper();
    protected final WidgetState widgetState = new WidgetState();
    protected ICreature creature;

    protected Placement layoutPlacement;
    protected LayoutWidget<AbstractWidget> layout;

    protected Placement topLayoutPlacement;
    protected LayoutWidget<AbstractWidget> topLayout;

    protected Placement statsTextPlacement;
    protected TextWidget statsText;

    protected Placement journalButtonPlacement;
    protected ButtonWidget journalButton;

    /**
     * A gui overlay that shows creature stats such as elements and worships.
     * @return The Creature Stats Overlay singleton instance.
     */
    public static CreatureOverlay get()
    {
        if (INSTANCE == null) {
            INSTANCE = new CreatureOverlay();
        }
        return INSTANCE;
    }

    /**
     * Called during the client tick event.
     * @param event The client tick event.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientUpdate(TickEvent.ClientTickEvent event)
    {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            this.creature = null;
            this.layout = null;
            return;
        }

        if (player.isRemoved() || player.isDeadOrDying()) {
            this.creature = null;
            this.layout = null;
            return;
        }

        if (this.creature == null || this.creature.livingEntity() != player) {
            this.creature = Creatures.get().getCreature(Minecraft.getInstance().player).orElse(null);

            this.statsTextPlacement = new Placement();
            this.statsText = new TextWidget(this.drawHelper, this.statsTextPlacement);

            Vec2 journalButtonSize = new Vec2(32, 32);
            this.journalButtonPlacement = new Placement(Vec3.ZERO, journalButtonSize);
            this.journalButton = new ButtonWidget(
                    this.drawHelper,
                    this.journalButtonPlacement,
                    new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal_background.png"),
                    () -> {
                        if (Minecraft.getInstance().player != null) {
                            Minecraft.getInstance().setScreen(new JournalScreen(Minecraft.getInstance().player));
                        }
                    });
            this.journalButton.setForeground(new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal.png"), journalButtonSize);
            this.journalButton.addTooltip(Component.translatable(Schism.NAMESPACE + ".journal.open"));

            this.topLayoutPlacement = new Placement();
            this.topLayout = new LayoutWidget<>(
                    this.drawHelper,
                    this.topLayoutPlacement,
                    new Vec3(1, 0, 10),
                    new Vec3(2, 0, 0)
            );
            this.topLayout.add(this.journalButton);
            this.topLayout.add(this.statsText);

            this.layoutPlacement = new Placement();
            this.layout = new LayoutWidget<>(
                    this.drawHelper,
                    this.layoutPlacement,
                    new Vec3(0, 1, 0),
                    new Vec3(0, 2, 0)
            );
            this.layout.setCenter(new Vec2(0, 0.5F));
            this.layout.add(this.topLayout);
            this.layout.add(new CreatureElementStats(this.drawHelper, new Placement(), this.creature, false));
            this.layout.add(new CreaturePietyStats(this.drawHelper, new Placement(), this.creature, false));
        }
    }

    /**
     * Called during the game gui overlay event.
     * @param event The overlay event.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onGameOverlayText(ScreenEvent.DrawScreenEvent event)
    {
        if (this.layout == null || !(event.getScreen() instanceof InventoryScreen inventoryScreen)) {
            return;
        }

        // Journal Button:
        if (this.journalButton != null) {
            this.journalButton.setColor(this.creature.elementColor());
            this.journalButton.setForegroundColor(Color.WHITE);
        }

        // Stats Text:
        if (this.statsText != null) {
            this.statsText.clear();
            this.statsTextPlacement.setSize(new Vec2(200, 0));
            this.statsText.add(Component.translatable(Schism.NAMESPACE + ".stats.level")
                    .append(": " + this.creature.stats().level()),
                    true, false, true);
            this.statsText.add(Component.translatable(Schism.NAMESPACE + ".stats.experience.compact")
                    .append(": " + this.creature.stats().experience() + "/" + this.creature.stats().experienceTarget()),
                    true, false, true);
        }

        // Draw:
        this.widgetState.beforeDraw(true, event.getMouseX(), event.getMouseY());
        this.widgetState.mouseActionPosition = new Vec2(event.getMouseX(), event.getMouseY());
        this.widgetState.mouseReleased = KeyHandler.get().widgetState.mouseReleased;

        this.drawHelper.setScreen(inventoryScreen);
        PoseStack poseStack = event.getPoseStack();
        this.layoutPlacement.setPosition(new Vec3(6, (float)inventoryScreen.height / 2, 0));
        this.layout.draw(poseStack, this.widgetState, true);

        this.widgetState.drawTooltip(this.drawHelper, poseStack).afterDraw();
        KeyHandler.get().widgetState.afterDraw();
    }
}
