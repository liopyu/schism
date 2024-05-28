package com.lycanitesmobs.core.container;

import com.lycanitesmobs.core.inventory.CreatureInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class BaseSlot extends Slot {
	
    // ==================================================
  	//                    Constructor
  	// ==================================================
	public BaseSlot(IInventory inventory, int slotIndex, int x, int y) {
		super(inventory, slotIndex, x, y);
	}
	
	
    // ==================================================
  	//                    Validation
  	// ==================================================
	public boolean mayPlace(ItemStack itemStack) {
		if(this.container == null) {
			return true;
		}
		return this.container.canPlaceItem(this.getSlotIndex(), itemStack);
    }
	
	public int getMaxStackSize() {
		if(this.container instanceof CreatureInventory)
			if(((CreatureInventory)this.container).getTypeFromSlot(this.getSlotIndex()) != null)
				return 1;
        return this.container.getMaxStackSize();
    }
}
