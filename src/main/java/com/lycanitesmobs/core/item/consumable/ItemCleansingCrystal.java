package com.lycanitesmobs.core.item.consumable;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.item.BaseItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemCleansingCrystal extends BaseItem {

	// ==================================================
	//                   Constructor
	// ==================================================
    public ItemCleansingCrystal(Item.Properties properties) {
        super(properties);
        this.modInfo = LycanitesMobs.modInfo;
        this.itemName = "cleansingcrystal";
        this.setup();
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

			if(!world.isClientSide && ObjectManager.getEffect("cleansed") != null) {
				player.addEffect(new EffectInstance(ObjectManager.getEffect("cleansed"), 10 * 20));
			}

			return new ActionResult(ActionResultType.SUCCESS, itemStack);
		}
}
