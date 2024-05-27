package com.schism.core.effects.actions;

import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

public class SpreadEffectAction extends AbstractEffectAction
{
    protected final BlockRegistryObject<MobEffect> cachedEffect;
    protected final boolean onDamageDealt;
    protected final float range;
    protected final int levelOffset;
    protected final int durationMin;
    protected final boolean durationInherit;
    protected final boolean includeSelf;

    public SpreadEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedEffect = new BlockRegistryObject<>(dataStore.stringProp("effect_id"), () -> ForgeRegistries.MOB_EFFECTS);
        this.onDamageDealt = dataStore.booleanProp("on_damage_dealt");
        this.range = dataStore.floatProp("range");
        this.levelOffset = dataStore.intProp("level_offset");
        this.durationMin = dataStore.intProp("duration_min");
        this.durationInherit = dataStore.booleanProp("duration_inherit");
        this.includeSelf = dataStore.booleanProp("include_self");
    }

    @Override
    public void update(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        if (!this.cachedEffect.isPresent() || this.range <= 0 || livingEntity.getLevel().getGameTime() % 40 != 0) {
            return;
        }

        int duration = Math.max(effectInstance.getDuration(), this.durationMin);
        int amplifier = effectInstance.getAmplifier() + this.levelOffset;
        if (duration <= 0 || amplifier < 0) {
            return;
        }

        if (includeSelf) {
            this.spreadToEntity(livingEntity, duration, amplifier);
        }

        livingEntity.getLevel().getEntities(livingEntity, livingEntity.getBoundingBox().inflate(this.range), entity -> {
            if (!(entity instanceof LivingEntity)) {
                return false;
            }
            if (entity instanceof Player player) {
                if (player.isCreative() || player.isSpectator()) {
                    return false;
                }
            }
            return true;
        }).stream().map(entity -> (LivingEntity) entity).forEach(targetEntity -> this.spreadToEntity(targetEntity, duration, amplifier));
    }

    @Override
    public float damageDealt(MobEffectInstance effectInstance, LivingEntity livingEntity, LivingEntity targetEntity, DamageSource damageSource, float amount)
    {
        if (!this.cachedEffect.isPresent() || !this.onDamageDealt || amount <= 0) {
            return amount;
        }

        int duration = Math.max(effectInstance.getDuration(), this.durationMin);
        int amplifier = Math.max(effectInstance.getAmplifier() + this.levelOffset, 0);
        this.spreadToEntity(targetEntity, duration, amplifier);

        return amount;
    }

    /**
     * Attempts to spread an effect to the provided living entity.
     * @param livingEntity The living entity to attempt to spread to.
     * @param duration The duration of the effect to apply.
     * @param amplifier The amplifier (level - 1) of the effect.
     */
    protected void spreadToEntity(LivingEntity livingEntity, int duration, int amplifier)
    {
        MobEffect effect = this.cachedEffect.get();
        int existingAmplifier = -1;
        int existingDuration = 0;
        if (livingEntity.hasEffect(effect)) {
            existingAmplifier = livingEntity.getEffect(effect).getAmplifier();
            existingDuration = livingEntity.getEffect(effect).getDuration();
        }
        if (existingAmplifier < amplifier) {
            livingEntity.addEffect(new MobEffectInstance(effect, duration, amplifier));
        } else if (existingAmplifier == amplifier && existingDuration < duration) {
            livingEntity.addEffect(new MobEffectInstance(effect, duration, amplifier));
        }
    }
}
