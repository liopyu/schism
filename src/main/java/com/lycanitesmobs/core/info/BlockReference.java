package com.lycanitesmobs.core.info;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;

public class BlockReference {
	protected final World world;
	protected final BlockPos pos;

	public BlockReference(World world, BlockPos pos) {
		this.world = world;
		this.pos = pos;
	}

	public World getWorld() {
		return this.world;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public BlockState getState() {
		return this.getWorld().getBlockState(this.getPos());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BlockReference)) return false;
		BlockReference that = (BlockReference) o;
		return world.equals(that.world) && pos.equals(that.pos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(world, pos);
	}
}
