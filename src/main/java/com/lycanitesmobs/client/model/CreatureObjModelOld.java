package com.lycanitesmobs.client.model;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.obj.ObjModel;
import com.lycanitesmobs.client.obj.ObjPart;
import com.lycanitesmobs.client.obj.VBOObjModel;
import com.lycanitesmobs.client.renderer.layer.LayerCreatureBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ModInfo;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class CreatureObjModelOld extends CreatureModel {
    // Global:
    /** An initial x rotation applied to make Blender models match Minecraft. **/
    public static float modelXRotOffset = 180F;
    /** An initial y offset applied to make Blender models match Minecraft. **/
    public static float modelYPosOffset = -1.5F;
	
	// Model:
    /** An instance of the model, the model should only be set once and not during every tick or things will get very laggy! **/
    public ObjModel objModel;

    /** A list of all parts that belong to this model. **/
    public List<ObjPart> wavefrontParts;

    /** A map containing the XYZ offset for each part to use when centering. **/
	public Map<String, float[]> partCenters = new HashMap<>();
    /** A map containing the XYZ sub-offset for each part to use when centering. These are for parts with two centers such as mouth parts that match their centers to the head part but have a subcenter for opening and closing. **/
	public Map<String, float[]> partSubCenters = new HashMap<>();
    /** A map to be used on the fly, this allows one part to apply a position offset to another part. This is no longer used though and will be made redundant when the new model code is created. **/
	public Map<String, float[]> offsets = new HashMap<>();

    // Head:
    /** If true, head pieces will ignore the x look rotation when animating. **/
	public boolean lockHeadX = false;
    /** If true, head pieces will ignore the y look rotation when animating. **/
	public boolean lockHeadY = false;

    // Head Model:
	/** For trophies. Used for displaying a body in place of a head/mount if the model has the head attached to the body part. Set to false if a head/mouth part is added. **/
	public boolean bodyIsTrophy = true;
    /** Used for scaling this model when displaying as a trophy. **/
    public float trophyScale = 1;
    /** Used for positioning this model when displaying as a trophy. If an empty array, no offset is applied, otherwise it must have at least 3 entries (x, y, z). **/
    public float[] trophyOffset = new float[0];
    /** Used for positioning this model's mouth parts when displaying as a trophy. If an empty array, no offset is applied, otherwise it must have at least 3 entries (x, y, z). **/
    public float[] trophyMouthOffset = new float[0];

    // Coloring:
	/** If true, no color effects will be applied, this is usually used for when the model is rendered as a red damage overlay, etc. **/
    public boolean dontColor = false;

	public CreatureObjModelOld() {
        this(1.0F);
    }
    
    public CreatureObjModelOld(float shadowSize) {
    	// Here a model should get its model, collect its parts into a list and then set the centers for each part.
    }

	/**
	 * Initializes this model, loading model data, etc.
	 * @param name The unique name this model should have.
	 * @param modInfo The mod this model belongs to.
	 * @param path The path to load the model data from (no extension).
	 * @return This model instance.
	 */
	public CreatureObjModelOld initModel(String name, ModInfo modInfo, String path) {
		// Check If Enabled:
		CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(name);
		if(creatureInfo != null && !creatureInfo.enabled) {
			return this;
		}

		this.objModel = new VBOObjModel(new ResourceLocation(modInfo.modid, "models/" + path + ".obj"));
        this.wavefrontParts = this.objModel.objParts;
        if(this.wavefrontParts.isEmpty())
			LycanitesMobs.logWarning("", "Unable to load (old format) model obj for: " + name + "");

        return this;
    }
    
    @Override
	public void render(BaseCreatureEntity entity, MatrixStack matrixStack, IVertexBuilder vertexBuilder, LayerCreatureBase layer, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness, int fade) {
    	this.matrixStack = matrixStack;

    	boolean isChild = false;
		if(entity != null) {
			isChild = entity.isBaby();
		}

        // Assess Scale and Check if Trophy:
		boolean trophyModel = false;
		if(scale < 0) {
            trophyModel = true;
			scale = -scale;
		}
		else {
			if(entity != null) {
                scale *= entity.getScale();
            }
		}

		// GUI Render:
		if(entity != null) {
			if(entity.onlyRenderTicks >= 0) {
				loop = entity.onlyRenderTicks;
			}
		}

        // Render and Animate Each Part:
        for(ObjPart part : this.wavefrontParts) {
    		if(part.getName() == null)
    			continue;
            String partName = part.getName().toLowerCase();

            // Trophy - Check if Trophy Part:
    		boolean isTrophyPart = this.isTrophyPart(partName);
    		if(this.bodyIsTrophy && partName.contains("body")) {
                isTrophyPart = true;
    		}

            // Skip Part If Not Rendered:
            if(!this.canRenderPart(partName, entity, layer, trophyModel) || (trophyModel && !isTrophyPart))
                continue;

            // Begin Rendering Part:
			matrixStack.pushPose();

            // Apply Initial Offsets: (To Match Blender OBJ Export)
            this.doAngle(modelXRotOffset, 1F, 0F, 0F);
            this.doTranslate(0F, modelYPosOffset, 0F);

            // Baby Heads:
            if(isChild && !trophyModel)
                this.childScale(partName);

            // Apply Scales:
            this.doScale(scale, scale, scale);
            if(trophyModel)
                this.doScale(this.trophyScale, this.trophyScale, this.trophyScale);

            // perching:
			if(entity != null && entity.hasPerchTarget()) {
				distance = 0;
			}

            // Animate (Part is centered and then animated):
            this.centerPart(partName);
            this.animatePart(partName, entity, time, distance, loop, -lookY, lookX, scale);

            // Trophy - Positioning:
            if(trophyModel) {
                if(!partName.contains("head") && !partName.contains("body")) {
                	float[] mouthOffset = this.comparePartCenters(this.bodyIsTrophy ? "body" : "head", partName);
                    this.doTranslate(mouthOffset[0], mouthOffset[1], mouthOffset[2]);
                    if(this.trophyMouthOffset.length >= 3)
                    	this.doTranslate(this.trophyMouthOffset[0], this.trophyMouthOffset[1], this.trophyMouthOffset[2]);
                }
                if(partName.contains("head")) {
                	if(!partName.contains("left")) {
                			this.doTranslate(-0.3F, 0, 0);
                			this.doAngle(5F, 0, 1, 0);
                	}
                	if(!partName.contains("right")) {
                			this.doTranslate(0.3F, 0, 0);
                			this.doAngle(-5F, 0, 1, 0);
                	}
                }
                this.uncenterPart(partName);
                if(this.trophyOffset.length >= 3)
                    this.doTranslate(this.trophyOffset[0], this.trophyOffset[1], this.trophyOffset[2]);
            }

            // Render:
            this.uncenterPart(partName);
			this.objModel.renderPart(vertexBuilder, matrixStack.last().normal(), matrixStack.last().pose(), this.getBrightness(partName, layer, entity, brightness), fade, part, this.getPartColor(partName, entity, layer, trophyModel, loop), this.getPartTextureOffset(partName, entity, layer, trophyModel, loop));
			matrixStack.popPose();
		}
	}

    /** Returns true if the provided part name should be shown for the trophy model. **/
    public boolean isTrophyPart(String partName) {
    	if(partName == null)
    		return false;
    	partName = partName.toLowerCase();
    	if(partName.contains("head") || partName.contains("mouth") || partName.contains("eye"))
			return true;
    	return false;
    }

    /**
     * Animates the individual part.
     * @param partName The name of the part (should be made all lowercase).
     * @param entity Can't be null but can be any entity. If the mob's exact entity or an EntityCreatureBase is used more animations will be used.
     * @param time How long the model has been displayed for? This is currently unused.
     * @param distance Used for movement animations, this should just count up form 0 every tick and stop back at 0 when not moving.
     * @param loop A continuous loop counting every tick, used for constant idle animations, etc.
     * @param lookY A y looking rotation used by the head, etc.
     * @param lookX An x looking rotation used by the head, etc.
     * @param scale Used for scale based changes during animation but not to actually apply the scale as it is applied in the renderer method.
     */
    public void animatePart(String partName, LivingEntity entity, float time, float distance, float loop, float lookY, float lookX, float scale) {
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
    	
    	// Head:
    	if(partName.toLowerCase().contains("head")) {
    		if(!lockHeadX)
    			rotX += Math.toDegrees(lookX / (180F / (float)Math.PI));
    		if(!lockHeadY)
    			rotY += Math.toDegrees(lookY / (180F / (float)Math.PI));
    	}
    	
    	// Apply Animations:
    	doAngle(rotation, angleX, angleY, angleZ);
    	doRotate(rotX, rotY, rotZ);
    	doTranslate(posX, posY, posZ);
    }

    public void childScale(String partName) {
    	doScale(0.5F, 0.5F, 0.5F);
    }

    public void setPartCenter(String partName, float centerX, float centerY, float centerZ) {
    	if(this.isTrophyPart(partName))
    		this.bodyIsTrophy = false;
    	this.partCenters.put(partName, new float[] {centerX, centerY, centerZ});
    }
    public void setPartCenters(float centerX, float centerY, float centerZ, String... partNames) {
    	for(String partName : partNames)
    		this.setPartCenter(partName, centerX, centerY, centerZ);
    }
    public float[] getPartCenter(String partName) {
    	if(!this.partCenters.containsKey(partName)) return new float[] {0.0F, 0.0F, 0.0F};
    	return this.partCenters.get(partName);
    }

    public void centerPart(String partName) {
    	if(!this.partCenters.containsKey(partName)) return;
    	float[] partCenter = this.partCenters.get(partName);
    	this.doTranslate(partCenter[0], partCenter[1], partCenter[2]);
    }
    public void uncenterPart(String partName) {
    	if(!this.partCenters.containsKey(partName)) return;
    	float[] partCenter = this.partCenters.get(partName);
    	this.doTranslate(-partCenter[0], -partCenter[1], -partCenter[2]);
    }

    public void centerPartToPart(String part, String targetPart) {
    	this.uncenterPart(part);
    	float[] partCenter = this.partCenters.get(targetPart);
    	if(partCenter != null)
    		this.doTranslate(partCenter[0], partCenter[1], partCenter[2]);
    }
    public void uncenterPartToPart(String part, String targetPart) {
    	float[] partCenter = this.partCenters.get(targetPart);
    	if(partCenter != null)
    		this.doTranslate(-partCenter[0], -partCenter[1], -partCenter[2]);
    	this.centerPart(part);
    }

    public float[] comparePartCenters(String centerPartName, String targetPartName) {
    	float[] centerPart = getPartCenter(centerPartName);
    	float[] targetPart = getPartCenter(targetPartName);
    	float[] partDifference = new float[3];
    	if(targetPart == null)
    		return partDifference;
    	for(int i = 0; i < 3; i++)
    		partDifference[i] = targetPart[i] - centerPart[i];
    	return partDifference;
    }

    public void setPartSubCenter(String partName, float centerX, float centerY, float centerZ) {
    	partSubCenters.put(partName, new float[] {centerX, centerY, centerZ});
    }
    public void setPartSubCenters(float centerX, float centerY, float centerZ, String... partNames) {
    	for(String partName : partNames)
    		setPartSubCenter(partName, centerX, centerY, centerZ);
    }
    public void subCenterPart(String partName) {
    	float[] offset = getSubCenterOffset(partName);
    	if(offset == null) return;
    	doTranslate(offset[0], offset[1], offset[2]);
    }
    public void unsubCenterPart(String partName) {
    	float[] offset = getSubCenterOffset(partName);
    	if(offset == null) return;
    	doTranslate(-offset[0], -offset[1], -offset[2]);
    }
    public float[] getSubCenterOffset(String partName) {
    	if(!partCenters.containsKey(partName)) return null;
    	if(!partSubCenters.containsKey(partName)) return null;
    	float[] partCenter = partCenters.get(partName);
    	float[] partSubCenter = partSubCenters.get(partName);
    	float[] offset = new float[3];
    	for(int coord = 0; coord < 3; coord++)
    		offset[coord] = partSubCenter[coord] - partCenter[coord];
    	return offset;
    }
    
    public void setOffset(String offsetName, float[] offset) {
    	offsets.put(offsetName,  offset);
    }
    public float[] getOffset(String offsetName) {
    	if(!offsets.containsKey(offsetName)) return new float[] { 0.0F, 0.0F, 0.0F };
    	return offsets.get(offsetName);
    }
}
