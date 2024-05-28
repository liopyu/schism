package com.lycanitesmobs.core.info.altar;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.AltarInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AltarInfoPhosphorescentChupacabra extends AltarInfo {

    // ==================================================
    //                    Constructor
    // ==================================================
    public AltarInfoPhosphorescentChupacabra(String name) {
        super(name);
    }


    // ==================================================
    //                     Checking
    // ==================================================
    /** Called first when checking for a valid altar, this should be fairly lightweight such as just checking if the first block checked is valid, a more in depth check if then done after. **/
    @Override
    public boolean quickCheck(Entity entity, World world, BlockPos pos) {
        if(world.getBlockState(pos).getBlock() != Blocks.DIAMOND_BLOCK)
            return false;
        return true;
    }

    /** Called if the QuickCheck() is passed, this should check the entire altar structure and if true is returned, the altar will activate. **/
    @Override
    public boolean fullCheck(Entity entity, World world, BlockPos pos) {
        if(!this.quickCheck(entity, world, pos))
            return false;

        Block bodyBlock = Blocks.OBSIDIAN;

        // Upper:
        if(world.getBlockState(pos.offset(0, 1, 0)).getBlock() != bodyBlock)
            return false;

        // Lower:
        if(world.getBlockState(pos.offset(0, -1, 0)).getBlock() != bodyBlock)
            return false;

        // X Rotation:
        if(this.checkRotationX(bodyBlock, entity, world, pos))
            return true;

        // Z Rotation:
        return this.checkRotationZ(bodyBlock, entity, world, pos);
    }


    private boolean checkRotationX(Block bodyBlock, Entity entity, World world, BlockPos pos) {
        // Left:
        if(world.getBlockState(pos.offset(-1, 0, 0)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(-2, 0, 0)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(-1, -1, 0)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(-1, -1, 0)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(-2, -2, 0)).getBlock() != bodyBlock)
            return false;

        // Right:
        if(world.getBlockState(pos.offset(1, 0, 0)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(2, 0, 0)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(1, -1, 0)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(1, -1, 0)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(2, -2, 0)).getBlock() != bodyBlock)
            return false;

        return true;
    }


    private boolean checkRotationZ(Block bodyBlock, Entity entity, World world, BlockPos pos) {
        // Left:
        if(world.getBlockState(pos.offset(0, 0, -1)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(0, 0, -2)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(0, -1, -1)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(0, -2, -1)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(0, -2, -2)).getBlock() != bodyBlock)
            return false;

        // Right:
        if(world.getBlockState(pos.offset(0, 0, 1)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(0, 0, 2)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(0, -1, 1)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(0, -2, 1)).getBlock() != bodyBlock)
            return false;
        if(world.getBlockState(pos.offset(0, -2, 2)).getBlock() != bodyBlock)
            return false;

        return true;
    }


    // ==================================================
    //                     Activate
    // ==================================================
    /** Called when this Altar should activate. This will typically destroy the Altar and summon a rare mob or activate an event such as a boss event. If false is returned then the activation did not work, this is the place to check for things like dimensions. **/
    @Override
    public boolean activate(Entity entity, World world, BlockPos pos, int variant) {
        if(world.isClientSide)
            return true;

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        // Create Mini Boss:
        BaseCreatureEntity entityCreature = (BaseCreatureEntity) CreatureManager.getInstance().getCreature("chupacabra").createEntity(world);
        if(checkDimensions && !entityCreature.isNativeDimension(world))
            return false;

        // Destroy Altar:
        int size = 4;
        for (int xTarget = x - size; xTarget <= x + size; xTarget++) {
            for (int zTarget = z - size; zTarget <= z + size; zTarget++) {
                for (int yTarget = y - size; yTarget <= y + size; yTarget++) {
                    if (y > 0)
                        world.removeBlock(new BlockPos(xTarget, yTarget, zTarget), true);
                }
            }
        }

        // Offset:
        if(entity != null)
            pos = this.getFacingPosition(pos, 10, entity.yRot);

        // Clear Spawn Area:
        for (int xTarget = x - size; xTarget <= x + size; xTarget++) {
            for (int zTarget = z - size; zTarget <= z + size; zTarget++) {
                for (int yTarget = y - size; yTarget <= y + size; yTarget++) {
                    if (y > 0)
                        world.removeBlock(new BlockPos(xTarget, yTarget, zTarget), true);
                }
            }
        }

        // Spawn Mini Boss:
        entityCreature.altarSummoned = true;
        entityCreature.forceBossHealthBar = true;
        entityCreature.applyVariant(3);
        entityCreature.moveTo(x, y - 2, z, 0, 0);
        world.addFreshEntity(entityCreature);
        entityCreature.destroyArea(x, y, z, 10000, false, 2);

        return true;
    }
}
