package com.lycanitesmobs.core.item.special;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.info.AltarInfo;
import com.lycanitesmobs.core.item.BaseItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ItemSoulkey extends BaseItem {
    public int variant = 0; // 0 = Standard, 1 = Diamond, 2 = Emerald

	// ==================================================
	//                   Constructor
	// ==================================================
    public ItemSoulkey(Item.Properties properties, String itemName, int variant) {
        super(properties);
        this.itemName = itemName;
        this.variant = variant;
        this.setup();
    }
    
    
	// ==================================================
	//                       Use
	// ==================================================
    @Override
    public ActionResultType useOn(ItemUseContext context) {
        World world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        PlayerEntity player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();

        if(!AltarInfo.checkAltarsEnabled() && !player.getCommandSenderWorld().isClientSide) {
            ITextComponent message = new TranslationTextComponent("message.soulkey.disabled");
            player.sendMessage(message, Util.NIL_UUID);
            return ActionResultType.FAIL;
        }

        // Get Possible Altars:
        List<AltarInfo> possibleAltars = new ArrayList<>();
        if(AltarInfo.altars.isEmpty())
            LycanitesMobs.logWarning("", "No altars have been registered, Soulkeys will not work at all.");
        for(AltarInfo altarInfo : AltarInfo.altars.values()) {
            if(altarInfo.checkBlockEvent(player, world, pos) && altarInfo.quickCheck(player, world, pos)) {
                possibleAltars.add(altarInfo);
            }
        }
        if(possibleAltars.isEmpty()) {
            ITextComponent message = new TranslationTextComponent("message.soulkey.none");
            player.sendMessage(message, Util.NIL_UUID);
            return ActionResultType.FAIL;
        }

        // Activate First Valid Altar:
        for(AltarInfo altarInfo : possibleAltars) {
            if(altarInfo.fullCheck(player, world, pos)) {

                // Valid Altar:
                if(!player.getCommandSenderWorld().isClientSide) {
                    if(!altarInfo.activate(player, world, pos, this.variant)) {
                        ITextComponent message = new TranslationTextComponent("message.soulkey.badlocation");
                        player.sendMessage(message, Util.NIL_UUID);
                        return ActionResultType.FAIL;
                    }
                    if (!player.abilities.instabuild)
                        itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
                    if (itemStack.getCount() <= 0)
                        player.inventory.setItem(player.inventory.selected, ItemStack.EMPTY);
                    ITextComponent message = new TranslationTextComponent("message.soulkey.active");
                    player.sendMessage(message, Util.NIL_UUID);
                }
                return ActionResultType.SUCCESS;
            }
        }
        if(!player.getCommandSenderWorld().isClientSide) {
            ITextComponent message = new TranslationTextComponent("message.soulkey.invalid");
            player.sendMessage(message, Util.NIL_UUID);
        }

        return ActionResultType.FAIL;
    }
}
