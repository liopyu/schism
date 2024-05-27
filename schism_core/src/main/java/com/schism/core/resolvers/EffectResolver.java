package com.schism.core.resolvers;

import com.schism.core.effects.Effect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public class EffectResolver
{
    private static final EffectResolver INSTANCE = new EffectResolver();

    /**
     * Gets the singleton EffectResolver instance.
     * @return The Effect Resolver, a utility for checking effect groups.
     */
    public static EffectResolver get()
    {
        return INSTANCE;
    }

    /**
     * Determines if the provided effect is in the provided effect group.
     * @param effect The effect to check.
     * @param group The group to check.
     * @return True if the effect is in the provided group.
     */
    public boolean inGroup(MobEffect effect, String group)
    {
        String effectGroup = this.getGroup(effect);
        if (effectGroup.equals(group)) {
            return true;
        }
        if (group.equals("boost")) {
            return effectGroup.equals("boon") || effectGroup.equals("charm");
        }
        if (group.equals("taint")) {
            return effectGroup.equals("ailment") || effectGroup.equals("curse");
        }
        return false;
    }

    /**
     * Gets the group of the provided effect.
     * @param mobEffect The effect to get a group for.
     * @return An effect group.
     */
    public String getGroup(MobEffect mobEffect)
    {
        if (mobEffect instanceof Effect effect) {
            return effect.definition().group();
        }

        // Ailments (physical detrimental):
        if (mobEffect == MobEffects.MOVEMENT_SLOWDOWN) {
            return "ailment";
        }
        if (mobEffect == MobEffects.DIG_SLOWDOWN) {
            return "ailment";
        }
        if (mobEffect == MobEffects.CONFUSION) {
            return "ailment";
        }
        if (mobEffect == MobEffects.BLINDNESS) {
            return "ailment";
        }
        if (mobEffect == MobEffects.HUNGER) {
            return "ailment";
        }
        if (mobEffect == MobEffects.WEAKNESS) {
            return "ailment";
        }
        if (mobEffect == MobEffects.POISON) {
            return "ailment";
        }

        // Charms (magical beneficial):
        if (mobEffect == MobEffects.HEAL) {
            return "charm";
        }
        if (mobEffect == MobEffects.REGENERATION) {
            return "charm";
        }
        if (mobEffect == MobEffects.INVISIBILITY) {
            return "charm";
        }
        if (mobEffect == MobEffects.NIGHT_VISION) {
            return "charm";
        }
        if (mobEffect == MobEffects.HEALTH_BOOST) {
            return "charm";
        }
        if (mobEffect == MobEffects.GLOWING) {
            return "charm";
        }
        if (mobEffect == MobEffects.LUCK) {
            return "charm";
        }
        if (mobEffect == MobEffects.SLOW_FALLING) {
            return "charm";
        }
        if (mobEffect == MobEffects.HERO_OF_THE_VILLAGE) {
            return "charm";
        }

        // Default to Curse (magical detrimental) or Boon (physical beneficial):
        return mobEffect.isBeneficial() ? "boon" : "curse";
    }
}
