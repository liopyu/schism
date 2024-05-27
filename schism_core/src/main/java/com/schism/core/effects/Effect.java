package com.schism.core.effects;

import com.schism.core.database.IHasDefinition;
import com.schism.core.effects.actions.AbstractEffectAction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMobEffect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;

public class Effect extends MobEffect implements IHasDefinition<EffectDefinition>, IForgeMobEffect
{
    protected final EffectDefinition definition;

    protected Effect(EffectDefinition effectDefinition)
    {
        super(effectDefinition.category(), effectDefinition.color());
        this.setRegistryName(effectDefinition.resourceLocation());
        this.definition = effectDefinition;
    }

    @Override
    public EffectDefinition definition()
    {
        return this.definition;
    }

    @Override
    public @NotNull MobEffectCategory getCategory()
    {
        return this.definition().category();
    }

    @Override
    public int getColor()
    {
        return this.definition().color();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    /**
     * Called on the update event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     */
    public void update(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        if (this.definition().persistent() && !effectInstance.getCurativeItems().isEmpty()) {
            effectInstance.setCurativeItems(new ArrayList<>());
        }
        if (!this.testConditions(livingEntity)) {
            return;
        }
        this.definition.actions().forEach(abstractEffectAction -> abstractEffectAction.update(effectInstance, livingEntity));
    }

    /**
     * Called on the jump event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @return Returns true to allow the jump or false to try and cancel the jump.
     */
    public boolean jump(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        boolean allow = true;
        if (!this.testConditions(livingEntity)) {
            return allow;
        }
        for (AbstractEffectAction abstractEffectAction : this.definition.actions()) {
            if (!abstractEffectAction.jump(effectInstance, livingEntity)) {
                allow = false;
            }
        }
        return allow;
    }

    /**
     * Called on the affected attacking entities of hurt event damage sources (when entities deal damage to other entities via the target's hurt event).
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @param damageSource The damage source instance.
     * @param amount The amount of damage to be dealt.
     * @return Returns a potentially modified amount of damage.
     */
    public float damageDealt(MobEffectInstance effectInstance, LivingEntity livingEntity, LivingEntity targetEntity, DamageSource damageSource, float amount)
    {
        if (!this.testConditions(livingEntity)) {
            return amount;
        }
        for (AbstractEffectAction abstractEffectAction : this.definition.actions()) {
            amount = abstractEffectAction.damageDealt(effectInstance, livingEntity, targetEntity, damageSource, amount);
        }
        return amount;
    }

    /**
     * Called on the hurt event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @param damageSource The damage source instance. This can be used to get an attacking entity if applicable.
     * @param amount The amount of damage received.
     * @return Returns true to allow the damage or false to try and cancel the damage.
     */
    public float damageTaken(MobEffectInstance effectInstance, LivingEntity livingEntity, DamageSource damageSource, float amount)
    {
        if (!this.testConditions(livingEntity)) {
            return amount;
        }
        for (AbstractEffectAction abstractEffectAction : this.definition.actions()) {
            amount = abstractEffectAction.damageTaken(effectInstance, livingEntity, damageSource, amount);
        }
        return amount;
    }

    /**
     * Called on the heal event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @param amount The amount of healing received.
     * @return Returns a potentially modified amount of healing.
     */
    public float healingTaken(MobEffectInstance effectInstance, LivingEntity livingEntity, float amount)
    {
        if (!this.testConditions(livingEntity)) {
            return amount;
        }
        for (AbstractEffectAction abstractEffectAction : this.definition.actions()) {
            amount = abstractEffectAction.healingTaken(effectInstance, livingEntity, amount);
        }
        return amount;
    }

    /**
     * Called on the sleep event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @param optionalPos The sleeping position, this is optional.
     * @return Returns true to allow the sleep event or false to try and cancel the sleep event.
     */
    public boolean sleep(MobEffectInstance effectInstance, LivingEntity livingEntity, Optional<BlockPos> optionalPos)
    {
        boolean allow = true;
        if (!this.testConditions(livingEntity)) {
            return allow;
        }
        for (AbstractEffectAction abstractEffectAction : this.definition.actions()) {
            if (!abstractEffectAction.sleep(effectInstance, livingEntity, optionalPos)) {
                allow = false;
            }
        }
        return allow;
    }

    /**
     * Called on the item use event of affected entities.
     * @param effectInstance The effect instance applied to the entity.
     * @param livingEntity The affected entity.
     * @param itemStack The item stack being used.
     * @param duration The duration the item has been used for.
     * @return Returns true to allow the item use or false to try and cancel the item use.
     */
    public boolean item(MobEffectInstance effectInstance, LivingEntity livingEntity, ItemStack itemStack, int duration)
    {
        boolean allow = true;
        if (!this.testConditions(livingEntity)) {
            return allow;
        }
        for (AbstractEffectAction abstractEffectAction : this.definition.actions()) {
            if (!abstractEffectAction.item(effectInstance, livingEntity, itemStack, duration)) {
                allow = false;
            }
        }
        return allow;
    }

    /**
     * Tests any conditions this effect might have.
     * @param livingEntity The entity to test conditions with.
     * @return True if conditions are passed.
     */
    public boolean testConditions(LivingEntity livingEntity)
    {
        if (this.definition().conditions().isEmpty()) {
            return true;
        }
        return this.definition().conditions().stream().allMatch(condition -> condition.test(livingEntity));
    }
}
