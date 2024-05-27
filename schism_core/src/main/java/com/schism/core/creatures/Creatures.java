package com.schism.core.creatures;

import com.schism.core.Schism;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Creatures
{
    private static final Creatures INSTANCE = new Creatures();

    protected final Map<LivingEntity, LazyOptional<ICreature>> serverCache;
    protected final Map<LivingEntity, LazyOptional<ICreature>> clientCache;
    protected final Map<LivingEntity, CreatureCapabilityProvider> serverProviders;
    protected final Map<LivingEntity, CreatureCapabilityProvider> clientProviders;

    protected Creatures()
    {
        this.serverCache = new HashMap<>();
        this.clientCache = new HashMap<>();
        this.serverProviders = new HashMap<>();
        this.clientProviders = new HashMap<>();
    }

    /**
     * Gets the singleton Creatures instance.
     * @return Creatures which provides creature capabilities for entities and responds to various events for creatures.
     */
    public static Creatures get()
    {
        return INSTANCE;
    }

    /**
     * Gets a creature capability for the provided entity.
     * @return An optional creature capability for the provided entity empty if no capability is available.
     */
    public Optional<ICreature> getCreature(LivingEntity livingEntity)
    {
        Map<LivingEntity, LazyOptional<ICreature>> cache = livingEntity.level().isClientSide() ? this.clientCache : this.serverCache;
        if (cache.containsKey(livingEntity)) {
            return cache.get(livingEntity).resolve();
        }
        LazyOptional<ICreature> lazyCreature = livingEntity.getCapability(CreatureCapabilityProvider.CAPABILITY);
        cache.put(livingEntity, lazyCreature);
        return lazyCreature.resolve();
    }

    /**
     * Creates a capability provider for the provided living entity.
     * @param livingEntity The living entity to create a capability provider for.
     * @return An optional capability provider which is empty if the entity cannot have a provider.
     */
    public Optional<CreatureCapabilityProvider> createProvider(LivingEntity livingEntity)
    {
        Optional<CreatureDefinition> creatureDefinition = CreatureRepository.get().getDefinition(livingEntity);
        if (creatureDefinition.isEmpty()) {
            return Optional.empty();
        }
        CreatureCapabilityProvider provider = new CreatureCapabilityProvider(livingEntity, creatureDefinition.get());
        return Optional.of(provider);
    }

    /**
     * Removes capability providers and cache for the provided living entity if one exists.
     * @param livingEntity The living entity to remove providers and cached capabilities for.
     */
    public void removeProvider(LivingEntity livingEntity)
    {
        Map<LivingEntity, CreatureCapabilityProvider> providers = livingEntity.level().isClientSide() ? this.clientProviders : this.serverProviders;
        if (providers.containsKey(livingEntity)) {
            providers.get(livingEntity).lazyCapability().invalidate();
            providers.remove(livingEntity);
        }

        Map<LivingEntity, LazyOptional<ICreature>> cache = livingEntity.level().isClientSide() ? this.clientCache : this.serverCache;
        cache.remove(livingEntity);
    }

    /**
     * Called when an entity performs an update tick.
     * @param event The living entity update tick event.
     */
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingTickEvent event)
    {
        this.getCreature(event.getEntity()).ifPresent(ICreature::tick);
    }

    /**
     * Called when an entity is hurt.
     * @param event The entity hurt event.
     */
    @SubscribeEvent
    public void onEntityHurt(LivingHurtEvent event)
    {
        if (event.isCanceled() || event.getEntity() == null) {
            return;
        }

        float amount = event.getAmount();
        List<ElementDefinition> damageElements = ElementRepository.get().damageElements(event.getSource(), event.getEntity().level(), event.getEntity().blockPosition());

        // Damage Dealt:
        if (event.getSource().getEntity() instanceof LivingEntity attackingEntity) {
            ICreature attackingCreature = this.getCreature(attackingEntity).orElse(null);
            if (attackingCreature != null) {
                amount = attackingCreature.onDamage(amount, event.getSource(), damageElements, event.getEntity());
            }
        }

        // Damage Taken:
        ICreature creature = this.getCreature(event.getEntity()).orElse(null);
        if (creature != null) {
            amount = creature.onHurt(amount, event.getSource(), damageElements);
        }

        // Update Damage Amount:
        if (amount <= 0) {
            event.setCanceled(true);
            if (amount < 0) {
                event.getEntity().heal(-amount);
            }
        }
        event.setAmount(amount);
    }

    /**
     * Called when an entity dies.
     * @param event The entity death event.
     */
    @SubscribeEvent
    public void onEntityDeath(LivingDeathEvent event)
    {
        if (event.isCanceled() || event.getEntity() == null) {
            return;
        }

        List<ElementDefinition> damageElements = ElementRepository.get().damageElements(event.getSource(), event.getEntity().level(), event.getEntity().blockPosition());

        // On Kill:
        if (event.getSource().getEntity() instanceof LivingEntity attackingEntity) {
            this.getCreature(attackingEntity).ifPresent(attackingCreature -> attackingCreature.onKill(event.getSource(), damageElements, event.getEntity()));
        }

        // On Death:
        this.getCreature(event.getEntity()).ifPresent(creature -> creature.onDeath(event.getSource(), damageElements));
    }

    /**
     * Called when testing if an effect is applicable to an entity.
     * @param event The entity effect applicable event.
     */
    @SubscribeEvent
    public void onEntityEffectApplicable(MobEffectEvent.Applicable event)
    {
        if(event.isCanceled() || event.getEntity() == null) {
            return;
        }

        ICreature creature = this.getCreature(event.getEntity()).orElse(null);
        if (creature == null) {
            return;
        }

        if (!creature.effectApplicable(event.getEffectInstance())) {
            event.setResult(Event.Result.DENY);
        }
    }

    /**
     * Called when an entity is removed from a level.
     * @param event The entity leave level event.
     */
    @SubscribeEvent
    public void onEntityLeaveLevel(EntityLeaveLevelEvent event)
    {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }
        this.removeProvider(livingEntity);
    }

    /**
     * Called when a player entity is cloned.
     * @param event The player entity clone event.
     */
    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event)
    {
        Optional<ICreature> optionalOriginalCreature = this.getCreature(event.getOriginal());
        if (optionalOriginalCreature.isPresent()) {
            Optional<ICreature> optionalCreature = this.getCreature(event.getEntity());
            optionalCreature.ifPresent(creature -> creature.piety().deserializeNBT(optionalOriginalCreature.get().piety().serializeNBT()));
        }
        this.removeProvider(event.getOriginal());
    }

    /**
     * Assigns capability providers to entities for capability attachment.
     * @param event The entity attach capabilities event.
     */
    @SubscribeEvent
    public void onEntityAttachCapabilities(AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof LivingEntity livingEntity)) {
            return;
        }
        Optional<CreatureCapabilityProvider> optionalProvider = this.createProvider(livingEntity);
        if (optionalProvider.isEmpty()) {
            return;
        }
        event.addCapability(new ResourceLocation(Schism.NAMESPACE, "creature"), optionalProvider.get());
    }
}
