package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateAquatic;
import com.lycanitesmobs.client.renderer.CreatureRenderer;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.client.renderer.layer.specific.LayerRoa;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

public class ModelRoa extends ModelTemplateAquatic {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelRoa() {
        this(1.0F);
    }

    public ModelRoa(float shadowSize) {
        // Load Model:
        this.initModel("roa", LycanitesMobs.modInfo, "entity/roa");

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
        this.trophyMouthOffset = new float[] {0.0F, -0.25F, 0.0F};
    }


    // ==================================================
    //             Add Custom Render Layers
    // ==================================================
    @Override
    public void addCustomLayers(CreatureRenderer renderer) {
        super.addCustomLayers(renderer);
        renderer.addLayer(new LayerRoa(renderer));
    }


    // ==================================================
    //                Can Render Part
    // ==================================================
    /** Returns true if the part can be rendered on the base layer. **/
    @Override
    public boolean canBaseRenderPart(String partName, Entity entity, boolean trophy) {
       if("effect".equals(partName)) {
           return false;
       }
       return true;
    }

    @Override
    public boolean canRenderPart(String partName, Entity entity, LayerCreatureBase layer, boolean trophy) {
        if("effect".equals(partName)) {
            return layer instanceof LayerRoa;
        }
        return super.canRenderPart(partName, entity, layer, trophy);
    }


    // ==================================================
    //                 Animate Part
    // ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
        float posX = 0F;
        float posY = 0F;
        float posZ = 0F;
        float rotX = 0F;
        float rotY = 0F;
        float rotZ = 0F;

        // Effect:
        if(partName.contains("effect")) {
            rotY += loop * 30;
            float effectScale = 1.05F + ((0.5F + (MathHelper.sin(loop / 4) / 2)) / 8);
            this.scale(effectScale, effectScale, effectScale);
        }

        // Apply Animations:
        this.rotate(rotX, rotY, rotZ);
        this.translate(posX, posY, posZ);
    }
}
