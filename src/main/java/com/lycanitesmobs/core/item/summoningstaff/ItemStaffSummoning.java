package com.lycanitesmobs.core.item.summoningstaff;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.PortalEntity;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.projectile.ProjectileManager;
import com.lycanitesmobs.core.item.BaseItem;
import com.lycanitesmobs.core.pets.SummonSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemStaffSummoning extends BaseItem {
	protected float damageScale = 1.0F;
	protected int weaponFlash = 0;
	
	// ==================================================
	//                   Constructor
	// ==================================================
    public ItemStaffSummoning(Item.Properties properties, String itemName, String textureName) {
        super(properties);
        this.itemName = itemName;
        this.setup();
    }


	// ==================================================
	//                       Use
	// ==================================================
    public void damageItemCharged(ItemStack itemStack, LivingEntity entity, float power) {
		ExtendedPlayer playerExt = null;
		if(entity instanceof PlayerEntity) {
			playerExt = ExtendedPlayer.getForPlayer((PlayerEntity)entity);
		}
    	if(playerExt != null && playerExt.staffPortal != null) {
            this.damageStaff(itemStack, playerExt.staffPortal.summonAmount, (ServerPlayerEntity)entity);
    	}
    }

	// ========== Prevent Swing ==========
	@Override
	public boolean onEntitySwing(ItemStack itemStack, LivingEntity entity) {
		if(entity instanceof PlayerEntity) {
			entity.startUsingItem(Hand.MAIN_HAND);
			return true;
		}
		return super.onEntitySwing(itemStack, entity);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack itemStack, PlayerEntity player, Entity entity) {
		return true;
	}

	// ========== Start ==========
	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		if (!CreatureManager.getInstance().config.isSummoningAllowed(world)) {
			return new ActionResult(ActionResultType.FAIL, player.getItemInHand(hand));
		}

		ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(player);
		if(playerExt != null) {
			// Summon Selected Mob:
			SummonSet summonSet = playerExt.getSelectedSummonSet();
			if(summonSet.isUseable()) {
				if(!player.getCommandSenderWorld().isClientSide) {
					playerExt.staffPortal = new PortalEntity((EntityType<? extends PortalEntity>)ProjectileManager.getInstance().oldProjectileTypes.get(PortalEntity.class), world, player, summonSet, this);
					playerExt.staffPortal.moveTo(player.position().x(), player.position().y(), player.position().z(), world.random.nextFloat() * 360.0F, 0.0F);
					world.addFreshEntity(playerExt.staffPortal);
				}
			}
			// Open Minion GUI If None Selected:
			else {
				playerExt.staffPortal = null;
				if(!player.getCommandSenderWorld().isClientSide())
					playerExt.sendAllSummonSetsToPlayer();
				else
					ClientManager.getInstance().displayGuiScreen("beastiary", player);
			}
		}
		player.startUsingItem(hand);
		return new ActionResult(ActionResultType.SUCCESS, player.getItemInHand(hand));
	}

	// ========== Using ==========
	@Override
	public void onUsingTick(ItemStack itemStack, LivingEntity user, int useRemaining) {
		if(!user.isUsingItem()) {
			return;
		}
		int useTime = this.getUseDuration(itemStack) - useRemaining;
		if(useTime >= this.getRapidTime(itemStack)) {
			int rapidRemainder = useTime % this.getRapidTime(itemStack);
			if(rapidRemainder == 0) {
				if(this.rapidAttack(itemStack, user.getCommandSenderWorld(), user)) {
					this.weaponFlash = Math.max(20, this.getRapidTime(itemStack));
				}
			}
		}
		if(useTime >= this.getChargeTime(itemStack))
			this.weaponFlash = Math.max(20, this.getChargeTime(itemStack));

		super.onUsingTick(itemStack, user, useRemaining);
	}

	// ========== Stop ==========
	@Override
	public void releaseUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
		int useTime = this.getUseDuration(stack) - timeLeft;
		float power = (float)useTime / (float)this.getChargeTime(stack);

		this.weaponFlash = 0;

		if((double)power < 0.1D)
			return;
		if(power > 1.0F)
			power = 1.0F;

		if(this.chargedAttack(stack, worldIn, entityLiving, power)) {
			this.damageItemCharged(stack, entityLiving, power);
			this.weaponFlash = Math.min(20, this.getChargeTime(stack));
		}

		ExtendedPlayer playerExt = null;
		if(entityLiving instanceof PlayerEntity) {
			playerExt = ExtendedPlayer.getForPlayer((PlayerEntity)entityLiving);
		}
		if(playerExt != null) {
			playerExt.staffPortal = null;
		}
	}

	// ========== Animation ==========
	@Override
	public UseAction getUseAnimation(ItemStack itemStack) {
		return UseAction.BOW;
	}

	// ========== Max Use Duration ==========
	@Override
	public int getUseDuration(ItemStack itemStack) {
		return 72000;
	}
    
    // ========== Charge Time ==========
    public int getChargeTime(ItemStack itemStack) {
        return 1;
    }
    
    // ========== Rapid Time ==========
    public int getRapidTime(ItemStack itemStack) {
        return 20;
    }
    
    // ========== Summon Cost ==========
    public int getSummonCostBoost() {
    	return 0;
    }
    public float getSummonCostMod() {
    	return 1.0F;
    }
    
    // ========== Summon Duration ==========
    public int getSummonDuration() {
    	return 60 * 20;
    }
    
    // ========== Summon Amount ==========
    public int getSummonAmount() {
    	return 1;
    }
    
    // ========== Additional Costs ==========
    public boolean getAdditionalCosts(PlayerEntity player) {
    	return true;
    }
    
    // ========== Minion Behaviour ==========
    public void applyMinionBehaviour(TameableCreatureEntity minion, PlayerEntity player) {
    	SummonSet summonSet = ExtendedPlayer.getForPlayer(player).getSelectedSummonSet();
        summonSet.applyBehaviour(minion);
        minion.setSubspecies(summonSet.subspecies);
        minion.applyVariant(summonSet.variant);
    }
    
    // ========== Minion Effects ==========
    public void applyMinionEffects(BaseCreatureEntity minion) {}
	
    
	// ==================================================
	//                      Attack
	// ==================================================
    // ========== Rapid ==========
    public boolean rapidAttack(ItemStack itemStack, World world, LivingEntity entity) {
    	return false;
    }

    protected void damageStaff(ItemStack itemStack, int amountToDamage, ServerPlayerEntity entity) {
        itemStack.hurt(amountToDamage, entity.getRandom(), entity);
        if (itemStack.getCount() == 0) {
            if (entity.getItemInHand(Hand.MAIN_HAND).equals(itemStack)) {
                entity.setItemInHand(Hand.MAIN_HAND, ItemStack.EMPTY);
            } else if (entity.getItemInHand(Hand.OFF_HAND).equals(itemStack)) {
                entity.setItemInHand(Hand.OFF_HAND, ItemStack.EMPTY);
            }
        }
    }
    
    // ========== Charged ==========
    public boolean chargedAttack(ItemStack itemStack, World world, LivingEntity entity, float power) {
    	ExtendedPlayer playerExt = null;
    	if(entity instanceof PlayerEntity) {
			playerExt = ExtendedPlayer.getForPlayer((PlayerEntity)entity);
		}
    	if(playerExt != null && playerExt.staffPortal != null) {
			int successCount = playerExt.staffPortal.summonCreatures();
            this.damageStaff(itemStack, successCount, (ServerPlayerEntity)entity);
			return successCount > 0;
		}
		return false;
    }
    
	
	// ==================================================
	//                     Repairs
	// ==================================================
    @Override
    public boolean isValidRepairItem(ItemStack itemStack, ItemStack repairStack) {
    	if(repairStack.getItem() == Items.GOLD_INGOT) return true;
        return super.isValidRepairItem(itemStack, repairStack);
    }
}
