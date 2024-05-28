package com.lycanitesmobs.client.gui.widgets;

import com.lycanitesmobs.client.TextureManager;
import com.lycanitesmobs.client.gui.SummoningPedestalScreen;
import com.lycanitesmobs.core.entity.ExtendedPlayer;
import com.lycanitesmobs.core.info.CreatureInfo;
import com.lycanitesmobs.core.info.CreatureManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class SummoningPedestalList extends BaseList<SummoningPedestalScreen> {
	public ExtendedPlayer playerExt;

	public SummoningPedestalList(SummoningPedestalScreen parentGui, ExtendedPlayer playerExt, int width, int height, int top, int bottom, int left) {
		super(parentGui, width, height, top, bottom, left, 28);
		this.playerExt = playerExt;
		this.createEntries(); // Called again here for playerExt.
	}

	@Override
	public void createEntries() {
		if(this.playerExt == null)
			return;
		for(String minionName : this.playerExt.getBeastiary().getSummonableList().values()) {
			this.addEntry(new SummoningPedestalEntry(this, minionName));
		}
	}

	@Override
	protected boolean isSelectedItem(int index) {
		if(!(this.getEntry(index) instanceof SummoningPedestalEntry))
			return false;
		return this.screen.getSelectedMinionName() != null && this.screen.getSelectedMinionName().equals(((SummoningPedestalEntry)this.getEntry(index)).minionName);
	}

	@OnlyIn(Dist.CLIENT)
	public class SummoningPedestalEntry extends BaseListEntry {
		SummoningPedestalList parentGUI;
		String minionName;

		public SummoningPedestalEntry(SummoningPedestalList list, String minionName) {
			this.parentGUI = list;
			this.minionName = minionName;
		}

		@Override
		public void render(MatrixStack matrixStack, int index, int top, int left, int bottom, int right, int mouseX, int mouseY, boolean focus, float partialTicks) {
			CreatureInfo creatureInfo = CreatureManager.getInstance().getCreature(this.minionName);

			// Summon Level:
			int levelBarWidth = 9;
			int levelBarHeight = 9;
			int levelBarX = left + 20;
			int levelBarY = top - levelBarHeight - 6;
			int level = creatureInfo.summonCost;
			if(level <= 10) {
				this.parentGUI.screen.drawHelper.drawBar(matrixStack, TextureManager.getTexture("GUIPetLevel"), levelBarX, levelBarY, 0, levelBarWidth, levelBarHeight, level, 10);
			}

			this.parentGUI.screen.drawHelper.getFontRenderer().draw(matrixStack, creatureInfo.getTitle().getString(), left + 20 , top + 4, 0xFFFFFF);
			this.parentGUI.screen.drawHelper.drawTexture(matrixStack, creatureInfo.getIcon(), left + 2, top + 4, 0, 1, 1, 16, 16);
		}

		@Override
		public List<? extends IGuiEventListener> children() {
			return null;
		}

		@Override
		protected void onClicked() {
			this.parentGUI.screen.selectMinion(this.minionName);
		}
	}
}
