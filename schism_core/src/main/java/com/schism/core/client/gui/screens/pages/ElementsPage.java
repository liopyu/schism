package com.schism.core.client.gui.screens.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Schism;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.GuiSounds;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.client.gui.components.CreatureElementStats;
import com.schism.core.client.gui.widgets.ButtonWidget;
import com.schism.core.client.gui.widgets.FixedLayoutWidget;
import com.schism.core.creatures.ICreature;
import com.schism.core.elements.ElementDefinition;
import com.schism.core.elements.ElementRepository;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import net.minecraft.resources.ResourceLocation;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;

import java.awt.*;

public class ElementsPage extends AbstractPage
{
    protected final ICreature creature;
    protected ElementDefinition element;

    protected final ResourceLocation listBackground;
    protected final Placement listPlacement;
    protected final FixedLayoutWidget<ButtonWidget> list;

    protected final Placement creatureElementStatsPlacement;
    protected final CreatureElementStats creatureElementStats;

    public ElementsPage(DrawHelper drawHelper, Placement placement, ICreature creature)
    {
        super(drawHelper, placement, new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal/page_elements.png"));
        this.creature = creature;
        this.element = null;

        // List:
        this.listBackground = new ResourceLocation(Schism.NAMESPACE, "textures/gui/list_background.png");
        this.listPlacement = new Placement();
        this.list = new FixedLayoutWidget<>(
                this.drawHelper,
                this.listPlacement,
                new Vec3(0, 1,0),
                new Vec3(0, 4, 0),
                new Vec2(32, this.drawHelper.screenY(0.85F)),
                true
        );
        this.list.setFlex(new Vec2(0.5F, 0));

        // Add Elements to List:
        Vec2 iconSize = new Vec2(28, 28);
        ResourceLocation buttonTexture = new ResourceLocation(Schism.NAMESPACE, "textures/gui/spot.png");
        for (ElementDefinition element : ElementRepository.get().definitions().stream().sorted().toList()) {
            if (element.icon().isEmpty()) {
                continue;
            }
            ButtonWidget button = new ButtonWidget(this.drawHelper, new Placement(iconSize), buttonTexture, () -> {
                this.selectElement(element);
            });
            button.setSelectedSupplier(() -> this.element == element);
            button.setForeground(element.icon().get(), iconSize.add(-2));
            button.setColor(Color.RGBtoHSB(element.color().darker().getRed(), element.color().darker().getGreen(), element.color().darker().getBlue(), null)[2] < 0.4F ? Color.WHITE : Color.BLACK);
            button.setForegroundColor(element.color());
            button.addTooltip(element.name().copy().withStyle(ChatFormatting.BOLD));
            this.list.add(button);
        }

        // Elements Stats:
        this.creatureElementStatsPlacement = new Placement();
        this.creatureElementStats = new CreatureElementStats(drawHelper, this.creatureElementStatsPlacement, creature, true);
    }

    /**
     * Selects the element to display information about.
     * @param element The element to display.
     */
    protected void selectElement(ElementDefinition element)
    {
        if (this.element != element) {
            element.soundEvent().ifPresent(soundEvent -> GuiSounds.get().play(soundEvent));
        }
        this.element = element;
    }

    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        super.draw(poseStack, widgetState, mouseInside);

        float padding = 2;
        float top = this.basePosition().y() + this.drawHelper.screenY(0.075F);
        float left = this.basePosition().x() + this.drawHelper.screenX(0.1F);
        float width = this.drawSize().x() * 0.8F;
        float height = this.drawSize().y() - this.drawHelper.screenY(0.15F);

        // Left Panel:
        float panelLeftX = left;
        float panelLeftY = top;
        float panelLeftWidth = (width / 2) - (padding * 2);
        float panelLeftCenter = panelLeftX + (panelLeftWidth / 2);

        Vec3 listPosition = new Vec3(panelLeftX, panelLeftY, 0);
        Vec2 listSizeMax = new Vec2(32, height);
        this.drawHelper.drawTexture(poseStack, this.listBackground, listPosition, listSizeMax, Vec2.ONE, new Color(0.75F, 0.75F, 1F, 0.75F));
        this.listPlacement.setPosition(listPosition);
        this.list.setSizeMax(listSizeMax).draw(poseStack, widgetState, mouseInside);
        panelLeftX += listSizeMax.x() + 8 + padding;
        panelLeftWidth -= listSizeMax.x() + 8 + padding;
        panelLeftCenter = panelLeftX + (panelLeftWidth / 2);

        if (this.element != null) {
            this.drawHelper.drawTextCentered(poseStack, this.element.name().copy().withStyle(ChatFormatting.BOLD), new Vec2(panelLeftCenter, panelLeftY), 0xFFFFFF, true);
            panelLeftY += 10 + padding;
            this.drawHelper.drawTextWrapped(poseStack, this.element.description(), new Vec2(panelLeftX, panelLeftY), (int)panelLeftWidth, 0xFFFFFF, true);
        }

        // Right Panel:
        float panelRightMargin = this.drawHelper.screenX(0.025F);
        float panelRightX = left + (width / 2) + padding + panelRightMargin;
        float panelRightY = top;
        float panelRightWidth = (width / 2) - (padding * 2) - panelRightMargin;
        float panelRightCenter = panelRightX + (panelRightWidth / 2);

        if (this.creature != null) {
            Component rightPanelTitle = Component.translatable(Schism.NAMESPACE + ".journal.title.element_affinities").withStyle(ChatFormatting.BOLD);
            this.drawHelper.drawTextCentered(poseStack, rightPanelTitle, new Vec2(panelRightCenter, panelRightY), 0xFFFFFF, true);
            panelRightY += 10 + padding;

            this.creatureElementStatsPlacement.setPosition(new Vec3(panelRightX, panelRightY, 0));
            this.creatureElementStats.draw(poseStack, widgetState, mouseInside);
        }
    }
}
