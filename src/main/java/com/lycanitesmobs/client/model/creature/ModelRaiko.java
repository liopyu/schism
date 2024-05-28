package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.CreatureObjModel;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelRaiko extends CreatureObjModel {

	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelRaiko() {
        this(1.0F);
    }

    public ModelRaiko(float shadowSize) {
    	// Load Model:
    	this.initModel("raiko", LycanitesMobs.modInfo, "entity/raiko");
    	
    	// Trophy:
        this.trophyScale = 1.0F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
    }
    
    
    // ==================================================
   	//                 Animate Part
   	// ==================================================
    float maxLeg = 0F;
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
    	float posX = 0F;
    	float posY = 0F;
    	float posZ = 0F;
    	float rotX = 0F;
    	float rotY = 0F;
    	float rotZ = 0F;
    	
    	// Idle:
    	if(partName.equals("mouth")) {
    		this.rotate((float)-Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F - 0.05F), 0.0F, 0.0F);
    	}

		// Walking:
		if(entity == null || entity.isOnGround()) {
			if(partName.equals("body")) {
				posY -= 0.6f;
			}
			float walkSwing = 0.6F;
			float wingOffset = 1;
			if(entity.isInWater()) {
				wingOffset = 0;
			}
			if(partName.equals("wingright")) {
				rotX += Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F) - (45 * wingOffset);
				this.rotate(1, 20 * wingOffset, 0 * wingOffset);
				rotZ -= Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F + 0.05F) + (90 * wingOffset);
				this.scale(-1, 1, 1);
			}
			if(partName.equals("wingleft")) {
				rotX += Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F) - (45 * wingOffset);
				this.rotate(1, -20 * wingOffset, 0 * wingOffset);
				rotZ += Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F + 0.05F) + (90 * wingOffset);
				this.scale(-1, 1, 1);
			}
			if(partName.equals("legleft")) {
				rotX += Math.toDegrees(MathHelper.cos(time * walkSwing + (float) Math.PI) * 0.8F * distance);
			}
			if(partName.equals("legright")) {
				rotX += Math.toDegrees(MathHelper.cos(time * walkSwing) * 0.8F * distance);
			}
			if (partName.contains("clawback")) {
				rotX += 90;
			}
			else if (partName.contains("claw")) {
				rotX -= 25;
			}
		}
		else if(partName.equals("body")) {
			float bob = -MathHelper.sin(loop * 0.2F) * 0.1F;
			if (bob < 0)
				bob = -bob;
			posY += bob;
		}

    	// Flying:
    	if(!entity.isInWater() && !entity.isOnGround()) {
			if (partName.equals("wingleft")) {
				rotX = 20;
				rotX -= Math.toDegrees(MathHelper.sin(loop * 0.4F) * 0.6F);
				rotZ -= Math.toDegrees(MathHelper.sin(loop * 0.4F) * 0.6F);
			}
			if (partName.equals("wingright")) {
				rotX = 20;
				rotX -= Math.toDegrees(MathHelper.sin(loop * 0.4F) * 0.6F);
				rotZ -= Math.toDegrees(MathHelper.sin(loop * 0.4F + (float) Math.PI) * 0.6F);
			}
			if (entity instanceof BaseCreatureEntity && ((BaseCreatureEntity) entity).hasPickupEntity()) {
				if (partName.contains("clawback")) {
					rotX -= 25;
				}
				else if (partName.contains("claw")) {
					rotX += 25;
				}
			}
			else {
				if(partName.equals("legleft")) {
					rotX -= Math.toDegrees(MathHelper.sin(loop * 0.1F + (float)Math.PI) * 0.2F);
				}
				if(partName.equals("legright")) {
					rotX -= Math.toDegrees(MathHelper.sin(loop * 0.1F) * 0.2F);
				}
				if (partName.contains("clawback")) {
					rotX += Math.toDegrees(MathHelper.sin(loop * 0.1F) * 0.4F);
				}
				else if (partName.contains("claw")) {
					rotX -= Math.toDegrees(MathHelper.sin(loop * 0.1F) * 0.4F);
				}
			}
		}

    	// Apply Animations:
    	this.translate(posX, posY, posZ);
		this.rotate(rotX, rotY, rotZ);
    }
}
