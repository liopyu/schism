package com.lycanitesmobs.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;

public abstract class BaseGui extends AbstractGui {
    public BaseGui(ITextComponent screenName) {
        super();
    }

    public static void renderLivingEntity(MatrixStack matrixStack, int x, int y, float scale, float lookX, float lookY, LivingEntity entity) {
        float lookXRot = (float)Math.atan((double)(lookX / 40.0F));
        float lookYRot = (float)Math.atan((double)(lookY / 40.0F));
        matrixStack.pushPose();
        matrixStack.translate((float)x, (float)y, 1500.0F);
        matrixStack.scale(1.0F, 1.0F, -1.0F);
        matrixStack.translate(0.0D, 0.0D, 1000.0D);
        matrixStack.scale(scale, scale, scale);
        Quaternion modelRotationRoll = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion modelRotationPitch = Vector3f.XP.rotationDegrees(lookYRot * 20.0F);
        modelRotationRoll.mul(modelRotationPitch);
        matrixStack.mulPose(modelRotationRoll);
        matrixStack.mulPose(Vector3f.YN.rotationDegrees(180.0F));

        float renderYawOffset = entity.yBodyRot;
        float rotationYaw = entity.yRot;
        float rotationPitch = entity.xRot;
        float prevRotationYawHead = entity.yHeadRotO;
        float rotationYawHead = entity.yHeadRot;
        entity.yBodyRot = lookXRot * 20.0F;
        entity.yRot = lookXRot * 40.0F;
        entity.xRot = -lookYRot * 20.0F;
        entity.yHeadRot = entity.yRot;
        entity.yHeadRotO = entity.yRot;
        entity.setOnGround(true);

        EntityRendererManager renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
        modelRotationPitch.conj();
        renderManager.overrideCameraOrientation(modelRotationPitch);
        renderManager.setRenderShadow(false);
        IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().renderBuffers().bufferSource();
        renderManager.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, renderTypeBuffer, 15728880);
        renderTypeBuffer.endBatch();
        renderManager.setRenderShadow(true);
        entity.yBodyRot = renderYawOffset;
        entity.yRot = rotationYaw;
        entity.xRot = rotationPitch;
        entity.yHeadRotO = prevRotationYawHead;
        entity.yHeadRot = rotationYawHead;
        matrixStack.popPose();
    }
}
