package com.lycanitesmobs.core.entity.projectile;

import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public class EntityHellfireWavePart extends EntityHellfireWall {

    // ==================================================
 	//                   Constructors
 	// ==================================================
    public EntityHellfireWavePart(EntityType<? extends BaseProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public EntityHellfireWavePart(EntityType<? extends BaseProjectileEntity> entityType, World world, LivingEntity par2LivingEntity) {
        super(entityType, world, par2LivingEntity);
    }

    public EntityHellfireWavePart(EntityType<? extends BaseProjectileEntity> entityType, World world, double par2, double par4, double par6) {
        super(entityType, world, par2, par4, par6);
    }

    // ========== Setup Projectile ==========
    @Override
    public void setup() {
        this.entityName = "hellfirewavepart";
        super.setup();
        this.animationFrameMax = 59;
    }


    // ==================================================
    //                      Visuals
    // ==================================================
    public String getTextureName() {
        return "hellfirewave";
    }
}
