package com.lycanitesmobs.client.gui;

import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;

public abstract class BaseContainerScreen<T extends Container> extends ContainerScreen<T> implements Button.IPressable {
    public DrawHelper drawHelper;

    public BaseContainerScreen(T container, PlayerInventory playerInventory, ITextComponent name) {
        super(container, playerInventory, name);
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
    protected void init() {
        super.init();
    }

    /**
     * Initialises all buttons and other widgets that this Screen uses.
     */
    protected abstract void initWidgets();

    /**
     * Draws and updates the GUI.
     * @param mouseX The x position of the mouse cursor.
     * @param mouseY The y position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
        this.renderWidgets(matrixStack, mouseX, mouseY, partialTicks); // Renders buttons.
        super.render(matrixStack, mouseX, mouseY, partialTicks); // Renders slots.
        this.renderForeground(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    /**
     * Draws the background image.
     * @param mouseX The x position of the mouse cursor.
     * @param mouseY The y position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    protected abstract void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks);
    protected void renderBg(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {} // Overridden as required but ignored.

    /**
     * Updates widgets like buttons and other controls for this screen. Super renders the button list, called after this.
     * @param mouseX The x position of the mouse cursor.
     * @param mouseY The y position of the mouse cursor.
     * @param partialTicks Ticks for animation.
     */
    protected void renderWidgets(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        for(int i = 0; i < this.buttons.size(); ++i) {
            this.buttons.get(i).render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    /**
     * Draws foreground elements.
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
}
