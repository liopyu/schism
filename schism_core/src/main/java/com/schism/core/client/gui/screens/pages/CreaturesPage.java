package com.schism.core.client.gui.screens.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Schism;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.GuiSounds;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.client.gui.components.CreatureDefinitionElements;
import com.schism.core.client.gui.widgets.ButtonWidget;
import com.schism.core.client.gui.widgets.FixedLayoutWidget;
import com.schism.core.client.gui.widgets.TextWidget;
import com.schism.core.creatures.CreatureDefinition;
import com.schism.core.creatures.CreatureRepository;
import com.schism.core.creatures.ICreature;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;

import java.awt.*;

public class CreaturesPage extends AbstractPage
{
    protected final ICreature creature;

    protected final ResourceLocation listBackground;
    protected final Placement listPlacement;
    protected final FixedLayoutWidget<ButtonWidget> list;

    protected CreatureDefinition creatureDefinition;

    protected Placement creatureDefinitionElementsPlacement;
    protected CreatureDefinitionElements creatureDefinitionElements;
    protected Placement creatureDescriptionLayoutPlacement;
    protected FixedLayoutWidget<TextWidget> creatureDescriptionLayout;
    protected Placement creatureDescriptionPlacement;
    protected FixedLayoutWidget<TextWidget> creatureDescription;

    public CreaturesPage(DrawHelper drawHelper, Placement placement, ICreature creature)
    {
        super(drawHelper, placement, new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal/page_creatures.png"));
        this.creature = creature;

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

        // Add Creature Definitions to List:
        Vec2 iconSize = new Vec2(28, 28);
        ResourceLocation buttonTexture = new ResourceLocation(Schism.NAMESPACE, "textures/gui/spot.png");
        for (CreatureDefinition creatureDefinition : CreatureRepository.get().definitions()) {
            ButtonWidget button = new ButtonWidget(this.drawHelper, new Placement(iconSize), buttonTexture, () -> this.selectCreature(creatureDefinition));
            button.setSelectedSupplier(() -> this.creatureDefinition == creatureDefinition);
            creatureDefinition.icon().ifPresent(icon -> button.setForeground(icon, iconSize.add(-2)));
            button.addTooltip(creatureDefinition.name());
            this.list.add(button);
        }

        // Description Layout:
        this.creatureDescriptionLayoutPlacement = new Placement();
        this.creatureDescription = new FixedLayoutWidget<>(
                this.drawHelper,
                this.creatureDescriptionLayoutPlacement,
                new Vec3(0, 1, 0),
                new Vec3(0, 10, 0),
                Vec2.ZERO,
                true
        );
        this.creatureDescriptionPlacement = new Placement();
        // TODO Create Description Here Instead
    }

    /**
     * Selects the creature definition to display information about.
     * @param creatureDefinition The effect to display.
     */
    protected void selectCreature(CreatureDefinition creatureDefinition)
    {
        if (this.creatureDefinition == creatureDefinition) {
            return;
        }
        this.creatureDefinition = creatureDefinition;
        this.creatureDefinitionElementsPlacement = new Placement();
        this.creatureDefinitionElements = new CreatureDefinitionElements(this.drawHelper, this.creatureDefinitionElementsPlacement, this.creatureDefinition, true);
        if (this.creatureDefinition.entityType().isPresent()) {
            ResourceLocation entityRegistryName = this.creatureDefinition.entityType().get().getRegistryName();
            if (entityRegistryName != null) {
                GuiSounds.get().play(entityRegistryName.getNamespace(), "entity." + entityRegistryName.getPath() + ".say");
            }
        }
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
        float panelLeftWidth = (width / 2) - (padding * 2);

        Vec3 listPosition = new Vec3(panelLeftX, top, 0);
        Vec2 listSizeMax = new Vec2(32, height);
        this.drawHelper.drawTexture(poseStack, this.listBackground, listPosition, listSizeMax, Vec2.ONE, new Color(0.75F, 0.75F, 1F, 0.75F));
        this.listPlacement.setPosition(listPosition);
        this.list.setSizeMax(listSizeMax).draw(poseStack, widgetState, mouseInside);

        panelLeftX += listSizeMax.x() + 8 + padding;
        panelLeftWidth -= listSizeMax.x() + 16 + padding;

        this.creatureDescription.clear();
        if (this.creatureDefinition != null) {
            TextWidget description = new TextWidget(this.drawHelper, this.creatureDescriptionPlacement);
            description.add(this.creatureDefinition.name().copy().withStyle(ChatFormatting.BOLD), true, true, true);
            description.add(this.creatureDefinition.description(), true, false, true);
            this.creatureDescription.add(description);

            MutableComponent habitatTitle = Component.translatable(Schism.NAMESPACE + ".journal.title.habitat").withStyle(ChatFormatting.BOLD);
            TextWidget habitat = new TextWidget(this.drawHelper, this.creatureDescriptionPlacement);
            habitat.add(habitatTitle, true, true, true);
            habitat.add(this.creatureDefinition.description("habitat"), true, false, true);

            MutableComponent realm = Component.translatable(Schism.NAMESPACE + ".realm").append(": ").append(this.creatureDefinition.realm().name());
            habitat.add(realm, true, false, true);
            this.creatureDescription.add(habitat);

            MutableComponent combatTitle = Component.translatable(Schism.NAMESPACE + ".journal.title.combat").withStyle(ChatFormatting.BOLD);
            TextWidget combat = new TextWidget(this.drawHelper, this.creatureDescriptionPlacement);
            combat.add(combatTitle, true, true, true);
            combat.add(this.creatureDefinition.description("combat"), true, false, true);
            this.creatureDescription.add(combat);

            this.creatureDescriptionLayoutPlacement.setSize(new Vec2(panelLeftWidth, height)).setPosition(new Vec3(panelLeftX, top, 0));
            this.creatureDescriptionPlacement.setSize(new Vec2(panelLeftWidth, height));
            this.creatureDescription.setSizeMax(new Vec2(panelLeftWidth, height));
            this.creatureDescription.draw(poseStack, widgetState, mouseInside);
        }

        // Right Panel:
        float panelRightMargin = this.drawHelper.screenX(0.025F);
        float panelRightX = left + (width / 2) + padding + panelRightMargin;
        float panelRightY = top;
        float panelRightWidth = (width / 2) - (padding * 2) - panelRightMargin;

        if (this.creatureDefinition != null) {
            MutableComponent title = Component.translatable(Schism.NAMESPACE + ".journal.title.element_affinities").withStyle(ChatFormatting.BOLD);
            panelRightY += this.drawHelper.drawTextWrapped(poseStack, title, new Vec2(panelRightX, panelRightY), (int)panelRightWidth, 0xFFFFFF, true);
            panelRightY += padding;

            this.creatureDefinitionElementsPlacement.setPosition(new Vec3(panelRightX, panelRightY, 0));
            this.creatureDefinitionElements.draw(poseStack, widgetState, mouseInside);
        }
    }
}
