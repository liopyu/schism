package com.lycanitesmobs.client.gui.widgets;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.list.AbstractOptionList;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseListEntry extends AbstractOptionList.Entry<BaseListEntry> {
	public int index;

	@Override
	public void render(MatrixStack matrixStack, int index, int top, int left, int bottom, int right, int mouseX, int mouseY, boolean p_render_8_, float partialTicks) {}

	/**
	 * Returns a list of child GUI elements such as buttons that should receive input events, etc.
	 * @return A list of child GUI elements.
	 */
	@Override
	public List<? extends IGuiEventListener> children() {
		return new ArrayList<>();
	}

	/**
	 * Called when the mouse is clicked on this entry.
	 * @param mouseX The mouse cursor X position.
	 * @param mouseY The mouse cursor Y position.
	 * @param mouseButton The button pressed.
	 * @return Returns true if the click should continue (allowing the list to scroll, etc) or false if it shouldn't.
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return true;
	}

	/**
	 * Called when the mouse is released on this entry.
	 * @param mouseX The mouse cursor X position.
	 * @param mouseY The mouse cursor Y position.
	 * @param mouseButton The button pressed.
	 * @return Returns true if the release should continue (allowing the list to scroll, etc) or false if it shouldn't.
	 */
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int mouseButton) {
		this.onClicked();
		return true;
	}

	/**
	 * Called when this entry is clicked.
	 */
	protected abstract void onClicked();
}
