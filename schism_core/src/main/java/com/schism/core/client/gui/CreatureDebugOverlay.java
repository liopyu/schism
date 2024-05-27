package com.schism.core.client.gui;

import com.schism.core.Log;
import com.schism.core.creatures.*;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.gods.PantheonDefinition;
import com.schism.core.gods.Piety;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.stream.Collectors;

public class CreatureDebugOverlay
{
    private static CreatureDebugOverlay INSTANCE = new CreatureDebugOverlay();

    protected LivingEntity livingEntity;
    protected CreatureDefinition creatureDefinition;
    protected ICreature creature;

    /**
     * A gui overlay that shows creature debug information about a targeted entity.
     * @return The Creature Debug Overlay singleton instance.
     */
    public static CreatureDebugOverlay get()
    {
        if (INSTANCE == null) {
            INSTANCE = new CreatureDebugOverlay();
        }
        return INSTANCE;
    }

    /**
     * Grabs a new debug target from the client player's line of sight.
     * If there is no valid target, the overlay is just disabled.
     */
    public void target()
    {
        this.clear();
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if ((!(hitResult instanceof EntityHitResult entityHitResult))) {
            return;
        }
        if (!(entityHitResult.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }
        this.track(livingEntity);
    }

    /**
     * Sets the player as the tracking target.
     */
    public void self()
    {
        this.clear();
        this.track(Minecraft.getInstance().player);
        Log.info("Tracking player: " + Minecraft.getInstance().player);
    }

    /**
     * Enables the overlay for the provided living entity.
     * @param livingEntity The living entity to track.
     */
    public void track(LivingEntity livingEntity)
    {
        this.livingEntity = livingEntity;
        this.creatureDefinition = CreatureRepository.get().getDefinition(livingEntity).orElse(null);
        this.creature = Creatures.get().getCreature(livingEntity).orElse(null);
    }

    /**
     * Clears the overlay.
     */
    public void clear()
    {
        this.livingEntity = null;
        this.creatureDefinition = null;
        this.creature = null;
    }

    /**
     * Called during the game gui overlay text event.
     * @param event The overlay event.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onGameOverlayText(RenderGameOverlayEvent.Text event)
    {
        if (this.livingEntity == null) {
            return;
        }
        if (this.livingEntity.isRemoved() || this.livingEntity.isDeadOrDying()) {
            this.livingEntity = null;
            return;
        }

        event.getLeft().add("");
        event.getLeft().add("--0=====[] Creature Tracker []=====0--");
        event.getLeft().add(livingEntity.getUUID().toString());
        event.getLeft().add("Entity: " + livingEntity.getType().getRegistryName());
        event.getLeft().add("Network ID: " + livingEntity.getId());

        if (this.creatureDefinition == null) {
            event.getLeft().add("No Creature Definition");
            return;
        }
        event.getLeft().add("Subject: " + this.creatureDefinition.subject());
        event.getLeft().add("Type: " + this.creatureDefinition.creatureType().subject());
        event.getLeft().add("Roles: " + (
                this.creatureDefinition.creatureRoles().isEmpty() ? "none" : this.creatureDefinition.creatureRoles().stream()
                        .map(CreatureRoleDefinition::subject)
                        .collect(Collectors.joining(", ")))
        );
        event.getLeft().add(Component.translatable("schism.element_set.base").getString() + ": " + this.creatureDefinition.baseElement().subject());

        if (this.creature == null) {
            event.getLeft().add("No Creature Capability");
            return;
        }

        // Creature Elements:
        event.getLeft().add(Component.translatable("schism.element_set.resist").getString() + ": " + (
                this.creature.resistElements().isEmpty() ? "none" : this.creature.resistElements().stream()
                        .map(ElementDefinition::subject)
                        .collect(Collectors.joining(", ")))
        );
        event.getLeft().add(Component.translatable("schism.element_set.weakness").getString() + ": " + (
                this.creature.weaknessElements().isEmpty() ? "none" : this.creature.weaknessElements().stream()
                        .map(ElementDefinition::subject)
                        .collect(Collectors.joining(", ")))
        );
        event.getLeft().add(Component.translatable("schism.element_set.attack").getString() + ": " + (
                this.creature.attackElements().isEmpty() ? "none" : this.creature.attackElements().stream()
                        .map(ElementDefinition::subject)
                        .collect(Collectors.joining(", ")))
        );

        // Piety:
        Piety piety = this.creature.piety();
        event.getLeft().add(Component.translatable("schism.journal.title.pantheon").getString() + ": " + piety.pantheon().map(PantheonDefinition::subject).orElse("none"));
        event.getLeft().add(Component.translatable("schism.journal.title.worships").getString() + ": " + (
                piety.worships().isEmpty() ? "atheist" : piety.worships().stream()
                        .map(worship -> worship.god().definition().subject() + " (" + worship.devotion() + " - " + worship.unlockedRanks().size() + ")")
                        .collect(Collectors.joining(", ")))
        );

        // Stats:
        CreatureStats stats = this.creature.stats();
        event.getLeft().add(Component.translatable("schism.stats.level").getString() + ": " + stats.level());
        event.getLeft().add(Component.translatable("schism.stats.experience").getString() + ": " + stats.experience() + "/" + stats.experienceTarget());
        event.getLeft().add(Component.translatable("schism.stats.health").getString() + ": "
                + String.format("%.2f", this.livingEntity.getHealth()) + "/" + String.format("%.2f", this.livingEntity.getMaxHealth())
                + " (+" + String.format("%.2f", stats.health()) + ")");
        event.getLeft().add(Component.translatable("schism.stats.attack").getString() + ": "
                + String.format("%.2f", this.livingEntity.getAttributeValue(Attributes.ATTACK_DAMAGE) + stats.attack())
                + " (+" + String.format("%.2f", stats.attack()) + ")");
        event.getLeft().add(Component.translatable("schism.stats.defense").getString() + ": " + stats.defense());
    }
}
