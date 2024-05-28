package com.lycanitesmobs.core.item.special;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemSoulstoneFilled extends ItemSoulstone {
	public ItemSoulstoneFilled(Item.Properties properties, CreatureType creatureType) {
        super(properties, creatureType);
        this.itemName = creatureType.getSoulstoneName();
        this.setRegistryName(this.modInfo.modid, this.itemName);
    }

    @Override
    public void setup() {}
    
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
    	ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
    	if(playerExt == null) {
            return new ActionResult(ActionResultType.SUCCESS, itemStack);
        }
        if(this.creatureType.tameableCreatures.isEmpty()) {
            LycanitesMobs.logInfo("", "Tried to use a " + this.creatureType.getSoulstoneName() + " but there are no tameable creatures for this type yet.");
            return new ActionResult(ActionResultType.FAIL, itemStack);
        }

        if(!player.getCommandSenderWorld().isClientSide) {
            int creatureIndex = 0;
            if(this.creatureType.tameableCreatures.size() > 1) {
                creatureIndex = player.getRandom().nextInt(this.creatureType.tameableCreatures.size());
            }
            TameableCreatureEntity entity = (TameableCreatureEntity)this.creatureType.tameableCreatures.get(creatureIndex).createEntity(world);
            entity.moveTo(player.getX(), player.getY(), player.getZ(), player.yRot, player.xRot);
            world.addFreshEntity(entity);
            entity.setPlayerOwner(player);
            super.interactLivingEntity(itemStack, player, entity, hand);
        }

        return new ActionResult(ActionResultType.SUCCESS, itemStack);
    }


    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if(this.creatureType.tameableCreatures.isEmpty()) {
            return super.interactLivingEntity(stack, player, entity, hand);
        }
        return ActionResultType.FAIL;
    }
}
