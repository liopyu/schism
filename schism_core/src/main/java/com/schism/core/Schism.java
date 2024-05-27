package com.schism.core;

import com.schism.core.blocks.BlockRepository;
import com.schism.core.client.CreaturesClient;
import com.schism.core.client.KeyHandler;
import com.schism.core.client.gui.GuiSounds;
import com.schism.core.client.renderers.RendererRegister;
import com.schism.core.client.gui.CreatureDebugOverlay;
import com.schism.core.client.gui.CreatureOverlay;
import com.schism.core.client.gui.Notifications;
import com.schism.core.commands.CommandRepository;
import com.schism.core.creatures.Creatures;
import com.schism.core.creatures.CreatureRepository;
import com.schism.core.database.Database;
import com.schism.core.effects.EffectEventListener;
import com.schism.core.effects.EffectRepository;
import com.schism.core.elements.ElementEventListener;
import com.schism.core.generation.LevelGenerator;
import com.schism.core.items.ItemRepository;
import com.schism.core.network.MessageRepository;
import com.schism.core.particles.ParticleRepository;
import com.schism.core.projectiles.ProjectileRepository;
import com.schism.core.resolvers.SoundEventResolver;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("schism")
public class Schism
{
    public static final String MOD_NAME = "Schism";
    public static final String NAMESPACE = "schism";

    static {
        Database.get().coreLoadListeners();
    }

    public Schism()
    {
        // Setup Events:
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        MinecraftForge.EVENT_BUS.register(this);

        // Load from Database:
        Database.get().loadJson();
        Database.get().logCoreDefinitions();

        // Mod Loading Events:
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(BlockRepository.get());
        modEventBus.register(ItemRepository.get());
        modEventBus.register(EffectRepository.get());
        modEventBus.register(ParticleRepository.get());
        modEventBus.register(CreatureRepository.get());
        modEventBus.register(ProjectileRepository.get());
        modEventBus.register(RendererRegister.get());
        modEventBus.register(SoundEventResolver.get());
        GuiSounds.get();

        // Game Event Listeners:
        MinecraftForge.EVENT_BUS.register(CommandRepository.get());
        MinecraftForge.EVENT_BUS.register(EffectEventListener.get());
        MinecraftForge.EVENT_BUS.register(ElementEventListener.get());
        MinecraftForge.EVENT_BUS.register(Creatures.get());
        MinecraftForge.EVENT_BUS.register(LevelGenerator.get());

        // Networking:
        MessageRepository.get().register();
    }

    /**
     * Called during mod setup, performs mod initialisation.
     * @param event The forge setup event.
     */
    private void setup(final FMLCommonSetupEvent event)
    {
        Log.info("core", "Starting up!");
    }

    /**
     * Called during server startup, performs server side only mod initialisation.
     * @param event The forge server starting event.
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        Log.info("core", "Server side startup!");
    }

    /**
     * Called during client setup, performs client side only mod initialisation.
     * @param event The forge client setup event.
     */
    @SubscribeEvent
    public void clientSetup(final FMLClientSetupEvent event)
    {
        // Client Setup:
        RendererRegister.get().setup();

        // Client Event Listeners:
        MinecraftForge.EVENT_BUS.register(Notifications.get());
        MinecraftForge.EVENT_BUS.register(CreatureOverlay.get());
        MinecraftForge.EVENT_BUS.register(CreatureDebugOverlay.get());
        MinecraftForge.EVENT_BUS.register(CreaturesClient.get());
        MinecraftForge.EVENT_BUS.register(KeyHandler.get());
    }

    /**
     * Send IMC Events, currently unused.
     * @param event The IMC event.
     */
    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        //
    }

    /**
     * Process IMC Events, currently unused.
     * @param event The IMC event.
     */
    private void processIMC(final InterModProcessEvent event)
    {
        //
    }
}
