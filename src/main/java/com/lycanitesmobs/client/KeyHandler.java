package com.lycanitesmobs.client;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.overlays.MinionSelectionOverlay;
import com.lycanitesmobs.client.gui.beastiary.CreaturesBeastiaryScreen;
import com.lycanitesmobs.client.gui.beastiary.IndexBeastiaryScreen;
import com.lycanitesmobs.client.gui.beastiary.PetsBeastiaryScreen;
import com.lycanitesmobs.client.gui.beastiary.SummoningBeastiaryScreen;
import com.lycanitesmobs.client.gui.buttons.TabManager;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.network.MessagePlayerAttack;
import com.lycanitesmobs.core.network.MessagePlayerControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {
	public static KeyHandler instance;
	public Minecraft mc;
	
	public boolean inventoryOpen = false;
	
	public KeyBinding dismount = new KeyBinding("key.mount.dismount", GLFW.GLFW_KEY_V, "Lycanites Mobs");
	public KeyBinding descend = new KeyBinding("key.mount.descend", GLFW.GLFW_KEY_LEFT_ALT, "Lycanites Mobs");
	public KeyBinding mountAbility = new KeyBinding("key.mount.ability", GLFW.GLFW_KEY_G, "Lycanites Mobs");
	public KeyBinding mountInventory = new KeyBinding("key.mount.inventory", GLFW.GLFW_KEY_K, "Lycanites Mobs");

	public KeyBinding beastiary = new KeyBinding("key.beastiary", GLFW.GLFW_KEY_UNKNOWN, "Lycanites Mobs");
	public KeyBinding index = new KeyBinding("key.index", GLFW.GLFW_KEY_B, "Lycanites Mobs");
	public KeyBinding pets = new KeyBinding("key.pets", GLFW.GLFW_KEY_UNKNOWN, "Lycanites Mobs");
	public KeyBinding summoning = new KeyBinding("key.summoning", GLFW.GLFW_KEY_UNKNOWN, "Lycanites Mobs");
	public KeyBinding minionSelection = new KeyBinding("key.minions", GLFW.GLFW_KEY_R, "Lycanites Mobs");


	public KeyHandler(Minecraft mc) {
		this.mc = mc;
		instance = this;
		
		// Register Keys:
		ClientRegistry.registerKeyBinding(this.dismount);
		ClientRegistry.registerKeyBinding(this.descend);
		ClientRegistry.registerKeyBinding(this.mountAbility);
		ClientRegistry.registerKeyBinding(this.mountInventory);
		ClientRegistry.registerKeyBinding(this.index);
		ClientRegistry.registerKeyBinding(this.beastiary);
		ClientRegistry.registerKeyBinding(this.pets);
		ClientRegistry.registerKeyBinding(this.summoning);
		ClientRegistry.registerKeyBinding(this.minionSelection);
	}


	@SubscribeEvent
	public void onPlayerTick(TickEvent.ClientTickEvent event) {
		if(event.phase != TickEvent.Phase.START) {
			return;
		}
		ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(this.mc.player);
		if(playerExt == null)
			return;
		byte controlStates = 0;
		
		// ========== GUI Keys ==========
		// Player Inventory: Adds extra buttons to the GUI.
		if(!this.inventoryOpen && mc.screen != null && mc.screen.getClass() == InventoryScreen.class) {
			TabManager.addTabsToInventory(mc.screen);
			this.inventoryOpen = true;
		}
		if(this.inventoryOpen && (mc.screen == null || mc.screen.getClass() != InventoryScreen.class)) {
			this.inventoryOpen = false;
		}
		
		// Mount Inventory: Adds to control states.
		if(this.mountInventory.consumeClick()) {
			controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_INVENTORY.id;
		}

		// Beastiary Index:
		if(this.index.consumeClick()) {
			this.mc.setScreen(new IndexBeastiaryScreen(this.mc.player));
		}

		// Beastiary Creatures:
		if(this.beastiary.consumeClick()) {
			this.mc.setScreen(new CreaturesBeastiaryScreen(this.mc.player));
		}

		// Beastiary Pets:
		if(this.pets.consumeClick()) {
			this.mc.setScreen(new PetsBeastiaryScreen(this.mc.player));
		}

		// Beastiary Summoning:
		if(this.summoning.consumeClick()) {
			this.mc.setScreen(new SummoningBeastiaryScreen(this.mc.player));
		}
		
		
		if(this.mc.isWindowActive()) {
			// ========== HUD Controls ==========
			// Minion Selection:
			if(this.minionSelection.consumeClick()) {
				this.mc.setScreen(new MinionSelectionOverlay(this.mc.player));
			}

			// ========== Action Controls ==========
			// Vanilla Jump: Adds to control states.
			if(this.mc.options.keyJump.isDown())
				controlStates += ExtendedPlayer.CONTROL_ID.JUMP.id;

			// Descend: Adds to control states.
			if(this.descend.isDown())
				controlStates += ExtendedPlayer.CONTROL_ID.DESCEND.id;
			
			// Mount Ability: Adds to control states.
			if(this.dismount.isDown())
				controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_DISMOUNT.id;

			// Mount Ability: Adds to control states.
			if(this.mountAbility.isDown())
				controlStates += ExtendedPlayer.CONTROL_ID.MOUNT_ABILITY.id;

            // Attack Key Pressed:
            if(Minecraft.getInstance().options.keyAttack.isDown()) {
                controlStates += ExtendedPlayer.CONTROL_ID.ATTACK.id;
            }
		}
		
		
		// ========== Sync Controls To Server ==========
		if(controlStates == playerExt.controlStates)
			return;
		MessagePlayerControl message = new MessagePlayerControl(controlStates);
		LycanitesMobs.packetHandler.sendToServer(message);
		playerExt.controlStates = controlStates;
	}


	/** Player keyboard events. **/
	@SubscribeEvent
	public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
		if(this.mc.player == null)
			return;

		// Minion Selection - Closes If Not Holding:
		if(event.getKey() == this.minionSelection.getKey().getValue() && this.mc.screen instanceof MinionSelectionOverlay) {
			if (event.getAction() == GLFW.GLFW_RELEASE) {
				this.mc.player.closeContainer();
			}
		}
	}


    /** Player 'mouse' events, these are actually events based on attack or item use actions and are still triggered if the key binding is no longer a mouse click. **/
    @SubscribeEvent
    public void onMouseEvent(InputEvent.MouseInputEvent event) {
        ExtendedPlayer playerExt = ExtendedPlayer.getForPlayer(this.mc.player);
        if(this.mc.player == null || playerExt == null || this.mc.hitResult == null)
            return;

        // Left (Attack):
        if(event.getButton() == 0) {
            // Disable attack for large entity reach override:
            if(!this.mc.player.isSpectator() && !this.mc.player.isHandsBusy() && this.mc.hitResult.getType() == RayTraceResult.Type.ENTITY) {
                Entity entityHit = ((EntityRayTraceResult)this.mc.hitResult).getEntity();
                if(playerExt.canMeleeBigEntity(entityHit)) {
                    MessagePlayerAttack message = new MessagePlayerAttack(entityHit);
                    LycanitesMobs.packetHandler.sendToServer(message);
                    //event.setCanceled(true);
                }
            }
        }
    }
}
