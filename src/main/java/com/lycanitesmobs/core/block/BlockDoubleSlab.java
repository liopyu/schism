package com.lycanitesmobs.core.block;

import com.lycanitesmobs.core.info.ModInfo;
import net.minecraft.block.Block;

public class BlockDoubleSlab extends BlockPillar {
    protected String slabName;

	// ==================================================
	//                   Constructor
	// ==================================================
	public BlockDoubleSlab(Block.Properties properties, ModInfo group, String name, String slabName) {
		super(properties, group, name);
        this.slabName = slabName;
	}


    // ==================================================
    //                      Break
    // ==================================================
    //========== Drops ==========
    // TODO Slab Drops
}
