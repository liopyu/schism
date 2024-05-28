package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateQuadruped;
import net.minecraft.entity.LivingEntity;

public class ModelAglebemu extends ModelTemplateQuadruped {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelAglebemu() {
        this(1.0F);
    }

    public ModelAglebemu(float shadowSize) {
        // Load Model:
        this.initModel("aglebemu", LycanitesMobs.modInfo, "entity/aglebemu");

        // Looking:
        this.lookHeadScaleX = 0.5f;
        this.lookHeadScaleY = 0.5f;

        // Trophy:
        this.trophyScale = 0.8F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
        this.trophyMouthOffset = new float[] {0.0F, -0.25F, 0.0F};
        this.bodyIsTrophy = true;
    }


    // ==================================================
    //                 Animate Part
    // ==================================================
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
        super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);

        // Attack:
        if(partName.equals("tongue")) {
            this.scale(this.getAttackProgress() * 2, this.getAttackProgress() * 2, this.getAttackProgress() * 2);
        }
    }
}
