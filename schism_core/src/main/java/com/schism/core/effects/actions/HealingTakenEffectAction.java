package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class HealingTakenEffectAction extends AbstractEffectAction
{
    protected final float multiply;
    protected final float add;
    protected final float subtract;

    public HealingTakenEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.multiply = dataStore.floatProp("multiply");
        this.add = dataStore.floatProp("add");
        this.subtract = dataStore.floatProp("subtract");
    }

    @Override
    public float healingTaken(MobEffectInstance effectInstance, LivingEntity livingEntity, float amount)
    {
        int power = effectInstance.getAmplifier() + 1;
        if (this.multiply != 1) {
            amount *= Math.pow(this.multiply, power);
        }
        amount += (this.add - this.subtract) * power;
        return amount;
    }
}
