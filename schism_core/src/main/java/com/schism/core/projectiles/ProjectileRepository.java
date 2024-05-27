package com.schism.core.projectiles;

import com.schism.core.AbstractRepository;
import com.schism.core.database.DataStore;
import com.schism.core.elements.ElementDefinition;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

public class ProjectileRepository extends AbstractRepository<ProjectileDefinition>
{
    private static final ProjectileRepository INSTANCE = new ProjectileRepository();

    /**
     * Gets the singleton ProjectileRepository instance.
     * @return The Projectile Repository which loads and stores pantheons.
     */
    public static ProjectileRepository get()
    {
        return INSTANCE;
    }

    protected ProjectileRepository()
    {
        super("projectile");
    }

    @Override
    public ProjectileDefinition createDefinition(String subject, DataStore dataStore)
    {
        return new ProjectileDefinition(subject, dataStore);
    }

    /**
     * Registers projectile charge items into Forge.
     * @param event The item registry event.
     */
    @SubscribeEvent
    public void onItemsRegistry(final RegistryEvent.Register<Item> event)
    {
        this.definitions.values().stream().sorted().forEach(projectileDefinition -> projectileDefinition.registerChargeItem(event.getRegistry()));
    }

    /**
     * Registers projectile entity types into Forge.
     * @param event The item registry event.
     */
    @SubscribeEvent
    public void onEntitiesRegistry(final RegistryEvent.Register<EntityType<?>> event)
    {
        this.definitions.values().stream().sorted().forEach(projectileDefinition -> projectileDefinition.registerEntityType(event.getRegistry()));
    }

    /**
     * Gets all projectiles where the provided list of elements contains all the charge elements.
     * @param elements The list of elements to get charges for.
     * @return A list of projectiles with charges that the provided list of elements has all (or more) elements for.
     */
    public List<ProjectileDefinition> withAllChargeElements(List<ElementDefinition> elements)
    {
        return this.definitions.values().stream().filter(projectileDefinition -> elements.containsAll(projectileDefinition.chargeElements())).toList();
    }
}
