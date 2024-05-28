package com.lycanitesmobs.client.renderer.layer;

import com.lycanitesmobs.client.renderer.CreatureRenderer;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.RideableCreatureEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LayerCreatureSaddle extends LayerCreatureBase {
    public LayerCreatureSaddle(CreatureRenderer renderer) {
        super(renderer);
    }

    @Override
    public boolean canRenderLayer(BaseCreatureEntity entity, float scale) {
        if(!super.canRenderLayer(entity, scale) || !(entity instanceof RideableCreatureEntity))
            return false;
        return ((RideableCreatureEntity)entity).hasSaddle();
    }

    @Override
    public ResourceLocation getLayerTexture(BaseCreatureEntity entity) {
        return entity.getEquipmentTexture("saddle");
    }
}
