package com.lycanitesmobs.core.worldgen.mobevents;

import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.block.BlockFireBase;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.projectile.EntityHellfireWall;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.mobevent.MobEventPlayerServer;
import com.lycanitesmobs.core.mobevent.effects.StructureBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

public class AsmodeusStructureBuilder extends StructureBuilder {

	public AsmodeusStructureBuilder() {
		this.name = "asmodeus";
	}

	@Override
	public void build(World world, PlayerEntity player, BlockPos pos, int level, int ticks, int variant) {
		ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
		int originX = pos.getX();
		int originY = pos.getY();
		int originZ = pos.getZ();

		originX += 20;
		int height = 40;
		if(originY < 5)
			originY = 5;
		if(world.getMaxBuildHeight() <= height)
			originY = 5;
		else if(originY + height >= world.getMaxBuildHeight())
			originY = Math.max(5, world.getMaxBuildHeight() - height - 1);

		// Effects:
		if(ticks == 1) {
			for(int i = 0; i < 5; i++) {
				BaseProjectileEntity baseProjectileEntity = new EntityHellfireWall(ProjectileManager.getInstance().oldProjectileTypes.get(EntityHellfireWall.class), world, originX, originY + (10 * i), originZ);
				baseProjectileEntity.projectileLife = 20 * 20;
				world.addFreshEntity(baseProjectileEntity);
				if(worldExt != null) {
					worldExt.bossUpdate(baseProjectileEntity);
					worldExt.overrideBossRange(baseProjectileEntity, CreatureManager.getInstance().getCreature("asmodeus").bossNearbyRange);
				}
			}
		}

		// Build Floor:
		if(ticks == 3 * 20) {
			this.buildArenaFloor(world, originX, originY, originZ);
		}

		// Build Walls:
		if(ticks == 5 * 20) {
			this.buildArenaWalls(world, originX, originY, originZ);
		}

		// Build Obstacles:
		if(ticks == 7 * 20) {
			this.buildObstacles(world, originX, originY, originZ);
		}

		// Explosions:
		if(ticks >= 10 * 20 && ticks % 10 == 0) {
			world.explode(null, originX - 20 + world.random.nextInt(40), originY + 25 + world.random.nextInt(10), originZ - 20 + world.random.nextInt(40), 2, Explosion.Mode.NONE);
		}

		// Spawn Boss:
		if(ticks == 20 * 20) {
			BaseCreatureEntity baseCreatureEntity = (BaseCreatureEntity) CreatureManager.getInstance().getCreature("asmodeus").createEntity(world);
			baseCreatureEntity.moveTo(originX, originY + 1, originZ, 0, 0);
			world.addFreshEntity(baseCreatureEntity);
			baseCreatureEntity.setArenaCenter(new BlockPos(originX, originY + 1, originZ));
			if(worldExt != null) {
				MobEventPlayerServer mobEventPlayerServer = worldExt.getMobEventPlayerServer(this.name);
				if(mobEventPlayerServer != null) {
					mobEventPlayerServer.mobEvent.onSpawn(baseCreatureEntity, world, player, pos, level, ticks, variant);
				}
			}
		}
	}


	// ==================================================
	//                     Arena Floor
	// ==================================================
	public void buildArenaFloor(World world, int originX, int originY, int originZ) {
		int radius = 80;
		int height = 30;
		int minX = originX - radius;
		int maxX = originX + radius;
		int minY = originY;
		int maxY = originY + height;
		int minZ = originZ - radius;
		int maxZ = originZ + radius;
		BlockState floor = ObjectManager.getBlock("aberrantstonetile").defaultBlockState();
		BlockState light = ObjectManager.getBlock("aberrantcrystal").defaultBlockState();
		BlockState trimming = ObjectManager.getBlock("aberrantstonechiseled").defaultBlockState();

		for(int x = minX; x <= maxX; x++) {
			for(int z = minZ; z <= maxZ; z++) {
				int topY = world.getHeightmapPos(Heightmap.Type.WORLD_SURFACE, new BlockPos(x, 0, z)).getY();
				for(int y = minY; y <= maxY; y++) {
					BlockPos buildPos = new BlockPos(x, y, z);
					if(y == minY) {
						if(x == minX || x == maxX || z == minZ || z == maxZ)
							world.setBlock(buildPos, trimming, 2);
						else if(x % 6 == 0 && z % 6 == 0)
							world.setBlock(buildPos, light, 2);
						else
							world.setBlock(buildPos, floor, 2);
					}
					else {
						if (y > minY + 5 && y >= topY)
							break;
						world.setBlock(buildPos, Blocks.AIR.defaultBlockState(), 2);
					}
				}
			}
		}
	}


	// ==================================================
	//                     Arena Walls
	// ==================================================
	public void buildArenaWalls(World world, int originX, int originY, int originZ) {
		int radius = 80;
		int thickness = 4;
		int height = 20;
		int minX = originX - (radius + thickness);
		int maxX = originX + (radius + thickness);
		int minY = originY;
		int maxY = originY + height;
		int minZ = originZ - (radius + thickness);
		int maxZ = originZ + (radius + thickness);
		BlockState base = ObjectManager.getBlock("aberrantstonebrick").defaultBlockState();
		BlockState light = ObjectManager.getBlock("aberrantcrystal").defaultBlockState();
		BlockState trimming = ObjectManager.getBlock("aberrantstonechiseled").defaultBlockState();
		BlockState top = ObjectManager.getBlock("aberrantstonepolished").defaultBlockState();
		BlockState fireBase = Blocks.OBSIDIAN.defaultBlockState();
		BlockState fire = ObjectManager.getBlock("hellfire").defaultBlockState().setValue(BlockFireBase.STATIC, true);

		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				if(x > minX + thickness && x < maxX - thickness && z  > minZ + thickness && z < maxZ - thickness)
					continue;
				for (int y = minY; y <= maxY; y++) {
					BlockPos buildPos = new BlockPos(x, y, z);
					if(y < maxY - 2) {
						if (y - 1 % 8 == 0)
							world.setBlock(buildPos, light, 2);
						else if (y % 8 == 0)
							world.setBlock(buildPos, trimming, 2);
						else
							world.setBlock(buildPos, base, 2);
					}
					else if(y == maxY - 2)
						world.setBlock(buildPos, top, 2);
					else if(y == maxY - 1)
						world.setBlock(buildPos, fireBase, 2);
					else
						world.setBlock(buildPos, fire, 2);
				}
			}
		}
	}


	// ==================================================
	//                   Arena Obstacles
	// ==================================================
	public void buildObstacles(World world, int originX, int originY, int originZ) {
		int gap = 20;
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				if(x >= -1 && x <= 1 && z >= -1 && z <= 1)
					continue;
				this.buildPillar(world, originX + (x * gap), originY, originZ + (z * gap));
			}
		}
	}

	/** Builds an actual pillar. **/
	public void buildPillar(World world, int originX, int originY, int originZ) {
		int radius = 2;
		int height = 30;
		int minX = originX - radius;
		int maxX = originX + radius;
		int minY = originY;
		int maxY = originY + height;
		int minZ = originZ - radius;
		int maxZ = originZ + radius;
		BlockState base = ObjectManager.getBlock("aberrantstonepillar").defaultBlockState();
		BlockState light = ObjectManager.getBlock("aberrantcrystal").defaultBlockState();
		BlockState trimming = ObjectManager.getBlock("aberrantstonechiseled").defaultBlockState();
		BlockState top = ObjectManager.getBlock("aberrantstonepolished").defaultBlockState();
		BlockState fireBase = Blocks.OBSIDIAN.defaultBlockState();
		BlockState fire = ObjectManager.getBlock("hellfire").defaultBlockState().setValue(BlockFireBase.STATIC, true);

		for(int x = minX; x <= maxX; x++) {
			for(int z = minZ; z <= maxZ; z++) {
				for(int y = minY; y <= maxY; y++) {
					BlockPos buildPos = new BlockPos(x, y, z);
					if(y < maxY - 2) {
						if (y % 7 == 0)
							world.setBlock(buildPos, light, 2);
						else if (y % 8 == 0)
							world.setBlock(buildPos, trimming, 2);
						else
							world.setBlock(buildPos, base, 2);
					}
					else if(y == maxY - 2)
						world.setBlock(buildPos, top, 2);
					else if(y == maxY - 1)
						world.setBlock(buildPos, fireBase, 2);
					else
						world.setBlock(buildPos, fire, 2);
				}
			}
		}
	}
}
