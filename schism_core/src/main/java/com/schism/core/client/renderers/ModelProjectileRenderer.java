package com.schism.core.client.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Log;
import com.schism.core.client.models.AbstractModel;
import com.schism.core.client.models.ObjModel;
import com.schism.core.projectiles.ProjectileDefinition;
import com.schism.core.projectiles.ProjectileEntity;
import com.schism.core.util.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModelProjectileRenderer extends SpriteProjectileRenderer
{
    protected final String modelFormat;
    protected AbstractModel model;

    protected ModelProjectileRenderer(ProjectileDefinition definition, String modelFormat, EntityRendererProvider.Context context)
    {
        super(definition, context);
        this.modelFormat = modelFormat;
        AbstractModel.addReloadListener(() -> this.model = null);
    }

    @Override
    public void render(@NotNull ProjectileEntity projectileEntity, float deltaTicks, float yaw, @NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int brightness)
    {
        float scale = projectileEntity.size() * projectileEntity.definition().renderScale();
        float loop = projectileEntity.renderTicks(Minecraft.getInstance().getDeltaFrameTime());

        ResourceLocation texture = this.getTextureLocation(projectileEntity);

        // Laser:
        if (projectileEntity.laserEnd() != Vec3.ZERO) {
            this.renderLaser(projectileEntity, poseStack, multiBufferSource, RenderTypes.spriteRenderType(texture, RenderTypes.Blending.NORM), scale, loop);
        }

        // Render Projectile Model:
        poseStack.pushPose();
        poseStack.translate(0, 0.25, 0);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(new Vec3(0.0F, 1.0F, 0.0F).rotationDegrees(180.0F));
        poseStack.mulPose(new Vec3(0, 0, 1).rotationDegrees(loop * projectileEntity.definition().renderSpin())); // Projectile Spinning
        if (this.model == null) {
            this.model = new ObjModel("projectile/" + this.definition().subject());
        }
        try {
            this.model.render(poseStack, multiBufferSource);
        } catch (Exception e) {
            e.printStackTrace();
            Log.error("model", "Error rendering projectile model for: " + this.definition().subject());
        }
        poseStack.popPose();
    }
}
