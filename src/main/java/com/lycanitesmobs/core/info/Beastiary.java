package com.lycanitesmobs.core.info;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.entity.FearEntity;
import com.lycanitesmobs.core.network.MessageBeastiary;
import com.lycanitesmobs.core.network.MessageCreatureKnowledge;
import com.lycanitesmobs.core.pets.SummonSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Beastiary {
	public ExtendedPlayer extendedPlayer;
	public Map<String, CreatureKnowledge> creatureKnowledgeList = new HashMap<>();

	/**
	 * Constructor
	 * @param extendedPlayer The Extended Player this Beastiary belongs to.
	 */
	public Beastiary(ExtendedPlayer extendedPlayer) {
		this.extendedPlayer = extendedPlayer;
	}
	
	
    // ==================================================
    //                     Knowledge
    // ==================================================
	/**
	 * Adds Creature Knowledge to this Beastiary after checking rank, etc.
	 * @param newKnowledge The new knowledge to add.
	 * @param sendToClient If true, send a network packet to the client.
	 * @return True if new knowledge is added and false if not.
	 */
	public boolean addCreatureKnowledge(CreatureKnowledge newKnowledge, boolean sendToClient) {
		CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(newKnowledge.creatureName);
		if(creatureInfo == null)
			return false;
		if(creatureInfo.dummy)
			return false;

		CreatureKnowledge currentKnowledge = this.getCreatureKnowledge(creatureInfo.getName());
		if(currentKnowledge != null) {
			currentKnowledge.rank = newKnowledge.rank;
			currentKnowledge.experience = newKnowledge.experience;
			if (sendToClient) {
				this.sendToClient(currentKnowledge);
			}
			return true;
		}

		this.creatureKnowledgeList.put(newKnowledge.creatureName, newKnowledge);
		if (sendToClient) {
			this.sendAddedMessage(newKnowledge);
			if (!this.extendedPlayer.player.getCommandSenderWorld().isClientSide) {
				this.sendToClient(newKnowledge);
			}
		}
		return true;
	}

	/**
	 * Attempt to add Creature Knowledge to this Beastiary based on the provided entity and sends feedback to the player.
	 * @param entity The entity being discovered.
	 * @param experience The Knowledge experience being gained.
	 * @return The newly added or updated knowledge or false if unchanged or invalid.
	 */
	public CreatureKnowledge addCreatureKnowledge(Entity entity, int experience) {
		// Invalid Entity:
		if(!(entity instanceof BaseCreatureEntity) || entity instanceof FearEntity) {
			return null;
		}

		CreatureInfo creatureInfo = ((BaseCreatureEntity)entity).creatureInfo;
		CreatureKnowledge newKnowledge = this.getCreatureKnowledge(creatureInfo.getName());
		if (newKnowledge == null) {
			newKnowledge = new CreatureKnowledge(this.extendedPlayer.getBeastiary(), creatureInfo.getName(), 1, experience);
			newKnowledge.getMaxExperience();
		}
		else {
			if (newKnowledge.getMaxExperience() <= 0) {
				return null;
			}
			newKnowledge.addExperience(experience);
		}
		this.addCreatureKnowledge(newKnowledge, true);

		return newKnowledge;
	}

	/**
	 * Sends a message to the player on gaining Creature Knowledge.
	 * @param creatureKnowledge The creature knowledge that was added.
	 */
	public void sendAddedMessage(CreatureKnowledge creatureKnowledge) {
		if(this.extendedPlayer.player.getCommandSenderWorld().isClientSide || !CreatureManager.getInstance().config.beastiaryKnowledgeMessages) {
			return;
		}
		CreatureInfo creatureInfo = creatureKnowledge.getCreatureInfo();
		String messageKey = "message.beastiary.rank";
		if (creatureKnowledge.rank == 1) {
			messageKey = "message.beastiary.new";
		}
		String messageText = new TranslationTextComponent(messageKey).getString();
		messageText = messageText.replaceAll("%creature%", "" + creatureInfo.getTitle().getString());
		messageText = messageText.replaceAll("%rank%", "" + creatureKnowledge.rank);
		ITextComponent message = new StringTextComponent(messageText);
		this.extendedPlayer.player.sendMessage(message, Util.NIL_UUID);

		if(creatureInfo.isSummonable() && creatureKnowledge.rank == 2) {
			ITextComponent summonMessage = new TranslationTextComponent("message.beastiary.summonable.prefix")
					.append(" ")
					.append(creatureInfo.getTitle())
					.append(" ")
					.append(new TranslationTextComponent("message.beastiary.summonable.suffix"));
			this.extendedPlayer.player.sendMessage(summonMessage, Util.NIL_UUID);
		}

		if(creatureInfo.isTameable() && creatureKnowledge.rank == 2) {
			ITextComponent tameMessage = new TranslationTextComponent("message.beastiary.tameable.prefix")
					.append(" ")
					.append(creatureInfo.getTitle())
					.append(" ")
					.append(new TranslationTextComponent("message.beastiary.tameable.suffix"));
			this.extendedPlayer.player.sendMessage(tameMessage, Util.NIL_UUID);
		}
	}


	/**
	 * Sends a message to the player if they attempt to add a creature that they already know.
	 * @param creatureKnowledge The creature knowledge that was trying to be added.
	 */
	public void sendKnownMessage(CreatureKnowledge creatureKnowledge) {
		if(this.extendedPlayer.player.getCommandSenderWorld().isClientSide) {
			return;
		}
		CreatureInfo creatureInfo = creatureKnowledge.getCreatureInfo();
		CreatureKnowledge currentKnowledge = this.extendedPlayer.getBeastiary().getCreatureKnowledge(creatureInfo.getName());
		ITextComponent message = new TranslationTextComponent("message.beastiary.known.prefix")
				.append(" " + currentKnowledge.rank + " ")
				.append(new TranslationTextComponent("message.beastiary.known.of"))
				.append(" ")
				.append(creatureInfo.getTitle())
				.append(" ")
				.append(new TranslationTextComponent("message.beastiary.known.suffix"));
		this.extendedPlayer.player.sendMessage(message, Util.NIL_UUID);
	}


	/**
	 * Returns the current knowledge of the provided creature. Use CreatureKnowledge.rank to get the current rank of knowledge the player has.
	 * @param creatureName The name of the creature to get the knowledge of.
	 * @return The creature knowledge or knowledge if there is no knowledge.
	 */
	@Nullable
	public CreatureKnowledge getCreatureKnowledge(String creatureName) {
		if(!this.creatureKnowledgeList.containsKey(creatureName)) {
			return null;
		}
		return this.creatureKnowledgeList.get(creatureName);
	}


	/**
	 * Returns if this Beastiary has the provided knowledge rank or higher.
	 * @param creatureName The name of the creature to check the knowledge rank of.
	 * @param rank The minimum knowledge rank required.
	 * @return True if the knowledge rank is met or exceeded.
	 */
	public boolean hasKnowledgeRank(String creatureName, int rank) {
		CreatureKnowledge creatureKnowledge = this.getCreatureKnowledge(creatureName);
		if(creatureKnowledge == null) {
			return false;
		}
		return creatureKnowledge.rank >= rank;
	}

	
	/**
	 * Returns how many creatures of the specified creature type the player has discovered.
	 * @param creatureType Creature Type to check with.
	 * @return True if the player has at least one creature form the specific creature type.
	 */
	public int getCreaturesDiscovered(CreatureType creatureType) {
		if(this.creatureKnowledgeList.size() == 0) {
			return 0;
		}

		int creaturesDescovered = 0;
		for(Entry<String, CreatureKnowledge> creatureKnowledgeEntry : this.creatureKnowledgeList.entrySet()) {
			if(creatureKnowledgeEntry.getValue() != null) {
				if (creatureKnowledgeEntry.getValue().getCreatureInfo().creatureType == creatureType) {
					creaturesDescovered++;
				}
			}
		}
		return creaturesDescovered;
	}
	
	
    // ==================================================
    //                     Summoning
    // ==================================================
	public Map<Integer, String> getSummonableList() {
		Map<Integer, String> minionList = new HashMap<>();
		int minionIndex = 0;
		for(String minionName : this.creatureKnowledgeList.keySet()) {
			CreatureKnowledge creatureKnowledge = this.creatureKnowledgeList.get(minionName);
			if(creatureKnowledge.rank >= 2 && SummonSet.isSummonableCreature(minionName)) {
				minionList.put(minionIndex++, minionName);
			}
		}
		return minionList;
	}
	
	
	// ==================================================
    //                    Network Sync
    // ==================================================
	/** Sends CreatureKnowledge to the client. For when it's added or changed server side but needs updated client side. **/
	public void sendToClient(CreatureKnowledge newKnowledge) {
		if(this.extendedPlayer.player.getCommandSenderWorld().isClientSide) {
			return;
		}
		MessageCreatureKnowledge message = new MessageCreatureKnowledge(newKnowledge);
		LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayerEntity)this.extendedPlayer.getPlayer());
	}
	
	/** Sends the whole Beastiary progress to the client, use sparingly! **/
	public void sendAllToClient() {
		MessageBeastiary message = new MessageBeastiary(this);
		LycanitesMobs.packetHandler.sendToPlayer(message, (ServerPlayerEntity)this.extendedPlayer.getPlayer());
	}
	
	
	// ==================================================
    //                        NBT
    // ==================================================
    /** Reads a list of Creature Knowledge from a player's NBTTag. **/
    public void readFromNBT(CompoundNBT nbtTagCompound) {
    	if(!nbtTagCompound.contains("CreatureKnowledge"))
    		return;
    	this.creatureKnowledgeList.clear();
    	ListNBT knowledgeList = nbtTagCompound.getList("CreatureKnowledge", 10);
    	for(int i = 0; i < knowledgeList.size(); ++i) {
	    	CompoundNBT nbtKnowledge = knowledgeList.getCompound(i);
    		if(nbtKnowledge.contains("CreatureName")) {
    			String creatureName = nbtKnowledge.getString("CreatureName");
				int rank = 0;
				if(nbtKnowledge.contains("Rank")) {
					rank = nbtKnowledge.getInt("Rank");
				}
				int experience = 0;
				if(nbtKnowledge.contains("Experience")) {
					experience = nbtKnowledge.getInt("Experience");
				}
	    		CreatureKnowledge creatureKnowledge = new CreatureKnowledge(
                        this,
	    				creatureName,
	    				rank,
						experience
	    			);
	    		this.addCreatureKnowledge(creatureKnowledge, false);
    		}
    	}
    }

    /** Writes a list of Creature Knowledge to a player's NBTTag. **/
    public void writeToNBT(CompoundNBT nbtTagCompound) {
    	ListNBT knowledgeList = new ListNBT();
		for(Entry<String, CreatureKnowledge> creatureKnowledgeEntry : creatureKnowledgeList.entrySet()) {
			CreatureKnowledge creatureKnowledge = creatureKnowledgeEntry.getValue();
			CompoundNBT nbtKnowledge = new CompoundNBT();
			nbtKnowledge.putString("CreatureName", creatureKnowledge.creatureName);
			nbtKnowledge.putInt("Rank", creatureKnowledge.rank);
			nbtKnowledge.putInt("Experience", creatureKnowledge.experience);
			knowledgeList.add(nbtKnowledge);
		}
		nbtTagCompound.put("CreatureKnowledge", knowledgeList);
    }
}
