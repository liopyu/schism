package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.AttackRangedGoal;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireOrb;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class EntityBehemophet extends TameableCreatureEntity implements IMob {

    // Data Manager:
    protected static final DataParameter<Integer> HELLFIRE_ENERGY = EntityDataManager.defineId(EntityBehemophet.class, DataSerializers.INT);

    public int hellfireEnergy = 0;
    public List<EntityHellfireOrb> hellfireOrbs = new ArrayList<>();

    public EntityBehemophet(EntityType<? extends EntityBehemophet> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEAD;
        this.hasAttackSound = false;
        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false).setRange(1D).setMaxChaseDistance(8.0F));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackRangedGoal(this).setSpeed(1.0D).setRange(16.0F).setMinChaseDistance(0F).setChaseTime(-1));
    }

    /** Initiates the entity setting all the values to be watched by the datawatcher. **/
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HELLFIRE_ENERGY, this.hellfireEnergy);
    }

	@Override
    public void aiStep() {
        super.aiStep();

        // Sync Hellfire Energy:
        if (!this.getCommandSenderWorld().isClientSide) {
            this.entityData.set(HELLFIRE_ENERGY, this.hellfireEnergy);
        }
        else {
            try {
                this.hellfireEnergy = this.entityData.get(HELLFIRE_ENERGY);
            }
            catch(Exception e) {}
        }

        // Hellfire Update:
        if(this.getCommandSenderWorld().isClientSide && this.hellfireEnergy > 0)
            EntityRahovart.updateHellfireOrbs(this, this.updateTick, 3, this.hellfireEnergy, 1F, this.hellfireOrbs);

        // Trail:
        if(!this.getCommandSenderWorld().isClientSide && this.isMoving() && this.tickCount % 5 == 0) {
            int trailHeight = 1;
            int trailWidth = 1;
            if(this.isRareVariant())
                trailWidth = 3;
            for(int y = 0; y < trailHeight; y++) {
                Block block = this.getCommandSenderWorld().getBlockState(this.blockPosition().offset(0, y, 0)).getBlock();
                if(block != null && (block == Blocks.AIR || block == Blocks.FIRE || block == Blocks.SNOW || block == Blocks.TALL_GRASS || block == ObjectManager.getBlock("frostfire") || block == ObjectManager.getBlock("icefire") || block == ObjectManager.getBlock("scorchfire") || block == ObjectManager.getBlock("doomfire"))) {
                    if(trailWidth == 1)
                        this.getCommandSenderWorld().setBlockAndUpdate(this.blockPosition().offset(0, y, 0), ObjectManager.getBlock("hellfire").defaultBlockState());
                    else
                        for(int x = -(trailWidth / 2); x < (trailWidth / 2) + 1; x++) {
                            for(int z = -(trailWidth / 2); z < (trailWidth / 2) + 1; z++) {
                                this.getCommandSenderWorld().setBlockAndUpdate(this.blockPosition().offset(x, y, z), ObjectManager.getBlock("hellfire").defaultBlockState());
                            }
                        }
                }
            }
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if(target instanceof EntityBelphegor)
            return false;
        return super.canAttack(target);
    }

    @Override
    public boolean canAttackWithPickup() {
        return true;
    }

    @Override
    public boolean attackMelee(Entity target, double damageScale) {
        if (!super.attackMelee(target, damageScale))
            return false;

        // Pickup and Throw:
        if (target instanceof LivingEntity) {
            LivingEntity entityLivingBase = (LivingEntity)target;
            if (this.canPickupEntity(entityLivingBase)) {
                this.pickupEntity(entityLivingBase);
            }
            else if (this.getPickupEntity() == target && this.getRandom().nextBoolean()) {
                this.dropPickupEntity();
                target.setDeltaMovement(this.getFacingPositionDouble(0, 1D, 0, 2D, this.yBodyRot));
            }
        }

        return true;
    }

    @Override
    public void attackRanged(Entity target, float range) {
        this.fireProjectile("hellfireball", target, range, 0, new Vector3d(0, 0, 0), 1.2f, 2f, 1F);
        super.attackRanged(target, range);
    }

    @Override
    public double[] getPickupOffset(Entity entity) {
        Vector3d offset = this.getFacingPositionDouble(0, 2, 0, 1.5D, this.yBodyRot);
        return new double[]{offset.x, offset.y, offset.z};
    }

    @Override
    public boolean canBurn() { return false; }

    public boolean petControlsEnabled() { return true; }

    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }

    /** Returns this creature's main texture. Also checks for for subspecies. **/
    @Override
    public ResourceLocation getTexture() {
        if(!this.hasCustomName() || !"Krampus".equals(this.getCustomName().getString()))
            return super.getTexture();

        String textureName = this.getTextureName() + "_krampus";
        if(TextureManager.getTexture(textureName) == null)
            TextureManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
        return TextureManager.getTexture(textureName);
    }
}
