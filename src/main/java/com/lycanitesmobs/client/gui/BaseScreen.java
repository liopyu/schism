package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;

import java.net.URI;

public abstract class BaseScreen extends Screen implements Button.IPressable {
	public DrawHelper drawHelper;
	public MainWindow scaledResolution;
	public float zLevel = 0;

	public BaseScreen(ITextComponent screenName) {
        super(screenName);
    }

    @Override
	public void init(Minecraft minecraft, int width, int height) {
		this.drawHelper = new DrawHelper(minecraft, minecraft.font);
    	this.minecraft = minecraft;
    	super.init(minecraft, width, height);
		this.initWidgets();
	}

	/**
	 * Secondary init method called by main init method.
	 */
	@Override
	protected void init() {
		super.init();
	}

	/**
	 * Initialises all buttons and other widgets that this Screen uses.
	 */
	protected abstract void initWidgets();

	/**
	 * Draws and updates the GUI.
	 * @param matrixStack The matrix stack to draw with.
	 * @param mouseX The x position of the mouse cursor.
	 * @param mouseY The y position of the mouse cursor.
	 * @param partialTicks Ticks for animation.
	 */
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
		this.renderWidgets(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks); // Renders buttons.
		this.renderForeground(matrixStack, mouseX, mouseY, partialTicks);
	}

	/**
	 * Draws the background image.
	 * @param matrixStack The matrix stack to draw with.
	 * @param mouseX The x position of the mouse cursor.
	 * @param mouseY The y position of the mouse cursor.
	 * @param partialTicks Ticks for animation.
	 */
	protected abstract void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

	/**
	 * Updates widgets like buttons and other controls for this screen. Super renders the button list, called after this.
	 * @param matrixStack The matrix stack to draw with.
	 * @param mouseX The x position of the mouse cursor.
	 * @param mouseY The y position of the mouse cursor.
	 * @param partialTicks Ticks for animation.
	 */
	protected void renderWidgets(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {}

	/**
	 * Draws foreground elements.
	 * @param matrixStack The matrix stack to draw with.
	 * @param mouseX The x position of the mouse cursor.
	 * @param mouseY The y position of the mouse cursor.
	 * @param partialTicks Ticks for animation.
	 */
	protected abstract void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);

	@Override
	public void onPress(Button guiButton) {
		if(!(guiButton instanceof ButtonBase)) {
			return;
		}
		this.actionPerformed(((ButtonBase)guiButton).buttonId);
	}

	/**
	 * Called when a Button Base is pressed providing the press button's id.
	 * @param buttonId The id of the button pressed.
	 */
	public abstract void actionPerformed(int buttonId);

	/**
	 * Returns a scaled x coordinate.
	 * @param x The x float to scale where 1.0 is the entire GUI width.
	 * @return A scaled x position.
	 */
	public int getScaledX(float x) {
		if(this.scaledResolution == null) {
			this.scaledResolution = this.minecraft.getWindow();
		}

		// Aspect Ratio:
		float targetAspect = 0.5625f; // 16:9
		float scaledHeight = scaledResolution.getGuiScaledHeight();
		float scaledWidth = scaledResolution.getGuiScaledWidth();
		float currentAspect = (scaledHeight * x) / (scaledWidth * x);

		// Wider Than target:
		if(currentAspect < targetAspect) {
			scaledWidth = scaledHeight + (scaledHeight * targetAspect);
		}

		// Taller Than target:
		else if(currentAspect > targetAspect) {
			scaledHeight = scaledWidth + (scaledWidth * targetAspect);
		}

		float guiWidth = scaledWidth * x;
		return Math.round(Math.max(x, guiWidth));
	}

	/**
	 * Returns a scaled y coordinate based on the scaled width with an aspect ratio applied to it.
	 * @param y The y float to scale where 1.0 is the entire GUI height.
	 * @return A scaled y position.
	 */
	public int getScaledY(float y) {
		float baseHeight = Math.round((float)this.getScaledX(y) * 0.5625f);
		return Math.round(baseHeight * y);
	}

	/**
	 * Opens a URI in the users default web browser.
	 * @param uri The URI link to open.
	 */
	protected void openURI(URI uri) {
		try {
			Util.getPlatform().openUri(uri);
		} catch (Exception e) {
			LycanitesMobs.logWarning("", "Unable to open link: " + uri.toString());
			e.printStackTrace();
		}
	}
}
