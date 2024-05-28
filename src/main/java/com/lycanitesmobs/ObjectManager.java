package com.lycanitesmobs;

import com.lycanitesmobs.core.EffectBase;
import com.lycanitesmobs.core.container.*;
import com.lycanitesmobs.core.entity.EntityFactory;
import com.lycanitesmobs.core.info.ItemManager;
import com.lycanitesmobs.core.info.ModInfo;
import com.lycanitesmobs.core.info.ObjectLists;
import com.lycanitesmobs.core.tileentity.EquipmentInfuserTileEntity;
import com.lycanitesmobs.core.tileentity.EquipmentStationTileEntity;
import com.lycanitesmobs.core.tileentity.TileEntityEquipmentForge;
import com.lycanitesmobs.core.tileentity.TileEntitySummoningPedestal;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.RegistryObject;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ObjectManager {
	public static ObjectManager INSTANCE;
	public static ObjectManager getInstance() {
		if(INSTANCE == null)
			INSTANCE = new ObjectManager();
		return INSTANCE;
	}
	
	// Maps:
	public static Map<String, Block> blocks = new HashMap<>();
    public static Map<String, Item> items = new HashMap<>();
    public static Map<Item, ModInfo> itemGroups = new HashMap<>();
	public static Map<String, ForgeFlowingFluid> fluids = new HashMap<>();
    public static Map<Block, Item> buckets = new HashMap<>();
    public static Map<String, Class> tileEntities = new HashMap<>();
	public static Map<String, EffectBase> effects = new HashMap<>();
	public static Map<String, SoundEvent> sounds = new HashMap<>();

	// Type Maps:
	public static Map<String, Class<? extends Entity>> specialEntities = new HashMap<>();
	public static Map<Class<? extends Entity>, Constructor<? extends Entity>> specialEntityConstructors = new HashMap<>();
	public static Map<Class<? extends Entity>, EntityType<? extends Entity>> specialEntityTypes = new HashMap<>();
	public static Map<Class<? extends TileEntity>, TileEntityType<? extends TileEntity>> tileEntityTypes = new HashMap<>();

    public static Map<String, DamageSource> damageSources = new HashMap<>();

	public static ModInfo currentModInfo;

	/** The next available network id for special entities to register by. **/
	protected static int nextSpecialEntityNetworkId = 0;

	
    // ==================================================
    //                        Setup
    // ==================================================
	public static void setCurrentModInfo(ModInfo group) {
		currentModInfo = group;
	}


	/**
	 * Generates the next available special entity network id to register with.
	 * @return The next special entity network id.
	 */
	public static int getNextSpecialEntityNetworkId() {
		return nextSpecialEntityNetworkId++;
	}
	
	
    // ==================================================
    //                        Add
    // ==================================================
	// ========== Block ==========
	public static Block addBlock(String name, Block block) {
		blocks.put(name, block);
        return block;
	}

	// ========== Fluid ==========
	public static Fluid addFluid(String name, ForgeFlowingFluid fluid) {
        fluids.put(name, fluid);
        return fluid;
	}

	// ========== Item ==========
	public static Item addItem(String name, Item item) {
		items.put(name, item);
        itemGroups.put(item, currentModInfo);
        return item;
	}

    // ========== Tile Entity ==========
    public static Class addTileEntity(String name, Class tileEntityClass) {
        name = name.toLowerCase();
        tileEntities.put(name, tileEntityClass);
        return tileEntityClass;
    }

    // ========== Potion Effect ==========
	public static EffectBase addPotionEffect(String name, boolean isBad, int color, boolean goodEffect) {
        EffectBase effect = new EffectBase(name, isBad, color);
		effects.put(name, effect);
		ObjectLists.addEffect(goodEffect ? "buffs" : "debuffs", effect, name);

		return effect;
	}

	// ========== Special Entity ==========
	public static void addSpecialEntity(String name, Class<? extends Entity> entityClass, Constructor<? extends Entity> specialEntityConstructor) {
		specialEntities.put(name, entityClass);
		specialEntityConstructors.put(entityClass, specialEntityConstructor);

	}

    // ========== Damage Source ==========
    public static void addDamageSource(String name, DamageSource damageSource) {
        name = name.toLowerCase();
        damageSources.put(name, damageSource);
    }

	// ========== Sound ==========
	public static void addSound(String name, ModInfo modInfo, String path) {
		name = name.toLowerCase();
		ResourceLocation resourceLocation = new ResourceLocation(modInfo.modid, path);
		SoundEvent soundEvent = new SoundEvent(resourceLocation);
		soundEvent.setRegistryName(resourceLocation);
		sounds.put(name, soundEvent);
	}
	
	
    // ==================================================
    //                        Get
    // ==================================================
	// ========== Block ==========
	public static Block getBlock(String name) {
		name = name.toLowerCase();
		if(!blocks.containsKey(name))
			return null;
		return blocks.get(name);
	}
	
	// ========== Item ==========
	public static Item getItem(String name) {
		name = name.toLowerCase();
		if(!items.containsKey(name))
			return null;
		return items.get(name);
	}

	// ========== Fluid ==========
	public static ForgeFlowingFluid getFluid(String name) {
		name = name.toLowerCase();
		if(!fluids.containsKey(name))
			return null;
		return fluids.get(name);
	}

    // ========== Tile Entity ==========
    public static Class getTileEntity(String name) {
        name = name.toLowerCase();
        if(!tileEntities.containsKey(name)) return null;
        return tileEntities.get(name);
    }
	
	// ========== Potion Effect ==========
	public static EffectBase getEffect(String name) {
		name = name.toLowerCase();
		if(!effects.containsKey(name)) return null;
		return effects.get(name);
	}

    // ========== Damage Source ==========
    public static DamageSource getDamageSource(String name) {
        name = name.toLowerCase();
        if(!damageSources.containsKey(name)) return null;
        return damageSources.get(name);
    }

	// ========== Sound ==========
	public static SoundEvent getSound(String name) {
		name = name.toLowerCase();
		if(!sounds.containsKey(name))
			return null;
		return sounds.get(name);
	}


    // ==================================================
    //                  Registry Events
    // ==================================================
    // ========== Blocks ==========
	@SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
		for(Block block : blocks.values()) {
			LycanitesMobs.logDebug("Item", "Registering block: " + block.getRegistryName());
            if(block.getRegistryName() == null) {
                LycanitesMobs.logWarning("", "Block: " + block + " has no Registry Name!");
            }
			event.getRegistry().register(block);
        }
    }

    // ========== Items ==========
	@SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
		for(Item item : items.values()) {
	    	LycanitesMobs.logDebug("Item", "Registering item: " + item.getRegistryName());
	        if(item.getRegistryName() == null) {
	            LycanitesMobs.logWarning("", "Item: " + item + " has no Registry Name!");
            }
            event.getRegistry().register(item);
        }

	    Item.Properties blockItemProperties = new Item.Properties().tab(ItemManager.getInstance().blocksGroup);
		for(Block block : blocks.values()) {
			BlockItem blockItem = new BlockItem(block, blockItemProperties);
			blockItem.setRegistryName(block.getRegistryName());
			LycanitesMobs.logDebug("Item", "Registering item block: " + blockItem.getRegistryName());
			if(block.getRegistryName() == null) {
				LycanitesMobs.logWarning("", "Block Item: " + blockItem + " has no Registry Name!");
			}
			event.getRegistry().register(blockItem);
		}
    }

    // ========== Potions ==========
	@SubscribeEvent
    public void registerEffects(RegistryEvent.Register<Effect> event) {
		for(EffectBase effect : effects.values()) {
        	event.getRegistry().register(effect);
		}
    }

	// ========== Fluids ==========
	@SubscribeEvent
	public void registerFluids(RegistryEvent.Register<Fluid> event) {
		for(Fluid fluid : fluids.values()) {
			event.getRegistry().register(fluid);
		}
	}

	// ========== Entities ==========
	@SubscribeEvent
	public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
		for(String entityName : specialEntities.keySet()) {
			EntityType.Builder entityTypeBuilder = EntityType.Builder.of(EntityFactory.getInstance(), EntityClassification.MISC);
			entityTypeBuilder.setTrackingRange(10);
			entityTypeBuilder.setUpdateInterval(3);
			entityTypeBuilder.setShouldReceiveVelocityUpdates(true);
			entityTypeBuilder.noSummon();

			EntityType entityType = entityTypeBuilder.build(entityName);
			entityType.setRegistryName(LycanitesMobs.MODID, entityName);
			EntityFactory.getInstance().addEntityType(entityType, specialEntityConstructors.get(specialEntities.get(entityName)), entityName);
			specialEntityTypes.put(specialEntities.get(entityName), entityType);
			event.getRegistry().register(entityType);
		}
	}

	// ========== Sounds ==========
	@SubscribeEvent
	public void registerSounds(RegistryEvent.Register<SoundEvent> event) {
		for(SoundEvent soundEvent : sounds.values()) {
			if(soundEvent.getRegistryName() == null) {
				LycanitesMobs.logWarning("", "Sound: " + soundEvent + " has no Registry Name!");
			}
			event.getRegistry().register(soundEvent);
		}
	}

	// ========== Containers ==========
	@SubscribeEvent
	public void registerContainers(RegistryEvent.Register<ContainerType<?>> event) {
		event.getRegistry().register(CreatureContainer.TYPE);
		event.getRegistry().register(SummoningPedestalContainer.TYPE);
		event.getRegistry().register(EquipmentForgeContainer.TYPE);
		event.getRegistry().register(EquipmentInfuserContainer.TYPE);
		event.getRegistry().register(EquipmentStationContainer.TYPE);
	}

	// ========== Tile Entities ==========
	@SubscribeEvent
	public void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
		TileEntityType<TileEntity> summoningPedestalType = TileEntityType.Builder.of((Supplier<TileEntity>) TileEntitySummoningPedestal::new,
				getBlock("summoningpedestal")
		).build(null);
		summoningPedestalType.setRegistryName(LycanitesMobs.MODID, "summoningpedestal");
		event.getRegistry().register(summoningPedestalType);
		tileEntityTypes.put(TileEntitySummoningPedestal.class, summoningPedestalType);

		TileEntityType<TileEntity> equipmentForgeType = TileEntityType.Builder.of((Supplier<TileEntity>) TileEntityEquipmentForge::new,
				getBlock("equipmentforge_lesser"),
				getBlock("equipmentforge_greater"),
				getBlock("equipmentforge_master")
		).build(null);
		equipmentForgeType.setRegistryName(LycanitesMobs.MODID, "equipmentforge");
		event.getRegistry().register(equipmentForgeType);
		tileEntityTypes.put(TileEntityEquipmentForge.class, equipmentForgeType);

		TileEntityType<TileEntity> equipmentInfuserType = TileEntityType.Builder.of((Supplier<TileEntity>) EquipmentInfuserTileEntity::new,
				getBlock("equipment_infuser")
		).build(null);
		equipmentInfuserType.setRegistryName(LycanitesMobs.MODID, "equipment_infuser");
		event.getRegistry().register(equipmentInfuserType);
		tileEntityTypes.put(EquipmentInfuserTileEntity.class, equipmentInfuserType);

		TileEntityType<TileEntity> equipmentStationType = TileEntityType.Builder.of((Supplier<TileEntity>) EquipmentStationTileEntity::new,
				getBlock("equipment_station")
		).build(null);
		equipmentStationType.setRegistryName(LycanitesMobs.MODID, "equipment_station");
		event.getRegistry().register(equipmentStationType);
		tileEntityTypes.put(EquipmentStationTileEntity.class, equipmentStationType);
	}
}
