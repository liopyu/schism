package com.lycanitesmobs.core.info.projectile.behaviours;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ProjectileBehaviourPlaceBlocks extends ProjectileBehaviour {
	/** The name of the block to place. **/
	public String blockName;

	/** The chance of placing a block at each location. **/
	public double chance = 1;

	/** The radius of blocks placed. **/
	public int radius = 1;

	/** The height of blocks placed. **/
	public int height = 1;

	/** Whether the block must be placed on top of a solid block (use for fires, etc) **/
	public boolean solidSurface = true;

	@Override
	public void loadFromJSON(JsonObject json) {
		this.blockName = json.get("block").getAsString();

		if (json.has("chance"))
			this.chance = json.get("chance").getAsDouble();

		if (json.has("radius"))
			this.radius = json.get("radius").getAsInt();

		if (json.has("height"))
			this.height = json.get("height").getAsInt();

		if (json.has("solidSurface"))
			this.solidSurface = json.get("solidSurface").getAsBoolean();
	}

	@Override
	public void onProjectileImpact(BaseProjectileEntity projectile, World world, BlockPos pos) {
		Block block = GameRegistry.findRegistry(Block.class).getValue(new ResourceLocation(this.blockName));
		if (block == null) {
			return;
		}
		BlockState blockState = block.defaultBlockState();
		if (block instanceof FlowingFluidBlock) {
			blockState = blockState.setValue(FlowingFluidBlock.LEVEL, 8);
		}

		pos = pos.above();
		for (int x = -this.radius + 1; x < this.radius; x++) {
			for (int y = this.height - 1; y < this.height; y++) {
				for (int z = -this.radius + 1; z < this.radius; z++) {
					BlockPos placePos = pos.offset(x, y, z);
					if (this.solidSurface && !world.getBlockState(placePos.below()).getMaterial().isSolidBlocking()) {
						continue;
					}
					if (projectile.canDestroyBlock(placePos) && (this.chance >= 1 || this.chance >= world.random.nextDouble())) {
						world.setBlockAndUpdate(placePos, blockState);
					}
				}
			}
		}
	}
}
