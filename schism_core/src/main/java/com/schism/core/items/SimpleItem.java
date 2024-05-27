package com.schism.core.items;

import com.mojang.datafixers.util.Pair;
import com.schism.core.database.IHasDefinition;
import com.schism.core.items.actions.AbstractItemAction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SimpleItem extends Item implements IHasDefinition<ItemDefinition>
{
    protected final ItemDefinition definition;

    public SimpleItem(ItemDefinition definition, Item.Properties properties)
    {
        super(properties);
        this.definition = definition;
        this.setRegistryName(definition.resourceLocation());
    }

    @Override
    public ItemDefinition definition()
    {
        return this.definition;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(itemStack, level, list, tooltipFlag);
        list.add(Component.translatable(this.getDescriptionId() + ".description").withStyle(ChatFormatting.GREEN));
    }

    @Override
    public boolean hasContainerItem(ItemStack itemStack)
    {
        return this.definition().containerItem() != null;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack)
    {
        if (!this.hasContainerItem(itemStack)) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(this.definition().containerItem(), 1);
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType)
    {
        if (this.definition().fuelTime() > 0) {
            return this.definition().fuelTime();
        }
        return -1;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack itemStack)
    {
        return this.definition().useAnimation();
    }

    @Override
    public int getUseDuration(ItemStack itemStack)
    {
        if (this.definition().useContinuous()) {
            return 72000;
        }
        return this.definition().useTicks() > 0 ? this.definition().useTicks() : super.getUseDuration(itemStack);
    }

    /**
     * Called when the item is used. Overridden to handle foods or use over time.
     * @param level The level.
     * @param player The using player.
     * @param hand The hand that the item is used in.
     * @return The use result.
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player.isUsingItem()) {
            return result;
        }
        if (this.definition().useTicks() > 0) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }
        this.actionsOnUse(player.getItemInHand(hand), player);
        return result;
    }

    /**
     * Called each tick that an item is being used by an entity (not for instant use). Overridden to handle continuous use effects.
     * @param itemStack The item stack.
     * @param livingEntity The using entity.
     * @param ticksRemaining The number of ticks that the item can used for remaining.
     */
    @Override
    public void onUsingTick(ItemStack itemStack, LivingEntity livingEntity, int ticksRemaining)
    {
        super.onUsingTick(itemStack, livingEntity, ticksRemaining);
        int ticks = this.definition().useTicks() - ticksRemaining;
        if (this.definition().useContinuous() && ticks % this.definition().useTicks() == 0) {
            this.actionsOnUse(itemStack, livingEntity);
        }
    }

    /**
     * Called when the item has finished being used. Overridden to handle action use effects.
     * @param itemStack The item stack.
     * @param level The level.
     * @param livingEntity The using entity.
     * @return A potentially modified item stack.
     */
    @Override
    public @NotNull ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity livingEntity) {
        itemStack = super.finishUsingItem(itemStack, level, livingEntity);
        itemStack = this.actionsOnUse(itemStack, livingEntity).getObject();
        return itemStack;
    }

    /**
     * Applies the provided result which includes decreasing stack size, awarding stats, etc.
     * @param result The result to apply.
     * @param itemStack The item stack to modify.
     * @param livingEntity The using entity.
     */
    public void applyResult(InteractionResult result, ItemStack itemStack, LivingEntity livingEntity)
    {
        if (result.shouldAwardStats() && livingEntity instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (livingEntity.getLevel().isClientSide()) {
            return;
        }
        if (result.consumesAction()) {
            if (!(livingEntity instanceof Player player) || !player.getAbilities().instabuild) {
                if (this.getMaxDamage(itemStack) > 0) {
                    itemStack.setDamageValue(itemStack.getDamageValue() - this.definition().useCost());
                } else {
                    itemStack.shrink(this.definition().useCost());
                }
            }
            if (this.definition().cooldown() > 0 && livingEntity instanceof Player player) {
                player.getCooldowns().addCooldown(this, this.definition().cooldown());
            }
        }
    }

    /**
     * Triggers the simple use of each action and returns their result.
     * @param itemStack The item stack being used.
     * @param livingEntity The entity using the item stack.
     * @return The use result.
     */
    protected InteractionResultHolder<ItemStack> actionsOnUse(ItemStack itemStack, LivingEntity livingEntity)
    {
        InteractionResultHolder<ItemStack> resultHolder = InteractionResultHolder.pass(itemStack);
        for (AbstractItemAction action : this.definition().actions()) {
            resultHolder = action.onUseSelf(resultHolder, livingEntity);
        }
        this.applyResult(resultHolder.getResult(), resultHolder.getObject(), livingEntity);
        return resultHolder;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand hand)
    {
        InteractionResult result = super.interactLivingEntity(itemStack, player, livingEntity, hand);
        for (AbstractItemAction action : this.definition().actions()) {
            result = action.onUseTarget(result, itemStack, player, livingEntity);
        }
        this.applyResult(result, itemStack, livingEntity);
        return result;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        InteractionResult result = super.useOn(context);
        for (AbstractItemAction action : this.definition().actions()) {
            result = action.onBlocks(result, context.getItemInHand(), context.getPlayer(), context.getClickedPos());
        }
        this.applyResult(result, context.getItemInHand(), context.getPlayer());
        return result;
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemStack, Player player, Entity targetEntity)
    {
        Pair<InteractionResult, Boolean> resultInterruptPair = Pair.of(InteractionResult.SUCCESS, false);
        for (AbstractItemAction action : this.definition().actions()) {
            resultInterruptPair = action.onHit(resultInterruptPair, itemStack, player, targetEntity);
        }
        this.applyResult(resultInterruptPair.getFirst(), itemStack, player);
        return resultInterruptPair.getSecond();
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Level level, Player player)
    {
        super.onCraftedBy(itemStack, level, player);
        InteractionResult result = InteractionResult.SUCCESS;
        for (AbstractItemAction action : this.definition().actions()) {
            result = action.onCraft(result, itemStack, player);
        }
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slotIndex, boolean selected)
    {
        if (this.definition().inventoryTicks() <= 0 || entity.tickCount % this.definition().inventoryTicks() != 0) {
            return;
        }
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        InteractionResult result = InteractionResult.SUCCESS;
        for (AbstractItemAction action : this.definition().actions()) {
            result = action.onInventoryTick(result, itemStack, livingEntity, slotIndex, selected);
        }
    }
}
