package com.lycanitesmobs.core.info;

import com.google.gson.JsonObject;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.core.FileLoader;
import com.lycanitesmobs.core.JSONLoader;
import com.lycanitesmobs.core.StreamLoader;
import com.lycanitesmobs.core.block.*;
import com.lycanitesmobs.core.block.building.HiveBlock;
import com.lycanitesmobs.core.block.effect.*;
import com.lycanitesmobs.core.block.fluid.*;
import com.lycanitesmobs.core.item.*;
import com.lycanitesmobs.core.item.consumable.*;
import com.lycanitesmobs.core.item.equipment.ItemEquipment;
import com.lycanitesmobs.core.item.special.ItemSoulContract;
import com.lycanitesmobs.core.item.special.ItemSoulgazer;
import com.lycanitesmobs.core.item.special.ItemSoulkey;
import com.lycanitesmobs.core.item.special.ItemSoulstone;
import com.lycanitesmobs.core.item.summoningstaff.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ItemManager extends JSONLoader {
	public static ItemManager INSTANCE;
//	public static final DeferredRegister<Fluid> FLUIDS = new DeferredRegister<>(ForgeRegistries.FLUIDS, LycanitesMobs.MODID); TODO Probably moved to a reg event finally...

	public Map<String, ItemInfo> items = new HashMap<>();

	/** A list of blocks that need to use the cutout renderer. **/
	public List<Block> cutoutBlocks = new ArrayList<>();

	/** A list of fluids blocks for reference (the worldgen manager uses this to automatically create lakes, etc). **/
	public Map<String, BaseFluidBlock> worldgenFluidBlocks = new HashMap<>();

	/** A list of mod groups that have loaded with this manager. **/
	public List<ModInfo> loadedGroups = new ArrayList<>();

	/** Handles all global item general config settings. **/
	public ItemConfig config;

	// Creative Tabs:
	public final ItemGroup itemsGroup = new LMItemsGroup(LycanitesMobs.MODID + ".items");
	public final ItemGroup blocksGroup = new LMBlocksGroup(LycanitesMobs.MODID + ".blocks");
	public final ItemGroup creaturesGroups = new LMCreaturesGroup(LycanitesMobs.MODID + ".creatures");
	public final ItemGroup chargesGroup = new LMChargesGroup(LycanitesMobs.MODID + ".charges");
	public final ItemGroup equipmentPartsGroup = new LMEquipmentPartsGroup(LycanitesMobs.MODID + ".equipmentparts");


	/** Returns the main Item Manager instance or creates it and returns it. **/
	public static ItemManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new ItemManager();
		}
		return INSTANCE;
	}

	/**
	 * Called during startup and initially loads everything in this manager.
	 * @param modInfo The mod loading this manager.
	 */
	public void startup(ModInfo modInfo) {
		this.loadItems();
		this.loadAllFromJson(modInfo);
	}


	/** Loads all JSON Items. **/
	public void loadAllFromJson(ModInfo modInfo) {
		if(!this.loadedGroups.contains(modInfo)) {
			this.loadedGroups.add(modInfo);
		}
		this.loadAllJson(modInfo, "Items", "items", "name", true, null, FileLoader.COMMON, StreamLoader.COMMON);
		LycanitesMobs.logDebug("Items", "Complete! " + this.items.size() + " JSON Items Loaded In Total.");
	}

	@Override
	public void parseJson(ModInfo modInfo, String loadGroup, JsonObject json) {
		ItemInfo itemInfo = new ItemInfo(modInfo);
		itemInfo.loadFromJSON(json);
		this.items.put(itemInfo.name, itemInfo);
	}

	@SubscribeEvent
	public void registerItems(RegistryEvent.Register<Item> event) {
		for(ItemInfo itemInfo : items.values()) {
			LycanitesMobs.logDebug("Item", "Registering item: " + itemInfo.getName());
			event.getRegistry().register(itemInfo.getItem());
		}
	}

	/** Called during early start up, loads all global configs into this manager. **/
	public void loadConfig() {
		ItemConfig.loadGlobalSettings();
	}

	/** Called during early start up, loads all non-json items. **/
	public void loadItems() {
		ModInfo modInfo = LycanitesMobs.modInfo;
		Item.Properties itemProperties = new Item.Properties().tab(this.itemsGroup);
		Item.Properties itemPropertiesNoStack = new Item.Properties().tab(this.itemsGroup).stacksTo(1);

		ObjectManager.addItem("soulgazer", new ItemSoulgazer(itemPropertiesNoStack));
		ObjectManager.addItem("mobtoken", new ItemMobToken(new Item.Properties(), modInfo));
		ObjectManager.addItem("soulstone", new ItemSoulstone(itemProperties, null));
		ObjectManager.addItem("soul_contract", new ItemSoulContract(itemPropertiesNoStack));

		// Equipment Pieces:
		Item.Properties equipmentProperties = new Item.Properties().stacksTo(1).setNoRepair().setISTER(() -> com.lycanitesmobs.client.renderer.EquipmentRenderer::new);
		ObjectManager.addItem("equipment", new ItemEquipment(equipmentProperties));

		// Keys:
		ObjectManager.addItem("soulkey", new ItemSoulkey(itemProperties, "soulkey", 0));
		ObjectManager.addItem("soulkeydiamond", new ItemSoulkey(itemProperties, "soulkeydiamond", 1));
		ObjectManager.addItem("soulkeyemerald", new ItemSoulkey(itemProperties, "soulkeyemerald", 2));


		// Utilities:
		ObjectManager.addBlock("summoningpedestal", new BlockSummoningPedestal(Block.Properties.of(Material.METAL).strength(5, 10).sound(SoundType.METAL), modInfo));
		ObjectManager.addBlock("equipmentforge_lesser", new BlockEquipmentForge(Block.Properties.of(Material.WOOD).strength(5, 10).sound(SoundType.WOOD), modInfo, 1));
		ObjectManager.addBlock("equipmentforge_greater", new BlockEquipmentForge(Block.Properties.of(Material.STONE).strength(5, 20).sound(SoundType.STONE), modInfo, 2));
		ObjectManager.addBlock("equipmentforge_master", new BlockEquipmentForge(Block.Properties.of(Material.METAL).strength(5, 1000).sound(SoundType.METAL), modInfo, 3));
		ObjectManager.addBlock("equipment_infuser", new EquipmentInfuserBlock(Block.Properties.of(Material.METAL).strength(5, 1000).sound(SoundType.METAL), modInfo));
		ObjectManager.addBlock("equipment_station", new EquipmentStationBlock(Block.Properties.of(Material.METAL).strength(5, 1000).sound(SoundType.METAL), modInfo));


		// Buff Items:
		ObjectManager.addItem("immunizer", new ItemImmunizer(itemProperties));
		ObjectManager.addItem("cleansingcrystal", new ItemCleansingCrystal(itemProperties));


		// Seasonal Items:
		ObjectManager.addItem("halloweentreat", new ItemHalloweenTreat(itemProperties));
		ObjectManager.addItem("wintergift", new ItemWinterGift(itemProperties));
		ObjectManager.addItem("wintergiftlarge", new ItemWinterGiftLarge(itemProperties));


		// Special:
		ObjectManager.addItem("frostyfur", new ItemBlockPlacer(itemProperties, "frostyfur", "frostcloud"));
		ObjectManager.addItem("poisongland", new ItemBlockPlacer(itemProperties, "poisongland", "poisoncloud"));
		ObjectManager.addItem("geistliver", new ItemBlockPlacer(itemProperties, "geistliver", "shadowfire"));


		// Summoning Staves:
		Item.Properties summoningStaffProperties = new Item.Properties().tab(this.itemsGroup).stacksTo(1).durability(500);
		ObjectManager.addItem("summoningstaff", new ItemStaffSummoning(summoningStaffProperties, "summoningstaff", "summoningstaff"));
		ObjectManager.addItem("stablesummoningstaff", new ItemStaffStable(summoningStaffProperties, "stablesummoningstaff", "staffstable"));
		ObjectManager.addItem("bloodsummoningstaff", new ItemStaffBlood(summoningStaffProperties, "bloodsummoningstaff", "staffblood"));
		ObjectManager.addItem("sturdysummoningstaff", new ItemStaffSturdy(summoningStaffProperties, "sturdysummoningstaff", "staffsturdy"));
		ObjectManager.addItem("savagesummoningstaff", new ItemStaffSavage(summoningStaffProperties, "savagesummoningstaff", "staffsavage"));


		// Building Blocks:
		BlockMaker.addDungeonBlocks(modInfo, "lush");
		BlockMaker.addDungeonBlocks(modInfo, "desert");
		BlockMaker.addDungeonBlocks(modInfo, "shadow");
		BlockMaker.addDungeonBlocks(modInfo, "demon");
		BlockMaker.addDungeonBlocks(modInfo, "aberrant");
		BlockMaker.addDungeonBlocks(modInfo, "ashen");
		ObjectManager.addBlock("soulcubedemonic", new BlockBase(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), modInfo, "soulcubedemonic"));
		ObjectManager.addBlock("soulcubeundead", new BlockBase(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), modInfo, "soulcubeundead"));
		ObjectManager.addBlock("soulcubeaberrant", new BlockBase(Block.Properties.of(Material.STONE).sound(SoundType.STONE).strength(2F, 1200.0F), modInfo, "soulcubeaberrant"));
		ObjectManager.addBlock("propolis", new HiveBlock(Block.Properties.of(Material.CLAY).sound(SoundType.WET_GRASS).strength(0.6F).randomTicks(), "propolis"));
		ObjectManager.addBlock("veswax", new HiveBlock(Block.Properties.of(Material.WOOD).sound(SoundType.WOOD).strength(0.6F).randomTicks(), "veswax"));


		// Effect Blocks:
		Block.Properties fireProperties = Block.Properties.of(Material.FIRE).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion();
		Block.Properties brightFireProperties = Block.Properties.of(Material.FIRE).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion().lightLevel((BlockState blockState) -> { return 15; });
		ObjectManager.addSound("frostfire", modInfo, "block.frostfire");
		ObjectManager.addBlock("frostfire", new BlockFrostfire(fireProperties));
		ObjectManager.addSound("icefire", modInfo, "block.icefire");
		ObjectManager.addBlock("icefire", new BlockIcefire(fireProperties));
		ObjectManager.addSound("hellfire", modInfo, "block.hellfire");
		ObjectManager.addBlock("hellfire", new BlockHellfire(brightFireProperties));
		ObjectManager.addSound("doomfire", modInfo, "block.doomfire");
		ObjectManager.addBlock("doomfire", new BlockDoomfire(brightFireProperties));
		ObjectManager.addSound("primefire", modInfo, "block.primefire");
		ObjectManager.addBlock("primefire", new BlockPrimefire(brightFireProperties));
		ObjectManager.addSound("scorchfire", modInfo, "block.scorchfire");
		ObjectManager.addBlock("scorchfire", new BlockScorchfire(brightFireProperties));
		ObjectManager.addSound("shadowfire", modInfo, "block.shadowfire");
		ObjectManager.addBlock("shadowfire", new BlockShadowfire(fireProperties));
		ObjectManager.addSound("smitefire", modInfo, "block.smitefire");
		ObjectManager.addBlock("smitefire", new BlockSmitefire(brightFireProperties));

		Block.Properties cloudProperties = Block.Properties.of(Material.DECORATION).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion();
		ObjectManager.addSound("frostcloud", modInfo, "block.frostcloud");
		ObjectManager.addBlock("frostcloud", new BlockFrostCloud(cloudProperties));
		ObjectManager.addSound("poisoncloud", modInfo, "block.poisoncloud");
		ObjectManager.addBlock("poisoncloud", new BlockPoisonCloud(cloudProperties));
		ObjectManager.addSound("poopcloud", modInfo, "block.poopcloud");
		ObjectManager.addBlock("poopcloud", new BlockPoopCloud(cloudProperties));

		Block.Properties webProperties = Block.Properties.of(Material.WEB).randomTicks().noCollission().dynamicShape().sound(SoundType.WOOL).noOcclusion();
		ObjectManager.addBlock("quickweb", new BlockQuickWeb(webProperties));
		ObjectManager.addBlock("frostweb", new BlockFrostweb(webProperties));


		// Fluids:
		Block.Properties waterBlockProperties = Block.Properties.of(Material.WATER).noCollission().randomTicks().strength(100).noDrops();
		Block.Properties waterBrightBlockProperties = Block.Properties.of(Material.WATER).noCollission().randomTicks().strength(100).noDrops().lightLevel((BlockState blockState) -> { return 10; });
		Block.Properties lavaBlockProperties = Block.Properties.of(Material.LAVA).noCollission().randomTicks().strength(100).noDrops().lightLevel((BlockState blockState) -> { return 15; });
		this.addFluid("ooze", 0x009F9F, 3000, 3000, 0, 10, false, OozeFluidBlock.class, waterBrightBlockProperties, "frost", false, true);
		this.addFluid("rabbitooze", 0x00AFAF, 3000, 3000, 0, 10, true, OozeFluidBlock.class, waterBrightBlockProperties, "frost", false, false);
		this.addFluid("moglava", 0xFF5722, 3000, 5000, 1100, 15, true, MoglavaFluidBlock.class, lavaBlockProperties, "lava", false, true);
		this.addFluid("acid", 0x8BC34A, 1000, 10, 40, 10, false, AcidFluidBlock.class, waterBrightBlockProperties, "acid", true, true);
		this.addFluid("sharacid", 0x8BB35A, 1000, 10, 40, 10, true, AcidFluidBlock.class, waterBrightBlockProperties, "acid", false, false);
		this.addFluid("poison", 0x9C27B0, 1000, 8, 20, 0, false, PoisonFluidBlock.class, waterBlockProperties, "poison", false, true);
		this.addFluid("vesspoison", 0xAC27A0, 1000, 8, 20, 0, true, PoisonFluidBlock.class, waterBlockProperties, "poison", false, false);
		this.addFluid("veshoney", 0xCEBC39, 4000, 4000, 0, 0, false, VeshoneyFluidBlock.class, waterBlockProperties, "fae", false, false);
		ObjectManager.addDamageSource("ooze", new DamageSource("ooze"));
		ObjectManager.addDamageSource("acid", new DamageSource("acid"));
	}

	public void addFluid(String fluidName, int fluidColor, int density, int viscosity, int temperature, int luminosity, boolean multiply, Class<? extends BaseFluidBlock> blockClass, AbstractBlock.Properties blockProperties, String elementName, boolean destroyItems, boolean worldgen) {
		ElementInfo element = ElementManager.getInstance().getElement(elementName);
		Supplier<ForgeFlowingFluid> stillFluidSupplier = () -> ObjectManager.getFluid(fluidName);
		Supplier<ForgeFlowingFluid> flowingFluidSupplier = () -> ObjectManager.getFluid(fluidName + "_flowing");

		FluidAttributes.Builder fluidBuilder = FluidAttributes.builder(new ResourceLocation(LycanitesMobs.MODID, "block/" + fluidName + "_still"), new ResourceLocation(LycanitesMobs.MODID, "block/" + fluidName + "_flowing"));
		fluidBuilder.color(fluidColor);
		fluidBuilder.density(density);
		fluidBuilder.viscosity(viscosity);
		fluidBuilder.temperature(temperature);
		fluidBuilder.luminosity(luminosity);
		ForgeFlowingFluid.Properties fluidProperties = new ForgeFlowingFluid.Properties(stillFluidSupplier, flowingFluidSupplier, fluidBuilder);
		if(multiply) {
			fluidProperties.canMultiply();
		}
		fluidProperties.bucket(() -> ObjectManager.getItem(fluidName + "_bucket"));
		fluidProperties.block(() -> (FlowingFluidBlock)ObjectManager.getBlock(fluidName));

		ObjectManager.addFluid(fluidName, new CustomFluid.Still(fluidProperties, fluidName));
		ObjectManager.addFluid(fluidName + "_flowing", new CustomFluid.Flowing(fluidProperties, fluidName + "_flowing"));
		ObjectManager.addSound(fluidName, LycanitesMobs.modInfo, "block." + fluidName);

		Item.Properties bucketProperties = new Item.Properties().tab(this.itemsGroup).craftRemainder(Items.BUCKET).stacksTo(1);
		ObjectManager.addItem(fluidName + "_bucket", new BucketItem(stillFluidSupplier, bucketProperties).setRegistryName(LycanitesMobs.MODID, fluidName + "_bucket"));

		try {
			Constructor<? extends BaseFluidBlock> blockConstructor = blockClass.getDeclaredConstructor(Supplier.class, AbstractBlock.Properties.class, String.class, ElementInfo.class, boolean.class);
			BaseFluidBlock fluidBlock = blockConstructor.newInstance(stillFluidSupplier, blockProperties, fluidName, element, destroyItems);
			ObjectManager.addBlock(fluidName, fluidBlock);
			if (worldgen) {
				this.worldgenFluidBlocks.put(fluidName, fluidBlock);
			}
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			LycanitesMobs.logError("Error loading fluid: " + fluidName);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Determines the Equipment Sharpness repair amount of the provided itemstack. Stack size is not taken into account.
	 * @param itemStack The itemstack to check the item and nbt data of.
	 * @return The amount of Sharpness the provided itemstack restores.
	 */
	public int getEquipmentSharpnessRepair(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return 0;
		}

		Item item = itemStack.getItem();
		for (String itemId : ItemConfig.lowEquipmentSharpnessItems) {
			if (new ResourceLocation(itemId).equals(item.getRegistryName())) {
				return ItemConfig.lowEquipmentRepairAmount;
			}
		}
		for (String itemId : ItemConfig.mediumEquipmentSharpnessItems) {
			if (new ResourceLocation(itemId).equals(item.getRegistryName())) {
				return ItemConfig.mediumEquipmentRepairAmount;
			}
		}
		for (String itemId : ItemConfig.highEquipmentSharpnessItems) {
			if (new ResourceLocation(itemId).equals(item.getRegistryName())) {
				return ItemConfig.highEquipmentRepairAmount;
			}
		}
		for (String itemId : ItemConfig.maxEquipmentSharpnessItems) {
			if (new ResourceLocation(itemId).equals(item.getRegistryName())) {
				return ItemEquipment.SHARPNESS_MAX;
			}
		}
		return 0;
	}

	/**
	 * Determines the Equipment Mana repair amount of the provided itemstack. Stack size is not taken into account.
	 * @param itemStack The itemstack to check the item and nbt data of.
	 * @return The amount of Mana the provided itemstack restores.
	 */
	public int getEquipmentManaRepair(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return 0;
		}

		Item item = itemStack.getItem();
		if (item instanceof ChargeItem) {
			return ItemConfig.highEquipmentRepairAmount;
		}
		for (String itemId : ItemConfig.lowEquipmentManaItems) {
			if (new ResourceLocation(itemId).equals(item.getRegistryName())) {
				return ItemConfig.lowEquipmentRepairAmount;
			}
		}
		for (String itemId : ItemConfig.mediumEquipmentManaItems) {
			if (new ResourceLocation(itemId).equals(item.getRegistryName())) {
				return ItemConfig.mediumEquipmentRepairAmount;
			}
		}
		for (String itemId : ItemConfig.highEquipmentManaItems) {
			if (new ResourceLocation(itemId).equals(item.getRegistryName())) {
				return ItemConfig.highEquipmentRepairAmount;
			}
		}
		for (String itemId : ItemConfig.maxEquipmentManaItems) {
			if (new ResourceLocation(itemId).equals(item.getRegistryName())) {
				return ItemEquipment.MANA_MAX;
			}
		}
		return 0;
	}
}
