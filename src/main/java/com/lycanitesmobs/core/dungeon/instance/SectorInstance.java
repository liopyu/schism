package com.lycanitesmobs.core.dungeon.instance;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.block.BlockFireBase;
import com.lycanitesmobs.core.block.effect.BlockFrostCloud;
import com.lycanitesmobs.core.block.effect.BlockPoisonCloud;
import com.lycanitesmobs.core.dungeon.definition.DungeonSector;
import com.lycanitesmobs.core.dungeon.definition.DungeonTheme;
import com.lycanitesmobs.core.dungeon.definition.SectorLayer;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.spawner.MobSpawn;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class SectorInstance {
	/** Sector Instances makeup an entire Dungeon Layout. **/

	/** The Dungeon Layout that this instance belongs to. **/
	public DungeonLayout layout;

	/** The Dungeon Sector that this instance is using. **/
	public DungeonSector dungeonSector;

	/** The connector that this sector is connected to, cannot be null. **/
	public SectorConnector parentConnector;

	/** A list of connectors that this sector provides to connect to other sectors with. **/
	public List<SectorConnector> connectors = new ArrayList<>();

	/** The room size of this Sector Instance, this includes the inside and inner floor, walls and ceiling. Used for building and sector to sector collision. **/
	protected Vector3i roomSize;

	/** The occupied size of this Sector Instance, includes the room size plus additional space taken up by sector layers, structures, padding, etc. **/
	protected Vector3i occupiedSize;

	/** The theme this Sector Instance is using. **/
	public DungeonTheme theme;

	/** The random light block for this sector instance to use. **/
	public BlockState lightBlock;

	/** The random torch block for this sector instance to use. **/
	public BlockState torchBlock;

	/** The random stairs block for this sector instance to use. **/
	public BlockState stairBlock;

	/** The random pit block for this sector instance to use. **/
	public BlockState pitBlock;

	/** How many chunks this sector has been built into. When this equals the total chunks this sector occupies it is considered fully built. **/
	public int chunksBuilt = 0;


	/**
	 * Constructor
	 * @param layout The Dungeon Layout to create this instance for.
	 * @param dungeonSector The Dungeon Sector to create this instance from.
	 * @param random The instance of Random to use.
	 */
	public SectorInstance(DungeonLayout layout, DungeonSector dungeonSector, Random random) {
		this.layout = layout;
		this.dungeonSector = dungeonSector;

		// Size:
		this.roomSize = this.dungeonSector.getRandomSize(random);
		this.occupiedSize = new Vector3i(
				this.roomSize.getX() + Math.max(1, this.dungeonSector.padding.getX()),
				this.roomSize.getY() + this.dungeonSector.padding.getY(),
				this.roomSize.getZ() + Math.max(1, this.dungeonSector.padding.getZ())
		);

		// Structures:
		// TODO Structures
	}


	/**
	 * Connects this sector to the provided connector. Should be called before init.
	 * @param parentConnector The connector that this sector is connecting from.
	 */
	public void connect(SectorConnector parentConnector) {
		this.parentConnector = parentConnector;
	}


	/**
	 * Initialises this Sector Instance. Must be connected to a parent connector.
	 * @param random The instance of Random to use.
	 */
	public void init(Random random) {
		if(this.parentConnector == null) {
			throw new RuntimeException("[Dungeon] Tried to initialise a Sector Instance with a null Parent Connector: " + this);
		}

		// Close Parent Connector:
		this.parentConnector.childSector = this;
		this.parentConnector.closed = true;
		this.layout.openConnectors.remove(this.parentConnector);

		// Theme:
		if(this.dungeonSector.changeTheme || this.parentConnector.parentSector == null) {
			this.theme = this.layout.dungeonInstance.schematic.getRandomTheme(random);
		}
		else {
			this.theme = this.parentConnector.parentSector.theme;
		}
		this.lightBlock = this.theme.getLight('B', random);
		this.torchBlock = this.theme.getTorch('B', random);
		this.stairBlock = this.theme.getStairs('B', random);
		this.pitBlock = this.theme.getPit('B', random);

		// Create Child Connectors:
		BlockPos boundsMin = this.getRoomBoundsMin();
		BlockPos boundsMax = this.getRoomBoundsMax();
		Vector3i size = this.getRoomSize();
		int centerX = boundsMin.getX() + Math.round((float)size.getX() / 2);
		int centerZ = boundsMin.getZ() + Math.round((float)size.getZ() / 2);

		// Upper Exit:
		int upperConnectorY = this.parentConnector.position.getY() + this.getRoomSize().getY();
		if(upperConnectorY < 255) {
			BlockPos blockPos = new BlockPos(centerX, upperConnectorY, centerZ);
			this.addConnector(blockPos, this.parentConnector.level + 1, Direction.UP);
		}

		if("corridor".equalsIgnoreCase(this.dungeonSector.type) || "room".equalsIgnoreCase(this.dungeonSector.type) || "tower".equalsIgnoreCase(this.dungeonSector.type) || "entrance".equalsIgnoreCase(this.dungeonSector.type) || "bossRoom".equalsIgnoreCase(this.dungeonSector.type)) {

			// Front/Back Exit:
			BlockPos frontPos = this.parentConnector.position;
			Direction frontFacing = Direction.SOUTH;
			BlockPos backPos = this.parentConnector.position;
			Direction backFacing = Direction.NORTH;
			if(this.parentConnector.facing == Direction.SOUTH || this.parentConnector.facing == Direction.UP) {
				frontPos = new BlockPos(this.getConnectorOffset(random, size.getX(), boundsMin.getX()), this.parentConnector.position.getY(), boundsMax.getZ() + 1);
				frontFacing = Direction.SOUTH;
				backPos = new BlockPos(this.getConnectorOffset(random, size.getX(), boundsMin.getX()), this.parentConnector.position.getY(), boundsMin.getZ() - 1);
				backFacing = Direction.NORTH;
			}
			else if(this.parentConnector.facing == Direction.EAST) {
				frontPos = new BlockPos(boundsMax.getX() + 1, this.parentConnector.position.getY(), this.getConnectorOffset(random, size.getZ(), boundsMin.getZ()));
				frontFacing = Direction.EAST;
				backPos = new BlockPos(boundsMin.getX() - 1, this.parentConnector.position.getY(), this.getConnectorOffset(random, size.getZ(), boundsMin.getZ()));
				backFacing = Direction.WEST;
			}
			else if(this.parentConnector.facing == Direction.NORTH) {
				frontPos = new BlockPos(this.getConnectorOffset(random, size.getX(), boundsMin.getX()), this.parentConnector.position.getY(), boundsMin.getZ() - 1);
				frontFacing = Direction.NORTH;
				backPos = new BlockPos(this.getConnectorOffset(random, size.getX(), boundsMin.getX()), this.parentConnector.position.getY(), boundsMax.getZ() + 1);
				backFacing = Direction.SOUTH;
			}
			else if(this.parentConnector.facing == Direction.WEST) {
				frontPos = new BlockPos(boundsMin.getX() - 1, this.parentConnector.position.getY(), this.getConnectorOffset(random, size.getZ(), boundsMin.getZ()));
				frontFacing = Direction.WEST;
				backPos = new BlockPos(boundsMax.getX() + 1, this.parentConnector.position.getY(), this.getConnectorOffset(random, size.getZ(), boundsMin.getZ()));
				backFacing = Direction.EAST;
			}
			this.addConnector(frontPos, this.parentConnector.level, frontFacing);
			if("tower".equalsIgnoreCase(this.dungeonSector.type)) {
				this.addConnector(backPos, this.parentConnector.level, backFacing);
			}

			// Side Exits:
			if("room".equalsIgnoreCase(this.dungeonSector.type) || "tower".equalsIgnoreCase(this.dungeonSector.type)) {
				BlockPos leftPos = this.parentConnector.position;
				Direction leftFacing = Direction.WEST;
				BlockPos rightPos = this.parentConnector.position;
				Direction rightFacing = Direction.EAST;
				if(this.parentConnector.facing == Direction.SOUTH || this.parentConnector.facing == Direction.NORTH || this.parentConnector.facing == Direction.UP) {
					leftPos = new BlockPos(boundsMin.getX() - 1, this.parentConnector.position.getY(), this.getConnectorOffset(random, size.getZ(), boundsMin.getZ()));
					leftFacing = Direction.WEST;
					rightPos = new BlockPos(boundsMax.getX() + 1, this.parentConnector.position.getY(), this.getConnectorOffset(random, size.getZ(), boundsMin.getZ()));
					rightFacing = Direction.EAST;
				}
				else if(this.parentConnector.facing == Direction.EAST || this.parentConnector.facing == Direction.WEST) {
					leftPos = new BlockPos(this.getConnectorOffset(random, size.getX(), boundsMin.getX()), this.parentConnector.position.getY(), boundsMax.getZ() + 1);
					leftFacing = Direction.SOUTH;
					rightPos = new BlockPos(this.getConnectorOffset(random, size.getX(), boundsMin.getX()), this.parentConnector.position.getY(), boundsMin.getZ() - 1);
					rightFacing = Direction.NORTH;
				}
				this.addConnector(leftPos, this.parentConnector.level, leftFacing);
				this.addConnector(rightPos, this.parentConnector.level, rightFacing);
			}
		}
		if("stairs".equalsIgnoreCase(this.dungeonSector.type)) {

			// Lower Exit:
			int y = this.parentConnector.position.getY() - (size.getY() * 2);
			if(y > 0) {
				BlockPos blockPos = new BlockPos(centerX, y, boundsMax.getZ() + 1);
				if (this.parentConnector.facing == Direction.EAST) {
					blockPos = new BlockPos(boundsMax.getX() + 1, y, centerZ);
				}
				else if (this.parentConnector.facing == Direction.NORTH) {
					blockPos = new BlockPos(centerX, y, boundsMin.getZ() - 1);
				}
				else if (this.parentConnector.facing == Direction.WEST) {
					blockPos = new BlockPos(boundsMin.getX() - 1, y, centerZ);
				}
				this.addConnector(blockPos, this.parentConnector.level - 1, this.parentConnector.facing);
			}
		}

		//LycanitesMobs.logDebug("Dungeon", "Initialised Sector Instance - Bounds: " + this.getOccupiedBoundsMin() + " to " + this.getOccupiedBoundsMax());
	}


	/**
	 * Gets a random offset for positioning a sector connector.
	 * @param random The instance of random to use.
	 * @param length The length to get a random offset from such as the x/z size of the sector instance.
	 * @param start The world position to add the offset to.
	 * @return
	 */
	public int getConnectorOffset(Random random, int length, int start) {
		int entrancePadding = 2;
		if(!"room".equalsIgnoreCase(this.dungeonSector.type) || length <= entrancePadding * 2) {
			return start + Math.round((float)length / 2);
		}
		return start + entrancePadding + random.nextInt(length - (entrancePadding * 2)) + 1;
	}


	/**
	 * Adds a new child Sector Connector to this Sector Instance.
	 * @param blockPos The position of the connector.
	 * @param level The level that the connector is on.
	 * @param facing The facing of the sector.
	 * @return The newly created Sector Connector.
	 */
	public SectorConnector addConnector(BlockPos blockPos, int level, Direction facing) {
		SectorConnector connector = new SectorConnector(blockPos, this, level, facing);
		this.connectors.add(connector);
		return connector;
	}


	/**
	 * Returns a random child connector for a Sector Instance to connect to.
	 * @param random The instance of Random to use.
	 * @return A random connector.
	 */
	public SectorConnector getRandomConnector(Random random, SectorInstance sectorInstance) {
		List<SectorConnector> openConnectors = this.getOpenConnectors(sectorInstance);
		if(openConnectors.isEmpty()) {
			return null;
		}
		if(openConnectors.size() == 1) {
			return openConnectors.get(0);
		}
		return openConnectors.get(random.nextInt(openConnectors.size()));
	}


	/**
	 * Returns a list of open connectors where they are not set to closed and have no child Sector Instance connected.
	 * @param sectorInstance The sector to get the open connectors for. If null, collision checks are skipped.
	 * @return A list of open connectors.
	 */
	public List<SectorConnector> getOpenConnectors(SectorInstance sectorInstance) {
		List<SectorConnector> openConnectors = new ArrayList<>();
		for(SectorConnector connector : this.connectors) {
			if(connector.canConnect(this.layout, sectorInstance)) {
				openConnectors.add(connector);
			}
		}
		return openConnectors;
	}


	/**
	 * Returns a list of every ChunkPos that this Sector Instance occupies.
	 * Applies an offset to occupied bounds for generating a chunk position.
	 * @return A list of ChunkPos.
	 */
	public List<ChunkPos> getChunkPositions() {
		ChunkPos minChunkPos = new ChunkPos(this.getOccupiedBoundsMin().offset(-1, 0, -1));
		ChunkPos maxChunkPos = new ChunkPos(this.getOccupiedBoundsMax().offset(1, 0, 1));
		List<ChunkPos> chunkPosList = new ArrayList<>();
		for(int x = minChunkPos.x; x <= maxChunkPos.x; x++) {
			for(int z = minChunkPos.z; z <= maxChunkPos.z; z++) {
				chunkPosList.add(new ChunkPos(x, z));
			}
		}
		return chunkPosList;
	}


	/**
	 * Returns a list of other sectors near this sector instance.
	 * @return A list of nearby sector instances.
	 */
	public List<SectorInstance> getNearbySectors() {
		List<SectorInstance> nearbySectors = new ArrayList<>();
		for(SectorInstance nearbySector : this.layout.sectors) {
			if(!nearbySectors.contains(nearbySector)) {
				nearbySectors.add(nearbySector);
			}
		}
		/*for(ChunkPos chunkPos : this.getChunkPositions()) {
			if(this.layout.sectorChunkMap.containsKey(chunkPos)) {
				for(SectorInstance nearbySector : this.layout.sectorChunkMap.get(chunkPos)) {
					if(!nearbySectors.contains(nearbySector)) {
						nearbySectors.add(nearbySector);
					}
				}
			}
		}*/
		return nearbySectors;
	}


	/**
	 * Returns true if this sector instance collides with the provided sector instance.
	 * @param sectorInstance The sector instance to check for collision with.
	 * @return True on collision.
	 */
	public boolean collidesWith(SectorInstance sectorInstance) {
		if(sectorInstance == this || sectorInstance == this.parentConnector.parentSector) {
			return false;
		}

		BlockPos boundsMin = this.getOccupiedBoundsMin();
		BlockPos boundsMax = this.getOccupiedBoundsMax();
		BlockPos targetMin = sectorInstance.getOccupiedBoundsMin();
		BlockPos targetMax = sectorInstance.getOccupiedBoundsMax();

		if(boundsMin.getY() != targetMin.getY() && !sectorInstance.dungeonSector.type.equals("stairs")) {
			return false; // Temporary simple y collision check (y doesn't take negative layer padding into account yet).
		}

		boolean withinX = boundsMin.getX() >= targetMin.getX() && boundsMin.getX() <= targetMax.getX();
		if(!withinX)
			withinX = boundsMax.getX() >= targetMin.getX() && boundsMax.getX() <= targetMax.getX();
		if(!withinX)
			return false;

//		boolean withinY = boundsMin.getY() >= targetMin.getY() && boundsMin.getY() <= targetMax.getY();
//		if(!withinY)
//			withinY = boundsMax.getY() >= targetMin.getY() && boundsMax.getY() <= targetMax.getY();
//		if(!withinY)
//			return false;

		boolean withinZ = boundsMin.getZ() >= targetMin.getZ() && boundsMin.getZ() <= targetMax.getZ();
		if(!withinZ)
			withinZ = boundsMax.getZ() >= targetMin.getZ() && boundsMax.getZ() <= targetMax.getZ();
		if(!withinZ)
			return false;

		return true;
	}


	/**
	 * Returns the room size of this sector. X and Z are swapped when facing EAST or WEST.
	 * This is how large the room to be built is excluding extra blocks added for layers or structures, etc.
	 * Used for building this sector.
	 * @return A vector of the room size.
	 */
	public Vector3i getRoomSize() {
		if(this.parentConnector.facing == Direction.EAST || this.parentConnector.facing == Direction.WEST) {
			return new Vector3i(this.roomSize.getZ(), this.roomSize.getY(), this.roomSize.getX());
		}
		return this.roomSize;
	}


	/**
	 * Returns the collision size of this sector. X and Z are swapped when facing EAST or WEST.
	 * This is how large this sector is including extra blocks added for layers or structures, etc.
	 * Used for detecting what chunks this sector needs to generate in and sector collision detection.
	 * @return A vector of the collision size.
	 */
	public Vector3i getOccupiedSize() {
		if(this.parentConnector.facing == Direction.EAST || this.parentConnector.facing == Direction.WEST) {
			return new Vector3i(this.occupiedSize.getZ(), this.occupiedSize.getY(), this.occupiedSize.getX());
		}
		return this.occupiedSize;
	}


	/**
	 * Returns the minimum xyz position that this Sector Instance from the provided bounds size.
	 * @param boundsSize The xyz size to use when calculating bounds.
	 * @return The minimum bounds position (corner).
	 */
	public BlockPos getBoundsMin(Vector3i boundsSize) {
		BlockPos bounds = new BlockPos(this.parentConnector.position);
		if(this.parentConnector.facing == Direction.UP) {
			bounds = bounds.offset(
					-(int)Math.ceil((double)boundsSize.getX() / 2),
					0,
					-(int)Math.ceil((double)boundsSize.getZ() / 2)
			);
		}
		else if(this.parentConnector.facing == Direction.SOUTH) {
			bounds = bounds.offset(
					-(int)Math.ceil((double)boundsSize.getX() / 2),
					0,
					0
			);
		}
		else if(this.parentConnector.facing == Direction.EAST) {
			bounds = bounds.offset(
					0,
					0,
					-(int)Math.ceil((double)boundsSize.getZ() / 2)
			);
		}
		else if(this.parentConnector.facing == Direction.NORTH) {
			bounds = bounds.offset(
					-(int)Math.ceil((double)boundsSize.getX() / 2),
					0,
					-boundsSize.getZ()
			);
		}
		else if(this.parentConnector.facing == Direction.WEST) {
			bounds = bounds.offset(
					-boundsSize.getX(),
					0,
					-(int)Math.ceil((double)boundsSize.getZ() / 2)
			);
		}

		return bounds;
	}


	/**
	 * Returns the maximum xyz position that this Sector Instance from the provided bounds size.
	 * @param boundsSize The xyz size to use when calculating bounds.
	 * @return The maximum bounds position (corner).
	 */
	public BlockPos getBoundsMax(Vector3i boundsSize) {
		BlockPos bounds = new BlockPos(this.parentConnector.position);
		if(this.parentConnector.facing == Direction.UP) {
			bounds = bounds.offset(
					(int)Math.ceil((double)boundsSize.getX() / 2),
					boundsSize.getY(),
					(int)Math.ceil((double)boundsSize.getZ() / 2)
			);
		}
		else if(this.parentConnector.facing == Direction.SOUTH) {
			bounds = bounds.offset(
					(int)Math.floor((double)boundsSize.getX() / 2),
					boundsSize.getY(),
					boundsSize.getZ()
			);
		}
		else if(this.parentConnector.facing == Direction.EAST) {
			bounds = bounds.offset(
					boundsSize.getX(),
					boundsSize.getY(),
					(int)Math.floor((double)boundsSize.getZ() / 2)
			);
		}
		else if(this.parentConnector.facing == Direction.NORTH) {
			bounds = bounds.offset(
					(int)Math.floor((double)boundsSize.getX() / 2),
					boundsSize.getY(),
					0
			);
		}
		else if(this.parentConnector.facing == Direction.WEST) {
			bounds = bounds.offset(
					0,
					boundsSize.getY(),
					(int)Math.floor((double)boundsSize.getZ() / 2)
			);
		}
		return bounds;
	}


	/**
	 * Returns the minimum xyz position that this Sector Instance occupies.
	 * @return The minimum bounds position (corner).
	 */
	public BlockPos getOccupiedBoundsMin() {
		BlockPos occupiedBoundsMin = this.getBoundsMin(this.getOccupiedSize());
		if("stairs".equals(this.dungeonSector.type)) {
			occupiedBoundsMin = occupiedBoundsMin.subtract(new Vector3i(0, this.getRoomSize().getY() * 2, 0));
		}
		return occupiedBoundsMin;
	}


	/**
	 * Returns the maximum xyz position that this Sector Instance occupies.
	 * @return The maximum bounds position (corner).
	 */
	public BlockPos getOccupiedBoundsMax() {
		return this.getBoundsMax(this.getOccupiedSize());
	}


	/**
	 * Returns the minimum xyz position that this Sector Instance builds from.
	 * @return The minimum bounds position (corner).
	 */
	public BlockPos getRoomBoundsMin() {
		return this.getBoundsMin(this.getRoomSize());
	}


	/**
	 * Returns the maximum xyz position that this Sector Instance builds to.
	 * @return The maximum bounds position (corner).
	 */
	public BlockPos getRoomBoundsMax() {
		return this.getBoundsMax(this.getRoomSize());
	}


	/**
	 * Returns the rounded center block position of this sector.
	 * @return The center block position.
	 */
	public BlockPos getCenter() {
		BlockPos startPos = this.getRoomBoundsMin();
		BlockPos stopPos = this.getRoomBoundsMax();

		Vector3i size = this.getRoomSize();
		int centerX = startPos.getX() + Math.round((float)size.getX() / 2);
		int centerZ = startPos.getZ() + Math.round((float)size.getZ() / 2);

		return new BlockPos(centerX, startPos.getY(), centerZ);
	}


	/**
	 * Places a block state in the world from this sector.
	 * @param worldWriter The world to place a block in. Cannot be the actual World during WorldGen.
	 * @param chunkPos The chunk position to build within.
	 * @param blockPos The position to place the block at.
	 * @param blockState The block state to place.
	 * @param random The instance of random, used for random mob spawns or loot on applicable blocks, etc.
	 */
	public void placeBlock(IWorld worldWriter, ChunkPos chunkPos, BlockPos blockPos, BlockState blockState, Direction facing, Random random) {
		// Restrict To Chunk Position:
		int chunkOffset = 0;
		if(blockPos.getX() < chunkPos.getMinBlockX() + chunkOffset || blockPos.getX() > chunkPos.getMaxBlockX() + chunkOffset) {
			return;
		}
		if(blockPos.getY() <= 0 || blockPos.getY() >= worldWriter.getMaxBuildHeight()) {
			return;
		}
		if(blockPos.getZ() < chunkPos.getMinBlockZ() + chunkOffset || blockPos.getZ() > chunkPos.getMaxBlockZ() + chunkOffset) {
			return;
		}

		// Don't Clear Chests:
		if(blockState.getBlock() == Blocks.AIR && worldWriter.getBlockState(blockPos).getBlock() == Blocks.CHEST) {
			return;
		}


		// Block State and Flags:
		int flags = 2;

		// Torch:
		if(blockState.getBlock() == Blocks.TORCH) {
			blockState = blockState.setValue(WallTorchBlock.FACING, facing);
			flags = 0;
		}

		// Chest:
		if(blockState.getBlock() == Blocks.CHEST) {
			blockState = blockState.setValue(ChestBlock.FACING, facing);
		}

		// Don't Update:
		if(blockState.getBlock() == Blocks.AIR || blockState.getBlock() == Blocks.CAVE_AIR || blockState.getBlock() instanceof FlowingFluidBlock ||
				blockState.getBlock() instanceof FireBlock || blockState.getBlock() instanceof BlockFireBase ||
				blockState.getBlock() instanceof BlockPoisonCloud || blockState.getBlock() instanceof BlockFrostCloud) {
			flags = 0;
		}


		// Set The Block:
		worldWriter.setBlock(blockPos, blockState, flags);


		// Tile Entities:

		// Spawner:
		if(blockState.getBlock() == Blocks.SPAWNER) {
			TileEntity tileEntity = worldWriter.getBlockEntity(blockPos);
			if(tileEntity instanceof MobSpawnerTileEntity) {
				MobSpawnerTileEntity spawner = (MobSpawnerTileEntity)tileEntity;
				MobSpawn mobSpawn = this.layout.dungeonInstance.schematic.getRandomMobSpawn(this.parentConnector.level, false, random);
				if(mobSpawn != null && mobSpawn.entityType != null) {
					spawner.getSpawner().setEntityId(mobSpawn.entityType);
				}
			}
			return;
		}

		// Chest:
		if(blockState.getBlock() == Blocks.CHEST) {
			TileEntity tileEntity = worldWriter.getBlockEntity(blockPos);
			if(tileEntity instanceof ChestTileEntity) {

				// Apply Loot Table:
				ChestTileEntity chest = (ChestTileEntity)tileEntity;
				ResourceLocation lootTable = this.layout.dungeonInstance.schematic.getRandomLootTable(this.parentConnector.level, random);
				if(lootTable != null) {
					chest.setLootTable(lootTable, Objects.hash(blockPos.hashCode(), random));
				}

				// Add Specific Items:
				// TODO Add a random amount of additional specific items.
			}
		}
	}


	/**
	 * Builds this sector. Wont build at y level 0 or below, beyond world height or outside of the chunk.
	 * @param world The world to build in.
	 * @param chunkPos The chunk position to build within.
	 * @param random The instance of random, used for characters that are random.
	 */
	public void build(IWorld worldWriter, World world, ChunkPos chunkPos, Random random) {
		this.clearArea(worldWriter, world, chunkPos, random);
		this.buildFloor(worldWriter, world, chunkPos, random, 0);
		this.buildWalls(worldWriter, world, chunkPos, random);
		this.buildCeiling(worldWriter, world, chunkPos, random);
		if("stairs".equalsIgnoreCase(this.dungeonSector.type)) {
			this.buildStairs(worldWriter, world, chunkPos, random);
			this.buildFloor(worldWriter, world, chunkPos, random, -(this.getRoomSize().getY() * 2));
		}
		if("tower".equalsIgnoreCase(this.dungeonSector.type)) {
			this.buildStairs(worldWriter, world, chunkPos, random);
		}
		this.buildEntrances(worldWriter, world, chunkPos, random);
		this.chunksBuilt++;
		if("bossRoom".equalsIgnoreCase(this.dungeonSector.type)) {
			MobSpawn mobSpawn = this.layout.dungeonInstance.schematic.getRandomMobSpawn(this.parentConnector.level, true, random);
			if(mobSpawn != null) {
				this.spawnMob(worldWriter, world, chunkPos, this.getCenter().offset(0, 1, 0), mobSpawn, random);
			}
		}
	}


	/**
	 * Sets the area of this sector to air for building in from within the chunk position.
	 * @param worldWriter The world to create blocks in.
	 * @param world The world being built in. This cannot be used for placement during WorldGen.
	 * @param chunkPos The chunk position to build within.
	 * @param random The instance of random, used for characters that are random.
	 */
	public void clearArea(IWorld worldWriter, World world, ChunkPos chunkPos, Random random) {
		// Get Start and Stop Positions:
		BlockPos startPos = this.getRoomBoundsMin();
		BlockPos stopPos = this.getRoomBoundsMax();
		int startX = Math.min(startPos.getX(), stopPos.getX());
		int stopX = Math.max(startPos.getX(), stopPos.getX());
		int startY = Math.min(startPos.getY(), stopPos.getY());
		int stopY = Math.max(startPos.getY(), stopPos.getY());
		int startZ = Math.min(startPos.getZ(), stopPos.getZ());
		int stopZ = Math.max(startPos.getZ(), stopPos.getZ());

		if("stairs".equalsIgnoreCase(this.dungeonSector.type)) {
			startY = Math.max(1, startPos.getY() - (this.getRoomSize().getY() * 2));
		}

		for(int x = startX; x <= stopX; x++) {
			for(int y = startY; y <= stopY; y++) {
				for(int z = startZ; z <= stopZ; z++) {
					this.placeBlock(worldWriter, chunkPos, new BlockPos(x, y, z), Blocks.CAVE_AIR.defaultBlockState(), Direction.SOUTH, random);
				}
			}
		}
	}


	/**
	 * Builds the floor of this sector from within the chunk position.
	 * @param worldWriter The world to create blocks in.
	 * @param world The world being built in. This cannot be used for placement during WorldGen.
	 * @param chunkPos The chunk position to build within.
	 * @param random The instance of random, used for characters that are random.
	 * @param offsetY The Y offset to build the floor at, useful for multiple floor sectors.
	 */
	public void buildFloor(IWorld worldWriter, World world, ChunkPos chunkPos, Random random, int offsetY) {
		// Get Start and Stop Positions:
		BlockPos startPos = this.getRoomBoundsMin().offset(0, offsetY, 0);
		BlockPos stopPos = this.getRoomBoundsMax().offset(0, offsetY, 0);
		int startX = Math.min(startPos.getX(), stopPos.getX());
		int stopX = Math.max(startPos.getX(), stopPos.getX());
		int startY = Math.min(startPos.getY(), stopPos.getY());
		int stopY = Math.max(startPos.getY(), stopPos.getY());
		int startZ = Math.min(startPos.getZ(), stopPos.getZ());
		int stopZ = Math.max(startPos.getZ(), stopPos.getZ());

		for(int layerIndex : this.dungeonSector.floor.layers.keySet()) {
			int y = startY + layerIndex;
			if(y <= 0 || y >= world.getMaxBuildHeight()) {
				continue;
			}
			SectorLayer layer = this.dungeonSector.floor.layers.get(layerIndex);
			for(int x = startX; x <= stopX; x++) {
				List<Character> row = layer.getRow(x - startX, stopX - startX);
				for(int z = startZ; z <= stopZ; z++) {
					char buildChar = layer.getColumn(x - startX, stopX - startX, z - startZ, stopZ - startZ, row);
					BlockPos buildPos = new BlockPos(x, y, z);
					BlockState blockState = this.theme.getFloor(this, buildChar, random);
					if(blockState.getBlock() != Blocks.CAVE_AIR)
						this.placeBlock(worldWriter, chunkPos, buildPos, blockState, Direction.UP, random);
				}
			}
		}
	}


	/**
	 * Builds the walls of this sector from within the chunk position.
	 * @param worldWriter The world to create blocks in.
	 * @param world The world being built in. This cannot be used for placement during WorldGen.
	 * @param chunkPos The chunk position to build within.
	 * @param random The instance of random, used for characters that are random.
	 */
	public void buildWalls(IWorld worldWriter, World world, ChunkPos chunkPos, Random random) {
		// Get Start and Stop Positions:
		BlockPos startPos = this.getRoomBoundsMin();
		BlockPos stopPos = this.getRoomBoundsMax();

		Vector3i size = this.getRoomSize();
		int startX = Math.min(startPos.getX(), stopPos.getX());
		int stopX = Math.max(startPos.getX(), stopPos.getX());
		int startY = Math.min(startPos.getY() + 1, stopPos.getY());
		int stopY = Math.max(startPos.getY() - 1, stopPos.getY());
		int startZ = Math.min(startPos.getZ(), stopPos.getZ());
		int stopZ = Math.max(startPos.getZ(), stopPos.getZ());

		if("stairs".equalsIgnoreCase(this.dungeonSector.type)) {
			startY = Math.max(1, startPos.getY() - (size.getY() * 2));
		}

		for(int layerIndex : this.dungeonSector.wall.layers.keySet()) {
			SectorLayer layer = this.dungeonSector.wall.layers.get(layerIndex);
			for(int y = startY; y <= stopY; y++) {
				// Y Limit:
				if(y <= 0 || y >= world.getMaxBuildHeight()) {
					continue;
				}

				// Get Row:
				int progressY = y - startY;
				int fullY = stopY - startY;
				List<Character> row = layer.getRow(progressY, fullY);

				// Build Front/Back:
				for(int x = startX; x <= stopX; x++) {
					char buildChar = layer.getColumn(progressY, fullY, x - startX, stopX - startX, row);
					BlockState blockState = this.theme.getWall(this, buildChar, random);
					if(blockState.getBlock() != Blocks.CAVE_AIR) {
						this.placeBlock(worldWriter, chunkPos, new BlockPos(x, y, startZ + layerIndex), blockState, Direction.SOUTH, random);
						this.placeBlock(worldWriter, chunkPos, new BlockPos(x, y, stopZ - layerIndex), blockState, Direction.NORTH, random);
					}
				}

				// Build Left/Right:
				for(int z = startZ; z <= stopZ; z++) {
					char buildChar = layer.getColumn(progressY, fullY, z - startZ, stopZ - startZ, row);
					BlockState blockState = this.theme.getWall(this, buildChar, random);
					if(blockState.getBlock() != Blocks.CAVE_AIR) {
						this.placeBlock(worldWriter, chunkPos, new BlockPos(startX + layerIndex, y, z), blockState, Direction.EAST, random);
						this.placeBlock(worldWriter, chunkPos, new BlockPos(stopX - layerIndex, y, z), blockState, Direction.WEST, random);
					}
				}
			}
		}
	}


	/**
	 * Builds the ceiling of this sector from within the chunk position.
	 * @param worldWriter The world to create blocks in.
	 * @param world The world being built in. This cannot be used for placement during WorldGen.
	 * @param chunkPos The chunk position to build within.
	 * @param random The instance of random, used for characters that are random.
	 */
	public void buildCeiling(IWorld worldWriter, World world, ChunkPos chunkPos, Random random) {
		// Get Start and Stop Positions:
		BlockPos startPos = this.getRoomBoundsMin();
		BlockPos stopPos = this.getRoomBoundsMax();
		int startX = Math.min(startPos.getX(), stopPos.getX());
		int stopX = Math.max(startPos.getX(), stopPos.getX());
		int startY = Math.min(startPos.getY(), stopPos.getY());
		int stopY = Math.max(startPos.getY(), stopPos.getY());
		int startZ = Math.min(startPos.getZ(), stopPos.getZ());
		int stopZ = Math.max(startPos.getZ(), stopPos.getZ());

		for(int layerIndex : this.dungeonSector.ceiling.layers.keySet()) {
			int y = stopY + layerIndex;
			if(y <= 0 || y >= world.getMaxBuildHeight()) {
				continue;
			}
			SectorLayer layer = this.dungeonSector.ceiling.layers.get(layerIndex);
			for(int x = startX; x <= stopX; x++) {
				List<Character> row = layer.getRow(x - startX, stopX - startX);
				for(int z = startZ; z <= stopZ; z++) {
					char buildChar = layer.getColumn(x - startX, stopX - startX, z - startZ, stopZ - startZ, row);
					BlockPos buildPos = new BlockPos(x, y, z);
					BlockState blockState = this.theme.getCeiling(this, buildChar, random);
					if(blockState.getBlock() != Blocks.CAVE_AIR)
						this.placeBlock(worldWriter, chunkPos, buildPos, blockState, Direction.DOWN, random);
				}
			}
		}
	}


	/**
	 * Builds the entrances of this sector from within the chunk position.
	 * @param worldWriter The world to create blocks in.
	 * @param world The world being built in. This cannot be used for placement during WorldGen.
	 * @param chunkPos The chunk position to build within.
	 * @param random The instance of random, used for characters that are random.
	 */
	public void buildEntrances(IWorld worldWriter, World world, ChunkPos chunkPos, Random random) {
		this.parentConnector.buildEntrance(worldWriter, world, chunkPos, random);
	}


	/**
	 * Builds a set of stairs leading down to a lower room to start the next level.
	 * @param worldWriter The world to create blocks in.
	 * @param world The world being built in. This cannot be used for placement during WorldGen.
	 * @param chunkPos The chunk position to build within.
	 * @param random The instance of random, used for characters that are random.
	 */
	public void buildStairs(IWorld worldWriter, World world, ChunkPos chunkPos, Random random) {
		// Get Start and Stop Positions:
		BlockPos startPos = this.getRoomBoundsMin();
		BlockPos stopPos = this.getRoomBoundsMax();

		Vector3i size = this.getRoomSize();
		int centerX = startPos.getX() + Math.round((float)size.getX() / 2);
		int centerZ = startPos.getZ() + Math.round((float)size.getZ() / 2);

		int stairsHeight = this.parentConnector.parentSector.getOccupiedSize().getY() - 1;
		if(this.dungeonSector.type.equalsIgnoreCase("stairs")) {
			stairsHeight = size.getY() * 2;
		}
		int startX = centerX - 1;
		int stopX = centerX + 1;
		int startY = Math.min(startPos.getY(), stopPos.getY());
		int stopY = Math.max(1, startPos.getY() - stairsHeight);

		int startZ = centerZ - 1;
		int stopZ = centerZ + 1;

		BlockState floorBlockState = this.theme.getFloor(this, 'B', random);
		BlockState stairsBlockState = this.stairBlock;

		for(int y = startY; y >= stopY; y--) {
			for(int x = startX; x <= stopX; x++) {
				for(int z = startZ; z <= stopZ; z++) {
					BlockState blockState = Blocks.CAVE_AIR.defaultBlockState();

					// Center:
					if(x == centerX && z == centerZ) {
						blockState = this.theme.getWall(this, 'B', random);
					}

					// Spiral Stairs:
					int step = y % 8;
					int offsetX = x - startX;
					int offsetZ = z - startZ;
					if(step % 4 == 3) {
						if (offsetX == 0 && offsetZ == 0) {
							blockState = floorBlockState;
						}
						else if (offsetX == 0 && offsetZ == 1) {
							blockState = stairsBlockState;
						}
					}
					if(step % 4 == 2) {
						if (offsetX == 0 && offsetZ == 2) {
							blockState = floorBlockState;
						}
						else if (offsetX == 1 && offsetZ == 2) {
							blockState = stairsBlockState.setValue(StairsBlock.FACING, Direction.WEST);
						}
					}
					if(step % 4 == 1) {
						if (offsetX == 2 && offsetZ == 2) {
							blockState = floorBlockState;
						}
						else if (offsetX == 2 && offsetZ == 1) {
							blockState = stairsBlockState.setValue(StairsBlock.FACING, Direction.SOUTH);
						}
					}
					if(step % 4 == 0) {
						if (offsetX == 2 && offsetZ == 0) {
							blockState = floorBlockState;
						}
						else if (offsetX == 1 && offsetZ == 0) {
							blockState = stairsBlockState.setValue(StairsBlock.FACING, Direction.EAST);
						}
					}

					BlockPos buildPos = new BlockPos(x, y, z);
					this.placeBlock(worldWriter, chunkPos, buildPos, blockState, Direction.UP, random);
				}
			}
		}
	}


	/**
	 * Spawns a mob in this sector.
	 * @param worldWriter The world to create blocks in.
	 * @param world The world being built in. This cannot be used for placement during WorldGen.
	 * @param chunkPos The chunk position to spawn within.
	 * @param blockPos The position to spawn the mob at.
	 * @param mobSpawn The Mob Spawn entry to use.
	 * @param random The instance of random, used for mob vacations where applicable.
	 */
	public void spawnMob(IWorld worldWriter, World world, ChunkPos chunkPos, BlockPos blockPos, MobSpawn mobSpawn, Random random) {
		// Restrict To Chunk Position:
		int chunkOffset = 8;
		if(blockPos.getX() < chunkPos.getMinBlockX() + chunkOffset || blockPos.getX() > chunkPos.getMaxBlockX() + chunkOffset) {
			return;
		}
		if(blockPos.getY() <= 0 || blockPos.getY() >= world.getMaxBuildHeight()) {
			return;
		}
		if(blockPos.getZ() < chunkPos.getMinBlockZ() + chunkOffset || blockPos.getZ() > chunkPos.getMaxBlockZ() + chunkOffset) {
			return;
		}

		// Spawn Mob:
		LycanitesMobs.logDebug("Dungeon", "Spawning mob " + mobSpawn + " at: " + blockPos + " level: " + this.parentConnector.level);
		LivingEntity entityLiving = mobSpawn.createEntity(world);
		entityLiving.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());

		if(entityLiving instanceof BaseCreatureEntity) {
			BaseCreatureEntity entityCreature = (BaseCreatureEntity)entityLiving;
			entityCreature.setHome(blockPos.getX(), blockPos.getY(), blockPos.getZ(), Math.max(3, Math.max(this.roomSize.getX(), this.roomSize.getZ())));
		}

		mobSpawn.onSpawned(entityLiving, null);
		worldWriter.addFreshEntity(entityLiving);
	}


	/**
	 * Formats this object into a String.
	 * @return A formatted string description of this object.
	 */
	@Override
	public String toString() {
		String bounds = "";
		String size = "";
		if(this.parentConnector != null) {
			bounds = " Bounds: " + this.getOccupiedBoundsMin() + " to " + this.getOccupiedBoundsMax();
			size = " Occupies: " + this.getOccupiedSize();
		}
		return "Sector Instance Type: " + (this.dungeonSector == null ? "Unset" : this.dungeonSector.type) + " Parent Connector Pos: " + (this.parentConnector == null ? "Unset" : this.parentConnector.position) + size + bounds;
	}
}
