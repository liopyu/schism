package com.schism.core.resolvers;

import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import com.schism.core.elements.IHasElements;
import net.minecraft.world.damagesource.DamageSource;

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
    public Optional<ElementDefinition> fromDamageSource(DamageSource damageSource, boolean priority)
    {
        if (priority) {
            if (damageSource == DamageSource.STARVE) {
                return ElementRepository.get().getDefinition("shadow");
            }
            if (damageSource.isExplosion()) {
                return ElementRepository.get().getDefinition("quake");
            }
            return Optional.empty();
        }

        if (damageSource == DamageSource.IN_FIRE) {
            return ElementRepository.get().getDefinition("fire");
        }
        if (damageSource == DamageSource.LIGHTNING_BOLT) {
            return ElementRepository.get().getDefinition("lightning");
        }
        if (damageSource == DamageSource.ON_FIRE) {
            return ElementRepository.get().getDefinition("fire");
        }
        if (damageSource == DamageSource.LAVA) {
            return ElementRepository.get().getDefinition("lava");
        }
        if (damageSource == DamageSource.HOT_FLOOR) {
            return ElementRepository.get().getDefinition("lava");
        }
        if (damageSource == DamageSource.IN_WALL) {
            return ElementRepository.get().getDefinition("earth");
        }
        if (damageSource == DamageSource.CRAMMING) {
            return ElementRepository.get().getDefinition("earth");
        }
        if (damageSource == DamageSource.DROWN) {
            return ElementRepository.get().getDefinition("water");
        }
        if (damageSource == DamageSource.STARVE) {
            return ElementRepository.get().getDefinition("shadow");
        }
        if (damageSource == DamageSource.CACTUS) {
            return ElementRepository.get().getDefinition("arbour");
        }
        if (damageSource == DamageSource.FALL) {
            return ElementRepository.get().getDefinition("quake");
        }
        if (damageSource == DamageSource.FLY_INTO_WALL) {
            return ElementRepository.get().getDefinition("quake");
        }
        if (damageSource == DamageSource.OUT_OF_WORLD) {
            return ElementRepository.get().getDefinition("void");
        }
        if (damageSource == DamageSource.GENERIC) {
            return ElementRepository.get().getDefinition("quake");
        }
        if (damageSource == DamageSource.MAGIC) {
            return ElementRepository.get().getDefinition("poison");
        }
        if (damageSource == DamageSource.WITHER) {
            return ElementRepository.get().getDefinition("shadow");
        }
        if (damageSource == DamageSource.ANVIL) {
            return ElementRepository.get().getDefinition("earth");
        }
        if (damageSource == DamageSource.FALLING_BLOCK) {
            return ElementRepository.get().getDefinition("earth");
        }
        if (damageSource == DamageSource.DRAGON_BREATH) {
            return ElementRepository.get().getDefinition("void");
        }
        if (damageSource == DamageSource.DRY_OUT) {
            return ElementRepository.get().getDefinition("lava");
        }
        if (damageSource == DamageSource.SWEET_BERRY_BUSH) {
            return ElementRepository.get().getDefinition("arbour");
        }
        if (damageSource == DamageSource.FREEZE) {
            return ElementRepository.get().getDefinition("frost");
        }
        if (damageSource == DamageSource.FALLING_STALACTITE) {
            return ElementRepository.get().getDefinition("quake");
        }
        if (damageSource == DamageSource.STALAGMITE) {
            return ElementRepository.get().getDefinition("quake");
        }

        if (damageSource.isFall()) {
            return ElementRepository.get().getDefinition("quake");
        }
        if (damageSource.isFire()) {
            return ElementRepository.get().getDefinition("fire");
        }
        if (damageSource.isMagic()) {
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
