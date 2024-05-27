package com.schism.core.resolvers;

import com.schism.core.Schism;
import com.schism.core.database.CachedObject;
import com.schism.core.database.CachedRegistryObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class SoundEventResolver
{
    private static final SoundEventResolver INSTANCE = new SoundEventResolver();

    /**
     * Gets the singleton SoundEventResolver instance.
     * @return The Sound Event Resolver, a utility for getting and registering sound events from ids.
     */
    public static SoundEventResolver get()
    {
        return INSTANCE;
    }

    protected final Map<String, CachedObject<SoundEvent>> soundEvents = new HashMap<>();

    /**
     * Gets a cached sound event from an id, also adds new sounds ready for registration if in this mod's namespace, must be called before forge registration for this.
     * @param id The sound event id.
     * @return A cached registry object that provides an optional sound event.
     */
    public CachedRegistryObject<SoundEvent> fromId(String id)
    {
        ResourceLocation resourceLocation = new ResourceLocation(id);
        if (resourceLocation.getNamespace().equals(Schism.NAMESPACE) && !this.soundEvents.containsKey(resourceLocation.getPath())) {
            this.soundEvents.put(resourceLocation.getPath(), CachedObject.of(() -> {
                SoundEvent soundEvent = new SoundEvent(resourceLocation);
                soundEvent.setRegistryName(resourceLocation);
                return soundEvent;
            }));
        }
        return new CachedRegistryObject<>(id, () -> ForgeRegistries.SOUND_EVENTS);
    }

    /**
     * Registers sound events into Forge.
     * @param event The sound event registry event.
     */
    @SubscribeEvent
    public void onSoundEventsRegistry(final RegistryEvent.Register<SoundEvent> event)
    {
        this.soundEvents.values().forEach(optionalSoundEvent -> {
            if (!optionalSoundEvent.isPresent()) {
                return;
            }
            event.getRegistry().register(optionalSoundEvent.get());
        });
    }
}
