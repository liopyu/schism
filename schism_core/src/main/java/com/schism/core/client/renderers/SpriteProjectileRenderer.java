package com.schism.core.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import com.schism.core.database.IHasDefinition;
import com.schism.core.projectiles.ProjectileDefinition;
import com.schism.core.projectiles.ProjectileEntity;
import com.schism.core.util.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SpriteProjectileRenderer extends EntityRenderer<ProjectileEntity> implements IHasDefinition<ProjectileDefinition>
{
    protected final ProjectileDefinition definition;
    protected RenderType spriteRenderType;

    protected SpriteProjectileRenderer(ProjectileDefinition definition, EntityRendererProvider.Context context)
    {
        super(context);
        this.definition = definition;
    }

    @Override
    public ProjectileDefinition definition()
    {
        return this.definition;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(ProjectileEntity projectileEntity)
    {
        return projectileEntity.definition().spriteTexture();
    }

    @Override
    public void render(@NotNull ProjectileEntity projectileEntity, float deltaTicks, float yaw, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int brightness)
    {
        super.render(projectileEntity, deltaTicks, yaw, poseStack, multiBufferSource, brightness);

        float scale = projectileEntity.size() * projectileEntity.definition().renderScale();
        float loop = projectileEntity.renderTicks(Minecraft.getInstance().getDeltaFrameTime());

        // Laser:
        if (projectileEntity.laserEnd() != Vec3.ZERO) {
            this.renderLaser(projectileEntity, poseStack, multiBufferSource, this.spriteRenderType(), scale, loop);
        }

        // Render Projectile Sprite:
        poseStack.pushPose();
        poseStack.translate(0, 0.25, 0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(new Vec3(0.0F, 1.0F, 0.0F).rotationDegrees(180.0F));
        poseStack.mulPose(new Vec3(0, 0, 1).rotationDegrees(loop * projectileEntity.definition().renderSpin())); // Projectile Spinning
        this.renderSprite(projectileEntity, poseStack, multiBufferSource, this.spriteRenderType(), scale, loop);
        poseStack.popPose();
    }

    /**
     * Gets or creates the sprite render type. This uses a texture from the definition and is cached.
     * @return A cached sprite render type using the base sprite texture.
     */
    protected RenderType spriteRenderType()
    {
        if (this.spriteRenderType == null) {
            ResourceLocation texture = this.definition().spriteTexture();
            this.spriteRenderType = RenderTypes.spriteRenderType(texture, RenderTypes.Blending.NORM);
        }
        return this.spriteRenderType;
    }

    /**
     * Renders a projectile sprite.
     * @param projectileEntity The projectile entity to render for.
     * @param poseStack The pose stack.
     * @param multiBufferSource The multi buffer source.
     * @param renderType The render type to use.
     * @param scale The scale to render at.
     * @param loop The animation loop.
     */
    public void renderSprite(ProjectileEntity projectileEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, RenderType renderType, float scale, float loop)
    {
        float x = scale * 0.75F;
        float y = scale * 0.75F;
        float minU = 0;
        float maxU = 1;
        float minV = 0;
        float maxV = 1;

        int frameCount = projectileEntity.definition().renderAnimationFrames();
        if(frameCount > 1) {
            float frame = (float)(projectileEntity.tickCount / 2 % frameCount) + 1;
            minV = frame / frameCount;
            maxV = minV + (1F / frameCount);
        }

        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
        Matrix4f matrix4f = poseStack.last().pose();

        vertexConsumer
                .vertex(matrix4f, -x, -y, 0.0F)
                .color(255, 255, 255, 255)
                .uv(minU, maxV)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, x, -y, 0.0F)
                .color(255, 255, 255, 255)
                .uv(maxU, maxV)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, x, y, 0.0F)
                .color(255, 255, 255, 255)
                .uv(maxU, minV)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
        vertexConsumer
                .vertex(matrix4f, -x, y, 0.0F)
                .color(255, 255, 255, 255)
                .uv(minU, minV)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    /**
     * Renders a laser using a series of sprites.
     * @param projectileEntity The projectile entity to render a laser for.
     * @param poseStack The pose stack.
     * @param multiBufferSource The buffer source.
     * @param renderType The render type to use.
     * @param scale The scale.
     * @param loop The animation loop.
     */
    public void renderLaser(ProjectileEntity projectileEntity, PoseStack poseStack, MultiBufferSource multiBufferSource, RenderType renderType, float scale, float loop)
    {
        double laserSize = projectileEntity.position().distanceTo(projectileEntity.laserEnd().physVec3());
        float spacing = 1;
        double factor = spacing / laserSize;
        if(laserSize <= 0) {
            return;
        }
        Vec3 direction = projectileEntity.laserEnd().subtract(new Vec3(projectileEntity.position())).normalize();
        for(float segment = 0; segment <= laserSize; segment += factor) {
            poseStack.pushPose();
            poseStack.translate(segment * direction.x() * spacing, segment * direction.y() * spacing, segment * direction.z() * spacing);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(new Vec3(0.0F, 1.0F, 0.0F).rotationDegrees(180.0F));
            poseStack.mulPose(new Vec3(0, 0, 1).rotationDegrees(loop * projectileEntity.definition().renderSpin())); // Projectile Spinning
            this.renderSprite(projectileEntity, poseStack, multiBufferSource, renderType, scale, loop);
            poseStack.popPose();
        }
    }
}
