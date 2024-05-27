package com.schism.core.creatures;

import com.schism.core.database.IHasDefinition;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.gods.Piety;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.awt.*;
import java.util.List;

public interface ICreature extends IHasDefinition<CreatureDefinition>
{
    /**
     * Gets this creature's entity.
     * @return The creature entity.
     */
    public LivingEntity livingEntity();

    /**
     * Called when the host entity does an update tick.
     */
    void tick();

    /**
     * Modifies the component used to display the creature's name tag.
     * @param component The name tag text component to potentially modify.
     * @return True if the name tag should be forcefully displayed despite no custom name.
     */
    public abstract boolean modifyNameTag(MutableComponent component);

    /**
     * Called when the host entity takes damage.
     * @param amount The amount of damage taken.
     * @param damageSource The damage source.
     * @param damageElements A list of elements applicable to the damage.
     * @return A potentially modified damage amount.
     */
    float onHurt(float amount, DamageSource damageSource, List<ElementDefinition> damageElements);

    /**
     * Called when the host entity dies.
     * @param damageSource The killing damage source.
     * @param damageElements A list of elements applicable to the killing damage.
     */
    void onDeath(DamageSource damageSource, List<ElementDefinition> damageElements);

    /**
     * Called when the host entity deals damage.
     * @param amount The amount of damage being dealt.
     * @param damageSource The damage source.
     * @param damageElements A list of elements applicable to the damage.
     * @param livingEntity The entity the host is damaging.
     * @return A potentially modified damage amount.
     */
    float onDamage(float amount, DamageSource damageSource, List<ElementDefinition> damageElements, LivingEntity livingEntity);

    /**
     * Called when the host entity kills another entity.
     * @param damageSource The killing damage source.
     * @param damageElements A list of elements applicable to the killing damage.
     * @param livingEntity The entity that the host has killed.
     */
    void onKill(DamageSource damageSource, List<ElementDefinition> damageElements, LivingEntity livingEntity);

    /**
     * Called when the host is being test for effect vulnerability.
     * @param effectInstance The effect that is trying to be applied.
     * @return True to allow the effect, false to prevent it.
     */
    boolean effectApplicable(MobEffectInstance effectInstance);

    /**
     * A list of elements this creature is resistant to.
     * @return A list of resist elements.
     */
    List<ElementDefinition> resistElements();

    /**
     * A list of elements this creature is weak to.
     * @return A list of weakness elements.
     */
    List<ElementDefinition> weaknessElements();

    /**
     * A list of elements to use when dealing damage.
     * @return A list of damage elements.
     */
    List<ElementDefinition> attackElements();

    /**
     * A list of elements that this creature harmonises with, affects behaviour of other creatures and various interactions.
     * @return A list of harmony elements.
     */
    List<ElementDefinition> harmonyElements();

    /**
     * A color based on this creature's elements.
     * @return The main color based on this creature's elements.
     */
    Color elementColor();

    /**
     * Gets the stats of this creature, used for levelling which enhance other stats.
     * @return The creature stats instance.
     */
    CreatureStats stats();

    /**
     * Gets the piety of this creature, used for god worship.
     * @return The god worship piety.
     */
    Piety piety();

    /**
     * Syncs this capability to all clients.
     */
    void syncClients();

    /**
     * Syncs this capability to the provided player.
     * @param serverPlayer The server player client to sync to.
     */
    void syncClient(ServerPlayer serverPlayer);
}
