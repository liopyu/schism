package com.lycanitesmobs.core.entity.creature;

import com.google.common.collect.Maps;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.AgeableCreatureEntity;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.goals.actions.AttackMeleeGoal;
import com.lycanitesmobs.core.entity.goals.actions.EatBlockGoal;
import com.lycanitesmobs.core.entity.goals.actions.TemptGoal;
import com.lycanitesmobs.core.info.ItemDrop;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.IForgeShearable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class EntityYale extends AgeableCreatureEntity implements IForgeShearable {

	protected static final DataParameter<Byte> FUR = EntityDataManager.defineId(EntityYale.class, DataSerializers.BYTE);

	private static final Map<DyeColor, IItemProvider> WOOL_BY_COLOR = Util.make(Maps.newEnumMap(DyeColor.class), (itemProviderMap) -> {
		itemProviderMap.put(DyeColor.WHITE, Blocks.WHITE_WOOL);
		itemProviderMap.put(DyeColor.ORANGE, Blocks.ORANGE_WOOL);
		itemProviderMap.put(DyeColor.MAGENTA, Blocks.MAGENTA_WOOL);
		itemProviderMap.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_WOOL);
		itemProviderMap.put(DyeColor.YELLOW, Blocks.YELLOW_WOOL);
		itemProviderMap.put(DyeColor.LIME, Blocks.LIME_WOOL);
		itemProviderMap.put(DyeColor.PINK, Blocks.PINK_WOOL);
		itemProviderMap.put(DyeColor.GRAY, Blocks.GRAY_WOOL);
		itemProviderMap.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_WOOL);
		itemProviderMap.put(DyeColor.CYAN, Blocks.CYAN_WOOL);
		itemProviderMap.put(DyeColor.PURPLE, Blocks.PURPLE_WOOL);
		itemProviderMap.put(DyeColor.BLUE, Blocks.BLUE_WOOL);
		itemProviderMap.put(DyeColor.BROWN, Blocks.BROWN_WOOL);
		itemProviderMap.put(DyeColor.GREEN, Blocks.GREEN_WOOL);
		itemProviderMap.put(DyeColor.RED, Blocks.RED_WOOL);
		itemProviderMap.put(DyeColor.BLACK, Blocks.BLACK_WOOL);
	});
	private static final Map<DyeColor, float[]> DYE_TO_RGB = Maps.newEnumMap(Arrays.stream(DyeColor.values()).collect(Collectors.toMap((DyeColor p_200204_0_) -> {
		return p_200204_0_;
	}, EntityYale::createSheepColor)));

    protected ItemDrop woolDrop;


    public EntityYale(EntityType<? extends EntityYale> entityType, World world) {
        super(entityType, world);
        
        // Setup:
        this.attribute = CreatureAttribute.UNDEFINED;
        this.hasAttackSound = false;

        this.canGrow = true;
        this.babySpawnChance = 0.1D;
        this.fleeHealthPercent = 1.0F;
        this.isAggressiveByDefault = false;
        this.setupMob();

		this.woolDrop = new ItemDrop(Blocks.WHITE_WOOL.getRegistryName().toString(), 1).setMinAmount(1).setMaxAmount(3);
    }

    @Override
    protected void registerGoals() {
		this.goalSelector.addGoal(this.nextIdleGoalIndex++, new EatBlockGoal(this).setBlocks(Blocks.GRASS_BLOCK).setReplaceBlock(Blocks.DIRT));
		super.registerGoals();
		this.goalSelector.addGoal(this.nextDistractionGoalIndex++, new TemptGoal(this).setIncludeDiet(true));
		this.goalSelector.addGoal(this.nextCombatGoalIndex++, new AttackMeleeGoal(this).setLongMemory(false));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FUR, (byte) 1);
    }

	@Override
	public void onFirstSpawn() {
		if(!this.isBaby())
			this.setColor(this.getRandomFurColor(this.getRandom()));
		super.onFirstSpawn();
	}

	@Override
	public boolean isShearable(@Nonnull ItemStack item, World world, BlockPos pos) {
		return this.hasFur() && !this.isBaby();
	}

	@Override
	public ArrayList<ItemStack> onSheared(@Nullable PlayerEntity player, @Nonnull ItemStack item, World world, BlockPos pos, int fortune) {
		ArrayList<ItemStack> dropStacks = new ArrayList<>();
		if(this.woolDrop == null) {
			return dropStacks;
		}

		this.setFur(false);
		this.playSound(SoundEvents.SHEEP_SHEAR, 1.0F, 1.0F);

		int quantity = this.woolDrop.getQuantity(this.getRandom(), fortune, 1);
		ItemStack dropStack = this.woolDrop.getEntityDropItemStack(this, quantity);
		if(dropStack != null && dropStack.getItem() instanceof BlockItem && ((BlockItem)dropStack.getItem()).getBlock().getRegistryName().toString().contains("_wool")) {
			dropStack = new ItemStack(WOOL_BY_COLOR.get(this.getColor()), dropStack.getCount());
		}
		dropStacks.add(dropStack);
		
		return dropStacks;
	}

	public boolean hasFur() {
		if(this.entityData == null) return true;
		return this.entityData.get(FUR) > 0;
	}

	public void setFur(boolean fur) {
		if(!this.getCommandSenderWorld().isClientSide)
			this.entityData.set(FUR, (byte) (fur ? 1 : 0));
	}
	
	@Override
	public void onEat() {
		if(!this.getCommandSenderWorld().isClientSide)
			this.setFur(true);
	}
	
	@Override
	public boolean canBeColored(PlayerEntity player) {
		return true;
	}
	
	@Override
	public void setColor(DyeColor color) {
		Item woolItem = WOOL_BY_COLOR.get(this.getColor()).asItem();
        if(this.woolDrop == null) {
			this.woolDrop = new ItemDrop(woolItem.getRegistryName().toString(), 1).setMinAmount(1).setMaxAmount(3);
		}
		else if(this.woolDrop.getItemStack().getItem() != woolItem) {
			this.woolDrop.setDrop(new ItemStack(woolItem, 1));
		}
		super.setColor(color);
	}

	public DyeColor getRandomFurColor(Random random) {
		int i = random.nextInt(100);
		if (i < 5) {
			return DyeColor.BLACK;
		} else if (i < 10) {
			return DyeColor.GREEN;
		} else if (i < 15) {
			return DyeColor.RED;
		} else if (i < 18) {
			return DyeColor.BROWN;
		} else {
			return random.nextInt(500) == 0 ? DyeColor.PINK : DyeColor.WHITE;
		}
	}
	
	/**
	 * Attempts to mix both parents to come up with a mixed dye color.
	 */
	private DyeColor getMixedFurColor(BaseCreatureEntity father, BaseCreatureEntity mother) {
		DyeColor dyeA = father.getColor();
		DyeColor dyeB = mother.getColor();
		CraftingInventory craftinginventory = mixColors(dyeA, dyeB);
		return this.level.getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, craftinginventory, this.level).map((craftingRecipe) ->
				craftingRecipe.assemble(craftinginventory)).map(ItemStack::getItem).filter(DyeItem.class::isInstance).map(DyeItem.class::cast).map(DyeItem::getDyeColor).orElseGet(() ->
				this.level.random.nextBoolean() ? dyeA : dyeB);
	}

	private static CraftingInventory mixColors(DyeColor dyeA, DyeColor dyeB) {
		CraftingInventory craftinginventory = new CraftingInventory(new Container(null, -1) {
			public boolean stillValid(PlayerEntity playerIn) {
				return false;
			}
		}, 2, 1);
		craftinginventory.setItem(0, new ItemStack(DyeItem.byColor(dyeA)));
		craftinginventory.setItem(1, new ItemStack(DyeItem.byColor(dyeB)));
		return craftinginventory;
	}

	private static float[] createSheepColor(DyeColor p_192020_0_) {
		if (p_192020_0_ == DyeColor.WHITE) {
			return new float[]{0.9019608F, 0.9019608F, 0.9019608F};
		} else {
			float[] afloat = p_192020_0_.getTextureDiffuseColors();
			float f = 0.75F;
			return new float[]{afloat[0] * 0.75F, afloat[1] * 0.75F, afloat[2] * 0.75F};
		}
	}

    @Override
    public float getBlockPathWeight(int x, int y, int z) {
        BlockState blockState = this.getCommandSenderWorld().getBlockState(new BlockPos(x, y - 1, z));
        Block block = blockState.getBlock();
        if(block != Blocks.AIR) {
            if(blockState.getMaterial() == Material.GRASS)
                return 10F;
            if(blockState.getMaterial() == Material.DIRT)
                return 7F;
        }
        return super.getBlockPathWeight(x, y, z);
    }

    @Override
    public boolean canBeLeashed(PlayerEntity player) {
        return true;
    }

	@Override
	public int getNoBagSize() { return 0; }

	@Override
	public int getBagSize() { return this.creatureInfo.bagSize; }

    @Override
    public float getFallResistance() {
    	return 50;
    }

	@Override
	public boolean canDropItem(ItemDrop itemDrop) {
		if(!super.canDropItem(itemDrop)) {
			return false;
		}
		if(itemDrop.getItemStack().getItem() instanceof BlockItem && ((BlockItem)itemDrop.getItemStack().getItem()).getBlock().getRegistryName().toString().contains("_wool")) {
			return this.hasFur();
		}
		return true;
	}

	@Override
	public void dropItem(ItemStack itemStack) {
		if(this.woolDrop != null && itemStack.getItem() instanceof BlockItem && ((BlockItem)itemStack.getItem()).getBlock().getRegistryName().toString().contains("_wool")) {
			itemStack = new ItemStack(WOOL_BY_COLOR.get(this.getColor()), itemStack.getCount());
		}
		super.dropItem(itemStack);
	}

	@Override
	public AgeableCreatureEntity createChild(AgeableCreatureEntity partner) {
		AgeableCreatureEntity baby = super.createChild(partner);
		DyeColor color = this.getMixedFurColor(this, partner);
        baby.setColor(color);
		return baby;
	}

    @Override
    public void readAdditionalSaveData(CompoundNBT nbt) {
    	super.readAdditionalSaveData(nbt);
    	if(nbt.contains("HasFur")) {
    		this.setFur(nbt.getBoolean("HasFur"));
    	}
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT nbt) {
    	super.addAdditionalSaveData(nbt);
    	nbt.putBoolean("HasFur", this.hasFur());
    }
}
