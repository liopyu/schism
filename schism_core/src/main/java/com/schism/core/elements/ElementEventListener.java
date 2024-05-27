package com.schism.core.elements;

import com.schism.core.network.MessageRepository;
import com.schism.core.network.messages.ElementParticlesMessage;
import com.schism.core.util.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ElementEventListener
{
    private static final ElementEventListener INSTANCE = new ElementEventListener();

    /**
     * Gets the singleton ElementEventListener instance.
     * @return The Element Event Listener which responds to various events for element based effects.
     */
    public static ElementEventListener get()
    {
        return INSTANCE;
    }

    /**
     * Called when an entity is hurt.
     * @param event The entity hurt event.
     */
    @SubscribeEvent
    public void onEntityDamage(LivingDamageEvent event)
    {
        if (event.getAmount() <= 0 || event.isCanceled() || event.getResult() == Event.Result.DENY || event.getEntityLiving() == null)
        {
            return;
        }
        Level level = event.getEntityLiving().getLevel();
        Vec3 position = new Vec3(event.getEntityLiving().position()).add(0, event.getEntityLiving().getEyeHeight(), 0);
        ElementRepository.get().damageElements(event.getSource(), event.getEntityLiving().getLevel(), event.getEntityLiving().blockPosition())
                .forEach(element -> MessageRepository.get().toLevel(new ElementParticlesMessage(element, position, (int)Math.ceil(event.getAmount())), level));
    }
}
