package com.lycanitesmobs.client.renderer;

import com.google.common.collect.Lists;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.ModelManager;
import com.lycanitesmobs.client.model.ProjectileModel;
import com.lycanitesmobs.client.model.ProjectileObjModel;
import com.lycanitesmobs.client.obj.VBOObjModel;
import com.lycanitesmobs.client.renderer.layer.LayerProjectileBase;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ProjectileModelRenderer extends EntityRenderer<BaseProjectileEntity> implements IEntityRenderer<BaseProjectileEntity, ProjectileModel> {
	protected ProjectileModel renderModel;
	protected ProjectileModel defaultModel;
	protected final List<LayerRenderer<BaseProjectileEntity, ProjectileModel>> renderLayers = Lists.newArrayList(); // TODO Layers for projectiles.


	public ProjectileModelRenderer(EntityRendererManager renderManager, ProjectileInfo projectileInfo) {
		super(renderManager);
		this.renderModel = ModelManager.getInstance().getProjectileModel(projectileInfo);
		this.defaultModel = this.renderModel;
		this.renderModel.addCustomLayers(this);
	}

    public ProjectileModelRenderer(EntityRendererManager renderManager, String projectileName) {
    	super(renderManager);
		ProjectileInfo projectileInfo = ProjectileManager.getInstance().getProjectile(projectileName);
		if(projectileInfo != null) {
			this.renderModel = ModelManager.getInstance().getProjectileModel(projectileInfo);
		}
    	else {
			this.renderModel = ModelManager.getInstance().getOldProjectileModel(projectileName);
		}
    	if(renderModel == null) {
    		return;
		}
		this.defaultModel = this.renderModel;
		this.renderModel.addCustomLayers(this);
    }

	@Override
	public void render(BaseProjectileEntity entity, float partialTicks, float yaw, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int brightness) {
		// Model States:
		float time = 0;
		float distance = 0;
		float loop = (float)entity.tickCount + (partialTicks % 1.0F);
		float lookYaw = 0;
		float lookPitch = 0;
		float scale = 1;
		boolean invisible = false;
		boolean allyInvisible = false;

		// Render Model and Layers:
		try {
			matrixStack.translate(0, -0.25F, 0); // translate
			matrixStack.scale(0.25F, 0.25F, 0.25F); // scale
			matrixStack.mulPose(new Vector3f(0.0F, 1.0F, 0.0F).rotationDegrees(entity.yRot)); // rotate

			if(this.getModel() == null) {
				LycanitesMobs.logWarning("", "Missing Projectile Model: " + entity);
			}
			else if (!(this.getModel() instanceof ProjectileObjModel)) {
				ResourceLocation texture = this.getTextureLocation(entity);
				if(texture == null) {
					return;
				}
				RenderType renderType = CustomRenderStates.getObjRenderType(texture, this.renderModel.getBlending(entity, null), this.renderModel.getGlow(entity, null));
				this.getModel().render(entity, matrixStack, renderTypeBuffer.getBuffer(renderType), null, 0, 0, loop, 0, 0, scale, brightness);
			}
			else {

				this.getModel().generateAnimationFrames(entity, time, distance, loop, lookYaw, lookPitch, 1, brightness);
				this.renderModel(entity, matrixStack, renderTypeBuffer, null, time, distance, loop, lookYaw, lookPitch, 1, brightness, invisible, allyInvisible);
				for(LayerRenderer<BaseProjectileEntity, ProjectileModel> layer : this.renderLayers) {
					if(!(layer instanceof LayerProjectileBase)) {
						continue;
					}
					LayerProjectileBase layerCreatureBase = (LayerProjectileBase)layer;
					if(!layerCreatureBase.canRenderLayer(entity, scale)) {
						continue;
					}
					this.renderModel(entity, matrixStack, renderTypeBuffer, layerCreatureBase, time, distance, loop, lookYaw, lookPitch, scale, brightness, invisible, allyInvisible);
				}
				this.getModel().clearAnimationFrames();

				VBOBatcher.getInstance().endBatches();
			}
		}
		catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * Renders the main model.
	 * @param entity The entity to render.
	 * @param matrixStack The matrix stack for animation.
	 * @param renderTypeBuffer  The render type buffer for rendering with.
	 * @param layer The layer to render, the base layer is null.
	 * @param time The current movement time for walk cycles, etc.
	 * @param distance The current movement amount for walk cycles, etc.
	 * @param loop A constant tick for looping animations.
	 * @param lookY The entity's yaw looking position for head rotation, etc.
	 * @param lookX The entity's pitch looking position for head rotation, etc.
	 * @param scale The base scale to render the model at, usually just 1 which scales 1m unit in Blender to a 1m block unit in Minecraft.
	 * @param brightness The brightness of the mob based on block location, etc.
	 * @param invisible If true, the entity has invisibility or some form of stealth.
	 * @param allyInvisible If true, the entity has invisibility or some form of stealth but is allied to the player so should be translucent, etc.
	 */
	protected void renderModel(BaseProjectileEntity entity, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, LayerProjectileBase layer, float time, float distance, float loop, float lookY, float lookX, float scale, int brightness, boolean invisible, boolean allyInvisible) {
		ResourceLocation texture = this.getEntityTexture(entity, layer);
		if (texture == null) {
			return;
		}

		// Render Model
		// TODO allyInvisible lower color alpha
		if (!invisible || allyInvisible) {
			VBOObjModel.renderType = CustomRenderStates.getObjVBORenderType(this.getModel().getBlending(entity, layer), this.getModel().getGlow(entity, layer));
			VBOObjModel.renderNormal = true;
		}
		if (entity.isGlowing()) {
			VBOObjModel.renderOutline = true;
		}
		VBOObjModel.tex = texture;
		this.getModel().render(entity, matrixStack, null, layer, time, distance, loop, lookY, lookX, 1, brightness);
		VBOObjModel.tex = null;
		VBOObjModel.renderOutline = false;
		VBOObjModel.renderNormal = false;
		VBOObjModel.renderType = null;
	}

	@Override
	public ProjectileModel getModel() {
		return this.renderModel;
	}

	public final boolean addLayer(LayerRenderer<BaseProjectileEntity, ProjectileModel> layer) {
		return this.renderLayers.add(layer);
	}

	/**
	 * Gets the texture to use.
	 * @param entity The entity to get the texture from.
	 * @param layer The layer to get the texture for.
	 * @return The texture to bind.
	 */
	public ResourceLocation getEntityTexture(BaseProjectileEntity entity, LayerProjectileBase layer) {
		if(layer == null) {
			return this.getTextureLocation(entity);
		}
		ResourceLocation layerTexture = layer.getLayerTexture(entity);
		return layerTexture != null ? layerTexture : this.getTextureLocation(entity);
	}

	@Override
	public ResourceLocation getTextureLocation(BaseProjectileEntity entity) {
		return entity.getTexture();
	}
}
