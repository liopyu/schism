package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ParticleEntity extends ThrowableEntity {
	// Particle:
	public int particleAge = 0;
	public int particleAgeMax = 20;
	public double particleGravity = 0D;
	public String texture;
	public ModInfo group;
	
    // ==================================================
    //                      Constructor
    // ==================================================
	public ParticleEntity(World world, double x, double y, double z, String texture, ModInfo group) {
		super(EntityType.SNOWBALL, world);
		this.setPos(x, y, z);
        this.xOld = x;
        this.yOld = y;
        this.zOld = z;
		this.texture = texture;
		this.group = group;
	}

	@Override
	public void defineSynchedData() {}
	
	
    // ==================================================
    //                       Update
    // ==================================================
	@Override
    public void tick() {
		System.out.println("Doing something!");
        this.xo = this.position().x();
        this.yo = this.position().y();
        this.zo = this.position().z();

        if(this.particleAge++ >= this.particleAgeMax)
            this.remove();

        /*this.motionY -= 0.04D * (double)this.particleGravity;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if(this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }*/
    }

	
    // ==================================================
    //                    Interaction
    // ==================================================
	public boolean canAttackWithItem() {
        return false;
    }
	
	protected boolean isMovementNoisy() {
        return false;
    }
	
	@Override
	protected void onHit(RayTraceResult rayTraceResult) {
		return;
	}
	
	
    // ==================================================
    //                        NBT
    // ==================================================
	@Override
	public void addAdditionalSaveData(CompoundNBT par1NBTTagCompound) {}

	@Override
    public void readAdditionalSaveData(CompoundNBT par1NBTTagCompound) {}
	
	
    // ==================================================
    //                      Visuals
    // ==================================================
    public ResourceLocation getTexture() {
    	if(TextureManager.getTexture(this.texture) == null)
    		TextureManager.addTexture(this.texture, this.group, "textures/particles/" + this.texture.toLowerCase() + ".png");
    	return TextureManager.getTexture(this.texture);
    }
}
