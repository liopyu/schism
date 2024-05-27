package com.schism.core.projectiles;

import com.schism.core.database.IHasDefinition;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ChargeDispenserBehavior extends AbstractProjectileDispenseBehavior implements IHasDefinition<ProjectileDefinition>
{
    protected final ProjectileDefinition definition;

    public ChargeDispenserBehavior(ProjectileDefinition definition)
    {
        this.definition = definition;
    }

    @Override
    public ProjectileDefinition definition()
    {
        return this.definition;
    }

    @Override
    protected Projectile getProjectile(Level level, Position position, ItemStack itemStack)
    {
        ProjectileEntity projectileEntity = this.definition().create(level);
        projectileEntity.setPos(position.x(), position.y(), position.z());
        return projectileEntity;
    }

    @Override
    protected void playSound(BlockSource blockSource) {
        // Sounds are played by the projectile entities themselves via Sound Projectile Actions.
    }
}
