package com.lycanitesmobs.core.container;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;

public class SummoningPedestalContainer extends BaseContainer {
    public static final ContainerType<SummoningPedestalContainer> TYPE = (ContainerType<SummoningPedestalContainer>)IForgeContainerType.create(SummoningPedestalContainer::new).setRegistryName(LycanitesMobs.MODID, "summoning_pedestal");
    public TileEntitySummoningPedestal summoningPedestal;

    /**
     * Client Constructor
     * @param windowId The window id for the gui screen to use.
     * @param playerInventory The accessing player's inventory.
     * @param extraData A packet sent from the server to create the Container from.
     */
    public SummoningPedestalContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData) {
        this(windowId, playerInventory, (TileEntitySummoningPedestal)playerInventory.player.getCommandSenderWorld().getBlockEntity(BlockPos.of(extraData.readLong())));
    }

    /**
     * Main Constructor
     * @param windowId
     * @param playerInventory
     * @param summoningPedestal
     */
    public SummoningPedestalContainer(int windowId, PlayerInventory playerInventory, TileEntitySummoningPedestal summoningPedestal) {
        super(TYPE, windowId);
        this.summoningPedestal = summoningPedestal;
        this.inventoryStart = this.slots.size();

        // Player Inventory
        this.playerInventoryStart = this.slots.size();
        this.addSlotGrid(playerInventory, 8, 171, 1, 0, 8);

        // Pedestal Inventory
        int slots = 0;
        if(summoningPedestal.getContainerSize() > 0) {
            this.addSlot(new BaseSlot(summoningPedestal, slots++, 93, 43));
        }
        this.inventoryFinish = this.inventoryStart + slots;
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        if(this.summoningPedestal == null || !this.summoningPedestal.stillValid(player))
            return false;
        return player == this.summoningPedestal.getPlayer();
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int slotID) {
        return ItemStack.EMPTY;
    }
}
