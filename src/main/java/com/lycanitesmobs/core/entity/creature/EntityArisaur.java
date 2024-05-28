package com.lycanitesmobs.core.entity.creature;

import com.lycanitesmobs.api.IGroupHeavy;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityArisaur extends AgeableCreatureEntity implements IGroupHeavy {

    public EntityArisaur(EntityType<? extends EntityArisaur> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = false;

        this.canGrow = true;
        this.babySpawnChance = 0.1D;
        this.fleeHealthPercent = 1.0F;
        this.isAggressiveByDefault = false;
        //this.solidCollision = true;
        this.setupMob();
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
		this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
        this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
    }

	@Override
	public float getBlockPathWeight(int x, int y, int z) {
		if(this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z)).getBlock() != Blocks.AIR) {
			BlockState blocState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
			if(blocState.getMaterial() == Material.GRASS)
				return 10F;
			if(blocState.getMaterial() == Material.DIRT)
				return 7F;
		}
        return super.getBlockPathWeight(x, y, z);
    }

    @Override
    public int getNoBagSize() { return 0; }

    @Override
    public int getBagSize() { return this.creatureInfo.bagSize; }

    @Override
    public boolean canBeLeashed(PlayerEntity player) {
	    return true;
    }

	/** Returns this creature's main texture. Also checks for for subspecies. **/
	@Override
	public ResourceLocation getTexture() {
		if(!this.hasCustomName() || !"Flowersaur".equals(this.getCustomName().getString()))
			return super.getTexture();

		String textureName = this.getTextureName() + "_flowersaur";
		if(TextureManager.getTexture(textureName) == null)
			TextureManager.addTexture(textureName, this.creatureInfo.modInfo, "textures/entity/" + textureName.toLowerCase() + ".png");
		return TextureManager.getTexture(textureName);
	}
}
