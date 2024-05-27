package com.schism.core.client.gui.screens;

import com.schism.core.Schism;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.widgets.ButtonWidget;
import com.schism.core.client.gui.widgets.LayoutWidget;
import com.schism.core.client.gui.screens.pages.AbstractPage;
import com.schism.core.creatures.ICreature;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import com.schism.core.util.Vec2;

import java.awt.*;
import java.util.function.Supplier;

public record JournalTab(String name, PageSupplier pageSupplier, Color color)
{
    public interface PageSupplier
    {
        /**
         * Creates a new Page.
         * @param drawHelper The draw helper for the page to use.
         * @param placement The placement for the page to use.
         * @param creature The creature instance for the page to use.
         */
        AbstractPage get(DrawHelper drawHelper, Placement placement, ICreature creature);
    }

    public interface PageSetter
    {
        /**
         * Sets the page of a screen that displays pages, etc
         * @param page The page name to set.
         */
        void call(AbstractPage page);
    }

    /**
     * Adds this tab to the provided layout generating an on demand page, etc.
     * @param drawHelper The draw helper to render widgets with.
     * @param creature The creature instance to get journal information from.
     * @param layout The layout to add the tab button to.
     * @param pageSetter A callable that sets the page of the journal or other screen that may be using tabs.
     * @param activePageSupplier A supplier that gets the current active page of the journal or other screen that may be using tabs.
     * @return The generated page that the tab button will set on the journal.
     */
    public AbstractPage addToLayout(DrawHelper drawHelper, ICreature creature, LayoutWidget<ButtonWidget> layout, PageSetter pageSetter, Supplier<AbstractPage> activePageSupplier)
    {
        AbstractPage page = this.pageSupplier.get(drawHelper, new Placement(), creature);

        Vec2 tabSize = new Vec2(32, 32);
        Vec2 tabIconSize = new Vec2(16, 16);
        ResourceLocation tabForeground = new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal/icon_" + this.name() + ".png");
        ResourceLocation tabBackground = new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal/tab.png");

        ButtonWidget tabButton = new ButtonWidget(drawHelper, new Placement(tabSize), tabBackground, () -> pageSetter.call(page));
        tabButton.setForeground(tabForeground, tabIconSize);
        tabButton.addTooltip(Component.translatable(Schism.NAMESPACE + ".journal.page." + this.name()).withStyle(ChatFormatting.BOLD));
        tabButton.addTooltip(Component.translatable(Schism.NAMESPACE + ".journal.page." + this.name() + ".description").withStyle(ChatFormatting.GREEN));
        tabButton.setColor(this.color());
        tabButton.setSelectedSupplier(() -> activePageSupplier.get() == page);

        layout.add(tabButton);
        return page;
    }
}
