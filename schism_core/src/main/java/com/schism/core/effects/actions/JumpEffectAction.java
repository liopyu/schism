package com.schism.core.effects.actions;

import com.schism.core.database.DataStore;
import com.schism.core.effects.EffectDefinition;
import com.schism.core.util.Vec3;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class JumpEffectAction extends AbstractEffectAction
{
    protected final boolean cancel;
    protected final float multiply;
    protected final float add;
    protected final float subtract;

    public JumpEffectAction(EffectDefinition definition, DataStore dataStore)
    {
        super(definition, dataStore);

        this.cancel = dataStore.booleanProp("cancel");
        this.multiply = dataStore.floatProp("multiply");
        this.add = dataStore.floatProp("add");
        this.subtract = dataStore.floatProp("subtract");
    }

    @Override
    public boolean jump(MobEffectInstance effectInstance, LivingEntity livingEntity)
    {
        Vec3 delta = new Vec3(livingEntity.getDeltaMovement());
        if (delta.y() > 0) {
            int power = effectInstance.getAmplifier() + 1;
            if (this.multiply != 1) {
                delta = delta.multiply(1, (float)Math.pow(this.multiply, power), 1);
            }
            delta = delta.add(0, this.add * power, 0);
            delta = delta.add(0, -this.subtract * power, 0);
            livingEntity.setDeltaMovement(delta.physVec3());
            if (livingEntity instanceof ServerPlayer player) {
                player.connection.send(new ClientboundSetEntityMotionPacket(player));
            }
        }
        return this.cancel;
    }
}
