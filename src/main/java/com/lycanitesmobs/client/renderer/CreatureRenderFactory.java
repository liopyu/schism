package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraftforge.fml.client.registry.IRenderFactory;

public class CreatureRenderFactory<T extends BaseCreatureEntity> implements IRenderFactory {
    protected CreatureInfo creatureInfo;

    public CreatureRenderFactory(CreatureInfo creatureInfo) {
        this.creatureInfo = creatureInfo;
    }

    @Override
    public EntityRenderer<? super T> createRenderFor(EntityRendererManager manager) {
        try {
            return new CreatureRenderer(this.creatureInfo.getName(), manager, (float) this.creatureInfo.width / 2);
        }
        catch (Exception e) {
            LycanitesMobs.logWarning("", "An exception occurred when creating a creature renderer:");
            e.printStackTrace();
        }
        return null;
    }

}
