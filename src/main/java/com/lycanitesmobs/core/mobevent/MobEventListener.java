package com.lycanitesmobs.core.mobevent;

import com.lycanitesmobs.ExtendedWorld;
import com.lycanitesmobs.core.mobevent.trigger.AltarMobEventTrigger;
import com.lycanitesmobs.core.mobevent.trigger.MobEventTrigger;
import com.lycanitesmobs.core.mobevent.trigger.RandomMobEventTrigger;
import com.lycanitesmobs.core.mobevent.trigger.TickMobEventTrigger;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;


public class MobEventListener {
	// Global:
    protected static MobEventListener INSTANCE;

	public List<RandomMobEventTrigger> randomMobEventTriggers = new ArrayList<>();
	public List<TickMobEventTrigger> tickMobEventTriggers = new ArrayList<>();


	/** Returns the main Mob Event Listener instance. **/
	public static MobEventListener getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new MobEventListener();
		}
		return INSTANCE;
	}


	/**
	 * Adds a new Mob Event Trigger.
	 * @return True on success, false if it failed to add (could happen if the Trigger type has no matching list created yet).
	 */
	public boolean addTrigger(MobEventTrigger mobEventTrigger) {
		if(mobEventTrigger instanceof RandomMobEventTrigger && !this.randomMobEventTriggers.contains(mobEventTrigger)) {
			this.randomMobEventTriggers.add((RandomMobEventTrigger)mobEventTrigger);
			return true;
		}
		if(mobEventTrigger instanceof TickMobEventTrigger && !this.tickMobEventTriggers.contains(mobEventTrigger)) {
			this.tickMobEventTriggers.add((TickMobEventTrigger)mobEventTrigger);
			return true;
		}
		return false;
	}

	/**
	 * Removes a Mob Event Trigger.
	 */
	public void removeTrigger(MobEventTrigger mobEventTrigger) {
		if(this.randomMobEventTriggers.contains(mobEventTrigger)) {
			this.randomMobEventTriggers.remove(mobEventTrigger);
		}
		if(this.tickMobEventTriggers.contains(mobEventTrigger)) {
			this.tickMobEventTriggers.remove(mobEventTrigger);
		}
		if(mobEventTrigger instanceof AltarMobEventTrigger) {
			AltarMobEventTrigger altarMobEventTrigger = (AltarMobEventTrigger)mobEventTrigger;
			altarMobEventTrigger.onRemove();
		}
	}


	/** Called every tick in a world and counts down to the next event then fires it! The countdown is paused during an event. **/
	@SubscribeEvent
	public void onWorldUpdate(TickEvent.WorldTickEvent event) {
		World world = event.world;
		if(world.isClientSide) {
			return;
		}
		ExtendedWorld worldExt = ExtendedWorld.getForWorld(world);
		if(worldExt == null)
			return;

		// Check If Events Are Completely Disabled:
		if(!MobEventManager.getInstance().mobEventsEnabled || world.getDifficulty() == Difficulty.PEACEFUL) {
			if(worldExt.serverWorldEventPlayer != null)
				worldExt.stopWorldEvent();
			return;
		}

        // Only Tick On World Time Ticks:
        if(worldExt.lastEventScheduleTime == world.getGameTime())
        	return;
		worldExt.lastEventScheduleTime = world.getGameTime();
		
		// Only Run If Players Are Present:
//		if(world.getPlayers().size() < 1) {
//			return;
//		}

		// Scheduled Mob Events:
		for(MobEventSchedule mobEventSchedule : MobEventManager.getInstance().mobEventSchedules) {
			if(mobEventSchedule.canStart(world)) {
				mobEventSchedule.start(worldExt);
			}
		}

		// Tick Mob Events:
		for(TickMobEventTrigger mobEventTrigger : this.tickMobEventTriggers) {
			mobEventTrigger.onTick(world, worldExt.lastEventScheduleTime);
		}

        // Random Mob Events:
        if(MobEventManager.getInstance().mobEventsRandom) {
			if (MobEventManager.getInstance().minEventsRandomDay > 0 && Math.floor((worldExt.useTotalWorldTime ? world.getGameTime() : world.getDayTime()) / 24000D) < MobEventManager.getInstance().minEventsRandomDay) {
				return;
			}
			if (worldExt.getWorldEventStartTargetTime() <= 0 || worldExt.getWorldEventStartTargetTime() > world.getGameTime() + MobEventManager.getInstance().maxTicksUntilEvent) {
				worldExt.setWorldEventStartTargetTime(world.getGameTime() + worldExt.getRandomEventDelay(world.random));
			}
			if (world.getGameTime() == worldExt.getWorldEventStartTargetTime()) {
				this.triggerRandomMobEvent(world, worldExt, 1);
			}
			else if (world.getGameTime() > worldExt.getWorldEventStartTargetTime()) {
				worldExt.setWorldEventStartTargetTime(0);
			}
		}
    }


	/**
	 * Triggers a Random Mob Event Trigger if one is available.
	 *  **/
	public void triggerRandomMobEvent(World world, ExtendedWorld worldExt, int level) {
        // Get Triggers and Total Weight:
		List<RandomMobEventTrigger> validTriggers = new ArrayList<>();
		int totalWeights = 0;
		int highestPriority = 0;
		for(RandomMobEventTrigger mobEventTrigger : this.randomMobEventTriggers) {
			if(mobEventTrigger.priority >= highestPriority && mobEventTrigger.canTrigger(world, null)) {
				if(mobEventTrigger.priority > highestPriority) {
					totalWeights = 0;
					validTriggers.clear();
				}
				totalWeights += mobEventTrigger.weight;
				highestPriority = mobEventTrigger.priority;
				validTriggers.add(mobEventTrigger);
			}
		}
		if(totalWeights <= 0) {
			return;
		}

		// Fire Random Trigger Using Weights:
		int randomWeight = 1;
		if(totalWeights > 1) {
			randomWeight = world.random.nextInt(totalWeights - 1) + 1;
		}
		int searchWeight = 0;
		for(RandomMobEventTrigger mobEventTrigger : validTriggers) {
			if(mobEventTrigger.weight + searchWeight > randomWeight) {
				mobEventTrigger.trigger(world, null, new BlockPos(0, 0, 0), level, -1);
				return;
			}
			searchWeight += mobEventTrigger.weight;
		}
	}
}
