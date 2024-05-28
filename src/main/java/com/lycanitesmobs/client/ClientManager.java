package com.lycanitesmobs.client;

import com.lycanitesmobs.client.gui.*;
import com.lycanitesmobs.client.gui.beastiary.SummoningBeastiaryScreen;
import com.lycanitesmobs.client.gui.overlays.BaseOverlay;
import com.lycanitesmobs.client.localisation.LanguageLoader;
import com.lycanitesmobs.client.localisation.LanguageManager;
import com.lycanitesmobs.client.renderer.RenderRegister;
import com.lycanitesmobs.core.container.*;
import com.lycanitesmobs.core.info.CreatureManager;
import com.lycanitesmobs.core.info.CreatureType;
import com.lycanitesmobs.core.item.ItemColorCustomSpawnEgg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ClientManager {
	public static int GL_TEXTURE0 = 33984;
	public static int GL_TEXTURE1 = 33986;
	public static int GL_FULL_BRIGHT = 15728880;

	public static int FULL_BRIGHT = 240;

	protected static ClientManager INSTANCE;
	public static ClientManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new ClientManager();
		}
		return INSTANCE;
	}

	protected FontRenderer fontRenderer;

	/**
	 * Sets up the Language Manager used for additional language files.
	 */
	public void initLanguageManager() {
		LanguageManager.getInstance();
	}

	/**
	 * Registers all GUI Screens, etc used by this mod.
	 */
	public void registerScreens() {
		ScreenManager.register(CreatureContainer.TYPE, CreatureInventoryScreen::new);
		ScreenManager.register(SummoningPedestalContainer.TYPE, SummoningPedestalScreen::new);
		ScreenManager.register(EquipmentForgeContainer.TYPE, EquipmentForgeScreen::new);
		ScreenManager.register(EquipmentInfuserContainer.TYPE, EquipmentInfuserScreen::new);
		ScreenManager.register(EquipmentStationContainer.TYPE, EquipmentStationScreen::new);
	}

	/**
	 * Registers all client side events listeners.
	 */
	public void registerEvents() {
		// Event Listeners:
		MinecraftForge.EVENT_BUS.register(new KeyHandler(Minecraft.getInstance()));
		MinecraftForge.EVENT_BUS.register(new BaseOverlay(Minecraft.getInstance()));
		MinecraftForge.EVENT_BUS.register(new ClientEventListener());
		IResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
		if(resourceManager instanceof IReloadableResourceManager) {
			((IReloadableResourceManager)resourceManager).registerReloadListener(LanguageLoader.getInstance());
		}
	}

	/**
	 * Initialises the Render Register which loads and registers models and sets up render factories, etc.
	 */
	public void initRenderRegister() {
		RenderRegister renderRegister = new RenderRegister();
		renderRegister.registerModelLoaders();
		renderRegister.registerRenderFactories();
		renderRegister.registerBlockRenderLayers();
	}

	/**
	 * Returns the client player entity.
	 * @return Client player entity.
	 */
	public PlayerEntity getClientPlayer() {
		return Minecraft.getInstance().player;
	}

	/**
	 * Displays a Screen GUI on the client.
	 */
	public void displayGuiScreen(String screenName, PlayerEntity player) {
		if("beastiary".equals(screenName)) {
			Minecraft.getInstance().setScreen(new SummoningBeastiaryScreen(player));
		}
	}

	/**
	 * Returns the Font Renderer used by Lycanites Mobs.
	 * @return A sexy Font Renderer, thanks for the heads up CedKilleur!
	 */
    public FontRenderer getFontRenderer() {
    	return Minecraft.getInstance().font;
		/*if(this.fontRenderer == null) {
			ResourceLocation fontResource = new ResourceLocation(LycanitesMobs.MODID, "fonts/diavlo_light.otf");
			this.fontRenderer = new FontRenderer(Minecraft.getInstance().getTextureManager(), new Font(Minecraft.getInstance().getTextureManager(), fontResource));
		}
		return this.fontRenderer;*/
	}

	/**
	 * Registers Colored Items
	 */
	@SubscribeEvent
	public void registerItemColors(ColorHandlerEvent.Item event) {
		for(CreatureType creatureType : CreatureManager.getInstance().creatureTypes.values()) {
			if(creatureType.spawnEgg != null) {
				event.getItemColors().register(new ItemColorCustomSpawnEgg(), creatureType.spawnEgg);
			}
		}
	}
}