package com.schism.core.effects;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class EffectEventListener
{
    private static final EffectEventListener INSTANCE = new EffectEventListener();

    /**
     * Gets the singleton EffectEventListener instance.
     * @return The Effect Event Listener which responds to various events and feeds them into any active effects on event targets.
     */
    public static EffectEventListener get()
    {
        return INSTANCE;
    }

    /**
     * Entity Update Event
     * @param event The event instance.
     */
    @SubscribeEvent
    public void onEntityUpdate(LivingEvent.LivingTickEvent event)
    {
        if(event.isCanceled() || event.getEntity() == null) {
            return;
        }
        List.copyOf(event.getEntity().getActiveEffects()).forEach(effectInstance -> {
            if (!event.getEntity().isDeadOrDying() && effectInstance.getEffect() instanceof Effect effect) {
                effect.update(effectInstance, event.getEntity());
            }
        });
    }

    /**
     * Entity Jump Event
     * @param event The event instance.
     */
    @SubscribeEvent
    public void onEntityJump(LivingEvent.LivingJumpEvent event)
    {
        if(event.isCanceled() || event.getEntity() == null) {
            return;
        }
        List.copyOf(event.getEntity().getActiveEffects()).forEach(effectInstance -> {
            if (effectInstance.getEffect() instanceof Effect effect) {
                if (!effect.jump(effectInstance, event.getEntity()) && event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        });
    }

    /**
     * Entity Hurt Event
     * @param event The event instance.
     */
    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event)
    {
        if(event.isCanceled() || event.getEntity() == null) {
            return;
        }

        // Damage Taken:
        List.copyOf(event.getEntity().getActiveEffects()).forEach(effectInstance -> {
            if (effectInstance.getEffect() instanceof Effect effect) {
                event.setAmount(effect.damageTaken(effectInstance, event.getEntity(), event.getSource(), event.getAmount()));
                if (event.getAmount() == 0 && event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        });

        // Damage Dealt:
        if (event.getSource().getEntity() instanceof LivingEntity sourceLivingEntity) {
            sourceLivingEntity.getActiveEffects().forEach(effectInstance -> {
                if (effectInstance.getEffect() instanceof Effect effect) {
                    event.setAmount(effect.damageDealt(effectInstance, sourceLivingEntity, event.getEntity(), event.getSource(), event.getAmount()));
                    if (event.getAmount() == 0 && event.isCancelable()) {
                        event.setCanceled(true);
                    }
                }
            });
        }
    }

    /**
     * Entity Heal Event
     * @param event The event instance.
     */
    @SubscribeEvent
    public void onEntityHeal(LivingHealEvent event)
    {
        if(event.isCanceled() || event.getEntity() == null) {
            return;
        }
        List.copyOf(event.getEntity().getActiveEffects()).forEach(effectInstance -> {
            if (effectInstance.getEffect() instanceof Effect effect) {
                event.setAmount(effect.healingTaken(effectInstance, event.getEntity(), event.getAmount()));
                if (event.getAmount() == 0 && event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        });
    }

    /**
     * Entity Sleep Event
     * @param event The event instance.
     */
    @SubscribeEvent
    public void onEntitySleep(PlayerSleepInBedEvent event)
    {
        if(event.isCanceled() || event.getEntity() == null) {
            return;
        }
        List.copyOf(event.getEntity().getActiveEffects()).forEach(effectInstance -> {
            if (effectInstance.getEffect() instanceof Effect effect) {
                if (!effect.sleep(effectInstance, event.getEntity(), event.getOptionalPos()) && event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        });
    }

    /**
     * Entity Use Item Event
     * @param event The event instance.
     */
    @SubscribeEvent
    public void onEntityUseItem(LivingEntityUseItemEvent event)
    {
        if(event.isCanceled() || event.getEntity() == null) {
            return;
        }
        List.copyOf(event.getEntity().getActiveEffects()).forEach(effectInstance -> {
            if (effectInstance.getEffect() instanceof Effect effect) {
                if (!effect.item(effectInstance, event.getEntity(), event.getItem(), event.getDuration()) && event.isCancelable()) {
                    event.setCanceled(true);
                }
            }
        });
    }
}
