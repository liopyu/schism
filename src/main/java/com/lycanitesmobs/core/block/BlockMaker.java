package com.lycanitesmobs.core.block;


import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

public class BlockMaker {
    // ==================================================
    //                Add Dungeon Blocks
    // ==================================================
    /** Creates a set of themed dungeon blocks such as tiles, bricks, pillars, etc as well as a crystal light source. The block name is added to a list that is used to automatically add each recipe at Post Init.
     * @param group The group info to add each block with.
     * @param stoneName The name of the stone block, such as "demon" or "shadow", etc. (stone and crystal are appended).
     * **/
    public static void addDungeonBlocks(ModInfo group, String stoneName) {
        Block.Properties properties = Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(2F, 1200.0F);
        Block.Properties crystalProperties = Block.Properties.of(Material.GLASS).sound(SoundType.GLASS).strength(0.5F, 1200.0F).lightLevel((BlockState blockState) -> { return 15; });

        BlockBase stoneBlock = new BlockBase(properties, group, stoneName + "stone");
        ObjectManager.addBlock(stoneName + "stone", stoneBlock);
        ObjectManager.addBlock(stoneName + "stone_stairs", new BlockStairsCustom(properties, stoneBlock));
        ObjectManager.addBlock(stoneName + "stone_slab", new BlockSlabCustom(properties, stoneBlock));

        BlockBase stoneBrickBlock = new BlockBase(properties, group, stoneName + "stonebrick");
        ObjectManager.addBlock(stoneName + "stonebrick", stoneBrickBlock);
        ObjectManager.addBlock(stoneName + "stonebrick_stairs", new BlockStairsCustom(properties, stoneBrickBlock));
        ObjectManager.addBlock(stoneName + "stonebrick_slab", new BlockSlabCustom(properties, stoneBrickBlock));
        ObjectManager.addBlock(stoneName + "stonebrick_fence", new BlockFenceCustom(properties, stoneBrickBlock));
        ObjectManager.addBlock(stoneName + "stonebrick_wall", new BlockWallCustom(properties, stoneBrickBlock));

        BlockBase stoneTileBlock = new BlockBase(properties, group, stoneName + "stonetile");
        ObjectManager.addBlock(stoneName + "stonetile", stoneTileBlock);
        ObjectManager.addBlock(stoneName + "stonetile_stairs", new BlockStairsCustom(properties, stoneTileBlock));
        ObjectManager.addBlock(stoneName + "stonetile_slab", new BlockSlabCustom(properties, stoneTileBlock));

        ObjectManager.addBlock(stoneName + "stonepolished", new BlockBase(properties, group, stoneName + "stonepolished"));
        ObjectManager.addBlock(stoneName + "stonechiseled", new BlockBase(properties, group, stoneName + "stonechiseled"));
        ObjectManager.addBlock(stoneName + "stonepillar", new BlockPillar(properties, group, stoneName + "stonepillar"));

        ObjectManager.addBlock(stoneName + "crystal", new BlockBase(crystalProperties, group, stoneName + "crystal"));
    }
}
