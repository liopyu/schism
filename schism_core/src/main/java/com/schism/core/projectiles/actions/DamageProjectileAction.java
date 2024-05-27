package com.schism.core.projectiles.actions;

import com.schism.core.database.CachedDefinition;
import com.schism.core.database.CachedList;
import com.schism.core.database.DataStore;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementsEntityDamageSource;
import com.schism.core.elements.ElementRepository;
import com.schism.core.projectiles.ProjectileDefinition;
import com.schism.core.projectiles.ProjectileEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class DamageProjectileAction extends AbstractProjectileAction
{
    protected final CachedList<ElementDefinition> cachedElements;
    protected final int damage;
    protected final float splashRadius;
    protected final boolean onImpact;
    protected final int onPierceTick;

    public DamageProjectileAction(ProjectileDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cachedElements = new CachedList<> (dataStore.listProp("elements").stream()
                .map(entry -> new CachedDefinition<>(entry.stringValue(), ElementRepository.get())).toList());
        this.damage = dataStore.intProp("damage");
        this.splashRadius = dataStore.floatProp("splash_radius");
        this.onImpact = dataStore.booleanProp("on_impact");
        this.onPierceTick = dataStore.intProp("on_pierce_tick");
    }

    @Override
    public void onPierceEntity(ProjectileEntity projectileEntity, Entity entity)
    {
        if (this.onPierceTick <= 0 || projectileEntity.tickCount() % this.onPierceTick != 0 || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        this.damage(projectileEntity, livingEntity);
        this.splashDamage(projectileEntity, livingEntity);
    }

    @Override
    public void onImpactEntity(ProjectileEntity projectileEntity, Entity entity)
    {
        if (!this.onImpact || !(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        this.damage(projectileEntity, livingEntity);
        this.splashDamage(projectileEntity, livingEntity);
    }

    /**
     * Deals splash damage to the provided entity.
     * @param projectileEntity The projectile entity.
     * @param livingEntity The entity to damage.
     */
    protected void damage(ProjectileEntity projectileEntity, LivingEntity livingEntity)
    {
        DamageSource damageSource = new DamageSource(this.definition().subject());
        if (!this.cachedElements.get().isEmpty()) {
            damageSource = new ElementsEntityDamageSource(this.cachedElements.get(), projectileEntity, projectileEntity.getOwner());
        }
        livingEntity.hurt(damageSource, this.damage);
    }

    /**
     * Deals splash damage to all valid entities within range excluding the provided entity.
     * @param projectileEntity The projectile entity.
     * @param livingEntity A specific entity to include, used to prevent an entity hit being damage twice.
     */
    protected void splashDamage(ProjectileEntity projectileEntity, LivingEntity livingEntity)
    {
        if (this.splashRadius <= 0) {
            return;
        }
        // TODO Projectile splash damage!
    }
}
