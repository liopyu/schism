package com.schism.core.client.renderers;

import com.schism.core.blocks.BlockRepository;
import com.schism.core.particles.ParticleRepository;
import com.schism.core.projectiles.ProjectileEntity;
import com.schism.core.projectiles.ProjectileRepository;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class RendererRegister
{
    private static RendererRegister INSTANCE;

    /**
     * Gets the singleton RenderRegister instance.
     * @return The Render Register which sets up renderers.
     */
    public static RendererRegister get()
    {
        if (INSTANCE == null) {
            INSTANCE = new RendererRegister();
        }
        return INSTANCE;
    }

    /**
     * Sets up all rendering handlers.
     */
    public void setup()
    {
        // Block Renderers:
        BlockRepository.get().definitions().forEach(definition -> {
            switch (definition.renderType()) {
                case "cutout_mipped" -> ItemBlockRenderTypes.setRenderLayer(definition.block(), RenderType.cutoutMipped());
                case "cutout" -> ItemBlockRenderTypes.setRenderLayer(definition.block(), RenderType.cutout());
                case "translucent" -> ItemBlockRenderTypes.setRenderLayer(definition.block(), RenderType.translucent());
            }
        });

        // Projectile Renderers:
        ProjectileRepository.get().definitions().forEach(definition -> {
            if (definition.renderType().equals("obj")) {
                EntityRenderers.register(definition.entityType(), context -> new ModelProjectileRenderer(definition, "obj", context));
                return;
            }
            EntityRenderers.register(definition.entityType(), context -> new SpriteProjectileRenderer(definition, context));
        });
    }

    /**
     * Called when particle factories should register their types to the particle engine.
     * @param event The particle factory register event.
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onParticleFactoryRegister(ParticleFactoryRegisterEvent event)
    {
        ParticleRepository.get().definitions().forEach(particleDefinition -> Minecraft.getInstance().particleEngine.register(particleDefinition.particleType(), DefinedParticleProvider::new));
    }
}
