package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelLacedon extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelLacedon() {
        this(1.0F);
    }
    
    public ModelLacedon(float shadowSize) {
    	// Load Model:
    	this.initModel("lacedon", LycanitesMobs.modInfo, "entity/lacedon");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 1.6F, 0.05F);
    	setPartCenter("body", 0F, 1.6F, 0.05F);
    	setPartCenter("leftarm", 0.25F, 1.4F, 0F);
    	setPartCenter("rightarm", -0.25F, 1.4F, 0F);
    	setPartCenter("leftleg", 0.25F, 0.85F, 0F);
    	setPartCenter("rightleg", -0.25F, 0.85F, 0F);
    	
    	// Trophy:
        this.trophyScale = 1.0F;
    }
    
    
    // ==================================================
   	//                 Animate Part
   	// ==================================================
    float maxLeg = 0F;
    @Override
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
    	super.animatePart(partName, entity, time, distance, loop, lookY, lookX, scale);
    	float pi = (float)Math.PI;
    	float posX = 0F;
    	float posY = 0F;
    	float posZ = 0F;
    	float angleX = 0F;
    	float angleY = 0F;
    	float angleZ = 0F;
    	float rotation = 0F;
    	float rotX = 0F;
    	float rotY = 0F;
    	float rotZ = 0F;
    	
    	// Idle:
    	if(partName.equals("leftarm")) {
	        rotZ -= Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX -= Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F);
    	}
    	if(partName.equals("rightarm")) {
	        rotZ += Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX += Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F);
    	}
    	
    	// Walking:
    	if(entity == null || entity.isOnGround() || entity.isInWater()) {
	    	float walkSwing = 0.6F;
	    	if(partName.equals("leftarm")) {
	    		rotX += Math.toDegrees(MathHelper.cos(time * walkSwing) * 1.0F * distance * 0.5F);
				rotZ -= Math.toDegrees(MathHelper.cos(time * walkSwing) * 0.5F * distance * 0.5F);
	    	}
	    	if(partName.equals("rightarm")) {
	    		rotX += Math.toDegrees(MathHelper.cos(time * walkSwing + (float)Math.PI) * 1.0F * distance * 0.5F);
				rotZ += Math.toDegrees(MathHelper.cos(time * walkSwing + (float)Math.PI) * 0.5F * distance * 0.5F);
	    	}
	    	if(partName.equals("leftleg"))
	    		rotX += Math.toDegrees(MathHelper.cos(time * walkSwing + (float)Math.PI) * 1.4F * distance);
	    	if(partName.equals("rightleg"))
	    		rotX += Math.toDegrees(MathHelper.cos(time * walkSwing) * 1.4F * distance);
    	}
				
		// Attack:
		if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
	    	if(partName.equals("leftarm"))
	    		doRotate(0.0F, -25.0F, 0.0F);
	    	if(partName.equals("rightarm"))
	    		doRotate(0.0F, 25.0F, 0.0F);
		}
		
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
}
