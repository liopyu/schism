package com.schism.core.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Schism;
import com.schism.core.client.gui.GuiSounds;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.widgets.ButtonWidget;
import com.schism.core.client.gui.widgets.FixedLayoutWidget;
import com.schism.core.client.gui.screens.pages.*;
import com.schism.core.creatures.Creatures;
import com.schism.core.creatures.ICreature;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class JournalScreen extends AbstractScreen
{
    public static List<JournalTab> TABS = new ArrayList<>();

    static {
        TABS.add(new JournalTab("index", IndexPage::new, Color.WHITE));
        TABS.add(new JournalTab("creatures", CreaturesPage::new, new Color(0xFFBB88)));
        TABS.add(new JournalTab("gods", GodsPage::new, new Color(0xFF88FF)));
        TABS.add(new JournalTab("altars", AltarsPage::new, new Color(0x88FFFF)));
        TABS.add(new JournalTab("elements", ElementsPage::new, new Color(0x88FF88)));
        TABS.add(new JournalTab("effects", EffectsPage::new, new Color(0x8888FF)));
        TABS.add(new JournalTab("realms", RealmsPage::new, new Color(0xFF8888)));
    }

    protected final ICreature creature;
    protected final ResourceLocation backgroundTexture;
    protected final Placement tabsLayoutPlacement;
    protected final FixedLayoutWidget<ButtonWidget> tabsLayout;
    protected AbstractPage currentPage;

    /**
     * The Journal screen which displays journal pages containing all sorts of information.
     * @param player The player instance.
     */
    public JournalScreen(Player player)
    {
        super(Component.translatable(Schism.NAMESPACE + ".journal"), player);
        this.creature = Creatures.get().getCreature(player).orElse(null);
        this.backgroundTexture = new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal/background.png");
        this.tabsLayoutPlacement = new Placement(new Vec2(32, 0));
        this.tabsLayout = new FixedLayoutWidget<>(
                this.drawHelper,
                this.tabsLayoutPlacement,
                new Vec3(0, 1, 0),
                new Vec3(0, 4, 0),
                new Vec2(32, (float)this.height * 0.85F),
                true
        );
        this.tabsLayout.setCenter(new Vec2(1F, 0));

        TABS.forEach(tab -> {
            AbstractPage page = tab.addToLayout(this.drawHelper, this.creature, this.tabsLayout, this::setPage, this::page);
            if (this.currentPage == null) {
                this.setPage(page);
            }
        });
    }

    /**
     * Gets the current page the journal has open.
     * @return The active journal page.
     */
    public AbstractPage page()
    {
        return this.currentPage;
    }

    /**
     * Sets the page that the journal should display.
     * @param page The page to display.
     */
    public void setPage(AbstractPage page)
    {
        if (page == this.currentPage) {
            return;
        }
        this.currentPage = page;
        GuiSounds.get().play(Schism.NAMESPACE, "gui.page");
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
    {
        if (this.creature == null) {
            return;
        }

        this.widgetState.beforeDraw(true, mouseX, mouseY);
        float marginLeft = this.drawHelper.screenX(0.075F);
        float marginBottom = this.drawHelper.screenY(0.025F);
        Vec2 center = new Vec2(this.drawHelper.screenX(0.5F), this.drawHelper.screenY(0.5F) - marginBottom);
        Vec2 size = new Vec2(this.drawHelper.screenX(0.95F), this.drawHelper.screenY(0.85F));
        Vec3 position = new Vec3(center.x() - (size.x() / 2), center.y() - (size.y() / 2), -1);

        // Background:
        this.drawHelper.drawTexture(poseStack, this.backgroundTexture, position, size, Vec2.ONE, this.creature.elementColor());

        // Tabs:
        Vec2 tabsSizeMax = new Vec2(this.tabsLayout.drawSize().x(), size.y() * 0.8F);
        Vec3 tabsPosition = new Vec3(position.x() + marginLeft, center.y() - (Math.min(tabsSizeMax.y(), this.tabsLayout.drawSize().y()) / 2), 0);
        this.tabsLayoutPlacement.setPosition(tabsPosition);
        this.tabsLayout.setSizeMax(tabsSizeMax).draw(poseStack, widgetState, true);

        // Page:
        this.currentPage.placement().setSize(size).setPosition(position);
        this.currentPage.draw(poseStack, this.widgetState, true);

        this.widgetState.drawTooltip(this.drawHelper, poseStack).afterDraw();
    }
}
