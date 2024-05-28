package com.lycanitesmobs.core.pets;

import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.TameableCreatureEntity;
import com.lycanitesmobs.core.info.*;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;

public class SummonSet {
	public ExtendedPlayer playerExt;
	
	// Summoning Details:
	public String summonType = "";
	public int subspecies = 0;
	public int variant = 0;
    public boolean summonableOnly = true;
	public boolean sitting = false;
	public boolean following = true;
	public boolean passive = false;
	public boolean assist = true;
	public boolean aggressive = false;
	public boolean pvp = false;
	
	// Behaviour Bits:
	/** A list bytes for each behaviour, used when syncing network packets. **/
	public enum BEHAVIOUR_ID {
		SITTING((byte)1), FOLLOWING((byte)2), PASSIVE((byte)4), ASSIST((byte)8), AGGRESSIVE((byte)16), PVP((byte)32);
		public byte id;
		BEHAVIOUR_ID(byte i) { id = i; }
	}
	
	// ==================================================
    //                   Static Methods
    // ==================================================
	public static boolean isSummonableCreature(String creatureName) {
		CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(creatureName);
		if(creatureInfo == null)
			return false;
		if(creatureInfo.dummy)
			return false;
		return creatureInfo.isSummonable();
	}
	
	
	// ==================================================
    //                    Constructor
    // ==================================================
	public SummonSet(ExtendedPlayer playerExt) {
		this.playerExt = playerExt;
	}
	
	
	// ==================================================
    //                      Behaviour
    // ==================================================
	public void setSummonType(String summonType) {
		/*if(this.playerExt != null && this.summonableOnly && (!this.playerExt.beastiary.getCreatureKnowledge(this.summonType) || !isSummonableCreature(this.summonType)))
			this.summonType = "";
        else*/
		if(summonType != null) {
			this.summonType = summonType.toLowerCase();
		}
		else {
			this.summonType = null;
		}
	}

	public void setSubspecies(int subspecies) {
		this.subspecies = subspecies;
	}

	public int getSubspecies() {
		return this.subspecies;
	}

	public void setVariant(int variant) {
		this.variant = variant;
	}

	public int getVariant() {
		return this.variant;
	}
	
	public boolean getSitting() {
		return this.sitting;
	}

	public boolean getFollowing() {
		return this.following;
	}

	public boolean getPassive() {
		return this.passive;
	}

	public boolean getAssist() {
		return this.assist;
	}

	public boolean getAggressive() {
		return this.aggressive;
	}

	public boolean getPVP() {
		return this.pvp;
	}

	/**
	 * Gets the creature info used by this summon set.
	 * @return The summon set creature info.
	 */
	public CreatureInfo getCreatureInfo() {
		return CreatureManager.getInstance().getCreature(this.summonType.toLowerCase());
	}

    /**
	 * Applies all behaviour in this set to the provided entity.
	 */
    public void applyBehaviour(TameableCreatureEntity minion) {
        minion.setSitting(this.getSitting());
        minion.setFollowing(this.getFollowing());
        minion.setPassive(this.getPassive());
		minion.setAssist(this.getAssist());
        minion.setAggressive(this.getAggressive());
        minion.setPVP(this.getPVP());
    }

    /**
	 * Copies the provided entity's behaviour into this summon set's behaviour.
	 */
    public void updateBehaviour(TameableCreatureEntity minion) {
        this.sitting = minion.isSitting();
        this.following = minion.isFollowing();
        this.passive = minion.isPassive();
        this.assist = minion.isAssisting();
		this.aggressive = minion.isAggressive();
        this.pvp = minion.isPVP();
    }
	
	
	// ==================================================
    //                        Info
    // ==================================================
	/** Returns true if this summon set has a valid mob to summon and can be used by staves, etc. **/
	public boolean isUseable() {
		if(this.summonType == null || "".equals(this.summonType) || CreatureManager.getInstance().getCreature(this.summonType) == null || !isSummonableCreature(this.summonType)) {
			return false;
		}
		if(this.playerExt != null) {
			CreatureInfo creatureInfo = this.getCreatureInfo();
			CreatureKnowledge creatureKnowledge = this.playerExt.getBeastiary().getCreatureKnowledge(creatureInfo.getName());
			if(creatureKnowledge == null) {
				return false;
			}
			Subspecies subspecies = creatureInfo.getSubspecies(this.subspecies);
			Variant variant = subspecies.getVariant(this.variant);
			if(variant == null) {
				return true;
			}
			return this.playerExt.getBeastiary().hasKnowledgeRank(creatureInfo.getName(), 2);
		}
		return true;
	}
	
	/** Returns the class of the creature to summon. **/
	public EntityType getCreatureType() {
		CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(this.summonType);
		if(creatureInfo == null) {
			return null;
		}
		return creatureInfo.getEntityType();
	}
	
	
	// ==================================================
    //                        Sync
    // ==================================================
	public void readFromPacket(String summonType, int subspecies, int variant, byte behaviour) {
		this.setSummonType(summonType);
		this.setSubspecies(subspecies);
		this.setVariant(variant);
		this.setBehaviourByte(behaviour);
	}

    public void setBehaviourByte(byte behaviour) {
        this.sitting = (behaviour & BEHAVIOUR_ID.SITTING.id) > 0;
        this.following = (behaviour & BEHAVIOUR_ID.FOLLOWING.id) > 0;
        this.passive = (behaviour & BEHAVIOUR_ID.PASSIVE.id) > 0;
		this.assist = (behaviour & BEHAVIOUR_ID.ASSIST.id) > 0;
        this.aggressive = (behaviour & BEHAVIOUR_ID.AGGRESSIVE.id) > 0;
        this.pvp = (behaviour & BEHAVIOUR_ID.PVP.id) > 0;
    }

	public byte getBehaviourByte() {
		byte behaviour = 0;
		if(this.getSitting()) behaviour += BEHAVIOUR_ID.SITTING.id;
		if(this.getFollowing()) behaviour += BEHAVIOUR_ID.FOLLOWING.id;
		if(this.getPassive()) behaviour += BEHAVIOUR_ID.PASSIVE.id;
		if(this.getAssist()) behaviour += BEHAVIOUR_ID.ASSIST.id;
		if(this.getAggressive()) behaviour += BEHAVIOUR_ID.AGGRESSIVE.id;
		if(this.getPVP()) behaviour += BEHAVIOUR_ID.PVP.id;
		return behaviour;
	}
	
	
	// ==================================================
    //                        NBT
    // ==================================================
   	// ========== Read ===========
    /** Reads a list of Creature Knowledge from a player's NBTTag. **/
    public void read(CompoundNBT nbtTagCompound) {
		if(nbtTagCompound.contains("SummonType"))
    		this.setSummonType(nbtTagCompound.getString("SummonType"));

		if(nbtTagCompound.contains("Subspecies"))
			this.setSubspecies(nbtTagCompound.getInt("Subspecies"));

		if(nbtTagCompound.contains("Variant"))
			this.setVariant(nbtTagCompound.getInt("Variant"));
    	
    	if(nbtTagCompound.contains("Sitting"))
    		this.sitting = nbtTagCompound.getBoolean("Sitting");
    	
    	if(nbtTagCompound.contains("Following"))
    		this.following = nbtTagCompound.getBoolean("Following");
    	
    	if(nbtTagCompound.contains("Passive"))
    		this.passive = nbtTagCompound.getBoolean("Passive");

		if(nbtTagCompound.contains("Assist"))
			this.assist = nbtTagCompound.getBoolean("Assist");
    	
    	if(nbtTagCompound.contains("Aggressive"))
    		this.aggressive = nbtTagCompound.getBoolean("Aggressive");
    	
    	if(nbtTagCompound.contains("PVP"))
    		this.pvp = nbtTagCompound.getBoolean("PVP");
    }
    
    // ========== Write ==========
    /** Writes a list of Creature Knowledge to a player's NBTTag. **/
    public void write(CompoundNBT nbtTagCompound) {
		nbtTagCompound.putString("SummonType", this.summonType);

		nbtTagCompound.putInt("Subspecies", this.getSubspecies());

		nbtTagCompound.putInt("Variant", this.getVariant());
    	
    	nbtTagCompound.putBoolean("Sitting", this.sitting);
    	
    	nbtTagCompound.putBoolean("Following", this.following);
    	
    	nbtTagCompound.putBoolean("Passive", this.passive);

		nbtTagCompound.putBoolean("Assist", this.assist);
    	
    	nbtTagCompound.putBoolean("Aggressive", this.aggressive);
    	
    	nbtTagCompound.putBoolean("PVP", this.pvp);
    }
}
