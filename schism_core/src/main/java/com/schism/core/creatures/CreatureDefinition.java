package com.schism.core.creatures;

import com.schism.core.database.*;
import com.schism.core.database.registryobjects.BlockRegistryObject;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import com.schism.core.lore.RealmDefinition;
import com.schism.core.lore.RealmRepository;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class CreatureDefinition extends AbstractDefinition
{
    protected String entityId;
    protected BlockRegistryObject<EntityType<?>> cachedEntityType;
    protected CachedDefinition<CreatureTypeDefinition> cachedCreatureType;
    protected CachedDefinition<RealmDefinition> cachedRealm;
    protected CachedList<CreatureRoleDefinition> cachedCreatureRoles;
    protected CachedDefinition<ElementDefinition> cachedBaseElement;
    protected CachedList<ElementDefinition> cachedResistElements;
    protected CachedList<ElementDefinition> cachedWeaknessElements;

    protected float levellingHealth;
    protected float levellingAttack;
    protected float levellingDefense;

    /**
     * Beastiary definitions hold beastiary information creatures.
     * @param subject The name of the creature.
     * @param dataStore The data store that holds config data about this definition.
     */
    public CreatureDefinition(String subject, DataStore dataStore)
    {
        super(subject, dataStore);
    }

    @Override
    public String type()
    {
        return CreatureRepository.get().type();
    }

    @Override
    public void update(DataStore dataStore)
    {
        super.update(dataStore);

        this.entityId = dataStore.stringProp("entity_id");
        this.cachedEntityType = new BlockRegistryObject<>(this.entityId, () -> ForgeRegistries.ENTITIES);
        this.cachedCreatureType = new CachedDefinition<>(dataStore.stringProp("creature_type"), CreatureTypeRepository.get());
        this.cachedRealm = new CachedDefinition<>(dataStore.stringProp("realm"), RealmRepository.get());
        this.cachedCreatureRoles = new CachedList<> (dataStore.listProp("creature_roles").stream()
                .map(entry -> new CachedDefinition<>(entry.stringValue(), CreatureRoleRepository.get())).toList());
        this.cachedBaseElement = new CachedDefinition<>(dataStore.stringProp("base_element"), ElementRepository.get());
        this.cachedResistElements = new CachedList<> (dataStore.listProp("resist_elements").stream()
                .map(entry -> new CachedDefinition<>(entry.stringValue(), ElementRepository.get())).toList());
        this.cachedWeaknessElements = new CachedList<> (dataStore.listProp("weakness_elements").stream()
                .map(entry -> new CachedDefinition<>(entry.stringValue(), ElementRepository.get())).toList());

        this.levellingHealth = dataStore.floatProp("stats_per_level.health");
        this.levellingAttack = dataStore.floatProp("stats_per_level.attack");
        this.levellingDefense = dataStore.floatProp("stats_per_level.defense");
    }

    /**
     * Gets the entity id that this creature definition is for.
     * @return The entity id this creature definition is for.
     */
    public String entityId()
    {
        return this.entityId;
    }

    /**
     * Gets the entity type that this creature definition is for.
     * @return The entity type this creature definition is for.
     */
    public BlockRegistryObject<EntityType<?>> entityType()
    {
        return this.cachedEntityType;
    }

    /**
     * Gets the creature type, used for categorising creatures in the beastiary.
     * @return The creature type.
     */
    public CreatureTypeDefinition creatureType()
    {
        return this.cachedCreatureType.get();
    }

    /**
     * Gets the realm that this creature originates from.
     * @return The creature's realm.
     */
    public RealmDefinition realm()
    {
        return this.cachedRealm.get();
    }

    /**
     * Gets a list of creature roles, used for creature targeting behavior, etc.
     * @return The list of creature roles.
     */
    public List<CreatureRoleDefinition> creatureRoles()
    {
        return this.cachedCreatureRoles.get();
    }

    /**
     * Gets the base element, used as a fallback for determine the element of any damage dealt by this creature.
     * @return The base element.
     */
    public ElementDefinition baseElement()
    {
        return this.cachedBaseElement.get();
    }

    /**
     * Gets the elements this creature is resistant to.
     * @return The list of resist elements.
     */
    public List<ElementDefinition> resistElements()
    {
        return this.cachedResistElements.get();
    }

    /**
     * Gets the elements this creature is weak to.
     * @return The list of weakness elements.
     */
    public List<ElementDefinition> weaknessElements()
    {
        return this.cachedWeaknessElements.get();
    }

    /**
     * How much health this creature gains per level on top of its base entity health (starting from level 2).
     * @return Health per level.
     */
    public float levellingHealth()
    {
        return this.levellingHealth;
    }

    /**
     * How much attack this creature gains per level on top of its base entity attack (starting from level 2).
     * @return Attack per level.
     */
    public float levellingAttack()
    {
        return this.levellingAttack;
    }

    /**
     * How much defense this creature gains per level on top of its base entity defense (starting from level 2).
     * @return Defense per level.
     */
    public float levellingDefense()
    {
        return this.levellingDefense;
    }
}
