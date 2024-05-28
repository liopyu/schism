package com.lycanitesmobs.client.mobevent;

import com.lycanitesmobs.client.ClientManager;
import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.client.gui.overlays.BaseOverlay;
import com.lycanitesmobs.core.MobEventSound;
import com.lycanitesmobs.core.mobevent.MobEvent;
import com.lycanitesmobs.core.mobevent.MobEventPlayerServer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

public class MobEventPlayerClient {

    // Properties:
	public MobEvent mobEvent;

    // Properties:
    public int ticks = 0;
    public World world;
    public SimpleSound sound;

    /** True if the started event was already running and show display as 'Event Extended' in chat. **/
    public boolean extended = false;
    
    
	// ==================================================
    //                     Constructor
    // ==================================================
	public MobEventPlayerClient(MobEvent mobEvent, World world) {
		this.mobEvent = mobEvent;
        this.world = world;
        if(!world.isClientSide)
            LycanitesMobs.logWarning("", "Created a MobEventClient with a server side world, this shouldn't happen, things are going to get weird!");
	}
	
	
    // ==================================================
    //                       Start
    // ==================================================
	public void onStart(PlayerEntity player) {
		if(!this.extended) {
			this.ticks = 0;
		}
		ITextComponent eventMessage = new TranslationTextComponent("event." + (extended ? "extended" : "started") + ".prefix")
				.append(" ")
				.append(this.mobEvent.getTitle())
				.append(" ")
				.append(new TranslationTextComponent("event." + (extended ? "extended" : "started") + ".suffix"));
		player.sendMessage(eventMessage, Util.NIL_UUID);

		if(player.abilities.instabuild && !MobEventPlayerServer.testOnCreative && "world".equalsIgnoreCase(this.mobEvent.channel)) {
			return;
		}

		this.playSound();
	}

    public void playSound() {
        if(ObjectManager.getSound("mobevent_" + this.mobEvent.title.toLowerCase()) == null) {
            LycanitesMobs.logWarning("MobEvent", "Sound missing for: " + this.mobEvent.getTitle());
            return;
        }
        this.sound = new MobEventSound(ObjectManager.getSound("mobevent_" + this.mobEvent.title.toLowerCase()), SoundCategory.RECORDS, ClientManager.getInstance().getClientPlayer(), 1.0F, 1.0F);
        Minecraft.getInstance().getSoundManager().play(this.sound);
    }
	
	
    // ==================================================
    //                      Finish
    // ==================================================
	public void onFinish(PlayerEntity player) {
		ITextComponent eventMessage = new TranslationTextComponent("event.finished.prefix")
				.append(" ")
				.append(this.mobEvent.getTitle())
				.append(" ")
				.append(new TranslationTextComponent("event.finished.suffix"));
		player.sendMessage(eventMessage, Util.NIL_UUID);
	}


    // ==================================================
    //                      Update
    // ==================================================
    public void onUpdate() {
        this.ticks++;
    }


    // ==================================================
    //                       GUI
    // ==================================================
    @OnlyIn(Dist.CLIENT)
    public void onGUIUpdate(MatrixStack matrixStack, BaseOverlay gui, int sWidth, int sHeight) {
    	PlayerEntity player = ClientManager.getInstance().getClientPlayer();
        if(player.abilities.instabuild && !MobEventPlayerServer.testOnCreative && "world".equalsIgnoreCase(this.mobEvent.channel)) {
			return;
		}
        if(this.world == null || this.world != player.getCommandSenderWorld()) return;
        if(!this.world.isClientSide) return;

        int introTime = 12 * 20;
        if(this.ticks > introTime) return;
        int startTime = 2 * 20;
        int stopTime = 4 * 20;
        float animation = 1.0F;

        if(this.ticks < startTime)
            animation = (float)this.ticks / (float)startTime;
        else if(this.ticks > introTime - stopTime)
            animation = ((float)(introTime - this.ticks) / (float)stopTime);

        int width = 256;
        int height = 256;
        int x = (sWidth / 2) - (width / 2);
        int y = (sHeight / 2) - (height / 2);
        int u = width;
        int v = height;
        x += 3 - (this.ticks % 6);
        y += 2 - (this.ticks % 4);

        gui.minecraft.getTextureManager().bind(this.getTexture());
        GL11.glColor4f(1.0F, 1.0F, 1.0F, animation);
        if(animation > 0) {
			gui.drawHelper.drawTexturedModalRect(matrixStack, x, y, u, v, width, height);
		}
    }

    @OnlyIn(Dist.CLIENT)
    public ResourceLocation getTexture() {
        if(TextureManager.getTexture("guimobevent" + this.mobEvent.title) == null)
            TextureManager.addTexture("guimobevent" + this.mobEvent.title, LycanitesMobs.modInfo, "textures/mobevents/" + this.mobEvent.title.toLowerCase() + ".png");
        return TextureManager.getTexture("guimobevent" + this.mobEvent.title);
    }
}
