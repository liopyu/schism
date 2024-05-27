package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import com.schism.core.util.Vec3;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class KnockbackEffectAction extends AbstractEffectAction
{
    protected final boolean onDamageDealt;
    protected final boolean onDamageTaken;
    protected final float force;

    public KnockbackEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.onDamageDealt = dataStore.booleanProp("on_damage_dealt");
        this.onDamageTaken = dataStore.booleanProp("on_damage_taken");
        this.force = dataStore.floatProp("force");
    }

    @Override
    public float damageDealt(MobEffectInstance effectInstance, LivingEntity livingEntity, LivingEntity targetEntity, DamageSource damageSource, float amount)
    {
        if (!this.onDamageDealt) {
            return amount;
        }

        Vec3 delta = new Vec3(targetEntity.getDeltaMovement());
        double cap = 10;
        if (Math.abs(delta.x()) >= cap || Math.abs(delta.z()) >= cap) {
            return amount;
        }

        int power = effectInstance.getAmplifier() + 1;
        float force = this.force * power;

        Vec3 position = new Vec3(livingEntity.position());
        Vec3 targetPosition = new Vec3(targetEntity.position());
        Vec3 diff = targetPosition.subtract(position);
        float xzDist = Math.max(position.xz().distance(targetPosition.xz()), 0.01F);
        float xVel = diff.x() / xzDist * force;
        float zVel = diff.z() / xzDist * force;
        delta = delta.add(xVel, 0, zVel);

        targetEntity.setDeltaMovement(delta.physVec3());
        if (targetEntity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }

        return amount;
    }

    @Override
    public float damageTaken(MobEffectInstance effectInstance, LivingEntity livingEntity, DamageSource damageSource, float amount)
    {
        if (!this.onDamageTaken) {
            return amount;
        }

        if (damageSource.getEntity() instanceof LivingEntity attackingEntity) {
            Vec3 delta = new Vec3(attackingEntity.getDeltaMovement());
            double cap = 10;
            if (Math.abs(delta.x()) >= cap || Math.abs(delta.z()) >= cap) {
                return amount;
            }

            int power = effectInstance.getAmplifier() + 1;
            float force = this.force * power;

            Vec3 position = new Vec3(livingEntity.position());
            Vec3 targetPosition = new Vec3(attackingEntity.position());
            Vec3 diff = targetPosition.subtract(position);
            float xzDist = Math.max(position.xz().distance(targetPosition.xz()), 0.01F);
            float xVel = diff.x() / xzDist * force;
            float zVel = diff.z() / xzDist * force;
            delta = delta.add(xVel, 0, zVel);

            attackingEntity.setDeltaMovement(delta.physVec3());
            if (attackingEntity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
            }
        }

        return amount;
    }
}
