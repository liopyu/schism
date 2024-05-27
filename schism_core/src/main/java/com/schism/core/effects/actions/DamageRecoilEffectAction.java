package com.schism.core.effects.actions;

import com.schism.core.database.CachedDefinition;
import com.schism.core.database.CachedList;
import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import com.schism.core.elements.ElementsDamageSource;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class DamageRecoilEffectAction extends AbstractEffectAction
{
    protected final CachedList<ElementDefinition> cachedElements;
    protected final float multiply;
    protected final float damage;

    public DamageRecoilEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedElements = new CachedList<> (dataStore.listProp("elements").stream()
                .map(entry -> new CachedDefinition<>(entry.stringValue(), ElementRepository.get())).toList());
        this.multiply = dataStore.floatProp("multiply");
        this.damage = dataStore.floatProp("damage");
    }

    @Override
    public float damageDealt(MobEffectInstance effectInstance, LivingEntity livingEntity, LivingEntity targetEntity, DamageSource damageSource, float amount)
    {
        int power = effectInstance.getAmplifier() + 1;
        float recoil = amount * this.multiply * power;
        recoil += this.damage * power;
        if (recoil > 0) {
            recoil = (float)Math.ceil(recoil);
            DamageSource recoilDamageSource = livingEntity.level().damageSources().generic();
            if (!this.cachedElements.get().isEmpty()) {
                recoilDamageSource = new ElementsDamageSource(this.cachedElements.get()).elementsDamage(livingEntity.level());
            }
            livingEntity.hurt(recoilDamageSource, recoil);
        } else if (recoil < 0) {
            recoil = (float)Math.floor(recoil);
            livingEntity.heal(-recoil);
        }

        return amount;
    }
}
