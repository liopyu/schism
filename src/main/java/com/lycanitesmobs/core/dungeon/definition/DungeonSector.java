package com.lycanitesmobs.core.dungeon.definition;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.helpers.JSONHelper;
import net.minecraft.util.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DungeonSector {
    /** Dungeon sectors can be corridors, rooms, entrances, etc that make up a dungeon. **/

	/** The unique name of this sector. Required. **/
	public String name = "";

    /** The type of sector that this is. Can be: room (default), corridor, stairs or entrance. **/
    public String type = "room";

	/** The weight to use for this sector when selecting randomly. **/
	public int weight = 8;

	/** If true, this sector will use a new theme selected from the dungeon. **/
	public boolean changeTheme = false;

	/** Defines the minimum size of this sector. **/
	public Vector3i sizeMin = new Vector3i(8, 8, 8);

	/** Defines the maximum size of this sector. **/
	public Vector3i sizeMax = new Vector3i(10, 10, 10);

	/** Sets a padding around this sector to count towards block occupation. This is will be automatically increased by negative segment layers as needed and is at least one for entrance carving. **/
	public Vector3i padding = new Vector3i(1, 0, 1);

    /** A list of Structures used by this sector. **/
    public List<String> structures = new ArrayList<>();

	/** The floor segment of this sector. **/
	public SectorSegment floor;

	/** The wall segment of this sector. **/
	public SectorSegment wall;

	/** The ceiling segment of this sector. **/
	public SectorSegment ceiling;


    /** Loads this Dungeon Sector from the provided JSON data. **/
	public void loadFromJSON(JsonObject json) {
		this.name = json.get("name").getAsString().toLowerCase();

		if(json.has("type"))
			this.type = json.get("type").getAsString().toLowerCase();

		if(json.has("changeTheme"))
			this.changeTheme = json.get("changeTheme").getAsBoolean();

		this.sizeMin = JSONHelper.getVector3i(json, "sizeMin");

		this.sizeMax = JSONHelper.getVector3i(json, "sizeMax");

		if(json.has("weight"))
			this.weight = json.get("weight").getAsInt();

		if(json.has("structures")) {
			for(JsonElement jsonElement : json.get("structures").getAsJsonArray()) {
				String structureName = jsonElement.getAsString();
				if(!this.structures.contains(structureName))
					this.structures.add(structureName);
			}
		}

		if(json.has("floor")) {
			this.floor = new SectorSegment();
			this.floor.loadFromJSON(json.get("floor"));
		}

		if(json.has("wall")) {
			this.wall = new SectorSegment();
			this.wall.loadFromJSON(json.get("wall"));
		}

		if(json.has("ceiling")) {
			this.ceiling = new SectorSegment();
			this.ceiling.loadFromJSON(json.get("ceiling"));
		}

		this.padding = JSONHelper.getVector3i(json, "padding");
		if(this.padding.getX() <= 0) {
			this.padding = new Vector3i(this.wall.padding, this.padding.getY(), this.padding.getZ());
		}
		if(this.padding.getZ() <= 0) {
			this.padding = new Vector3i(this.padding.getX(), this.padding.getY(), this.wall.padding);
		}
	}


	/**
	 * Returns a random room size to use based on the size min max values.
	 * @param random The instance of Random to use.
	 * @return A random size to use.
	 */
	public Vector3i getRandomSize(Random random) {
		int x = this.sizeMin.getX();
		if(this.sizeMax.getX() > x)
			x += random.nextInt(this.sizeMax.getX() - x + 1);
		int y = this.sizeMin.getY();
		if(this.sizeMax.getY() > y)
			y += random.nextInt(this.sizeMax.getY() - y + 1);
		int z = this.sizeMin.getZ();
		if(this.sizeMax.getZ() > z)
			z += random.nextInt(this.sizeMax.getZ() - z + 1);
		return new Vector3i(x, y, z);
	}
}
