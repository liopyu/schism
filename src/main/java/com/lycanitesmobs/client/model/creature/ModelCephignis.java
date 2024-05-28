package com.lycanitesmobs.client.model.creature;


import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.model.CreatureObjModelOld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModelCephignis extends CreatureObjModelOld {
	
	// ==================================================
  	//                    Constructors
  	// ==================================================
    public ModelCephignis() {
        this(1.0F);
    }
    
    public ModelCephignis(float shadowSize) {
    	// Load Model:
    	this.initModel("cephignis", LycanitesMobs.modInfo, "entity/cephignis");
    	


    	
    	// Set Rotation Centers:
    	setPartCenter("head", 0F, 0.5F, 0.4F);
    	setPartCenter("body", 0F, 0.5F, 0.4F);
    	setPartCenter("armleft", 0.15F, 0.4F, 0.3F);
    	setPartCenter("armright", -0.15F, 0.4F, 0.3F);
    	
    	// Trophy:
        this.trophyScale = 0.8F;
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
    	
    	// Idle:
    	if(partName.equals("body")) {
			rotY = (float)-Math.toDegrees(MathHelper.cos(loop * 0.2F) * 0.05F - 0.05F);
		}
    	if(partName.equals("armleft")) {
	        rotZ -= Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX -= Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F);
    	}
    	if(partName.equals("armright")) {
	        rotZ += Math.toDegrees(MathHelper.cos(loop * 0.09F) * 0.05F + 0.05F);
	        rotX += Math.toDegrees(MathHelper.sin(loop * 0.067F) * 0.05F);
    	}
    	
    	// Walking:
    	if(entity != null && entity.isInWater()) {
	    	if(partName.equals("body")) {
				rotY += (float)-Math.toDegrees(MathHelper.cos(time * 0.1F) * 0.2F);
			}
	    	float walkSwing = 0.6F;
	    	if(partName.equals("armleft")) {
	    		rotX += Math.toDegrees(MathHelper.cos(time * walkSwing) * 1.0F * distance * 0.5F);
				rotZ -= Math.toDegrees(MathHelper.cos(time * walkSwing) * 0.5F * distance * 0.5F);
	    	}
	    	if(partName.equals("armright")) {
	    		rotX += Math.toDegrees(MathHelper.cos(time * walkSwing + (float)Math.PI) * 1.0F * distance * 0.5F);
				rotZ += Math.toDegrees(MathHelper.cos(time * walkSwing + (float)Math.PI) * 0.5F * distance * 0.5F);
	    	}
    	}
		
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }
    
    
    // ==================================================
   	//              Rotate and Translate
   	// ==================================================
    @Override
    public void childScale(String partName) {
    	if(partName.equals("head"))
    		doTranslate(-(getPartCenter(partName)[0] / 2), -(getPartCenter(partName)[1] / 2), -(getPartCenter(partName)[2] / 2));
    	else
        	super.childScale(partName);
    }
}
