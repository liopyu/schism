package com.lycanitesmobs.core.item.equipment.features;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import com.lycanitesmobs.core.helpers.JSONHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

public class HarvestEquipmentFeature extends EquipmentFeature {
	/** List of blocks by harvest type. These are checked after Materials are checked. **/
	private static final Set<Block> SPADE_HARVEST = Sets.newHashSet(Blocks.CLAY, Blocks.DIRT, Blocks.FARMLAND, Blocks.GRASS_BLOCK, Blocks.GRAVEL, Blocks.MYCELIUM, Blocks.SAND, Blocks.SNOW, Blocks.SOUL_SAND, Blocks.GRASS_PATH, Blocks.GRAY_CONCRETE_POWDER);
	private static final Set<Block> PICKAXE_HARVEST = Sets.newHashSet(Blocks.ACTIVATOR_RAIL, Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.DETECTOR_RAIL, Blocks.DIAMOND_BLOCK, Blocks.DIAMOND_ORE, Blocks.POWERED_RAIL, Blocks.GOLD_BLOCK, Blocks.GOLD_ORE, Blocks.ICE, Blocks.IRON_BLOCK, Blocks.IRON_ORE, Blocks.LAPIS_BLOCK, Blocks.LAPIS_ORE, Blocks.MOSSY_COBBLESTONE, Blocks.NETHERRACK, Blocks.PACKED_ICE, Blocks.RAIL, Blocks.REDSTONE_ORE, Blocks.SANDSTONE, Blocks.RED_SANDSTONE, Blocks.STONE, Blocks.STONE_SLAB, Blocks.STONE_BUTTON, Blocks.STONE_PRESSURE_PLATE);
	private static final Set<Block> AXE_HARVEST = Sets.newHashSet(Blocks.OAK_PLANKS, Blocks.BOOKSHELF, Blocks.CHEST, Blocks.PUMPKIN, Blocks.MELON, Blocks.LADDER, Blocks.OAK_BUTTON, Blocks.OAK_PRESSURE_PLATE);
	private static final Map<Block, BlockState> HOE_LOOKUP = Maps.newHashMap(ImmutableMap.of(Blocks.GRASS_BLOCK, Blocks.FARMLAND.defaultBlockState(), Blocks.GRASS_PATH, Blocks.FARMLAND.defaultBlockState(), Blocks.DIRT, Blocks.FARMLAND.defaultBlockState(), Blocks.COARSE_DIRT, Blocks.DIRT.defaultBlockState()));

	/** The type of tool to harvest as. Can be: pickaxe, axe, shovel, hoe, sword or shears. **/
	public String harvestType;

	/** The shape of the harvest. Can be block, cross or random. **/
	public String harvestShape = "block";

	/** How much harvest speed to add when harvesting compatible blocks. **/
	public float harvestSpeed = 1;

	/** The level of harvesting. -1 = Unable, 0 = Wood, 1 = Stone, 2 = Iron, 3 = Diamond. **/
	public int harvestLevel = 3;

	/** The additional block range of the harvest shape, relative to the harvesting direction, the central block is not affected by this. X = number of blocks both sides laterally (sideways). Y = Number of blocks vertically. Z = Number of blocks forwards. **/
	public Vector3i harvestRange = new Vector3i(0, 0, 0);


	// ==================================================
	//                        JSON
	// ==================================================
	@Override
	public void loadFromJSON(JsonObject json) {
		super.loadFromJSON(json);

		this.harvestType = json.get("harvestType").getAsString();

		if (json.has("harvestType"))
			this.harvestType = json.get("harvestType").getAsString();

		if (json.has("harvestSpeed"))
			this.harvestSpeed = json.get("harvestSpeed").getAsFloat();

		if (json.has("harvestLevel"))
			this.harvestLevel = json.get("harvestLevel").getAsInt();

		if (json.has("harvestShape"))
			this.harvestShape = json.get("harvestShape").getAsString();

		this.harvestRange = JSONHelper.getVector3i(json, "harvestRange");
	}

	@Override
	public ITextComponent getDescription(ItemStack itemStack, int level) {
		if (!this.isActive(itemStack, level)) {
			return null;
		}
		return new TranslationTextComponent("equipment.feature." + this.featureType).append(" ")
				.append(this.getSummary(itemStack, level));
	}

	@Override
	public ITextComponent getSummary(ItemStack itemStack, int level) {
		if (!this.isActive(itemStack, level)) {
			return null;
		}
		TranslationTextComponent summary = new TranslationTextComponent("equipment.harvest.type." + this.harvestType);
		if (this.harvestRange.distSqr(0, 0, 0, false) > 0) {
			summary.append(" (").append(new TranslationTextComponent("equipment.harvest.shape." + this.harvestShape));
			summary.append(" " + this.getHarvestRangeString(level)).append(")");
		}
		return summary;
	}

	public String getHarvestRangeString(int level) {
		String harvestRangeString = "" + ((this.harvestRange.getX()) * 2 + 1);
		harvestRangeString += "x" + (this.harvestRange.getY() + 1);
		harvestRangeString += "x" + (this.harvestRange.getZ() + 1);
		return harvestRangeString;
	}


	// ==================================================
	//                     Harvesting
	// ==================================================

	/**
	 * Gets the Tool Type provided by this Harvest Feature.
	 * @return The Tool Type of this feature, will return null if it's not a Pickaxe, Axe or Shovel.
	 */
	@Nullable
	public ToolType getToolType() {
		return ToolType.get(this.harvestType);
	}

	/**
	 * Returns if this feature can harvest the provided block or not.
	 * @param blockState The blockstate to check.
	 * @return True if the block can be destroyed by this feature.
	 */
	public boolean canHarvestBlock(BlockState blockState) {
		Block block = blockState.getBlock();
		Material material = blockState.getMaterial();

		// Stone:
		if (material == Material.METAL || material == Material.HEAVY_METAL || material == Material.STONE || PICKAXE_HARVEST.contains(block)) {
			return this.harvestType.equalsIgnoreCase("pickaxe");
		}

		// Wood:
		if (material == Material.WOOD || AXE_HARVEST.contains(block)) {
			return this.harvestType.equalsIgnoreCase("axe");
		}

		// Plants:
		if (material == Material.PLANT || material == Material.REPLACEABLE_PLANT) {
			return this.harvestType.equalsIgnoreCase("axe") || this.harvestType.equalsIgnoreCase("sword") || this.harvestType.equalsIgnoreCase("shears");
		}

		// Web and Leaves:
		if (material == Material.WEB || material == Material.LEAVES || material == Material.REPLACEABLE_WATER_PLANT) {
			return this.harvestType.equalsIgnoreCase("sword") || this.harvestType.equalsIgnoreCase("shears");
		}

		// Dirt:
		if (material == Material.DIRT || material == Material.CLAY || material == Material.SAND || material == Material.GRASS || material == Material.SNOW || material == Material.TOP_SNOW || SPADE_HARVEST.contains(block)) {
			return this.harvestType.equalsIgnoreCase("shovel");
		}

		// Growth:
		if (material == Material.CORAL || material == Material.VEGETABLE) {
			return this.harvestType.equalsIgnoreCase("sword");
		}

		// Wire:
		if (block == Blocks.TRIPWIRE) {
			return this.harvestType.equalsIgnoreCase("shears");
		}

		return false;
	}


	/**
	 * Returns the speed that this feature adds to harvesting the provided block.
	 * @param blockState The block to harvest.
	 * @return The harvest speed to add (all harvest features have their speed added together).
	 */
	public float getHarvestSpeed(BlockState blockState) {
		if (!this.canHarvestBlock(blockState)) {
			return 0;
		}

		return this.harvestSpeed * this.getHarvestMultiplier(blockState);
	}


	/**
	 * Returns a harvest speed multiplier for the provided block.
	 * @param blockState The block to check.
	 * @return A harvest speed multiplier.
	 */
	public float getHarvestMultiplier(BlockState blockState) {
		Material material = blockState.getMaterial();

		// Web:
		if (material == Material.WEB) {
			return 10;
		}

		// Shears:
		if ((material == Material.LEAVES || material == Material.REPLACEABLE_PLANT) && this.harvestType.equalsIgnoreCase("shears")) {
			return 10;
		}

		return 1;
	}


	/**
	 * Called when a block is destroyed by Equipment with this Feature.
	 * @param world The world where the block was destroyed.
	 * @param harvestedBlockState The block state that was destroyed.
	 * @param harvestedPos The position of the destroyed block.
	 * @param livingEntity The entity that destroyed the block.
	 */
	public void onBlockDestroyed(World world, BlockState harvestedBlockState, BlockPos harvestedPos, LivingEntity livingEntity) {
		if (livingEntity == null || livingEntity.isShiftKeyDown()) {
			return;
		}

		// Get Facing:
		Direction facingH = livingEntity.getDirection();
		Direction facingLat = facingH.getClockWise();
		Direction facing = facingH;
		if (livingEntity.xRot > 45) {
			facing = Direction.DOWN;
		}
		else if (livingEntity.xRot < -45) {
			facing = Direction.UP;
		}
		Vector3i[][] selectionRanges = new Vector3i[3][2];
		int lon = 0;
		int lat = 1;
		int vert = 2;
		int min = 0;
		int max = 1;

		// Get Longitudinal (Z):
		selectionRanges[lon][min] = new Vector3i(
				Math.min(0, this.harvestRange.getZ() * facing.getStepX()),
				Math.min(0, this.harvestRange.getZ() * facing.getStepY()),
				Math.min(0, this.harvestRange.getZ() * facing.getStepZ())
		);
		selectionRanges[lon][max] = new Vector3i(
				Math.max(0, this.harvestRange.getZ() * facing.getStepX()),
				Math.max(0, this.harvestRange.getZ() * facing.getStepY()),
				Math.max(0, this.harvestRange.getZ() * facing.getStepZ())
		);

		// Get Lateral (X):
		selectionRanges[lat][min] = new Vector3i(
				this.harvestRange.getX() * -Math.abs(facingLat.getStepX()),
				this.harvestRange.getX() * -Math.abs(facingLat.getStepY()),
				this.harvestRange.getX() * -Math.abs(facingLat.getStepZ())
		);
		selectionRanges[lat][max] = new Vector3i(
				this.harvestRange.getX() * Math.abs(facingLat.getStepX()),
				this.harvestRange.getX() * Math.abs(facingLat.getStepY()),
				this.harvestRange.getX() * Math.abs(facingLat.getStepZ())
		);

		// Get Vertical (Y):
		if (facing != Direction.DOWN && facing != Direction.UP) {
			int vertOffset = this.harvestRange.getY() != 0 ? -1 : 0;
			selectionRanges[vert][min] = new Vector3i(0, vertOffset, 0);
			selectionRanges[vert][max] = new Vector3i(0, this.harvestRange.getY() + vertOffset, 0);
		}
		else {
			selectionRanges[vert][min] = new Vector3i(
					this.harvestRange.getY() * -Math.abs(facingH.getStepX()) * 0.5F,
					this.harvestRange.getY() * -Math.abs(facingH.getStepY()) * 0.5F,
					this.harvestRange.getY() * -Math.abs(facingH.getStepZ()) * 0.5F
			);
			selectionRanges[vert][max] = new Vector3i(
					this.harvestRange.getY() * Math.abs(facingH.getStepX()) * 0.5F,
					this.harvestRange.getY() * Math.abs(facingH.getStepY()) * 0.5F,
					this.harvestRange.getY() * Math.abs(facingH.getStepZ()) * 0.5F
			);
		}

		// Block and Random Area Harvesting:
		if (this.harvestShape.equalsIgnoreCase("block") || this.harvestShape.equalsIgnoreCase("random")) {
			boolean random = this.harvestShape.equalsIgnoreCase("random");

			// Longitude:
			for (int longX = selectionRanges[lon][min].getX(); longX <= selectionRanges[lon][max].getX(); longX++) {
				for (int longY = selectionRanges[lon][min].getY(); longY <= selectionRanges[lon][max].getY(); longY++) {
					for (int longZ = selectionRanges[lon][min].getZ(); longZ <= selectionRanges[lon][max].getZ(); longZ++) {

						// Latitude:
						for (int latX = selectionRanges[lat][min].getX(); latX <= selectionRanges[lat][max].getX(); latX++) {
							for (int latY = selectionRanges[lat][min].getY(); latY <= selectionRanges[lat][max].getY(); latY++) {
								for (int latZ = selectionRanges[lat][min].getZ(); latZ <= selectionRanges[lat][max].getZ(); latZ++) {

									// Vertical:
									for (int vertX = selectionRanges[vert][min].getX(); vertX <= selectionRanges[vert][max].getX(); vertX++) {
										for (int vertY = selectionRanges[vert][min].getY(); vertY <= selectionRanges[vert][max].getY(); vertY++) {
											for (int vertZ = selectionRanges[vert][min].getZ(); vertZ <= selectionRanges[vert][max].getZ(); vertZ++) {

												BlockPos destroyPos = harvestedPos.offset(longX, longY, longZ).offset(latX, latY, latZ).offset(vertX, vertY, vertZ);
												if (this.shouldHarvestBlock(world, harvestedBlockState, harvestedPos, destroyPos) && (!random || world.random.nextBoolean())) {
													world.destroyBlock(destroyPos, true);
												}

											}
										}
									}

								}
							}
						}

					}
				}
			}

			return;
		}

		// Cross Area Harvesting:
		if (this.harvestShape.equalsIgnoreCase("cross")) {

			// Longitude:
			for (int longX = selectionRanges[lon][min].getX(); longX <= selectionRanges[lon][max].getX(); longX++) {
				for (int longY = selectionRanges[lon][min].getY(); longY <= selectionRanges[lon][max].getY(); longY++) {
					for (int longZ = selectionRanges[lon][min].getZ(); longZ <= selectionRanges[lon][max].getZ(); longZ++) {

						// Latitude:
						for (int latX = selectionRanges[lat][min].getX(); latX <= selectionRanges[lat][max].getX(); latX++) {
							for (int latY = selectionRanges[lat][min].getY(); latY <= selectionRanges[lat][max].getY(); latY++) {
								for (int latZ = selectionRanges[lat][min].getZ(); latZ <= selectionRanges[lat][max].getZ(); latZ++) {
									BlockPos destroyPos = harvestedPos.offset(longX, longY, longZ).offset(latX, latY, latZ);
									if (this.shouldHarvestBlock(world, harvestedBlockState, harvestedPos, destroyPos)) {
										world.destroyBlock(destroyPos, true);
									}
								}
							}
						}

						// Vertical:
						for (int vertX = selectionRanges[vert][min].getX(); vertX <= selectionRanges[vert][max].getX(); vertX++) {
							for (int vertY = selectionRanges[vert][min].getY(); vertY <= selectionRanges[vert][max].getY(); vertY++) {
								for (int vertZ = selectionRanges[vert][min].getZ(); vertZ <= selectionRanges[vert][max].getZ(); vertZ++) {
									BlockPos destroyPos = harvestedPos.offset(longX, longY, longZ).offset(vertX, vertY, vertZ);
									if (this.shouldHarvestBlock(world, harvestedBlockState, harvestedPos, destroyPos)) {
										world.destroyBlock(destroyPos, true);
									}
								}
							}
						}

					}
				}
			}
		}
	}

	/**
	 * Returns if the target block position should be area harvested.
	 * @param world The world to check in.
	 * @param harvestedBlockState The initial block harvested to compare to.
	 * @param harvestedPos The initial block position that was harvested at.
	 * @param targetPos The target area harvesting position to check.
	 * @return True if the block should be area harvested.
	 */
	public boolean shouldHarvestBlock(World world, BlockState harvestedBlockState, BlockPos harvestedPos, BlockPos targetPos) {
		if (harvestedPos.equals(targetPos)) {
			return false;
		}
		BlockState targetBlockState = world.getBlockState(targetPos);
		if (targetBlockState.getBlock() != harvestedBlockState.getBlock()) {
			return false;
		}

		// Don't area harvest Block Entities.
		if (world.getBlockEntity(targetPos) != null) {
			return false;
		}

		return this.canHarvestBlock(world.getBlockState(targetPos));
	}

	/**
	 * Called when a player right clicks on a block.
	 * @param context The item use context.
	 */
	public boolean onBlockUsed(ItemUseContext context) {
		if (!"hoe".equals(this.harvestType)) {
			return false;
		}

		int hook = net.minecraftforge.event.ForgeEventFactory.onHoeUse(context);
		if (hook != 0) return false;
		World world = context.getLevel();
		BlockPos blockPos = context.getClickedPos();
		if (context.getClickedFace() != Direction.DOWN && world.isEmptyBlock(blockPos.above())) {
			BlockState blockstate = HOE_LOOKUP.get(world.getBlockState(blockPos).getBlock());
			if (blockstate != null) {
				PlayerEntity playerentity = context.getPlayer();
				world.playSound(playerentity, blockPos, SoundEvents.HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
				if (!world.isClientSide) {
					world.setBlock(blockPos, blockstate, 11);
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * Called when a player right clicks on an entity.
	 * @param player The player using the equipment.
	 * @param entity The entity the player is using the equipment on.
	 * @param itemStack The equipment itemstack.
	 */
	public boolean onEntityInteraction(PlayerEntity player, LivingEntity entity, ItemStack itemStack) {
		if (!"shears".equals(this.harvestType) || player.getCommandSenderWorld().isClientSide) {
			return false;
		}

		if (entity instanceof net.minecraftforge.common.IForgeShearable) {
			net.minecraftforge.common.IForgeShearable target = (net.minecraftforge.common.IForgeShearable)entity;
			BlockPos pos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());
			if (target.isShearable(itemStack, entity.level, pos)) {
				java.util.List<ItemStack> drops = target.onSheared(player, itemStack, entity.level, pos,
						net.minecraft.enchantment.EnchantmentHelper.getItemEnchantmentLevel(net.minecraft.enchantment.Enchantments.BLOCK_FORTUNE, itemStack));
				java.util.Random rand = new java.util.Random();
				drops.forEach(d -> {
					net.minecraft.entity.item.ItemEntity ent = entity.spawnAtLocation(d, 1.0F);
					ent.setDeltaMovement(ent.getDeltaMovement().add((rand.nextFloat() - rand.nextFloat()) * 0.1F, rand.nextFloat() * 0.05F, (rand.nextFloat() - rand.nextFloat()) * 0.1F));
				});
			}
			return true;
		}

		return false;
	}
}
