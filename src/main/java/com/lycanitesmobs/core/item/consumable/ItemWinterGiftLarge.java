package com.lycanitesmobs.core.item.consumable;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.item.BaseItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

public class ItemWinterGiftLarge extends BaseItem {

	// ==================================================
	//                   Constructor
	// ==================================================
    public ItemWinterGiftLarge(Item.Properties properties) {
        super(properties);
        this.modInfo = LycanitesMobs.modInfo;
        this.itemName = "wintergiftlarge";
        this.setup();
        ObjectManager.addSound(this.itemName + "_bad", this.modInfo, "item." + this.itemName + ".bad");
    }
    
    
    // ==================================================
 	//                    Item Use
 	// ==================================================
    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
         if(!player.abilities.instabuild) {
             itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
         }
         
         if(!world.isClientSide) {
         	this.open(itemStack, world, player);
         }

        return new ActionResult(ActionResultType.SUCCESS, itemStack);
     }
    
    
    // ==================================================
  	//                       Open
  	// ==================================================
    public void open(ItemStack itemStack, World world, PlayerEntity player) {
        ITextComponent message = new TranslationTextComponent("item.lycanitesmobs." + this.itemName + ".bad");
		player.sendMessage(message, Util.NIL_UUID);
        this.playSound(world, player.blockPosition(), ObjectManager.getSound(this.itemName + "_bad"), SoundCategory.AMBIENT, 5.0F, 1.0F);
		
		// Lots of Random Tricks:
        List<EntityType> entityTypes = ObjectLists.getEntites("winter_tricks");
        if(entityTypes.isEmpty())
            return;
        EntityType entityType = entityTypes.get(player.getRandom().nextInt(entityTypes.size()));
        if(entityType != null) {
            Entity entity = entityType.create(world);
            if (entity != null) {
                entity.moveTo(player.position().x(), player.position().y(), player.position().z(), player.yRot, player.xRot);

                // Themed Names:
                if (entity instanceof BaseCreatureEntity) {
                    BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;
                    entityCreature.addLevel(world.random.nextInt(10));
                    if (entityCreature.creatureInfo.getName().equals("wildkin"))
                        entityCreature.setCustomName(new StringTextComponent("Gooderness"));
                    else if (entityCreature.creatureInfo.getName().equals("jabberwock"))
                        entityCreature.setCustomName(new StringTextComponent("Rudolph"));
                    else if (entityCreature.creatureInfo.getName().equals("ent"))
                        entityCreature.setCustomName(new StringTextComponent("Salty Tree"));
                    else if (entityCreature.creatureInfo.getName().equals("treant"))
                        entityCreature.setCustomName(new StringTextComponent("Salty Tree"));
                    else if (entityCreature.creatureInfo.getName().equals("reaper"))
                        entityCreature.setCustomName(new StringTextComponent("Satan Claws"));
                    else if(entityCreature.creatureInfo.getName().equals("behemoth"))
                        entityCreature.setCustomName(new StringTextComponent("Krampus"));
                }

                world.addFreshEntity(entity);
            }
        }
    }
}
