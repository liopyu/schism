package com.lycanitesmobs.client.model.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.CreatureObjModelOld;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelClink extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelClink() {
        this(1.0F);
    }
    
    public ModelClink(float shadowSize) {
    	// Load Model:
    	this.initModel("Clink", LycanitesMobs.modInfo, "entity/clink");
    	


    	
    	// Set Rotation Centers:
    	setPartCenters(0F, 1.5F, 0.2F, "head", "mouth");
    	setPartSubCenter("mouth", 0F, 1.475F, 0.45F);
    	setPartCenter("body", 0F, 1.5F, 0.2F);
    	setPartCenter("leftarm", 0.55F, 1.5F, 0.15F);
    	setPartCenter("rightarm", -0.55F, 1.5F, 0.15F);
    	setPartCenter("leftleg", 0.3F, 0.75F, -0.25F);
    	setPartCenter("rightleg", -0.3F, 0.75F, -0.25F);
    	setPartCenters(0.0F, 0.9F, -0.4F, "tail01", "tail02", "tail03", "tail04", "tail05");
    	setPartSubCenter("tail02", 0.0F, 1.1F, -0.8F);
    	setPartSubCenter("tail03", 0.0F, 1.55F, -0.9F);
    	setPartSubCenter("tail04", 0.0F, 0.8F, -0.65F);
    	setPartSubCenter("tail05", 0.0F, 2.0F, -0.8F);
    	
    	// Trophy:
        this.trophyScale = 1.2F;
        this.trophyOffset = new float[] {0.0F, 0.0F, -0.4F};
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
    	
    	// Looking:
    	if(partName.equals("mouth")) {
    		if(!lockHeadX)
    			rotX += Math.toDegrees(lookX / (180F / (float)Math.PI));
    		if(!lockHeadY)
    			rotY += Math.toDegrees(lookY / (180F / (float)Math.PI));
    	}
    	
    	// Idle:
    	if(partName.equals("mouth")) {
    		subCenterPart("mouth");
    		doRotate((float)-Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F - 0.05F), 0.0F, 0.0F);
    		unsubCenterPart("mouth");
    	}
    	if(partName.equals("leftarm")) {
	        rotZ -= Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX -= Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F);
    	}
    	if(partName.equals("rightarm")) {
	        rotZ += Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX += Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F);
    	}
    	
    	// Walking:
    	float walkSwing = 0.6F;
    	if(partName.equals("leftarm"))
    		rotX += Math.toDegrees(MathHelper.cos(time * walkSwing) * 2.0F * distance * 0.5F);
    	if(partName.equals("rightarm"))
    		rotX += Math.toDegrees(MathHelper.cos(time * walkSwing + (float)Math.PI) * 2.0F * distance * 0.5F);
    	if(partName.equals("leftleg"))
    		rotX += Math.toDegrees(MathHelper.cos(time * walkSwing + (float)Math.PI) * 1.4F * distance);
    	if(partName.equals("rightleg"))
    		rotX += Math.toDegrees(MathHelper.cos(time * walkSwing) * 1.4F * distance);
    	
    	// Attacks:
    	if(entity instanceof BaseCreatureEntity && ((BaseCreatureEntity)entity).isAttackOnCooldown()) {
	    	if(partName.equals("mouth")) {
	    		subCenterPart("mouth");
	    		doRotate(30.0F, 0.0F, 0.0F);
	    		unsubCenterPart("mouth");
	    	}
	    	if(partName.equals("rightarm")) {
    			angleX = -0.2F;
    			angleZ = 0F;
	    		if(((BaseCreatureEntity)entity).getAttackPhase() == 2) {
	    			angleY = 0.2F;
	    			rotation = 85;
	    		}
	    		else if(((BaseCreatureEntity)entity).getAttackPhase() == 3) {
	    			angleY = 0.7F;
	    			angleZ = -0.1F;
	    			rotation = 40;
	    		}
	    		else {
	    			angleY = 0.5F;
	    			rotation = 85;
	    		}
	    	}
    	}
    	
    	
    	// Tail:
    	if(partName.equals("tail01") || partName.equals("tail02") || partName.equals("tail03") || partName.equals("tail04") || partName.equals("tail05")) {
    		rotX = (float)-Math.toDegrees(MathHelper.cos(loop * 0.1F) * 0.05F - 0.05F);
    		rotY = (float)-Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F - 0.05F);
    	}
    	
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
}
