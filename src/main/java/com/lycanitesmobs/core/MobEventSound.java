package com.lycanitesmobs.core;

import net.minecraft.client.audio.ITickableSound;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobEventSound extends SimpleSound implements ITickableSound {
    protected final Entity entity;
    protected boolean donePlaying;

    public MobEventSound(SoundEvent soundEvent, SoundCategory categoryIn, Entity entity, float volume, float pitch) {
        super(soundEvent, categoryIn, volume, pitch, (float)entity.position().x(), (float)entity.position().y(), (float)entity.position().z());
        this.entity = entity;
        this.looping = true;
        this.volume = volume;
        this.pitch = 1.0F;
        this.looping = false;
    }

    @Override
    public void tick() {
        if (this.entity == null || !this.entity.isAlive()) {
            this.donePlaying = true;
        }
        else {
            this.x = (float)this.entity.position().x();
            this.y = (float)this.entity.position().y();
            this.z = (float)this.entity.position().z();
        }
    }

    @Override
    public boolean isStopped() {
        return this.donePlaying;
    }
}