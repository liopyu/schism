package com.lycanitesmobs.core.item.consumable;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.CustomItemEntity;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.item.BaseItem;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

public class ItemHalloweenTreat extends BaseItem {
	
	// ==================================================
	//                   Constructor
	// ==================================================
    public ItemHalloweenTreat(Item.Properties properties) {
		super(properties);
        this.modInfo = LycanitesMobs.modInfo;
        this.itemName = "halloweentreat";
        this.setup();
		ObjectManager.addSound(this.itemName + "_good", this.modInfo, "item." + this.itemName + ".good");
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
         	if(player.getRandom().nextBoolean())
         		this.openGood(itemStack, world, player);
         	else
         		this.openBad(itemStack, world, player);
         }

        return new ActionResult(ActionResultType.SUCCESS, itemStack);
     }
    
    
    // ==================================================
  	//                       Good
  	// ==================================================
    public void openGood(ItemStack itemStack, World world, PlayerEntity player) {
		ITextComponent message = new TranslationTextComponent("item.lycanitesmobs." + this.itemName + ".good");
		player.sendMessage(message, Util.NIL_UUID);
        this.playSound(world, player.position().x(), player.position().y(), player.position().z(), ObjectManager.getSound(this.itemName + "_good"), SoundCategory.AMBIENT, 5.0F, 1.0F);
		
		// Three Random Treats:
		List<ItemStack> dropStacks = ObjectLists.getItems("halloween_treats");
		if(dropStacks == null || dropStacks.isEmpty())
			return;
		ItemStack dropStack = dropStacks.get(player.getRandom().nextInt(dropStacks.size()));
		dropStack.setCount(1 + player.getRandom().nextInt(4));
		CustomItemEntity entityItem = new CustomItemEntity(world, player.position().x(), player.position().y(), player.position().z(), dropStack);
		entityItem.setPickUpDelay(10);
		world.addFreshEntity(entityItem);
    }
    
    
    // ==================================================
  	//                       Bad
  	// ==================================================
    public void openBad(ItemStack itemStack, World world, PlayerEntity player) {
		ITextComponent message = new TranslationTextComponent("item.lycanitesmobs." + this.itemName + ".bad");
		player.sendMessage(message, Util.NIL_UUID);
        this.playSound(world, player.position().x(), player.position().y(), player.position().z(), ObjectManager.getSound(this.itemName + "_bad"), SoundCategory.AMBIENT, 5.0F, 1.0F);
		
		// One Random Trick:
		List<EntityType> entityTypes = ObjectLists.getEntites("halloween_tricks");
		if(entityTypes.isEmpty())
			return;
		EntityType entityType = entityTypes.get(player.getRandom().nextInt(entityTypes.size()));
		if(entityType != null) {
			Entity entity = entityType.create(world);
            if(entity != null) {
	            entity.moveTo(player.position().x(), player.position().y(), player.position().z(), player.yRot, player.xRot);

                // Themed Names:
                if (entity instanceof BaseCreatureEntity) {
                    BaseCreatureEntity entityCreature = (BaseCreatureEntity) entity;
					entityCreature.addLevel(world.random.nextInt(10));
                    if (entityCreature.creatureInfo.getName().equals("ent"))
                        entityCreature.setCustomName(new StringTextComponent("Twisted Ent"));
					else if (entityCreature.creatureInfo.getName().equals("treant"))
						entityCreature.setCustomName(new StringTextComponent("Wicked Treant"));
					else if (entityCreature.creatureInfo.getName().equals("epion"))
						entityCreature.setCustomName(new StringTextComponent("Vampire Bat"));
					else if (entityCreature.creatureInfo.getName().equals("grue"))
						entityCreature.setCustomName(new StringTextComponent("Shadow Clown"));
                }

	            world.addFreshEntity(entity);
            }
		}
    }


    // ==================================================
    //                       Lists
    // ==================================================
    public static void createObjectLists() {
        // Halloween Treats:
        ObjectLists.addItem("halloween_treats", Items.DIAMOND);
        ObjectLists.addItem("halloween_treats", Items.GOLD_INGOT);
        ObjectLists.addItem("halloween_treats", Items.EMERALD);
        ObjectLists.addItem("halloween_treats", Blocks.IRON_BLOCK);
        ObjectLists.addItem("halloween_treats", Items.ENDER_PEARL);
        ObjectLists.addItem("halloween_treats", Items.BLAZE_ROD);
        ObjectLists.addItem("halloween_treats", Items.GLOWSTONE_DUST);
        ObjectLists.addItem("halloween_treats", ObjectManager.getItem("mosspie"));
        ObjectLists.addItem("halloween_treats", ObjectManager.getItem("bulwarkburger"));
        ObjectLists.addItem("halloween_treats", ObjectManager.getItem("paleosalad"));
        ObjectLists.addItem("halloween_treats", ObjectManager.getItem("searingtaco"));
        ObjectLists.addItem("halloween_treats", ObjectManager.getItem("devillasagna"));
        ObjectLists.addFromConfig("halloween_treats");

        // Halloween Mobs:
        ObjectLists.addEntity("halloween_tricks", "behemoth");
        ObjectLists.addEntity("halloween_tricks", "ent");
        ObjectLists.addEntity("halloween_tricks", "treant");
        ObjectLists.addEntity("halloween_tricks", "wraith");
        ObjectLists.addEntity("halloween_tricks", "grue");
        ObjectLists.addEntity("halloween_tricks", "reaper");
        ObjectLists.addEntity("halloween_tricks", "epion");
        ObjectLists.addEntity("halloween_tricks", "tpumpkyn");
    }
}
