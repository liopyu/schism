package com.schism.core.items.actions;

import com.mojang.datafixers.util.Pair;
import com.schism.core.database.Action;
import com.schism.core.database.DataStore;
import com.schism.core.items.ItemDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

abstract public class AbstractItemAction extends Action<ItemDefinition>
{
    protected final boolean onSelfUse;
    protected final boolean onTargetUse;
    protected final boolean onHit;
    protected final boolean onCraft;
    protected final boolean onInventoryTick;
    protected final List<String> onBlockIds;

    public AbstractItemAction(ItemDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.onSelfUse = dataStore.booleanProp("on_self_use");
        this.onTargetUse = dataStore.booleanProp("on_target_use");
        this.onHit = dataStore.booleanProp("on_hit");
        this.onCraft = dataStore.booleanProp("on_craft");
        this.onInventoryTick = dataStore.booleanProp("on_inventory_tick");
        this.onBlockIds = dataStore.listProp("on_block_ids").stream().map(DataStore::stringValue).toList();
    }

    /**
     * Creates a list of item actions from the provided map.
     * @param definition The definition the list is for.
     * @param dataStoreMap A map of keys and nested data stores for populating actions with.
     * @return The newly created item action list.
     */
    public static List<AbstractItemAction> listFromMap(ItemDefinition definition, Map<String, DataStore> dataStoreMap)
    {
        return dataStoreMap.entrySet().stream()
                .map(dataStoreEntry -> AbstractItemAction.create(definition, dataStoreEntry))
                .filter(Objects::nonNull).toList();
    }

    /**
     * Creates a new item action instance for the provided data store entry.
     * @param definition The definition the action is for.
     * @param dataStoreEntry The data store entry to get action properties from.
     * @return The newly created item action.
     */
    public static AbstractItemAction create(ItemDefinition definition, Map.Entry<String, DataStore> dataStoreEntry)
    {
        DataStore dataStore = dataStoreEntry.getValue();
        return switch (dataStore.stringProp("type")) {
            case "altar" -> new AltarItemAction(definition, dataStore);
            case "effect" -> new EffectItemAction(definition, dataStore);
            default -> throw new RuntimeException("Invalid Item Action Type: " + dataStore.stringProp("type"));
        };
    }

    /**
     * Called when the item with this action is used in any way by default. For specific control, other methods can be overridden like onUseSelf, onUseTarget, etc.
     * @param itemStack The item stack being used.
     * @param userEntity The entity using the item stack.
     * @param targetEntity The entity the item stack is being used on.
     * @return An interaction result holder containing the result and a potentially modified item stack.
     */
    public InteractionResultHolder<ItemStack> onUse(ItemStack itemStack, LivingEntity userEntity, Entity targetEntity)
    {
        return InteractionResultHolder.success(itemStack);
    }

    /**
     * Called when the item with this action is used by an entity on itself.
     * @param resultHolder The result holder to check before continuing and to get the item stack from.
     * @param userEntity The entity using the item.
     * @return An interaction result holder containing the result and a potentially modified item stack.
     */
    public InteractionResultHolder<ItemStack> onUseSelf(InteractionResultHolder<ItemStack> resultHolder, LivingEntity userEntity)
    {
        if (!this.onSelfUse || resultHolder.getResult() == InteractionResult.FAIL) {
            return resultHolder;
        }
        return this.onUse(resultHolder.getObject(), userEntity, userEntity);
    }

    /**
     * Called when the item with this action is used by an entity on another entity.
     * @param result The result to check before continuing.
     * @param itemStack The item stack being used.
     * @param userEntity The entity using the item stack.
     * @param targetEntity The entity the item stack is being used on.
     * @return An interaction result.
     */
    public InteractionResult onUseTarget(InteractionResult result, ItemStack itemStack, LivingEntity userEntity, LivingEntity targetEntity)
    {
        if (!this.onTargetUse || result == InteractionResult.FAIL) {
            return result;
        }
        return this.onUse(itemStack, userEntity, targetEntity).getResult();
    }

    /**
     * Called when the item with this action is being used to hit an entity from another entity.
     * @param resultInterruptPair A pair containing a result to check before continuing and whether hitting should be interrupted or not.
     * @param itemStack The item stack being used.
     * @param userEntity The entity using the item stack.
     * @param targetEntity The entity the item stack is being used on.
     * @return A pair containing an interaction result and a boolean, of whether hitting should continue (and potentially deal damage) or not.
     */
    public Pair<InteractionResult, Boolean> onHit(Pair<InteractionResult, Boolean> resultInterruptPair, ItemStack itemStack, LivingEntity userEntity, Entity targetEntity)
    {
        if (!this.onHit || resultInterruptPair.getFirst() == InteractionResult.FAIL) {
            return resultInterruptPair;
        }
        return Pair.of(this.onUse(itemStack, userEntity, targetEntity).getResult(), resultInterruptPair.getSecond());
    }

    /**
     * Called when the item with this action is used by an entity on a block.
     * @param result The result to check before continuing.
     * @param itemStack The item stack being used.
     * @param userEntity The entity using the item stack.
     * @param blockPos The block position the item is being used on.
     * @return An interaction result.
     */
    public InteractionResult onBlocks(InteractionResult result, ItemStack itemStack, LivingEntity userEntity, BlockPos blockPos)
    {
        if (this.onBlockIds.isEmpty() || result == InteractionResult.FAIL || !this.isValidBlock(userEntity, blockPos)) {
            return InteractionResult.PASS;
        }
        return this.onUse(itemStack, userEntity, userEntity).getResult();
    }

    /**
     * Determines if the block found at the provided position in the provided entity's level is valid for using this action on.
     * @param userEntity The entity to get the world from.
     * @param blockPos The position to get the block state and block from.
     * @return True if the block should trigger this action.
     */
    protected boolean isValidBlock(LivingEntity userEntity, BlockPos blockPos)
    {
        Block block = userEntity.getLevel().getBlockState(blockPos).getBlock();
        return DataStore.idInList(block.getRegistryName(), this.onBlockIds);
    }

    /**
     * Called when the item with this action is newly crafted.
     * @param result The result to check before continuing.
     * @param itemStack The item stack that was crafted.
     * @param craftingEntity The entity that crafted the item stack.
     * @return An interaction result.
     */
    public InteractionResult onCraft(InteractionResult result, ItemStack itemStack, LivingEntity craftingEntity)
    {
        if (!this.onCraft || result == InteractionResult.FAIL) {
            return result;
        }
        return this.onUse(itemStack, craftingEntity, craftingEntity).getResult();
    }

    /**
     * Called when the item with this action ticks in an inventory.
     * @param result The result to check before continuing.
     * @param itemStack The item stack that ticked.
     * @param ownerEntity The entity that owns the inventory.
     * @param slotIndex The inventory slot index where the item stack is located.
     * @param selected True if the item stack is currently selected.
     * @return An interaction result.
     */
    public InteractionResult onInventoryTick(InteractionResult result, ItemStack itemStack, LivingEntity ownerEntity, int slotIndex, boolean selected)
    {
        if (!this.onInventoryTick || result == InteractionResult.FAIL) {
            return result;
        }
        return this.onUse(itemStack, ownerEntity, ownerEntity).getResult();
    }
}
