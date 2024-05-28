package com.lycanitesmobs.client.gui.beastiary;

import com.lycanitesmobs.client.gui.beastiary.lists.ElementDescriptionList;
import com.lycanitesmobs.client.gui.beastiary.lists.ElementList;
import com.lycanitesmobs.core.info.ElementInfo;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ElementsBeastiaryScreen extends BeastiaryScreen {
	public ElementInfo elementInfo;
	protected ElementList elementList;
	protected ElementDescriptionList descriptionList;

	public ElementsBeastiaryScreen(PlayerEntity player) {
		super(player);
	}

	@Override
	public void initWidgets() {
		super.initWidgets();

		this.elementList = new ElementList(this, this.colLeftWidth, this.colLeftHeight, this.colLeftY,this.colLeftY + this.colLeftHeight, this.colLeftX);
		this.children.add(this.elementList);

		int descriptionListY = this.colRightY;
		this.descriptionList = new ElementDescriptionList(this, this.colRightWidth, this.colRightHeight, descriptionListY, this.colRightY + this.colRightHeight, this.colRightX);
		this.children.add(this.descriptionList);
	}

	@Override
	public void renderBackground(MatrixStack matrixStack, int x, int y, float partialTicks) {
		super.renderBackground(matrixStack, x, y, partialTicks);
	}

	@Override
	protected void renderWidgets(MatrixStack matrixStack, int x, int y, float partialTicks) {
		this.elementList.render(matrixStack, x, y, partialTicks);

		if(this.elementInfo != null) {
			this.descriptionList.setElementInfo(this.elementInfo);
			this.descriptionList.render(matrixStack, x, y, partialTicks);
		}
	}

	@Override
	public void renderForeground(MatrixStack matrixStack, int x, int y, float partialTicks) {
		super.renderForeground(matrixStack, x, y, partialTicks);

		if(this.elementInfo == null) {
			String info = new TranslationTextComponent("gui.beastiary.elements.about").getString();
			this.drawHelper.drawStringWrapped(matrixStack, info, colRightX + 1, colRightY + 12 + 1, colRightWidth, 0xFFFFFF, true);
		}
	}

	@Override
	public ITextComponent getTitle() {
		if(this.elementInfo != null) {
			return new StringTextComponent("");
		}
		return new TranslationTextComponent("gui.beastiary.elements");
	}
}
