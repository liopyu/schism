package com.lycanitesmobs.core.tileentity;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.block.BlockSummoningPedestal;
import com.lycanitesmobs.core.config.ConfigExtra;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.CustomItemEntity;
import com.lycanitesmobs.core.entity.PortalEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.network.MessageSummoningPedestalStats;
import com.lycanitesmobs.core.network.MessageSummoningPedestalSummonSet;
import com.lycanitesmobs.core.pets.SummonSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class TileEntitySummoningPedestal extends TileEntityBase {

    public long updateTick = 0;

    // Summoning Properties:
    public PortalEntity summoningPortal;
    public UUID ownerUUID;
    public SummonSet summonSet;
    public int summonAmount = 1;

    // Summoning Stats:
    public int capacityCharge = 100;
    public int capacity = 0;
    public int capacityMax = (this.capacityCharge * 10);
    public int summonProgress = 0;
    public int summonProgressMax = 3 * 60;

    // Inventory:
    public String inventoryName = "";
    public NonNullList<ItemStack> itemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
    public int summoningFuel = 0;
    public int summoningFuelMax;
    public int summoningFuelAmount = 10 * 60 * 20; // 10 minutes per Redstone Dust

    // Summoned Minions:
    public List<BaseCreatureEntity> minions = new ArrayList<>();
    protected String[] loadMinionIDs; // Temporary array for initially populating from NBT data in update.

    // Block:
    protected boolean blockStateSet = false;

    /** Constructor **/
    public TileEntitySummoningPedestal() {
        super();
        this.summoningFuelMax = ConfigExtra.INSTANCE.summoningPedestalRedstoneTime.get();
    }

    @Override
    public TileEntityType<?> getType() {
        return ObjectManager.tileEntityTypes.get(this.getClass());
    }

    /** Can be called by a block when broken to alert this TileEntity that it is being removed. **/
    @Override
    public void setRemoved() {
        if(this.summoningPortal != null && this.summoningPortal.isAlive()) {
            this.summoningPortal.remove();
        }
        for (ItemStack itemStack : this.itemStacks) {
            CustomItemEntity entityItem = new CustomItemEntity(this.getLevel(), this.getBlockPos().getX(), this.getBlockPos().getY() + 0.5D, this.getBlockPos().getZ(), itemStack);
            this.getLevel().addFreshEntity(entityItem);
        }
        this.clearContent();
        super.setRemoved();
    }

    /** The main update called every tick. **/
    @Override
    public void tick() {
        // Client Side Only:
        if(this.getLevel().isClientSide) {

            // Summoning Progress Animation:
            if(this.summoningFuel > 0) {
                if (this.summonProgress >= this.summonProgressMax)
                    this.summonProgress = 0;
                else if (this.summonProgress > 0)
                    this.summonProgress++;
            }

            return;
        }

        // Load Minion IDs:
        if(this.loadMinionIDs != null) {
            int range = 20;
            List nearbyEntities = this.getLevel().getEntitiesOfClass(BaseCreatureEntity.class,
                    new AxisAlignedBB(this.getBlockPos().getX() - range, this.getBlockPos().getY() - range, this.getBlockPos().getZ() - range,
                            this.getBlockPos().getX() + range, this.getBlockPos().getY() + range, this.getBlockPos().getZ() + range));
            Iterator possibleEntities = nearbyEntities.iterator();
            while(possibleEntities.hasNext()) {
                BaseCreatureEntity possibleEntity = (BaseCreatureEntity)possibleEntities.next();
                for(String loadMinionID : this.loadMinionIDs) {
                    UUID uuid = null;
                    try { uuid = UUID.fromString(loadMinionID); } catch (Exception e) {}
                    if(possibleEntity.getUUID().equals(uuid)) {
                        this.minions.add(possibleEntity);
                        break;
                    }
                }
            }
            this.loadMinionIDs = null;
        }

        // Summoning:
        if(this.summonSet != null && this.summonSet.getCreatureInfo() != null) {
			if (this.summonSet.getFollowing()) {
				this.summonSet.following = false;
			}

			// Summoning Portal:
			if (this.summoningPortal == null || !this.summoningPortal.isAlive()) {
				this.summoningPortal = new PortalEntity((EntityType<? extends PortalEntity>) ProjectileManager.getInstance().oldProjectileTypes.get(PortalEntity.class), this.getLevel(), this);
				this.summoningPortal.setProjectileScale(6);
				this.getLevel().addFreshEntity(this.summoningPortal);
			}

			// Update Minions:
			if (this.updateTick % 100 == 0) {
				this.capacity = 0;
				for (BaseCreatureEntity minion : this.minions.toArray(new BaseCreatureEntity[this.minions.size()])) {
					if (minion == null || !minion.isAlive())
						this.minions.remove(minion);
					else {
						this.capacity += (minion.creatureInfo.summonCost * this.capacityCharge);
					}
				}
			}

			// Check Capacity:
			if (this.capacity + this.summonSet.getCreatureInfo().summonCost > this.capacityMax) {
				this.summonProgress = 0;
			}

			// Try To Summon:
			else {
			    if(this.summoningFuel <= 0) {
			        ItemStack fuelStack = this.getItem(0);
			        if(!fuelStack.isEmpty()) {
			            int refuel = this.summoningFuelAmount;
                        if(fuelStack.getItem() == Item.byBlock(Blocks.REDSTONE_BLOCK)) {
							refuel = this.summoningFuelAmount * 9;
                        }
			            fuelStack.split(1);
                        this.summoningFuel = refuel;
                        this.summoningFuelMax = refuel;
                    }
                }

                if (this.summoningFuel > 0) {
                    this.summoningFuel--;

                    // Summon Minions:
                    if (this.summonProgress++ >= this.summonProgressMax) {
                        this.summoningPortal.summonCreatures();
                        this.summonProgress = 0;
                        this.capacity = Math.min(this.capacity + (this.capacityCharge * this.summonSet.getCreatureInfo().summonCost), this.capacityMax);
                    }
                }
            }
		}

		// Block State:
		if (!this.blockStateSet) {
			if (!"".equals(this.getOwnerName()))
				BlockSummoningPedestal.setState(BlockSummoningPedestal.EnumSummoningPedestal.PLAYER, this.getLevel(), this.getBlockPos());
			else
				BlockSummoningPedestal.setState(BlockSummoningPedestal.EnumSummoningPedestal.NONE, this.getLevel(), this.getBlockPos());
			this.blockStateSet = true;
		}

        // Sync To Client:
        if(this.updateTick % 20 == 0) {
            MessageSummoningPedestalStats message = new MessageSummoningPedestalStats(this.capacity, this.summonProgress, this.summoningFuel, this.summoningFuelMax, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ());
            LycanitesMobs.packetHandler.sendToAllAround(message, this.getLevel(), Vector3d.atLowerCornerOf(this.getBlockPos()), 5);
        }

        this.updateTick++;
    }


    // ========================================
    //           Summoning Pedestal
    // ========================================
    /** Sets the owner of this block. **/
    public void setOwner(LivingEntity entity) {
        if(entity instanceof PlayerEntity) {
            PlayerEntity entityPlayer = (PlayerEntity)entity;
            this.ownerUUID = entityPlayer.getUUID();
        }
    }

    /** Returns the name of the owner of this pedestal. **/
	@Nullable
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    /** Returns the name of the owner of this pedestal. **/
    public ITextComponent getOwnerName() {
        if(this.getPlayer() != null) {
            return this.getPlayer().getDisplayName();
        }
        return new StringTextComponent("");
    }

    /** Returns the player that this belongs to or null if owned by no player. **/
	@Nullable
    public PlayerEntity getPlayer() {
        if(this.ownerUUID == null) {
			return null;
		}
        return this.getLevel().getPlayerByUUID(this.ownerUUID);
    }

    /** Returns the class that this is summoning. **/
    @Nullable
    public EntityType getSummonType() {
    	if(this.summonSet == null) {
    		return null;
		}
        return this.summonSet.getCreatureType();
    }

    /** Returns the class that this is summoning. **/
    @Nullable
    public CreatureInfo getCreatureInfo() {
    	if(this.summonSet == null) {
    		return null;
		}
        return this.summonSet.getCreatureInfo();
    }

    /** Sets the Summon Set for this to use. **/
    public void setSummonSet(SummonSet summonSet) {
    	if(this.getPlayer() != null && !summonSet.isUseable()) {
    		return;
		}
        this.summonSet = new SummonSet(null);
        this.summonSet.setSummonType(summonSet.summonType);
        this.summonSet.sitting = summonSet.getSitting();
        this.summonSet.following = false;
        this.summonSet.passive = summonSet.getPassive();
        this.summonSet.aggressive = summonSet.getAggressive();
        this.summonSet.pvp = summonSet.getPVP();
    }


    // ========== Minion Behaviour ==========
    /** Applies the minion behaviour to the summoned player owned minion. **/
    public void applyMinionBehaviour(TameableCreatureEntity minion) {
        if(this.summonSet != null) {
            this.summonSet.applyBehaviour(minion);
            minion.setSubspecies(this.summonSet.subspecies);
            minion.applyVariant(this.summonSet.variant);
        }
        this.minions.add(minion);
        minion.setHome(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), 20);
    }


    // ========================================
    //                Inventory
    // ========================================
    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.itemStacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the stack in the given slot.
     */
    @Override
    public ItemStack getItem(int index) {
        return this.itemStacks.get(index);
    }

    /**
     * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
     */
    @Override
    public ItemStack removeItem(int index, int count) {
        return ItemStackHelper.removeItem(this.itemStacks, index, count);
    }

    /**
     * Removes a stack from the given slot and returns it.
     */
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ItemStackHelper.takeItem(this.itemStacks, index);
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setItem(int index, ItemStack stack) {
        this.itemStacks.set(index, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
     */
    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void startOpen(PlayerEntity player) {

    }

    @Override
    public void stopOpen(PlayerEntity player) {

    }

    /**
     * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
     * guis use Slot.isItemValid
     */
    @Override
    public boolean canPlaceItem(int index, ItemStack itemStack) {
        return itemStack.getItem() == Items.REDSTONE || itemStack.getItem() == Item.byBlock(Blocks.REDSTONE_BLOCK);
    }

    @Override
    public void clearContent() {
        this.itemStacks.clear();
    }

    /**
     * Gets the display name of this tile entity.
     * @return The display name.
     */
    public ITextComponent getName() {
        return new TranslationTextComponent(this.inventoryName);
    }

    /**
     * Returns if this Tile Entity has a custom name.
     * @return True if this Tile Entity has a custom name.
     */
    public boolean hasCustomName() {
        return !"".equals(this.inventoryName);
    }


    // ========================================
    //              Client Events
    // ========================================
    @Override
    public boolean triggerEvent(int eventID, int eventArg) {
        return false;
    }


    // ========================================
    //             Network Packets
    // ========================================
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT syncData = new CompoundNBT();

        // Both:
        if(this.summonSet != null) {
            CompoundNBT summonSetNBT = new CompoundNBT();
            this.summonSet.write(summonSetNBT);
            syncData.put("SummonSet", summonSetNBT);
        }

        // Server to Client:
        if(!this.getLevel().isClientSide && this.getOwnerUUID() != null && this.getOwnerName() != null) {
            syncData.putString("OwnerUUID", this.getOwnerUUID().toString());
            syncData.putString("InventoryName", this.inventoryName);
        }

        return new SUpdateTileEntityPacket(this.getBlockPos(), 1, syncData);
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        if(!this.getLevel().isClientSide)
            return;

        CompoundNBT syncData = packet.getTag();
        if (syncData.contains("OwnerUUID"))
            this.ownerUUID = UUID.fromString(syncData.getString("OwnerUUID"));
        if (syncData.contains("InventoryName"))
            this.inventoryName = syncData.getString("InventoryName");
        if (syncData.contains("SummonSet")) {
            SummonSet summonSet = new SummonSet(null);
            summonSet.read(syncData.getCompound("SummonSet"));
            this.summonSet = summonSet;
        }
    }

    public void sendSummonSetToServer(SummonSet summonSet) {
        LycanitesMobs.packetHandler.sendToServer(new MessageSummoningPedestalSummonSet(summonSet, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ()));
    }


    // ========================================
    //                 NBT Data
    // ========================================
    /** Reads from saved NBT data. **/
    @Override
    public void load(BlockState blockState, CompoundNBT nbtTagCompound) {
        super.load(blockState, nbtTagCompound);

        if(nbtTagCompound.contains("OwnerUUID")) {
            String uuidString = nbtTagCompound.getString("OwnerUUID");
            if(!"".equals(uuidString))
                this.ownerUUID = UUID.fromString(uuidString);
            else
                this.ownerUUID = null;
        }
        else {
            this.ownerUUID = null;
        }

        if(nbtTagCompound.contains("SummonSet")) {
            CompoundNBT summonSetNBT = nbtTagCompound.getCompound("SummonSet");
            SummonSet summonSet = new SummonSet(null);
            summonSet.read(summonSetNBT);
            this.summonSet = summonSet;
        }
        else {
			this.summonSet = null;
		}

        if(nbtTagCompound.contains("MinionIDs")) {
            ListNBT minionIDs = nbtTagCompound.getList("MinionIDs", 10);
            this.loadMinionIDs = new String[minionIDs.size()];
            for(int i = 0; i < minionIDs.size(); i++) {
                CompoundNBT minionID = minionIDs.getCompound(i);
                if(minionID.contains("ID")) {
                    this.loadMinionIDs[i] = minionID.getString("ID");
                }
            }
        }

        // Fuel:
		if(nbtTagCompound.contains("Fuel")) {
			this.summoningFuel = nbtTagCompound.getInt("Fuel");
		}
		if(nbtTagCompound.contains("FuelMax")) {
			this.summoningFuelMax = nbtTagCompound.getInt("FuelMax");
		}
		if(nbtTagCompound.contains("Items")) {
			NonNullList<ItemStack> itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
			ItemStackHelper.loadAllItems(nbtTagCompound, itemStacks); // Reads ItemStack into a List from "Items" tag.

			for(int i = 0; i < itemStacks.size(); ++i) {
				if(i < this.getContainerSize()) {
					ItemStack itemStack = itemStacks.get(i);
					if(itemStack.isEmpty())
						this.setItem(i, ItemStack.EMPTY);
					else {
						this.setItem(i, itemStack);
					}
				}
			}
		}

		super.load(blockState, nbtTagCompound);
    }

    /** Writes to NBT data. **/
    @Override
    public CompoundNBT save(CompoundNBT nbtTagCompound) {
        super.save(nbtTagCompound);

        if(this.ownerUUID == null) {
            nbtTagCompound.putString("OwnerUUID", "");
        }
        else {
            nbtTagCompound.putString("OwnerUUID", this.ownerUUID.toString());
        }

        if(this.summonSet != null) {
            CompoundNBT summonSetNBT = new CompoundNBT();
            this.summonSet.write(summonSetNBT);
            nbtTagCompound.put("SummonSet", summonSetNBT);
        }

        if(this.minions.size() > 0) {
            ListNBT minionIDs = new ListNBT();
            for(LivingEntity minion : this.minions) {
                CompoundNBT minionID = new CompoundNBT();
                minionID.putString("ID", minion.getUUID().toString());
                minionIDs.add(minionID);
            }
            nbtTagCompound.put("MinionIDs", minionIDs);
        }

        // Fuel:
		nbtTagCompound.putInt("Fuel", this.summoningFuel);
		nbtTagCompound.putInt("FuelMax", this.summoningFuelMax);
		ItemStackHelper.saveAllItems(nbtTagCompound, this.itemStacks);

        return nbtTagCompound;
    }
}
