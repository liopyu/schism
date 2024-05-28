package com.lycanitesmobs.core.dungeon.instance;

import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.dungeon.DungeonManager;
import com.lycanitesmobs.core.dungeon.definition.DungeonSchematic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class DungeonInstance {
	/** A Dungeon Instance is a dungeon that is active in the world. **/

	/** A unique identifier for this Dungeon Instance. **/
	public UUID uuid;

	/** The Schematic this instance builds from. Can be null when a Schematic has been removed or renamed in which case the dungeon is immediately set as complete. **/
	public DungeonSchematic schematic;

	/** If true, this dungeon has been fully built and does not need to generate its layout, etc. This is where all chunks this dungeon is in have been loaded. **/
	public boolean complete = false;

	/** Stores how many chunks have been built. When this matches the number of chunks that are used by this dungeon, this dungeon is marked as complete. **/
	public int chunksBuilt = 0;

	/** The origin block position of this dungeon where it begins building from. **/
	public BlockPos originPos;

	/** The minimum xz chunk that sectors that this layout is in. **/
	public ChunkPos chunkMin;

	/** The maximum xz chunk that sectors that this layout is in. **/
	public ChunkPos chunkMax;

	/** The world that the dungeon should build in. **/
	public World world;

	/** The seed for generating this dungeon. All random decisions are based on this seed so that the Dungeon Layout can generate the same on world reload, etc. **/
	long seed = 0;

	/** The random instance to use when randomly generating, this can be seeded for consistent results. **/
	public Random random;

	/** The generated layout of this dungeon, this contains all randomly selected sectors and their structures, etc. This is null on complete dungeons. **/
	public DungeonLayout layout;


	/**
	 * Sets the origin position. This must be set before init. Reading from NBT sets this from the NBT data.
	 * @param blockPos The exact block position that this dungeon builds from.
	 */
	public void setOrigin(BlockPos blockPos) {
		this.originPos = blockPos;
		if(this.chunkMin == null) {
			this.chunkMin = new ChunkPos(blockPos);
		}
		if(this.chunkMax == null) {
			this.chunkMax = new ChunkPos(blockPos);
		}
	}


	/**
	 * Initialises this Dungeon where if it's not complete it will generate its layout, etc. Should be called after readFromNBT when loading an existing dungeon and origin must be set.
	 * @param world The world that this Instance will build in.
	 * @return True on success and false if unable to initialize.
	 */
	public boolean init(World world) {
		this.world = world;
		if(this.complete) {
			return true;
		}
		if(this.world == null || this.originPos == null) {
			LycanitesMobs.logWarning("", "Tried to initialise a dungeon with a missing world or origin. " + this);
			return false;
		}

		// Get Schematic:
		if(this.schematic == null) {
			List<DungeonSchematic> schematics = new ArrayList<>();
			for(DungeonSchematic schematic : DungeonManager.getInstance().schematics.values()) {
				if(schematic.canBuild(world, this.originPos)) {
					schematics.add(schematic);
				}
			}
			if(schematics.isEmpty()) {
				LycanitesMobs.logDebug("Dungeon", "No valid dungeon schematics found for origin position: " + this.originPos);
				return false;
			}
			if(schematics.size() == 1) {
				this.schematic = schematics.get(0);
			}
			else {
				this.schematic = schematics.get(this.world.random.nextInt(schematics.size()));
			}
		}

		// Get Seed:
		if(this.seed == 0) {
			this.seed = world.random.nextLong();
		}
		this.random = new Random(this.seed);

		// Generate Layout:
		LycanitesMobs.logDebug("Dungeon", "Starting Dungeon Instance Generation For " + this);
		if(this.layout == null) {
			this.layout = new DungeonLayout(this);
			this.layout.generate(this.random);
		}
		LycanitesMobs.logInfo("Dungeon", "Generated New Dungeon Instance " + this);

		// Mark For Save:
		ExtendedWorld extendedWorld = ExtendedWorld.getForWorld(world);
		if(extendedWorld != null) {
			extendedWorld.setDirty();
		}

		return true;
	}


	/**
	 * Returns true if the chunk position is within this dungeon's area.
	 * @param chunkPos The chunk position to check.
	 * @param padding Increases the dungeon area, useful for finding chunks within range of this layout as well.
	 * @return True if the chunk is within this dungeon's area.
	 */
	public boolean isChunkPosWithin(ChunkPos chunkPos, int padding) {
		if(chunkPos.x < this.chunkMin.x - padding || chunkPos.x > this.chunkMax.x + padding) {
			return false;
		}

		if(chunkPos.z < this.chunkMin.z - padding || chunkPos.z > this.chunkMax.z + padding) {
			return false;
		}

		return true;
	}


	/**
	 * Builds blocks from every sector within the provided chunk position.
	 * @param worldWriter The world to create blocks in.
	 * @param world The world being built in. This cannot be used for placement during WorldGen.
	 * @param chunkPos The chunk position to build within.
	 */
	public void buildChunk(IWorld worldWriter, World world, ChunkPos chunkPos, Random random) {
		if(this.complete || this.layout == null || !this.layout.sectorChunkMap.containsKey(chunkPos)) {
			return;
		}
		for(SectorInstance sectorInstance : this.layout.sectorChunkMap.get(chunkPos)) {
			// The world random is used when building as it can be volatile unlike layouts which must be done by seed in order.
			//LycanitesMobs.logInfo("", "Building In Chunk: " + chunkPos);
			sectorInstance.build(worldWriter, world, chunkPos, random);
		}

		// Update Chunks Built:
		if(++this.chunksBuilt >= this.layout.sectorChunkMap.size()) {
			this.complete = true;
		}
	}


	/**
	 * Loads this Dungeon Instance from the provided NBT data, this is mostly just if the dungeon is built and what its origin position is.
	 * @param nbtTagCompound The NBT Data to read from.
	 */
	public void readFromNBT(CompoundNBT nbtTagCompound) {
		this.uuid = nbtTagCompound.getUUID("Id");
		this.schematic = DungeonManager.getInstance().getSchematic(nbtTagCompound.getString("Schematic"));
		this.seed = nbtTagCompound.getLong("Seed");
		this.complete = nbtTagCompound.getBoolean("Complete");
		this.chunksBuilt = nbtTagCompound.getInt("ChunksBuilt");
		if(this.schematic == null) {
			this.complete = true;
		}
		int[] originPos = nbtTagCompound.getIntArray("OriginPos");
		this.originPos = new BlockPos(originPos[0], originPos[1], originPos[2]);
		int[] chunkMin = nbtTagCompound.getIntArray("ChunkMin");
		this.chunkMin = new ChunkPos(chunkMin[0], chunkMin[1]);
		int[] chunkMax = nbtTagCompound.getIntArray("ChunkMax");
		this.chunkMax = new ChunkPos(chunkMax[0], chunkMax[1]);

		LycanitesMobs.logDebug("Dungeon", "Loaded Dungeon Instance from NBT: " + this);
	}

	/**
	 * Writes this dungeon to NBT. Should only be called after the this instance has been initialised.
	 * @param nbtTagCompound The NBTData to write to.
	 * @return The written to NBTData. Null if this Dungeon Instance cannot be saved.
	 */
	public CompoundNBT writeToNBT(CompoundNBT nbtTagCompound) {
		if(this.uuid == null || this.schematic == null)
			return null;

		nbtTagCompound.putUUID("Id", this.uuid);
		nbtTagCompound.putString("Schematic", this.schematic.name);
		nbtTagCompound.putLong("Seed", this.seed);
		nbtTagCompound.putBoolean("Complete", this.complete);
		nbtTagCompound.putInt("ChunksBuilt", this.chunksBuilt);
		nbtTagCompound.putIntArray("OriginPos", new int[] {this.originPos.getX(), this.originPos.getY(), this.originPos.getZ()});
		nbtTagCompound.putIntArray("ChunkMin", new int[] {this.chunkMin.z, this.chunkMin.z});
		nbtTagCompound.putIntArray("ChunkMax", new int[] {this.chunkMax.z, this.chunkMax.z});

		LycanitesMobs.logDebug("Dungeon", "Saved Dungeon Instance to NBT: " + this);
		return nbtTagCompound;
	}


	/**
	 * Returns a descriptive string of this Dungeon Instance.
	 * @return A formatted string.
	 */
	@Override
	public String toString() {
		String schematic = "";
		if(this.schematic != null)
			schematic = " - Schematic: " + this.schematic.name;
		String tpCommand = "/tp " + this.originPos.getX() + " " + (this.originPos.getY() + 2) + " " + this.originPos.getZ() + " ";
		return "Dungeon Instance" + schematic + " - TP Command: " + tpCommand + " - Origin: " + this.originPos + " - Complete: " + complete + " - Seed: " + this.seed + " - ID: " + this.uuid;
	}
}
