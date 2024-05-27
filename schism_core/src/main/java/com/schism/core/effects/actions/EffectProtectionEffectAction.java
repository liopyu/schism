package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.Effect;
import com.schism.core.effects.EffectDefinition;
import com.schism.core.resolvers.EffectResolver;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public class EffectProtectionEffectAction extends AbstractEffectAction
{
    protected final String group;

    public EffectProtectionEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.group = dataStore.stringProp("group");
    }

    @Override
    public void update(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        List.copyOf(livingEntity.getActiveEffects()).forEach(entityEffectInstance -> {
            if (effectInstance.getEffect() instanceof Effect effect && effect.definition().persistent()) {
                return;
            }
            if (EffectResolver.get().inGroup(entityEffectInstance.getEffect(), this.group)) {
                livingEntity.removeEffect(entityEffectInstance.getEffect());
            }
        });
    }
}
