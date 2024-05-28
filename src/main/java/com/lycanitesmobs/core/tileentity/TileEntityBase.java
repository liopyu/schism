package com.lycanitesmobs.core.tileentity;

import com.lycanitesmobs.LycanitesMobs;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

import javax.annotation.Nullable;

public abstract class TileEntityBase extends TileEntity implements ITickableTileEntity, ISidedInventory {
    /**
     * Constructor
     */
    public TileEntityBase() {
        super(TileEntityType.CHEST);
    }

    /**
     * Gets the Tile Entity Type used by this Tile Entity. Should be overridden.
     * @return The Tile Entity Type.
     */
    public TileEntityType<?> getType() {
        return super.getType();
    }

    /**
     * The main update called every tick.
     */
    @Override
    public void tick() {}

    @Override
    public boolean stillValid(PlayerEntity player) {
        if (this.isRemoved()) {
            return false;
        }
        return this.getBlockPos().distSqr(player.blockPosition()) < 16F;
    }

    /**
     * Removes this Tile Entity.
     */
    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    /**
     * Called when receiving an event from a client, used for opening GUIs, etc.
     * @param eventID The ID of the event.
     * @param eventArg The argument ID of the event.
     * @return
     */
    @Override
    public boolean triggerEvent(int eventID, int eventArg) {
        return false;
    }

    /**
     * Gets the update packet for this Tile Entity.
     * @return The update packet to use or null if not needed.
     */
    @Override
    @Nullable
    public SUpdateTileEntityPacket getUpdatePacket() {
        return super.getUpdatePacket();
    }

    /**
     * Called when this Tile Entity receives a Data Packet.
     * @param net The Network Manager.
     * @param pkt The packet received.
     */
    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);
    }

    /**
     * Called when a GUI opened by this Tile Entity has a button pressed.
     * @param buttonId The ID of the button pressed.
     */
    public void onGuiButton(int buttonId) {}

    /**
     * Reads from saved NBT data.
     * @param nbtTagCompound The NBT to read from.
     */
    @Override
    public void load(BlockState blockState, CompoundNBT nbtTagCompound) {
        super.load(blockState, nbtTagCompound);
    }

    /**
     * Writes to NBT data.
     * @param nbtTagCompound The NBT data to write to.
     * @return The written to NBT data.
     */
    @Override
    public CompoundNBT save(CompoundNBT nbtTagCompound) {
        return super.save(nbtTagCompound);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return new int[] {};
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStackIn, @Nullable Direction direction) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false;
    }
}
