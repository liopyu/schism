package com.lycanitesmobs.client.renderer;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.CustomProjectileEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class RenderRegister {

    public void registerModelLoaders() {
        //ModelLoaderRegistry.registerLoader(new ResourceLocation(LycanitesMobs.MODID), new ModelLoader()); Not needed.
    }

    public void registerRenderFactories() {

        // Creatures:
        for(CreatureInfo creatureInfo : CreatureManager.getInstance().creatures.values()) {
            if(creatureInfo.dummy) {
                RenderingRegistry.registerEntityRenderingHandler(creatureInfo.getEntityType(), new RenderFactoryNone<BaseProjectileEntity>(creatureInfo.entityClass));
            }
            RenderingRegistry.registerEntityRenderingHandler(creatureInfo.getEntityType(), new CreatureRenderFactory<BaseCreatureEntity>(creatureInfo));
        }

        // Projectiles:
        for(ProjectileInfo projectileInfo : ProjectileManager.getInstance().projectiles.values()){
            if(projectileInfo.modelClassName != null) {
                RenderingRegistry.registerEntityRenderingHandler(projectileInfo.getEntityType(), new ProjectileRenderFactory<CustomProjectileEntity>(projectileInfo));
            }
            else {
                RenderingRegistry.registerEntityRenderingHandler(projectileInfo.getEntityType(), new ProjectileRenderFactory<CustomProjectileEntity>(projectileInfo));
            }
        }

        // Old Sprite Projectiles:
        for(String projectileName : ProjectileManager.getInstance().oldSpriteProjectiles.keySet()) {
            Class projectileClass = ProjectileManager.getInstance().oldSpriteProjectiles.get(projectileName);
            RenderingRegistry.registerEntityRenderingHandler(ProjectileManager.getInstance().oldProjectileTypes.get(projectileClass), new ProjectileRenderFactory<BaseProjectileEntity>(projectileName, projectileClass, false));
        }

        // Old Model Projectiles:
        for(String projectileName : ProjectileManager.getInstance().oldModelProjectiles.keySet()) {
            Class projectileClass = ProjectileManager.getInstance().oldModelProjectiles.get(projectileName);
            RenderingRegistry.registerEntityRenderingHandler(ProjectileManager.getInstance().oldProjectileTypes.get(projectileClass), new ProjectileRenderFactory<BaseProjectileEntity>(projectileName, projectileClass, true));
        }

        // Special Entities:
        for(Class specialClass : ObjectManager.specialEntities.values()) {
            RenderingRegistry.registerEntityRenderingHandler(ObjectManager.specialEntityTypes.get(specialClass), new RenderFactoryNone<BaseProjectileEntity>(specialClass));
        }
    }

    public void registerBlockRenderLayers() {
        for(Block block : ItemManager.getInstance().cutoutBlocks) {
            RenderTypeLookup.setRenderLayer(block, RenderType.cutout());
        }
    }
}
