package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.template.ModelTemplateArachnid;
import net.minecraft.entity.LivingEntity;

public class ModelHerma extends ModelTemplateArachnid {

    // ==================================================
    //                    Constructors
    // ==================================================
    public ModelHerma() {
        this(1.0F);
    }

    public ModelHerma(float shadowSize) {
        // Load Model:
        this.initModel("herma", LycanitesMobs.modInfo, "entity/herma");

        // Looking:
        this.lookHeadScaleX = 0;
        this.lookHeadScaleY = 0;

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

        // Random Walk Rotation:
        if(partName.equals("head") && this.currentModelState != null) {
            if(entity != null && distance >= 0.25f) {
                if (this.currentModelState.getFloat("walkingAngleChangeTime") <= 0) {
                    float random = entity.getRandom().nextFloat();
                    if (random <= 0.3f)
                        this.currentModelState.setFloat("walkingAngleTarget", 90);
                    else if (random <= 0.6f)
                        this.currentModelState.setFloat("walkingAngleTarget", -90);
                    else
                        this.currentModelState.setFloat("walkingAngleTarget", 0);
                    this.currentModelState.setFloat("walkingAngleChangeTime", 10 * 20);
                }
            }
            else {
                this.currentModelState.setFloat("walkingAngleTarget", 0);
            }
            float walkingAngleCurrent = this.currentModelState.getFloat("walkingAngleCurrent");
            if(walkingAngleCurrent < this.currentModelState.getFloat("walkingAngleTarget"))
                this.currentModelState.setFloat("walkingAngleCurrent", walkingAngleCurrent + 1);
            else if(walkingAngleCurrent > this.currentModelState.getFloat("walkingAngleTarget"))
                this.currentModelState.setFloat("walkingAngleCurrent", walkingAngleCurrent - 1);
            this.rotate(0, this.currentModelState.getFloat("walkingAngleCurrent"), 0);
            this.currentModelState.setFloat("walkingAngleChangeTime", this.currentModelState.getFloat("walkingAngleChangeTime") - 1);
        }
    }
}
