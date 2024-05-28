package com.lycanitesmobs.core.info;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.util.HashMap;
import java.util.Map;


public class ModInfo {
	/** A map containing all groups by their name. **/
	public static Map<String, ModInfo> modInfos = new HashMap<>();

    // ========== Group General ==========
	/** The mod this info belongs to. **/
	public Object mod;

    /** A unique Order ID for this mod, used when all groups need to be displayed in an order. Orders above 99 will be ignored. **/
    public int order;
	
    /** The name of this mod, normally displayed in the config. **/
    public String name;

    /** The filename of this mod, used for assets, config, etc. This should usually match the mod ID. **/
    public String modid;
	
	// ========== Spawn Dimensions ========== TODO Remove
    /** A comma separated list of dimensions that mobs in this mod spawn in. As read from the config **/
    public String dimensionEntries = "";
	
	/** Controls the behaviour of how Dimension IDs are read. If true only listed Dimension IDs are allowed instead of denied. **/
	public boolean dimensionWhitelist = false;

    // ========== Spawn Biomes ========== TODO Remove
    /** The list of biomes that mobs in this mod spawn. As read from the config. Stores biome tags and special tags. **/
    public String biomeEntries = "";
	
	/** The list of biomes that mobs in this mod spawn. This stores the actual biomes not biome tags. **/
	public Biome[] biomes = new Biome[0];

    /** The list of biome types that mobs in this mod can spawn in. **/
    public BiomeDictionary.Type[] biomeTypesAllowed;

    /** The list of biome types that mobs in this mod cannot spawn in. **/
    public BiomeDictionary.Type[] biomeTypesDenied;


    // ==================================================
    //                     Constructor
    // ==================================================
    public ModInfo(Object mod, String name, int order) {
    	this.mod = mod;
        this.name = name;
        this.modid = name.toLowerCase().replace(" ", "");
        this.order = order;

        modInfos.put(this.name, this);
    }


	/**
	 * Returns the display title of this mod.
	 * @return The text to display.
	 */
	public ITextComponent getTitle() {
    	return new TranslationTextComponent(this.modid + ".name");
	}
}
