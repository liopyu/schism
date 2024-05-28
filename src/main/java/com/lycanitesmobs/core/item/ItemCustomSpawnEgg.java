package com.lycanitesmobs.core.item;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.dispenser.SpawnEggDispenseBehaviour;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.CreatureType;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.*;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.spawner.AbstractSpawner;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCustomSpawnEgg extends CreatureTypeItem {

    public ItemCustomSpawnEgg(Item.Properties properties, CreatureType creatureType) {
        super(properties, creatureType.getSpawnEggName(), creatureType);
        this.setRegistryName(this.modInfo.modid, this.itemName);
        DispenserBlock.registerBehavior(this, new SpawnEggDispenseBehaviour());
		LycanitesMobs.logDebug("Creature Type", "Created Creature Type Spawn Egg: " + this.itemName);
    }

    @Override
    public ITextComponent getName(ItemStack itemStack) {
		TextComponent displayName = (TextComponent)new TranslationTextComponent("creaturetype.spawn")
				.append(" ")
				.append(this.creatureType.getTitle())
				.append(": ");
		CreatureInfo creatureInfo = this.getCreatureInfo(itemStack);
		if(creatureInfo != null)
			displayName.append(creatureInfo.getTitle());
		else
			displayName.append("Missing Creature NBT");
        return displayName;
    }

    @Override
	public void appendHoverText(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flag) {
		ITextComponent description = this.getDescription(stack, worldIn, tooltip, flag);
        if(!"".equalsIgnoreCase(description.getString()) && !("item." + this.itemName + ".description").equals(description.getContents())) {
			tooltip.add(description);
        }
    }

    @Override
	public ITextComponent getDescription(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		CreatureInfo creatureInfo = this.getCreatureInfo(itemStack);
		if(creatureInfo == null) {
			String creatureName = this.getCreatureName(itemStack);
			LycanitesMobs.logWarning("Mob Spawn Egg", "Unable to get Creature Info for id: " + creatureName);
			return new StringTextComponent("Unable to get Creature Info for id: '" + creatureName + "' this spawn egg may have been created by a give command without NBT data.");
		}
		return creatureInfo.getDescription().plainCopy().withStyle(TextFormatting.GREEN);
    }

	@Override
	public void fillItemCategory(ItemGroup tab, NonNullList<ItemStack> items) {
		if(!this.allowdedIn(tab)) {
			return;
		}

		for(CreatureInfo creatureInfo : this.creatureType.creatures.values()) {
			ItemStack itemstack = new ItemStack(this, 1);
			this.applyCreatureInfoToItemStack(itemstack, creatureInfo);
			items.add(itemstack);
		}
	}

	/**
	 * Applies creature info to a spawn egg item stack.
	 * @param itemStack The spawn egg item stack top apply to.
	 * @param creatureInfo The creature info to apply.
	 */
	public void applyCreatureInfoToItemStack(ItemStack itemStack, CreatureInfo creatureInfo) {
		CompoundNBT itemStackNBT = itemStack.hasTag() ? itemStack.getTag() : new CompoundNBT();
		CompoundNBT spawnEggNBT = new CompoundNBT();
		spawnEggNBT.putString("creaturename", creatureInfo.getName());
		itemStackNBT.put("CreatureInfoSpawnEgg", spawnEggNBT);
		itemStack.setTag(itemStackNBT);
	}

    @Override
	public ActionResultType useOn(ItemUseContext context) {
		World world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		PlayerEntity player = context.getPlayer();
		ItemStack itemStack = context.getItemInHand();

        BlockState blockState = world.getBlockState(pos);
        Block block = blockState.getBlock();

        if (world.isClientSide) {
            return ActionResultType.SUCCESS;
        }
        if (!player.mayUseItemAt(pos.relative(context.getClickedFace()), context.getClickedFace(), itemStack)) {
            return ActionResultType.FAIL;
        }
        
        // Edit Spawner:
        if(block == Blocks.SPAWNER) {
            TileEntity tileEntity = world.getBlockEntity(pos);
			AbstractSpawner mobspawnerbaselogic = ((MobSpawnerTileEntity)tileEntity).getSpawner();
            mobspawnerbaselogic.setEntityId(this.getCreatureInfo(itemStack).getEntityType());
            tileEntity.setChanged();
            world.sendBlockUpdated(pos, blockState, blockState, 3);
            if (!player.abilities.instabuild) {
                itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
            }

            return ActionResultType.SUCCESS;
        }
        
        // Spawn Mob:
		pos = pos.relative(context.getClickedFace());
		double d0 = 0.0D;
		if (context.getClickedFace() == Direction.UP && blockState.getBlock() instanceof FenceBlock) {
			d0 = 0.5D;
		}

		LivingEntity entity = this.spawnCreature(world, itemStack, (double)pos.getX() + 0.5D, (double)pos.getY() + d0, (double)pos.getZ() + 0.5D);
		if(entity != null) {
			if(itemStack.hasCustomHoverName()) {
				entity.setCustomName(itemStack.getHoverName());
			}
			if(!player.abilities.instabuild) {
				itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
			}
		}

        return ActionResultType.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if(world.isClientSide)
            return new ActionResult(ActionResultType.PASS, itemStack);
        else {
            RayTraceResult rayTraceResult = this.getPlayerPOVHitResult(world, player, RayTraceContext.FluidMode.ANY);

            if(rayTraceResult.getType() != RayTraceResult.Type.BLOCK)
                return new ActionResult(ActionResultType.PASS, itemStack);

            BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult)rayTraceResult;
			BlockPos pos = blockRayTraceResult.getBlockPos();

			if (!world.mayInteract(player, pos)) {
				return new ActionResult(ActionResultType.FAIL, itemStack);
			}

			if (!player.mayUseItemAt(pos, blockRayTraceResult.getDirection(), itemStack)) {
				return new ActionResult(ActionResultType.PASS, itemStack);
			}

			if (world.getBlockState(pos).getMaterial() == Material.WATER) {
				LivingEntity entity = spawnCreature(world, itemStack, (double) pos.getX(), (double) pos.getY(), (double) pos.getZ());
				if (entity != null)
					if (itemStack.hasCustomHoverName()) {
						entity.setCustomName(itemStack.getHoverName());
					}
				if (!player.abilities.instabuild) {
					itemStack.setCount(Math.max(0, itemStack.getCount() - 1));
				}
			}

			return new ActionResult(ActionResultType.SUCCESS, itemStack);
        }
    }

	/**
	 * Get Creature Info
	 * @param itemStack The spawn egg item stack to get the creature from.
	 * @return The Creature Info of the stack spawn egg or null if unknown.
	 */
	public CreatureInfo getCreatureInfo(ItemStack itemStack) {
		String creatureName = this.getCreatureName(itemStack);
		return CreatureManager.getInstance().getCreature(creatureName);
	}

	/**
	 * Get Creature Name
	 * @param itemStack The spawn egg item stack to get the creature name from.
	 * @return The name of the creature that the spawn egg item stack should spawn.
	 */
	public String getCreatureName(ItemStack itemStack) {
		CompoundNBT itemStackNBT = itemStack.getTag();
		if (itemStackNBT == null || !itemStackNBT.contains("CreatureInfoSpawnEgg", 10)) {
			return null;
		}
		CompoundNBT spawnEggNBT = itemStackNBT.getCompound("CreatureInfoSpawnEgg");
		return !spawnEggNBT.contains("creaturename", 8) ? null : spawnEggNBT.getString("creaturename");
	}

	/**
	 * Spawn Creature
	 * @param world The world to spawn in.
	 * @param itemStack The spawn egg itemstack to spawn from.
	 * @param x X spawn coordinate.
	 * @param y Y spawn coordinate.
	 * @param z Z spawn coordinate.
	 * @return The spawned entity instance.
	 */
	public LivingEntity spawnCreature(World world, ItemStack itemStack, double x, double y, double z) {
		LivingEntity entity = this.getCreatureInfo(itemStack).createEntity(world);
		if(entity != null && world instanceof IServerWorld) {
			entity.moveTo(x, y, z, MathHelper.wrapDegrees(world.random.nextFloat() * 360.0F), 0.0F);
			entity.yHeadRot = entity.yRot;
			entity.yBodyRot = entity.yRot;

			if(itemStack.hasCustomHoverName()) {
				entity.setCustomName(itemStack.getHoverName());
			}

			if(entity instanceof MobEntity) {
				MobEntity mobEntity = (MobEntity)entity;
				mobEntity.finalizeSpawn((IServerWorld)world, world.getCurrentDifficultyAt(mobEntity.blockPosition()), SpawnReason.SPAWN_EGG, null, null);
				mobEntity.playAmbientSound();
			}

			world.addFreshEntity(entity);
		}
		return entity;
	}
}