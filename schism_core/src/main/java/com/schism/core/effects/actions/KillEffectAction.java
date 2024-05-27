package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class KillEffectAction extends AbstractEffectAction
{
    protected final boolean immediate;
    protected final int levelEasy;
    protected final int levelNormal;
    protected final int levelHard;

    public KillEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.immediate = dataStore.booleanProp("immediate");
        this.levelEasy = dataStore.intProp("level_easy");
        this.levelNormal = dataStore.intProp("level_normal");
        this.levelHard = dataStore.intProp("level_hard");
    }

    @Override
    public void update(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        if (livingEntity.getLevel().isClientSide() || (livingEntity instanceof Player player && player.getAbilities().invulnerable)) {
            return;
        }

        if (!this.immediate && effectInstance.getDuration() != 1) {
            return;
        }

        int requiredLevel = this.levelEasy;
        if (livingEntity.getLevel().getDifficulty() == Difficulty.NORMAL) {
            requiredLevel = this.levelNormal;
        } else if (livingEntity.getLevel().getDifficulty() == Difficulty.HARD) {
            requiredLevel = this.levelHard;
        }

        if (effectInstance.getAmplifier() + 1 >= requiredLevel) {
            livingEntity.kill();
        }
    }
}
