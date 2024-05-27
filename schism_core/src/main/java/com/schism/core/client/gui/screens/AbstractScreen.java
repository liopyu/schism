package com.schism.core.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.WidgetState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractScreen extends Screen
{
    protected final Player player;
    protected final DrawHelper drawHelper;
    protected final WidgetState widgetState;

    /**
     * The Journal screen which displays journal pages containing all sorts of information.
     * @param player The player instance.
     */
    public AbstractScreen(Component titleComponent, Player player)
    {
        super(titleComponent);
        this.player = player;
        this.drawHelper = new DrawHelper().setScreen(this);
        this.widgetState = new WidgetState();
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int keyCodeB, int keyCodeC)
    {
        // Close on Escape or Inventory Keys:
        if (keyCode == 1 || keyCode == this.drawHelper.minecraft().options.keyInventory.getKey().getValue()) {
            this.player.closeContainer();
        }

        return super.keyReleased(keyCode, keyCodeB, keyCodeC);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        this.widgetState.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        this.widgetState.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY)
    {
        this.widgetState.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double direction)
    {
        this.widgetState.mouseScrolled(mouseX, mouseY, direction);
        return super.mouseScrolled(mouseX, mouseY, direction);
    }

    @Override
    public void init()
    {
        super.init();
    }

    @Override
    public abstract void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks);
}
