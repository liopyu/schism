package com.lycanitesmobs.core.dungeon.instance;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.config.ConfigDungeons;
import com.lycanitesmobs.core.dungeon.definition.DungeonSector;
import com.lycanitesmobs.core.worldgen.DungeonFeature;
import net.minecraft.util.Direction;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class DungeonLayout {
	/** A Dungeon Layout is a procedurally generated collection of connecting Sectors with Structures and a Theme as well as other properties. **/

	/** The Dungeon Instance that this Layout belongs to. **/
	public DungeonInstance dungeonInstance;

	/** A list of generated Sector Instances. **/
	public List<SectorInstance> sectors = new ArrayList<>();

	/** A map that stores all sectors in each ChunkPos. Used for finding nearby sectors when collision detecting and when world generating. **/
	public Map<ChunkPos, List<SectorInstance>> sectorChunkMap = new HashMap<>();

	/** The starting connector to use, this should have no parent Sector Instance assigned. **/
	public SectorConnector originConnector;

	/** A list of open Sector Connectors for stemming from. **/
	List<SectorConnector> openConnectors = new ArrayList<>();


	/**
	 * Constructor
	 * @param dungeonInstance The Dungeon Instance that this Layout will belong to.
	 */
	public DungeonLayout(DungeonInstance dungeonInstance) {
		this.dungeonInstance = dungeonInstance;
	}


	/**
	 * Generates (or regenerates) this entire Dungeon Layout.
	 */
	public void generate(Random random) {
		this.sectors.clear();
		this.sectorChunkMap.clear();
		this.openConnectors.clear();

		// Start:
		SectorInstance entranceSector = this.start(random);
		LycanitesMobs.logDebug("Dungeon", "Created Entrance Sector: " + entranceSector);
		this.openConnectors.clear();

		// Underground Levels:
		SectorInstance exitSector = entranceSector;
		int level = 1;
		boolean onLastLevel = false;
		while(!onLastLevel && level <= 10) {
			int sectorCount = this.dungeonInstance.schematic.getRandomSectorCount(random);
			LycanitesMobs.logDebug("Dungeon", "Starting Underground Level -" + level + " - Sector Count: " + sectorCount);

			// Snake:
			int snakeCount = Math.round((float)sectorCount * 0.4f);
			exitSector = this.snake(random, exitSector, Math.max(3, snakeCount));
			LycanitesMobs.logDebug("Dungeon", "Snake Sectors: " + snakeCount + " - From Sector: " + exitSector);
			if(exitSector == null) {
				onLastLevel = true;
			}

			// Stem:
			sectorCount -= snakeCount;
			LycanitesMobs.logDebug("Dungeon", "Stem Sectors: " + sectorCount + " Open Snake Sectors: " + this.openConnectors.size());
			while(sectorCount > 0) {
				int stemmedSectors = this.stem(random, sectorCount).size();
				if(stemmedSectors == 0) {
					LycanitesMobs.logWarning("Dungeon", "Unable to stem any sectors.");
					break;
				}
				sectorCount -= stemmedSectors;
			}

			this.openConnectors.clear();
			LycanitesMobs.logDebug("Dungeon", "Completed Underground Level -" + level + (onLastLevel ? " (Final)" : ""));
			level++;
		}

		// Tower Levels:
		if(random.nextDouble() <= ConfigDungeons.INSTANCE.towerChance.get()) {
			exitSector = entranceSector;
			level = 1;
			while (level <= 10) {
				LycanitesMobs.logDebug("Dungeon", "Starting Tower Level " + level);

				// Tower:
				exitSector = this.tower(random, exitSector);
				LycanitesMobs.logDebug("Dungeon", "Tower Sector: " + exitSector);
				if (exitSector == null) {
					break;
				}

				// Ledge:
				int ledgeCount = random.nextInt(exitSector.getOpenConnectors(null).size()) + 1;
				LycanitesMobs.logDebug("Dungeon", "Ledges: " + ledgeCount);
				this.ledge(random, exitSector, ledgeCount);

				this.openConnectors.clear();
				LycanitesMobs.logDebug("Dungeon", "Completed Tower Level " + level);
				level++;
			}
		}

		LycanitesMobs.logDebug("Dungeon", "Dungeon Instance Generation Complete!");
	}


	/**
	 * Startup Phase - Starts generation by creating an entrance.
	 * @param random The instance of Random to use.
	 * @return The generated entrance sector.
	 */
	public SectorInstance start(Random random) {
		this.originConnector = new SectorConnector(this.dungeonInstance.originPos, null, 0, Direction.from2DDataValue(random.nextInt(100)));
		DungeonSector entranceDungeonSector = this.dungeonInstance.schematic.getRandomSector("entrance", random);
		SectorInstance entranceSector = new SectorInstance(this, entranceDungeonSector, random);
		entranceSector.connect(this.originConnector);
		entranceSector.init(random);
		this.addSectorInstance(entranceSector);

		DungeonSector dungeonSector = this.dungeonInstance.schematic.getRandomSector("stairs", random);
		SectorInstance sectorInstance = new SectorInstance(this, dungeonSector, random);
		sectorInstance.connect(entranceSector.getRandomConnector(random, sectorInstance));
		sectorInstance.init(random);
		this.addSectorInstance(sectorInstance);

		return sectorInstance;
	}


	/**
	 * First Underground Phase - Generates a linear set of sectors from the starting sector to a finishing sector.
	 * @param random The instance of Random to use.
	 * @param startSector The Sector Instance to snake from.
	 * @param length How many sectors to generate.
	 * @return The end sector or null if it is the finish sector.
	 */
	public SectorInstance snake(Random random, SectorInstance startSector, int length) {
		SectorInstance lastSector = startSector;
		for(int i = 0; i < length; i++) {
			String nextType = this.dungeonInstance.schematic.getNextConnectingSector(lastSector.dungeonSector.type, random);
			if(i == length - 2) {
				nextType = "bossRoom";
			}
			else if(i == length - 1) {
				nextType = "stairs";
			}
			DungeonSector dungeonSector = this.dungeonInstance.schematic.getRandomSector(nextType, random);
			SectorInstance sectorInstance = new SectorInstance(this, dungeonSector, random);
			SectorConnector sectorConnector = lastSector.getRandomConnector(random, sectorInstance);
			sectorInstance.connect(sectorConnector);

			// Finish Sector:
			if("stairs".equals(nextType) && sectorInstance.getOccupiedBoundsMin().getY() <= 1) {
				nextType = "finish";
				dungeonSector = this.dungeonInstance.schematic.getRandomSector(nextType, random);
				sectorInstance = new SectorInstance(this, dungeonSector, random);
				sectorInstance.connect(sectorConnector);
			}

			sectorInstance.init(random);
			this.addSectorInstance(sectorInstance);
			lastSector = sectorInstance;
			if("finish".equalsIgnoreCase(nextType)) {
				lastSector = null;
			}
		}

		return lastSector;
	}


	/**
	 * Third Phase - Generates sectors on any open connectors.
	 * @param random The instance of Random to use.
	 * @param maxSectors The maximum amount of sectors to stem.
	 * @return A list of generated sectors.
	 */
	public List<SectorInstance> stem(Random random, int maxSectors) {
		List<SectorInstance> generatedSectors = new ArrayList<>();
		int stemmedSectors = 0;
		for(SectorConnector connector : this.openConnectors.toArray(new SectorConnector[this.openConnectors.size()])) {
			// Get New Sector:
			String nextType = this.dungeonInstance.schematic.getNextConnectingSector(connector.parentSector.dungeonSector.type, random);
			DungeonSector dungeonSector = this.dungeonInstance.schematic.getRandomSector(nextType, random);
			SectorInstance sectorInstance = new SectorInstance(this, dungeonSector, random);

			// Try To Connect New Sector:
			if(!connector.canConnect(this, sectorInstance)) {
				continue;
			}
			sectorInstance.connect(connector);
			sectorInstance.init(random);
			this.addSectorInstance(sectorInstance);
			generatedSectors.add(sectorInstance);
			if(this.openConnectors.contains(connector)) {
				this.openConnectors.remove(connector);
			}
			connector.closed = true;
			if(++stemmedSectors >= maxSectors) {
				break;
			}
		}

		return generatedSectors;
	}


	/**
	 * First Tower Phase - Creates a tower sector with stairs.
	 * @param random The instance of Random to use.
	 * @param startSector The sector to build on top of.
	 * @return The generated tower sector.
	 */
	public SectorInstance tower(Random random, SectorInstance startSector) {
		DungeonSector dungeonSector = this.dungeonInstance.schematic.getRandomSector("tower", random);
		SectorInstance sectorInstance = new SectorInstance(this, dungeonSector, random);
		SectorConnector sectorConnector = startSector.getRandomConnector(random, sectorInstance);
		sectorInstance.connect(sectorConnector);

		if(sectorInstance.getOccupiedBoundsMax().getY() > 255) {
			return null;
		}

		sectorInstance.init(random);
		this.addSectorInstance(sectorInstance);
		return sectorInstance;
	}


	/**
	 * Second Tower Phase - Creates ledges out from a tower sector consisting of a corridor and room sector.
	 * @param random The instance of Random to use.
	 * @param startSector The sector to build out of.
	 * @param ledgeCount How many ledges to make, there must be enough open connectors to finish.
	 * @return The generated tower sector.
	 */
	public SectorInstance ledge(Random random, SectorInstance startSector, int ledgeCount) {
		SectorInstance lastSectorInstance = null;
		int bossIndex = 0;
		if(ledgeCount > 0) {
			bossIndex = random.nextInt(ledgeCount);
		}
		for(int i = 0; i < ledgeCount; i++) {
			DungeonSector corridorSector = this.dungeonInstance.schematic.getRandomSector("corridor", random);
			SectorInstance corridorInstance = new SectorInstance(this, corridorSector, random);
			SectorConnector corridorConnector = startSector.getRandomConnector(random, corridorInstance);
			if(corridorConnector == null) {
				break;
			}
			corridorInstance.connect(corridorConnector);
			corridorInstance.init(random);
			this.addSectorInstance(corridorInstance);

			String roomType = "room";
			if(bossIndex == i) {
				roomType = "bossRoom";
			}
			DungeonSector roomSector = this.dungeonInstance.schematic.getRandomSector(roomType, random);
			SectorInstance roomInstance = new SectorInstance(this, roomSector, random);
			SectorConnector roomConnector = corridorInstance.getRandomConnector(random, roomInstance);
			roomInstance.connect(roomConnector);
			roomInstance.init(random);
			this.addSectorInstance(roomInstance);

			lastSectorInstance = roomInstance;
		}

		return lastSectorInstance;
	}


	/**
	 * Adds a Sector Instance to this Dungeon Layout for reference. Also adds all open Sector Connectors that it has.
	 * @param sectorInstance The Sector Instance to add.
	 */
	public void addSectorInstance(SectorInstance sectorInstance) {
		this.sectors.add(sectorInstance);

		// Update Dungeon Bounds:
		ChunkPos minChunkPos = new ChunkPos(sectorInstance.getOccupiedBoundsMin());
		if(this.dungeonInstance.chunkMin == null) {
			this.dungeonInstance.chunkMin = minChunkPos;
		}
		else {
			if (minChunkPos.x < this.dungeonInstance.chunkMin.x) {
				this.dungeonInstance.chunkMin = new ChunkPos(minChunkPos.x, this.dungeonInstance.chunkMin.z);
			}
			if (minChunkPos.z < this.dungeonInstance.chunkMin.z) {
				this.dungeonInstance.chunkMin = new ChunkPos(this.dungeonInstance.chunkMin.x, minChunkPos.z);
			}
		}
		ChunkPos maxChunkPos = new ChunkPos(sectorInstance.getOccupiedBoundsMax());
		if(this.dungeonInstance.chunkMax == null) {
			this.dungeonInstance.chunkMax = maxChunkPos;
		}
		else {
			if (maxChunkPos.x > this.dungeonInstance.chunkMax.x) {
				this.dungeonInstance.chunkMax = new ChunkPos(maxChunkPos.x, this.dungeonInstance.chunkMax.z);
			}
			if (maxChunkPos.z > this.dungeonInstance.chunkMax.z) {
				this.dungeonInstance.chunkMax = new ChunkPos(this.dungeonInstance.chunkMax.x, maxChunkPos.z);
			}
		}

		// Add To Chunk Map:
		for(ChunkPos chunkPos : sectorInstance.getChunkPositions()) {
			if(!this.sectorChunkMap.containsKey(chunkPos)) {
				this.sectorChunkMap.put(chunkPos, new ArrayList<>());
			}
			this.sectorChunkMap.get(chunkPos).add(sectorInstance);
		}

		// Add Connectors:
		if(!"stairs".equalsIgnoreCase(sectorInstance.dungeonSector.type)) {
			this.openConnectors.addAll(sectorInstance.getOpenConnectors(null));
		}
	}
}
