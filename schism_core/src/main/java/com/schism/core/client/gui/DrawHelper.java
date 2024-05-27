package com.schism.core.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class DrawHelper
{
    protected final Minecraft minecraft;
    protected final Font font;
    protected Screen screen;

    public DrawHelper()
    {
        this.minecraft = Minecraft.getInstance();
        this.font = Minecraft.getInstance().font;
    }

    /**
     * The current minecraft instance.
     * @return The minecraft client instance.
     */
    public Minecraft minecraft()
    {
        return this.minecraft;
    }

    /**
     * The font to draw with.
     * @return The font to draw with.
     */
    public Font font()
    {
        return this.font;
    }

    /**
     * Gets an optional screen that this draw helper can draw with.
     * @return An optional screen.
     */
    public Optional<Screen> screen()
    {
        if (this.screen == null) {
            return Optional.empty();
        }
        return Optional.of(this.screen);
    }

    /**
     * Sets an optional screen to draw with. Used for drawing tooltips, etc.
     * @param screen The screen to use.
     * @return The current instance.
     */
    public DrawHelper setScreen(Screen screen)
    {
        this.screen = screen;
        return this;
    }

    public void preDraw() {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
    }

    public void postDraw() {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    /**
     * Draws a texture.
     * @param poseStack The pose stack to draw with.
     * @param texture The texture resource location.
     * @param position The xyz position to draw at.
     * @param size The width and height of the texture.
     * @param uv The texture ending uv coordinates.
     */
    public void drawTexture(PoseStack poseStack, ResourceLocation texture, Vec3 position, Vec2 size, Vec2 uv)
    {
        this.drawTexture(poseStack, texture, position, size, uv, Color.WHITE);
    }

    /**
     * Draws a texture with coloring applied.
     * @param poseStack The pose stack to draw with.
     * @param texture The texture resource location.
     * @param position The xyz position to draw at.
     * @param size The width and height of the texture.
     * @param uv The texture ending uv coordinates.
     * @param color The color of the texture.
     */
    public void drawTexture(PoseStack poseStack, ResourceLocation texture, Vec3 position, Vec2 size, Vec2 uv, Color color)
    {
        this.preDraw();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor((float)color.getRed() / 255,(float)color.getGreen() / 255,(float)color.getBlue() / 255, (float)color.getAlpha() / 255);
        Matrix4f matrix = poseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, position.x(), position.y() + size.y(), position.z())
                .uv(0, uv.y())
                .endVertex();
        buffer.vertex(matrix, position.x() + size.x(), position.y() + size.y(), position.z())
                .uv(uv.x(), uv.y())
                .endVertex();
        buffer.vertex(matrix, position.x() + size.x(), position.y(), position.z())
                .uv(uv.x(), 0)
                .endVertex();
        buffer.vertex(matrix, position.x(), position.y(), position.z())
                .uv(0, 0)
                .endVertex();
        tesselator.end();
        this.postDraw();
    }

    /**
     * Draws a bar.
     * @param poseStack The pose stack to draw with.
     * @param texture The texture resource location.
     * @param position The xyz position to draw at.
     * @param size The width and height of the texture.
     * @param segments How many segments to draw.
     * @param limit How many segments to draw up to. If negative the bar is drawn in reverse.
     * @param vertical If true, the bar will be vertical, otherwise horizontal.
     */
    public void drawBar(PoseStack poseStack, ResourceLocation texture, Vec3 position, Vec2 size, int segments, int limit, boolean vertical)
    {
        boolean reverse = limit < 0;
        if(reverse) {
            limit = -limit;
        }
        for (int i = 0; i < Math.min(segments, limit); i++) {
            int segment = reverse ? limit - i - 1 : i;
            this.drawTexture(poseStack, texture, position.add(vertical ? 0 : size.x() * segment, vertical ? size.y() * segment : 0, 0), size, Vec2.ONE);
        }
    }

    /**
     * Draws text .
     * @param poseStack The pose stack to draw with.
     * @param component The text component to output.
     * @param position The position.
     * @param color The color of the text. Use 0x###### hex format.
     * @param shadow If true, a drop shadow will be drawn under the text.
     */
    public void drawText(PoseStack poseStack, Component component, Vec2 position, int color, boolean shadow)
    {
        if (shadow) {
            this.font().drawShadow(poseStack, component, position.x(), position.y(), color);
            return;
        }
        this.font().draw(poseStack, component, position.x(), position.y(), color);
    }

    /**
     * Draws text .
     * @param poseStack The pose stack to draw with.
     * @param component The text component to output.
     * @param position The position.
     * @param color The color of the text.
     */
    public void drawText(PoseStack poseStack, Component component, Vec2 position, int color)
    {
        this.drawText(poseStack, component, position, color, false);
    }

    /**
     * Draws text centered at the x position.
     * @param poseStack The pose stack to draw with.
     * @param component The text component to output.
     * @param position The position.
     * @param color The color of the text.
     * @param shadow If true, a drop shadow will be drawn under the text.
     */
    public void drawTextCentered(PoseStack poseStack, Component component, Vec2 position, int color, boolean shadow)
    {
        float textWidth = this.textWidth(component);
        float textOffset = (int)Math.floor(textWidth / 2);
        this.drawText(poseStack, component, position.add(new Vec2(-textOffset, 0)), color, shadow);
    }

    /**
     * Draws text that is split onto new lines when it exceeds wrap width.
     * @param poseStack The pose stack to draw with.
     * @param component The text component to output.
     * @param position The position.
     * @param wrapWidth The width to wrap text at.
     * @param color The color of the text.
     * @param shadow If true, a drop shadow wil be drawn under the text.
     * @return The height (in pixels) the provided text has with word wrapping.
     */
    public int drawTextWrapped(PoseStack poseStack, Component component, Vec2 position, int wrapWidth, int color, boolean shadow)
    {
        List<FormattedCharSequence> textLines = this.font().split(component, wrapWidth);
        float lineY = position.y();
        for(FormattedCharSequence textLine : textLines) {
            if (shadow) {
                this.font().drawShadow(poseStack, textLine, position.x(), lineY, color);
                lineY += 10;
                continue;
            }
            this.font().draw(poseStack, textLine, position.x(), lineY, color);
            lineY += 10;
        }
        return textLines.size() * 10;
    }

    /**
     * Determines the width of the provided text.
     * @param component The text component to determine the render width of.
     * @return The width (in pixels) the provided text would have.
     */
    public int textWidth(Component component)
    {
        return this.font().width(component);
    }

    /**
     * Determines the height of the provided text component once word wrapped.
     * @param component The text component to determine the render height of.
     * @param wrapWidth The maximum width the text is allowed to have before wrapping at.
     * @return The height (in pixels) the provided text would have with word wrapping.
     */
    public int wrappedHeight(Component component, int wrapWidth)
    {
        return this.font().split(component, wrapWidth).size() * 11;
    }

    /**
     * Cuts all content rendered after that is outside the provided area.
     * Call scissorStop when finished or everything outside the gui may end up cut!
     * @param position The position of the scissor area. Z position is ignored.
     * @param size The size of the provided area.
     */
    public void scissorStart(Vec3 position, Vec2 size)
    {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        double screenScale = this.minecraft().getWindow().getGuiScale();
        double invertedY = (this.minecraft().getWindow().getGuiScaledHeight() - position.y() - size.y()) * screenScale;
        GL11.glScissor((int)(position.x() * screenScale), (int)invertedY, (int)(size.x() * screenScale), (int)(size.y() * screenScale));
    }

    /**
     * Disables any active scissor.
     */
    public void scissorStop()
    {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    /**
     * Gets a fraction of the screen width.
     * @param fraction The fraction to multiply the screen width by.
     * @return The fraction of the screen width.
     */
    public float screenX(float fraction)
    {
        if (this.screen().isPresent()) {
            return fraction * this.screen().get().width;
        }
        return fraction * this.minecraft().getWindow().getGuiScaledWidth();
    }

    /**
     * Gets a fraction of the screen height.
     * @param fraction The fraction to multiply the screen height by.
     * @return The fraction of the screen height.
     */
    public float screenY(float fraction)
    {
        if (this.screen().isPresent()) {
            return fraction * this.screen().get().height;
        }
        return fraction * this.minecraft().getWindow().getGuiScaledHeight();
    }

    /**
     * Draws the provided item stack in the form of an item pickup or block.
     * @param poseStack The pose stack to render on.
     * @param itemStack The item stack to render.
     * @param position The position to render at.
     * @param scale The scale to render at relative to a standard item icon size.
     */
    public void drawItemStack(PoseStack poseStack, ItemStack itemStack, Vec3 position, float scale)
    {
        if (itemStack.isEmpty()) {
            return;
        }
        BakedModel bakedModel = this.minecraft().getItemRenderer().getModel(itemStack, null, null, 0);
        this.minecraft().textureManager.getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);

        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();

        poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.translate(position.x(), position.y(), position.z());
        poseStack.translate(8.0D, 8.0D, 0.0D);
        poseStack.scale(scale, -scale, scale);
        poseStack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();

        PoseStack renderPoseStack = new PoseStack();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean blockLighting = !bakedModel.usesBlockLight();
        if (blockLighting) {
            Lighting.setupForFlatItems();
        }

        this.minecraft().getItemRenderer().render(itemStack, ItemTransforms.TransformType.GUI, false, renderPoseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
        bufferSource.endBatch();

        RenderSystem.enableDepthTest();
        if (blockLighting) {
            Lighting.setupFor3DItems();
        }
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }
}
