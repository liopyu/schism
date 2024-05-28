package com.lycanitesmobs.client.gui.beastiary;

import com.lycanitesmobs.LycanitesMobs;
import com.lycanitesmobs.client.gui.beastiary.lists.BeastiaryIndexList;
import com.lycanitesmobs.client.gui.buttons.ButtonBase;
import com.lycanitesmobs.core.VersionChecker;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.net.URI;
import java.net.URISyntaxException;

public class IndexBeastiaryScreen extends BeastiaryScreen {
	public BeastiaryIndexList indexList;
	VersionChecker.VersionInfo versionInfo;

	public IndexBeastiaryScreen(PlayerEntity player) {
		super(player);
		this.versionInfo = VersionChecker.INSTANCE.getLatestVersion();
	}

	@Override
	protected void initWidgets() {
		super.initWidgets();

		int menuWidth = this.colRightWidth;

		int buttonCount = 6;
		int buttonPadding = 2;
		int buttonWidth = Math.round((float)(menuWidth / buttonCount)) - buttonPadding;
		int buttonWidthPadded = buttonWidth + buttonPadding;
		int buttonHeight = 20;
		int buttonX = this.colRightX + buttonPadding;
		int buttonY = this.colRightY + this.colRightHeight - buttonHeight;
		ButtonBase button;

		// Links:
		button = new ButtonBase(100, buttonX, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("Website"), this);
		this.addButton(button);
		button = new ButtonBase(101, buttonX + buttonWidthPadded, buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("Twitter"), this);
		this.addButton(button);
		button = new ButtonBase(102, buttonX + (buttonWidthPadded * 2), buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("Patreon"), this);
		this.addButton(button);
		button = new ButtonBase(103, buttonX + (buttonWidthPadded * 3), buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("Guilded"), this);
		this.addButton(button);
		button = new ButtonBase(104, buttonX + (buttonWidthPadded * 4), buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("Discord"), this);
		this.addButton(button);
		button = new ButtonBase(105, buttonX + (buttonWidthPadded * 5), buttonY, buttonWidth, buttonHeight, new TranslationTextComponent("Shard"), this);
		this.addButton(button);

		// Lists:
		if(this.versionInfo != null) {
			this.indexList = new BeastiaryIndexList(this, this.colRightWidth, this.colRightHeight, this.colRightY + 93, buttonY - buttonPadding, this.colRightX + 2, this.versionInfo);
			this.children.add(this.indexList);
		}
	}

	@Override
	public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderBackground(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void renderWidgets(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderWidgets(matrixStack, mouseX, mouseY, partialTicks);
		if(this.indexList != null)
			this.indexList.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void renderForeground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.renderForeground(matrixStack, mouseX, mouseY, partialTicks);

		int yOffset = this.colRightY + 13;
		String info = new TranslationTextComponent("gui.beastiary.index.description").getString();
		this.drawHelper.drawStringWrapped(matrixStack, info, this.colRightX + 1, yOffset, this.colRightWidth, 0xFFFFFF, true);
		yOffset += this.drawHelper.getWordWrappedHeight(info, this.colRightWidth);

		if(this.versionInfo == null)
			return;

		// Check Mod Version:
		String version = "\n\u00A7l" + new TranslationTextComponent("gui.beastiary.index.version").getString() + ": \u00A7r";
		if(this.versionInfo.isNewer) {
			version += "\u00A74";
		}
		version += LycanitesMobs.versionNumber + "\u00A7r";
		if(this.versionInfo.isNewer) {
			version += " \u00A7l" + new TranslationTextComponent("gui.beastiary.index.version.newer").getString() + ": \u00A7r\u00A72" + this.versionInfo.versionNumber + "\u00A7r";
		}
		this.drawHelper.drawStringWrapped(matrixStack, version, this.colRightX + 1, yOffset, this.colRightWidth, 0xFFFFFF, true);
	}

	@Override
	public void actionPerformed(int buttonId) {
		if(buttonId == 100) {
			try {
				this.openURI(new URI(LycanitesMobs.website));
			} catch (URISyntaxException e) {}
		}
		if(buttonId == 101) {
			try {
				this.openURI(new URI(LycanitesMobs.twitter));
			} catch (URISyntaxException e) {}
		}
		if(buttonId == 102) {
			try {
				this.openURI(new URI(LycanitesMobs.patreon));
			} catch (URISyntaxException e) {}
		}
		if(buttonId == 103) {
			try {
				this.openURI(new URI(LycanitesMobs.guilded));
			} catch (URISyntaxException e) {}
		}
		if(buttonId == 104) {
			try {
				this.openURI(new URI(LycanitesMobs.discord));
			} catch (URISyntaxException e) {}
		}
		if(buttonId == 105) {
			try {
				this.openURI(new URI("https://shardtcg.com"));
			} catch (URISyntaxException e) {}
		}
		super.actionPerformed(buttonId);
	}

	@Override
	public ITextComponent getTitle() {
		return new TranslationTextComponent("gui.beastiary.index.title");
	}
}
