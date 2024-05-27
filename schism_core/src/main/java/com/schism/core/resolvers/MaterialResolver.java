package com.schism.core.resolvers;

import net.minecraft.world.level.material.Material;

public class MaterialResolver
{
    private static final MaterialResolver INSTANCE = new MaterialResolver();

    /**
     * Gets the singleton MaterialResolver instance.
     * @return The Material Resolver, a utility for getting materials from strings.
     */
    public static MaterialResolver get()
    {
        return INSTANCE;
    }

    /**
     * Converts a string to a Material, the string should be lower_snake_case.
     * @param string The string to get a Material for.
     * @return A Minecraft Material.
     */
    public Material fromString(String string)
    {
        return switch (string) {
            case "air" -> Material.AIR;
            case "portal" -> Material.PORTAL;
            case "cloth_decoration" -> Material.CLOTH_DECORATION;
            case "plant" -> Material.PLANT;
            case "water_plant" -> Material.WATER_PLANT;
            case "water" -> Material.WATER;
            case "lava" -> Material.LAVA;
            case "top_snow" -> Material.TOP_SNOW;
            case "fire" -> Material.FIRE;
            case "decoration" -> Material.DECORATION;
            case "web" -> Material.WEB;
            case "buildable_glass" -> Material.BUILDABLE_GLASS;
            case "clay" -> Material.CLAY;
            case "dirt" -> Material.DIRT;
            case "grass" -> Material.GRASS;
            case "ice_solid" -> Material.ICE_SOLID;
            case "sand" -> Material.SAND;
            case "sponge" -> Material.SPONGE;
            case "wood" -> Material.WOOD;
            case "nether_wood" -> Material.NETHER_WOOD;
            case "bamboo" -> Material.BAMBOO;
            case "wool" -> Material.WOOL;
            case "explosive" -> Material.EXPLOSIVE;
            case "leaves" -> Material.LEAVES;
            case "glass" -> Material.GLASS;
            case "ice" -> Material.ICE;
            case "cactus" -> Material.CACTUS;
            case "metal" -> Material.METAL;
            case "snow" -> Material.SNOW;
            case "heavy_metal" -> Material.HEAVY_METAL;
            case "moss" -> Material.MOSS;
            case "vegetable" -> Material.VEGETABLE;
            case "amethyst" -> Material.AMETHYST;
            case "powder_snow" -> Material.POWDER_SNOW;
            default -> Material.STONE;
        };
    }
}
