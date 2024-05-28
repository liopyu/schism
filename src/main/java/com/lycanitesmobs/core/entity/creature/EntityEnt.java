package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.info.ObjectLists;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityEnt extends TameableCreatureEntity implements IMob {
    
    // ==================================================
 	//                    Constructor
 	// ==================================================
    public EntityEnt(EntityType<? extends EntityEnt> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.spawnsUnderground = false;
        this.hasAttackSound = true;
        this.spreadFire = true;

        this.canGrow = true;
        this.babySpawnChance = 0.1D;
        this.setupMob();
    }

    // ========== Init AI ==========
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setTargetClass(PlayerEntity.class).setLongMemory(false));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this));
    }
	
	
    // ==================================================
    //                      Updates
    // ==================================================
	// ========== Living Update ==========
	@Override
    public void aiStep() {
        super.aiStep();

        // Water Healing:
        if(this.getAirSupply() >= 0) {
            if (this.isInWater())
                this.addEffect(new EffectInstance(Effects.REGENERATION, 3 * 20, 1));
            else if (this.isInWaterRainOrBubble())
                this.addEffect(new EffectInstance(Effects.REGENERATION, 3 * 20, 0));
        }
    }
    
    // ==================================================
    //                      Attacks
    // ==================================================
    @Override
    public boolean canAttack(LivingEntity target) {
        if(target instanceof EntityTreant)
            return false;
        return super.canAttack(target);
    }
    
    // ========== Melee Attack ==========
    @Override
    public boolean attackMelee(Entity target, double damageScale) {
    	if(!super.attackMelee(target, damageScale))
    		return false;
    	
    	// Leech:
    	float leeching = Math.max(1, this.getAttackDamage(damageScale));
    	this.heal(leeching);
        
        return true;
    }
    
    
    // ==================================================
   	//                    Taking Damage
   	// ==================================================
    // ========== Damage Modifier ==========
    public float getDamageModifier(DamageSource damageSrc) {
        if(damageSrc.isFire())
            return 2.0F;
        if(damageSrc.getEntity() != null) {
            ItemStack heldItem = ItemStack.EMPTY;
            if(damageSrc.getEntity() instanceof LivingEntity) {
                LivingEntity entityLiving = (LivingEntity)damageSrc.getEntity();
                if(!entityLiving.getItemInHand(Hand.MAIN_HAND).isEmpty()) {
                    heldItem = entityLiving.getItemInHand(Hand.MAIN_HAND);
                }
            }
            if(ObjectLists.isAxe(heldItem)) {
                return 2.0F;
            }
        }
        return super.getDamageModifier(damageSrc);
    }

    // ==================================================
    //                     Equipment
    // ==================================================
    @Override
    public int getNoBagSize() { return 0; }
    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }
    // ==================================================
   	//                     Immunities
   	// ==================================================
    @Override
    public float getFallResistance() {
    	return 100;
    }
	
	
	// ==================================================
    //                     Pet Control
    // ==================================================
    public boolean petControlsEnabled() { return true; }


    // ==================================================
    //                       Visuals
    // ==================================================
    /** Returns this creature's main texture. Also checks for for subspecies. **/
    public ResourceLocation getTexture() {
        if(this.hasCustomName() && "Twisted Ent".equals(this.getCustomName().getString())) {
            String textureName = this.getTextureName() + "_twisted";
            if (TextureManager.getTexture(textureName) == null)
                TextureManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
            return TextureManager.getTexture(textureName);
        }

        if(this.hasCustomName() && "Salty Tree".equals(this.getCustomName().getString())) {
            String textureName = this.getTextureName() + "_saltytree";
            if (TextureManager.getTexture(textureName) == null)
                TextureManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
            return TextureManager.getTexture(textureName);
        }

        return super.getTexture();
    }
}
