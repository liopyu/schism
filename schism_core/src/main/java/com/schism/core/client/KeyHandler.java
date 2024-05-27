package com.schism.core.client;

import com.schism.core.Schism;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.client.gui.screens.JournalScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class KeyHandler
{
    private static KeyHandler INSTANCE;

    protected final Minecraft minecraft;
    protected final KeyMapping journal = new KeyMapping(Schism.NAMESPACE + ".key.journal", GLFW.GLFW_KEY_J, Schism.MOD_NAME);

    public final WidgetState widgetState;

    /**
     * Gets the singleton KeyHandler instance.
     * @return The Key Handler which is used to handle custom player input keys and mouse events.
     */
    public static KeyHandler get()
    {
        if (INSTANCE == null) {
            INSTANCE = new KeyHandler();
        }
        return INSTANCE;
    }

    /**
     * The Key Handler is used to handle custom player input keys and mouse events.
     */
    public KeyHandler()
    {
        this.minecraft = Minecraft.getInstance();
        this.widgetState = new WidgetState();
        ClientRegistry.registerKeyBinding(this.journal);
    }

    /**
     * Called during the client tick event, handles custom controls.
     * @param event The client tick event.
     */
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event)
    {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        // Journal:
        if (this.journal.consumeClick()) {
            this.minecraft.setScreen(new JournalScreen(this.minecraft.player));
        }
    }

    /**
     * Called during a mouse event, handles custom controls.
     * @param event The mouse input event.
     */
    @SubscribeEvent
    public void onMouseEvent(InputEvent.MouseInputEvent event)
    {
        // Mouse Release:
        if (event.getAction() == GLFW.GLFW_RELEASE) {
            if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_1) {
                this.widgetState.mouseReleased = event.getButton();
            }
        }
    }
}
