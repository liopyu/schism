package com.lycanitesmobs.core.spawner.condition;

import com.google.gson.JsonObject;
import com.lycanitesmobs.ExtendedWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EventSpawnCondition extends SpawnCondition {

    /** The name of the mob event that must be active. Required. **/
    public String eventName = "";

    /** The minimum time (in ticks) the event must have been running for. **/
    public int eventTimeMin = -1;

    /** The maximum event time (in ticks), after this the condition is false. **/
    public int eventTimeMax = -1;


	@Override
	public void loadFromJSON(JsonObject json) {
		if(json.has("eventName"))
			this.eventName = json.get("eventName").getAsString();

		if(json.has("eventTimeMin"))
			this.eventTimeMin = json.get("eventTimeMin").getAsInt();

		if(json.has("eventTimeMax"))
			this.eventTimeMax = json.get("eventTimeMax").getAsInt();

		super.loadFromJSON(json);
	}


    @Override
    public boolean isMet(World world, PlayerEntity player, BlockPos position) {
        ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
        if(worldExt == null) {
            return false;
        }

        if(!"".equals(this.eventName)) {
			if (worldExt.getWorldEventName() == null || !worldExt.getWorldEventName().equalsIgnoreCase(this.eventName)) {
				return false;
			}
		}

		if(this.eventTimeMin >= 0 && worldExt.getWorldEventCount() < this.eventTimeMin) {
        	return false;
		}

		if(this.eventTimeMax >= 0 && worldExt.getWorldEventCount() > this.eventTimeMax) {
			return false;
		}

		return super.isMet(world, player, position);
    }
}
