package com.schism.core.client.gui;

import com.schism.core.Schism;
import com.schism.core.gods.GodRepository;
import com.schism.core.resolvers.SoundEventResolver;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class GuiSounds
{
    private static GuiSounds INSTANCE = new GuiSounds();

    /**
     * A helper for registering and playing gui sounds.
     * @return The Gui Sounds singleton instance.
     */
    public static GuiSounds get()
    {
        if (INSTANCE == null) {
            INSTANCE = new GuiSounds();
        }
        return INSTANCE;
    }

    public GuiSounds()
    {
        // Generic Sounds:
        SoundEventResolver.get().fromId(Schism.NAMESPACE + ":gui.page");

        // Notification Sounds:
        SoundEventResolver.get().fromId(Schism.NAMESPACE + ":notification." + GodRepository.get().type());
    }

    /**
     * Plays a gui sound.
     * @param namespace The namespace of the sound event to get and play.
     * @param soundId The id of the sound event to get and play.
     */
    public void play(String namespace, String soundId)
    {
        SoundEventResolver.get().fromId(namespace + ":" + soundId).optional().ifPresent(this::play);
    }

    /**
     * Plays a gui sound.
     * @param soundEvent The sound event to play.
     */
    public void play(SoundEvent soundEvent)
    {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.getLevel().playLocalSound(player.position().x(), player.position().y(), player.position().z(), soundEvent, SoundSource.AMBIENT, 1, 1, false);
    }
}
