package com.lycanitesmobs.core.spawner.trigger;

import com.google.gson.JsonObject;
import com.lycanitesmobs.core.spawner.Spawner;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class ExplosionSpawnTrigger extends EntitySpawnTrigger {

	/** The minimum strength or size of the explosion. Currently not working as there isn';'t a way to get explosion size from an explosion event. **/
	public int strength = 4;

	/** If true, only players can activate this Trigger, fake players are not counted. Players that prime TNT count. **/
	public boolean playerOnly = false;


	/** Constructor **/
	public ExplosionSpawnTrigger(Spawner spawner) {
		super(spawner);
	}


	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("strength"))
			this.strength = json.get("strength").getAsInt();

		if(json.has("playerOnly"))
			this.playerOnly = json.get("playerOnly").getAsBoolean();

		super.loadFromJSON(json);
	}


	/** Called every time an explosion is created. **/
	public boolean onExplosion(World world, PlayerEntity player, Explosion explosion) {
		// TODO Figure out strength/size workaround.

		// Player Only:
		if(this.playerOnly && player == null) {
			return false;
		}

		// Chance:
		if(this.chance < 1 && world.random.nextDouble() > this.chance) {
			return false;
		}

		// Entity Check:
		if(!this.playerOnly && explosion.getSourceMob() != null) {
			if(!this.isMatchingEntity(explosion.getSourceMob())) {
				return false;
			}
		}

		return this.trigger(world, player, new BlockPos(explosion.getPosition()), 0, 0);
	}
}
