package com.lycanitesmobs.core.entity.goals.actions.abilities;

import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;

public class SuicideGoal extends Goal {
	BaseCreatureEntity host;

    // Properties:
	protected int countdown = 10 * 20;
	protected int phase = -1;

	/**
	 * Constrcutor
	 * @param setHost The creature using this goal.
	 */
	public SuicideGoal(BaseCreatureEntity setHost) {
        this.host = setHost;
    }

	/**
	 * Sets how long until the creature kills itself.
	 * @param countdown The summoning tick rate.
	 * @return This goal for chaining.
	 */
	public SuicideGoal setCountdown(int countdown) {
    	this.countdown = countdown;
    	return this;
    }

	/**
	 * Sets the battle phase to restrict this goal to.
	 * @param phase The phase to restrict to, if below 0 phases are ignored.
	 * @return This goal for chaining.
	 */
	public SuicideGoal setPhase(int phase) {
		this.phase = phase;
		return this;
	}

	@Override
    public boolean canUse() {
		if(!this.host.isAlive() ) {
			return false;
		}
		return this.phase < 0 || this.phase == this.host.getBattlePhase();
    }

	@Override
    public void tick() {
		if(this.countdown-- > 0) {
			return;
		}

		DamageSource damageSource = new EntityDamageSource("mob", this.host);
		damageSource.bypassMagic();
		damageSource.bypassArmor();
		this.host.hurt(damageSource, this.host.getHealth());
    }
}
