package com.schism.core.resolvers;

import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ElementResolver
{
    private static final ElementResolver INSTANCE = new ElementResolver();

    /**
     * Gets the singleton ElementResolver instance.
     * @return The Element Resolver, a utility for getting elements.
     */
    public static ElementResolver get()
    {
        return INSTANCE;
    }

    /**
     * Gets an element for the provided damage source.
     * Note: This does not check the source entity, use ElementRepository.damageElements() instead.
     * @param damageSource The damage source to get an element for.
     * @param priority If true, the priority is high and should override owner elements, if false it is low and should act as a fallback.
     * @return An optional element.
     */
    public Optional<ElementDefinition> fromDamageSource(DamageSource damageSource, Level level, boolean priority)
    {
        ResourceKey<DamageType> key = damageSource.typeHolder().unwrapKey().isPresent() ? damageSource.typeHolder().unwrapKey().get() : null;
        if (key == null) return Optional.empty();
        if (priority) {
            if (key == DamageTypes.STARVE) {
                return ElementRepository.get().getDefinition("shadow");
            }
            if (key == DamageTypes.EXPLOSION) {
                return ElementRepository.get().getDefinition("quake");
            }
            return Optional.empty();
        }

        if (key == DamageTypes.IN_FIRE) {
            return ElementRepository.get().getDefinition("fire");
        }
        if (key == DamageTypes.LIGHTNING_BOLT) {
            return ElementRepository.get().getDefinition("lightning");
        }
        if (key == DamageTypes.ON_FIRE) {
            return ElementRepository.get().getDefinition("fire");
        }
        if (key == DamageTypes.LAVA) {
            return ElementRepository.get().getDefinition("lava");
        }
        if (key == DamageTypes.HOT_FLOOR) {
            return ElementRepository.get().getDefinition("lava");
        }
        if (key == DamageTypes.IN_WALL) {
            return ElementRepository.get().getDefinition("earth");
        }
        if (key == DamageTypes.CRAMMING) {
            return ElementRepository.get().getDefinition("earth");
        }
        if (key == DamageTypes.DROWN) {
            return ElementRepository.get().getDefinition("water");
        }
        if (key == DamageTypes.STARVE) {
            return ElementRepository.get().getDefinition("shadow");
        }
        if (key == DamageTypes.CACTUS) {
            return ElementRepository.get().getDefinition("arbour");
        }
        if (key == DamageTypes.FALL) {
            return ElementRepository.get().getDefinition("quake");
        }
        if (key == DamageTypes.FLY_INTO_WALL) {
            return ElementRepository.get().getDefinition("quake");
        }
        if (key == DamageTypes.FELL_OUT_OF_WORLD) {
            return ElementRepository.get().getDefinition("void");
        }
        if (key == DamageTypes.GENERIC) {
            return ElementRepository.get().getDefinition("quake");
        }
        if (key == DamageTypes.MAGIC) {
            return ElementRepository.get().getDefinition("poison");
        }
        if (key == DamageTypes.WITHER) {
            return ElementRepository.get().getDefinition("shadow");
        }
        if (key == DamageTypes.FALLING_ANVIL) {
            return ElementRepository.get().getDefinition("earth");
        }
        if (key == DamageTypes.FALLING_BLOCK) {
            return ElementRepository.get().getDefinition("earth");
        }
        if (key == DamageTypes.DRAGON_BREATH) {
            return ElementRepository.get().getDefinition("void");
        }
        if (key == DamageTypes.DRY_OUT) {
            return ElementRepository.get().getDefinition("lava");
        }
        if (key == DamageTypes.SWEET_BERRY_BUSH) {
            return ElementRepository.get().getDefinition("arbour");
        }
        if (key == DamageTypes.FREEZE) {
            return ElementRepository.get().getDefinition("frost");
        }
        if (key == DamageTypes.FALLING_STALACTITE) {
            return ElementRepository.get().getDefinition("quake");
        }
        if (key == DamageTypes.STALAGMITE) {
            return ElementRepository.get().getDefinition("quake");
        }

        if (damageSource == level.damageSources().fall()) {
            return ElementRepository.get().getDefinition("quake");
        }
        if (damageSource == level.damageSources().onFire() || damageSource == level.damageSources().inFire()) {
            return ElementRepository.get().getDefinition("fire");
        }
        if (damageSource == level.damageSources().magic()) {
            return ElementRepository.get().getDefinition("arcane");
        }

        return switch(damageSource.getMsgId()) {
            case "mob", "player", "arrow", "trident", "thrown", "thorns", "explosion.player", "explosion" -> ElementRepository.get().getDefinition("quake");
            case "fireworks", "onFire" -> ElementRepository.get().getDefinition("fire");
            case "witherSkull" -> ElementRepository.get().getDefinition("shadow");
            case "indirectMagic" -> ElementRepository.get().getDefinition("arcane");
            default -> Optional.empty();
        };
    }
}
