package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.entity.LaserEndProjectileEntity;
import com.lycanitesmobs.core.entity.LaserProjectileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProjectileSpriteRenderer extends EntityRenderer<BaseProjectileEntity> {
    private Class projectileClass;

    public ProjectileSpriteRenderer(EntityRendererManager renderManager, Class projectileClass) {
    	super(renderManager);
        this.projectileClass = projectileClass;
    }

    @Override
	public void render(BaseProjectileEntity entity, float partialTicks, float yaw, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int brightness) {
		if(entity instanceof CustomProjectileEntity && ((CustomProjectileEntity)entity).projectileInfo == null) {
			return;
		}
		if(entity.getClass() == LaserEndProjectileEntity.class) {
			return;
		}

		// Render States:
    	float loop = (float)entity.tickCount + Math.min(1, partialTicks);
    	float scale = entity.getProjectileScale();

		// Render Laser:
		if(entity instanceof CustomProjectileEntity && ((CustomProjectileEntity)entity).getLaserEnd() != null) {
			matrixStack.pushPose();
			this.renderLaser((CustomProjectileEntity)entity, matrixStack, renderTypeBuffer, ((CustomProjectileEntity)entity).laserWidth / 4, loop);
			matrixStack.popPose();
			return;
		}

    	// Render Projectile Sprite:
		matrixStack.pushPose();
		matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
		matrixStack.mulPose(new Vector3f(0.0F, 1.0F, 0.0F).rotationDegrees(180.0F));
		matrixStack.translate(0, entity.getTextureOffsetY(), 0); // translate
		matrixStack.mulPose(new Vector3f(0, 0, 1).rotationDegrees(loop * entity.rollSpeed * 4)); // Projectile Spinning
		matrixStack.scale(scale, scale, scale); // Projectile Scaling
		ResourceLocation texture = this.getTextureLocation(entity);
		RenderType rendertype = CustomRenderStates.getSpriteRenderType(texture);
		this.renderSprite(entity, matrixStack, renderTypeBuffer, rendertype, entity.textureScale);
		matrixStack.popPose();
    }

    public void renderSprite(BaseProjectileEntity entity, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, RenderType rendertype, float scale) {
		float textureWidth = 0.25F;
		float textureHeight = 0.25F;
		float minU = 0;
		float maxU = 1;
		float minV = 0;
		float maxV = 1;
		if(entity.animationFrameMax > 0) {
			minV = (float)entity.animationFrame / (float)entity.animationFrameMax;
			maxV = minV + (1F / (float)entity.animationFrameMax);
			textureWidth *= scale;
			textureHeight *= scale;
		}

		Matrix4f matrix4f = matrixStack.last().pose();
		IVertexBuilder vertexBuilder = renderTypeBuffer.getBuffer(rendertype);
		vertexBuilder
				.vertex(matrix4f, -textureWidth, -textureHeight + (textureHeight / 2), 0.0F) // pos
				.color(255, 255, 255, 255) // color
				.uv(minU, maxV) // texture
				.normal(0.0F, 1.0F, 0.0F) // normal
				.endVertex();
		vertexBuilder
				.vertex(matrix4f, textureWidth, -textureHeight + (textureHeight / 2), 0.0F)
				.color(255, 255, 255, 255) // color
				.uv(maxU, maxV)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		vertexBuilder
				.vertex(matrix4f, textureWidth, textureHeight + (textureHeight / 2), 0.0F)
				.color(255, 255, 255, 255) // color
				.uv(maxU, minV)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
		vertexBuilder
				.vertex(matrix4f, -textureWidth, textureHeight + (textureHeight / 2), 0.0F)
				.color(255, 255, 255, 255) // color
				.uv(minU, minV)
				.normal(0.0F, 1.0F, 0.0F)
				.endVertex();
    }

    public void renderLaser(CustomProjectileEntity entity, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, float scale, float loop) {
    	double laserSize = entity.position().distanceTo(entity.getLaserEnd());
		float spacing = 1;
		double factor = spacing / laserSize;
		if(laserSize <= 0) {
			return;
		}
		ResourceLocation texture = this.getTextureLocation(entity);
		RenderType rendertype = CustomRenderStates.getSpriteRenderType(texture);
		Vector3d direction = entity.getLaserEnd().subtract(entity.position()).normalize();
		for(float segment = 0; segment <= laserSize; segment += factor) {
			matrixStack.pushPose();
			matrixStack.translate(segment * direction.x() * spacing, segment * direction.y() * spacing, segment * direction.z() * spacing);
			matrixStack.translate(0, entity.getTextureOffsetY(), 0); // translate
			matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
			matrixStack.mulPose(new Vector3f(0.0F, 1.0F, 0.0F).rotationDegrees(180.0F));
			matrixStack.scale(scale, scale, scale); // Laser Scaling
			matrixStack.mulPose(new Vector3f(0, 0, 1).rotationDegrees(loop * entity.rollSpeed)); // Projectile Spinning
			this.renderSprite(entity, matrixStack, renderTypeBuffer, rendertype, scale);
			matrixStack.popPose();
		}
    }

    @Override
    public ResourceLocation getTextureLocation(BaseProjectileEntity entity) {
		return entity.getTexture();
	}

    protected ResourceLocation getLaserTexture(LaserProjectileEntity entity) {
    	return entity.getBeamTexture();
    }

	public void bindTexture(ResourceLocation texture) {
		// TODO Remove
	}
}
