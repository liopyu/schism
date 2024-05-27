package com.schism.core.items;

import com.schism.core.Schism;
import com.schism.core.database.IHasDefinition;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.projectiles.ProjectileDefinition;
import com.schism.core.projectiles.ProjectileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ChargeItem extends Item implements IHasDefinition<ProjectileDefinition>
{
    protected final ProjectileDefinition definition;

    public ChargeItem(ProjectileDefinition definition, Properties properties)
    {
        super(properties);
        this.definition = definition;
        this.setRegistryName(new ResourceLocation(Schism.NAMESPACE, definition.subject() + "_charge"));
    }

    @Override
    public ProjectileDefinition definition()
    {
        return this.definition;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(itemStack, level, list, tooltipFlag);

        list.add(Component.translatable("item." + Schism.NAMESPACE + ".charge.description", this.definition().name()).withStyle(ChatFormatting.GREEN));
        list.add(Component.translatable("item." + Schism.NAMESPACE + ".charge.help").withStyle(ChatFormatting.GOLD));

        if (!this.definition().chargeElements().isEmpty()) {
            MutableComponent elementsComponent = Component.translatable(Schism.NAMESPACE + ".elements").append(":");
            for (ElementDefinition element : this.definition().chargeElements()) {
                elementsComponent.append(" ").append(element.name());
            }
            list.add(elementsComponent.withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    @Override
    public boolean hasContainerItem(ItemStack itemStack)
    {
        return false;
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack)
    {
        return ItemStack.EMPTY;
    }

    @Override
    public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType)
    {
        return -1;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack itemStack)
    {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack itemStack)
    {
        return super.getUseDuration(itemStack);
    }

    /**
     * Called when the item is used.
     * @param level The level.
     * @param player The using player.
     * @param hand The hand that the item is used in.
     * @return The use result.
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (player.isShiftKeyDown()) {
            result = this.actionsOnUse(player.getItemInHand(hand), player);
        }
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
                    itemStack.setDamageValue(itemStack.getDamageValue() - 1);
                } else {
                    itemStack.shrink(1);
                }
            }
            if (livingEntity instanceof Player player) {
                player.getCooldowns().addCooldown(this, 20);
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
        ProjectileEntity projectileEntity = this.definition().create(livingEntity.getLevel());
        projectileEntity.initFromEntity(livingEntity);
        livingEntity.getLevel().addFreshEntity(projectileEntity);
        InteractionResultHolder<ItemStack> resultHolder = InteractionResultHolder.consume(itemStack);
        this.applyResult(resultHolder.getResult(), resultHolder.getObject(), livingEntity);
        return resultHolder;
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity livingEntity, InteractionHand hand)
    {
        return super.interactLivingEntity(itemStack, player, livingEntity, hand);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        return super.useOn(context);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack itemStack, Player player, Entity targetEntity)
    {
        return true;
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Level level, Player player)
    {
        super.onCraftedBy(itemStack, level, player);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slotIndex, boolean selected)
    {
        super.inventoryTick(itemStack, level, entity, slotIndex, selected);
    }
}
