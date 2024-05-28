package com.lycanitesmobs.core.dispenser;

import com.lycanitesmobs.core.item.ItemCustomSpawnEgg;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class SpawnEggDispenseBehaviour extends DefaultDispenseItemBehavior {
    @Override
    public ItemStack execute(IBlockSource blockSource, ItemStack itemStack) {
        if(!(itemStack.getItem() instanceof ItemCustomSpawnEgg))
            return itemStack;

        ItemCustomSpawnEgg itemCustomSpawnEgg = (ItemCustomSpawnEgg)itemStack.getItem();
        IPosition position = DispenserBlock.getDispensePosition(blockSource);
        Entity entity = itemCustomSpawnEgg.spawnCreature(blockSource.getLevel(), itemStack, position.x(), position.y(), position.z());
        if (itemStack.hasCustomHoverName())
            entity.setCustomName(itemStack.getHoverName());
        
        itemStack.split(1);
        return itemStack;
    }
}
