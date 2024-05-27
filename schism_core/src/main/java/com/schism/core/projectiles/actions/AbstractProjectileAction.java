package com.schism.core.projectiles.actions;

import com.schism.core.database.Action;
import com.schism.core.database.DataStore;
import com.schism.core.projectiles.ProjectileDefinition;
import com.schism.core.projectiles.ProjectileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractProjectileAction extends Action<ProjectileDefinition>
{
    public AbstractProjectileAction(ProjectileDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);
    }

    /**
     * Creates a list of projectile actions from the provided map.
     * @param definition The definition the list is for.
     * @param dataStoreMap A map of keys and nested data stores for populating actions with.
     * @return The newly created projectile action list.
     */
    public static List<AbstractProjectileAction> listFromMap(ProjectileDefinition definition, Map<String, DataStore> dataStoreMap)
    {
        return dataStoreMap.entrySet().stream()
                .map(dataStoreEntry -> AbstractProjectileAction.create(definition, dataStoreEntry))
                .filter(Objects::nonNull).toList();
    }

    /**
     * Creates a new projectile action instance for the provided data store entry.
     * @param definition The definition the action is for.
     * @param dataStoreEntry The data store entry to get action properties from.
     * @return The newly created projectile action.
     */
    public static AbstractProjectileAction create(ProjectileDefinition definition, Map.Entry<String, DataStore> dataStoreEntry)
    {
        DataStore dataStore = dataStoreEntry.getValue();
        return switch (dataStore.stringProp("type")) {
            case "damage" -> new DamageProjectileAction(definition, dataStore);
            case "effect" -> new EffectProjectileAction(definition, dataStore);
            case "particles" -> new ParticlesProjectileAction(definition, dataStore);
            case "place_blocks" -> new PlaceBlocksProjectileAction(definition, dataStore);
            case "sound" -> new SoundProjectileAction(definition, dataStore);
            default -> throw new RuntimeException("Invalid Projectile Action Type: " + dataStore.stringProp("type"));
        };
    }

    /**
     * Called by projectile entity with this action each tick.
     * @param projectileEntity The projectile entity.
     */
    public void onTick(ProjectileEntity projectileEntity)
    {

    }

    /**
     * Called by projectile entity with this action each tick while it pierces through a valid block.
     * @param projectileEntity The projectile entity.
     * @param blockState The block state being pierced.
     * @param blockPos The position of the block being pierced.
     */
    public void onPierceBlock(ProjectileEntity projectileEntity, BlockState blockState, BlockPos blockPos)
    {

    }

    /**
     * Called by projectile entity once when it hits a block and is then destroyed.
     * @param projectileEntity The projectile entity.
     * @param blockState The block state being pierced.
     * @param blockPos The position of the block hit.
     */
    public void onImpactBlock(ProjectileEntity projectileEntity, BlockState blockState, BlockPos blockPos)
    {

    }

    /**
     * Called by projectile entity with this action each tick while it pierces through an entity.
     * @param projectileEntity The projectile entity.
     * @param entity The entity being pierced.
     */
    public void onPierceEntity(ProjectileEntity projectileEntity, Entity entity)
    {

    }

    /**
     * Called by projectile entity once when it hits an entity and is then destroyed.
     * @param projectileEntity The projectile entity.
     * @param entity The entity hit.
     */
    public void onImpactEntity(ProjectileEntity projectileEntity, Entity entity)
    {

    }

    /**
     * Called by projectile entity if it expires.
     * @param projectileEntity The projectile entity.
     */
    public void onExpire(ProjectileEntity projectileEntity)
    {

    }
}
