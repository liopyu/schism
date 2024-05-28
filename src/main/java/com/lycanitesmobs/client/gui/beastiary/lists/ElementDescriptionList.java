package com.lycanitesmobs.client.gui.beastiary.lists;

import com.lycanitesmobs.client.gui.beastiary.BeastiaryScreen;
import com.lycanitesmobs.client.gui.widgets.BaseList;
import com.lycanitesmobs.client.gui.widgets.BaseListEntry;
import com.lycanitesmobs.core.info.ElementInfo;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.potion.Effect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.List;

public class ElementDescriptionList extends BaseList {
	protected ElementInfo elementInfo;

	/**
	 * Constructor
	 * @param width The width of the list.
	 * @param height The height of the list.
	 * @param top The y position that the list starts at.
	 * @param bottom The y position that the list stops at.
	 * @param x The x position of the list.
	 */
	public ElementDescriptionList(BeastiaryScreen parentGui, int width, int height, int top, int bottom, int x) {
		super(parentGui, width, height, top, bottom, x, 500);
	}

	public void setElementInfo(ElementInfo elementInfo) {
		this.elementInfo = elementInfo;
	}

	@Override
	protected int getMaxPosition() {
		return this.drawHelper.getWordWrappedHeight(this.getContent(), (this.width / 2)) + this.headerHeight;
	}

	@Override
	public void createEntries() {
		this.addEntry(new Entry(this));
	}

	/**
	 * List Entry
	 */
	public static class Entry extends BaseListEntry {
		private ElementDescriptionList parentList;

		public Entry(ElementDescriptionList parentList) {
			this.parentList = parentList;
		}

		@Override
		public void render(MatrixStack matrixStack, int index, int top, int left, int bottom, int right, int mouseX, int mouseY, boolean focus, float partialTicks) {
			if(index == 0) {
				this.parentList.drawHelper.drawStringWrapped(matrixStack, this.parentList.getContent(), left + 6, top, this.parentList.getWidth() - 20, 0xFFFFFF, true);
			}
		}

		@Override
		protected void onClicked() {}

		@Override
		public List<? extends IGuiEventListener> children() {
			return null;
		}
	}

	public String getContent() {
		if(this.elementInfo == null) {
			return "";
		}

		// Summary:
		ITextComponent text = new StringTextComponent("\u00A7l")
				.append(elementInfo.getTitle())
				.append(": " + "\u00A7r\n")
				.append(elementInfo.getDescription());

		// Buffs:
		new TranslationTextComponent("\n\n\u00A7l")
			.append(new TranslationTextComponent("gui.beastiary.elements.buffs"))
			.append(": " + "\u00A7r");
		for(String buff : this.elementInfo.buffs) {
			Effect effect = GameRegistry.findRegistry(Effect.class).getValue(new ResourceLocation(buff));
			if(effect == null) {
				continue;
			}
			ResourceLocation effectResource = new ResourceLocation(buff);
			new StringTextComponent("\n")
					.append(effect.getDisplayName())
					.append(": ")
					.append(new TranslationTextComponent("effect." + effectResource.getPath() + ".description"));
		}

		// Debuffs:
		new TranslationTextComponent("\n\n\u00A7l")
				.append(new TranslationTextComponent("gui.beastiary.elements.debuffs"))
				.append(": " + "\u00A7r");
		for(String debuff : this.elementInfo.debuffs) {
			if("burning".equals(debuff)) {
				new StringTextComponent("\n")
				.append(new TranslationTextComponent("effect.burning"))
				.append(": ")
				.append(new TranslationTextComponent("effect.burning.description"));
				continue;
			}
			Effect effect = GameRegistry.findRegistry(Effect.class).getValue(new ResourceLocation(debuff));
			if(effect == null) {
				continue;
			}
			ResourceLocation effectResource = new ResourceLocation(debuff);
			new StringTextComponent("\n")
				.append(effect.getDisplayName())
				.append(": ")
				.append(new TranslationTextComponent("effect." + effectResource.getPath() + ".description"));
		}

		return text.getString();
	}
}
