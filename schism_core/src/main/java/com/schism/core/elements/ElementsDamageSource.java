package com.schism.core.elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class ElementsDamageSource implements IHasElements
{
    protected final List<ElementDefinition> elements;

    public ElementsDamageSource(List<ElementDefinition> elements)
    {
        this.elements = elements;
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        if (!elements.isEmpty()) {
            elements.forEach(element -> {
                createDamageTypeResources(event,element.type(),DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.5F, DamageEffects.BURNING,"config/schism/damage_types");
            });
        }
    }
    public void createDamageTypeResources(ServerStartingEvent event, String messageID,DamageScaling scaling,float exhaustion, DamageEffects effects, String filePath) {
        // Generate JSON data dynamically
        DamageType damageType = new DamageType(messageID,scaling, exhaustion, effects,DeathMessageType.DEFAULT);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String jsonString = gson.toJson(damageType);

        // Save JSON to a temporary file or inject directly
        try {
            Path path = FMLPaths.GAMEDIR.get().resolve(filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, jsonString.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Inject JSON data into the game (assuming you have a mechanism for this)
            injectJsonData(event.getServer().getResourceManager(), path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void injectJsonData(ResourceManager resourceManager, Path path) {
        // Implement your custom logic to load the JSON file into the game
        // This part will depend on what you're trying to achieve and the Minecraft version/modding framework
    }


    public DamageSource elementsDamage(Entity p_270560_, @Nullable Entity p_270646_) {
        var damagetype = ResourceKey.createRegistryKey(new ResourceLocation("damage_type"));
        ResourceKey<Object> key = ResourceKey.create(damagetype,DamageTypes.THORNS.location());
        var holder = p_270560_.level().registryAccess().registryOrThrow(damagetype).getHolderOrThrow(key);
        DamageType type = (DamageType) holder.get();
        Holder<DamageType> holder1 = Holder.direct(type);
        return new DamageSource(holder1,p_270560_,p_270646_);
    }
    public DamageSource createDamageSource(Level level, ResourceLocation name) {
        var damagetype = ResourceKey.createRegistryKey(new ResourceLocation("damage_type"));
        ResourceKey<Object> key = ResourceKey.create(damagetype,name);
        var holder = level.registryAccess().registryOrThrow(damagetype).getHolderOrThrow(key);
        DamageType type = (DamageType) holder.get();
        Holder<DamageType> holder1 = Holder.direct(type);
        return new DamageSource(holder1);
    }
    @Override
    public List<ElementDefinition> elements()
    {
        return this.elements;
    }
}
