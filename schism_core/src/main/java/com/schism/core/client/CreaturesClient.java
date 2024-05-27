package com.schism.core.client;

import com.schism.core.creatures.Creatures;
import com.schism.core.creatures.ICreature;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

public class CreaturesClient
{
    private static CreaturesClient INSTANCE;

    /**
     * Gets the singleton CreaturesClient instance.
     * @return The Creatures Client which is used for client side visuals on entity related to creatures, etc.
     */
    public static CreaturesClient get()
    {
        if (INSTANCE == null) {
            INSTANCE = new CreaturesClient();
        }
        return INSTANCE;
    }

    /**
     * Called when nameplates are rendered.
     * @param event The render nameplate event.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderNameplate(RenderNameplateEvent event)
    {
        if (event.isCanceled() || !(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }

        Optional<ICreature> creature = Creatures.get().getCreature(livingEntity);
        if (creature.isPresent()) {
            MutableComponent component = new TextComponent("").append(event.getContent());
            if (creature.get().modifyNameTag(component)) {
                event.setResult(Event.Result.ALLOW);
            }
            event.setContent(component);
        }
    }
}
