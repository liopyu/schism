package com.lycanitesmobs.core.item;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseProjectileEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.ElementInfo;
import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ChargeItem extends BaseItem {
    /** How much experience a Charge Item grants per element matched. **/
    public static int CHARGE_EXPERIENCE = 50;

    /** The projectile info that this projectile charge item belongs to. **/
    public ProjectileInfo projectileInfo;

    /**
     * Constructor
     * @param projectileInfo The projectile info to base this charge off.
     */
    public ChargeItem(Item.Properties properties, ProjectileInfo projectileInfo) {
        super(properties);
        this.projectileInfo = projectileInfo;
        this.modInfo = LycanitesMobs.modInfo;
        if(this.projectileInfo != null) {
            this.itemName = projectileInfo.chargeItemName;
            LycanitesMobs.logDebug("Projectile", "Created Charge Item: " + projectileInfo.chargeItemName);
            this.setup();
        }
    }

    @Override
    public ITextComponent getName(ItemStack itemStack) {
        return this.getProjectileName().append(" ").append(new TranslationTextComponent("item.lycanitesmobs.charge"));
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, world, tooltip, tooltipFlag);
        FontRenderer fontRenderer = Minecraft.getInstance().font;
        for(ITextComponent description : this.getAdditionalDescriptions(itemStack, world, tooltipFlag)) {
            tooltip.add(description);
        }
    }

    @Override
    public ITextComponent getDescription(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        return new TranslationTextComponent("item.lycanitesmobs.charge.description").withStyle(TextFormatting.GREEN);
    }

    public List<ITextComponent> getAdditionalDescriptions(ItemStack itemStack, @Nullable World world, ITooltipFlag tooltipFlag) {
        List<ITextComponent> descriptions = new ArrayList<>();

        descriptions.add(new TranslationTextComponent("item.lycanitesmobs.charge.projectile").withStyle(TextFormatting.GOLD)
                .append(" ").append(this.getProjectileName()));

        if(!this.getElements().isEmpty()) {
            descriptions.add(new TranslationTextComponent("item.lycanitesmobs.charge.elements").withStyle(TextFormatting.DARK_AQUA)
                    .append(" ").append(this.getElementNames()));
        }

        return descriptions;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if(!world.isClientSide && player.isShiftKeyDown()) { // isSneaking()
            BaseProjectileEntity projectile = this.createProjectile(itemStack, world, player);
            if(projectile == null) {
                LycanitesMobs.logWarning("", "Failed to create projectile from Charge Item: " + this.itemName);
                return new ActionResult<>(ActionResultType.FAIL, itemStack);
            }
            world.addFreshEntity(projectile);
            if(!player.abilities.instabuild) {
                itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
            }
            this.playSound(world, player.blockPosition(), projectile.getLaunchSound(), SoundCategory.NEUTRAL, 0.5F, 0.4F / (player.getRandom().nextFloat() * 0.4F + 0.8F));
        }

        return new ActionResult<>(ActionResultType.SUCCESS, itemStack);
    }

    @Override
    public ActionResultType interactLivingEntity(ItemStack stack, PlayerEntity player, LivingEntity entity, Hand hand) {
        if(entity instanceof TameableCreatureEntity && ((TameableCreatureEntity)entity).getPlayerOwner() == player) {
            return ActionResultType.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, entity, hand);
    }

    /**
     * Creates a projectile instance from this charge item.
     * @param itemStack The charge itemstack.
     * @param world The world to create the projectile in.
     * @param entityPlayer The player using the charge.
     * @return A projectile instance.
     */
    public BaseProjectileEntity createProjectile(ItemStack itemStack, World world, PlayerEntity entityPlayer) {
        if(this.projectileInfo != null) {
            return this.projectileInfo.createProjectile(world, entityPlayer);
        }
        return null;
    }

    /**
     * Gets the Elements of this Charge.
     * @return A list of Elements that this Charge contains.
     */
    public List<ElementInfo> getElements() {
        if(this.projectileInfo == null) {
            return new ArrayList<>();
        }
        return this.projectileInfo.elements;
    }

    /**
     * Returns a comma separated list of Elements this Charge contains.
     * @return The Elements this Charge contains.
     */
    public ITextComponent getElementNames() {
        TextComponent elementNames = new StringTextComponent("");
        boolean firstElement = true;
        for(ElementInfo element : this.getElements()) {
            if(!firstElement) {
                elementNames.append(", ");
            }
            firstElement = false;
            elementNames.append(element.getTitle());
        }
        return elementNames;
    }

    /**
     * Returns the display name of the projectile fired by this Charge.
     * @return The Projectile this Charge fires.
     */
    public TextComponent getProjectileName() {
        if(this.projectileInfo != null) {
            return this.projectileInfo.getTitle();
        }
        return new TranslationTextComponent("item.lycanitesmobs.charge");
    }
}
