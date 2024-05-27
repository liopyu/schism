package com.schism.core.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Schism;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.client.gui.widgets.AbstractWidget;
import com.schism.core.client.gui.widgets.IconWidget;
import com.schism.core.client.gui.widgets.LayoutWidget;
import com.schism.core.creatures.CreatureDefinition;
import com.schism.core.elements.ElementDefinition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreatureDefinitionElements extends AbstractWidget
{
    protected final CreatureDefinition creatureDefinition;
    protected final Placement layoutPlacement;
    protected final LayoutWidget<AbstractWidget> layout;
    protected final boolean descriptions;

    public CreatureDefinitionElements(DrawHelper drawHelper, Placement placement, CreatureDefinition creatureDefinition, boolean descriptions)
    {
        super(drawHelper, placement);
        this.creatureDefinition = creatureDefinition;
        this.layoutPlacement = new Placement();
        this.layout = new LayoutWidget<>(
                this.drawHelper,
                this.layoutPlacement,
                new Vec3(0, 1, 0),
                new Vec3(0, 2, 0)
        );
        this.descriptions = descriptions;
    }

    @Override
    public Vec2 drawSize()
    {
        return this.layout.drawSize();
    }

    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        this.layout.removeIf(abstractWidget -> true);
        LinkedHashMap<String, List<ElementDefinition>> elementSets = new LinkedHashMap<>();
        List<ElementDefinition> baseElements = new ArrayList<>();
        baseElements.add(this.creatureDefinition.baseElement());
        elementSets.put("base", baseElements);
        elementSets.put("resist", this.creatureDefinition.resistElements());
        elementSets.put("weakness", this.creatureDefinition.weaknessElements());

        Placement setPlacement = new Placement(new Vec2(24, 24));
        Placement iconPlacement = new Placement(new Vec2(22, 22));
        Vec3 spacingH = new Vec3(2, 0, 0);
        Vec2 flexH = new Vec2(0, 0.5F);
        for (Map.Entry<String, List<ElementDefinition>> elementSet : elementSets.entrySet()) {
            if (!this.descriptions && elementSet.getValue().isEmpty()) {
                continue;
            }

            LayoutWidget<AbstractWidget> elementLayout = new LayoutWidget<>(this.drawHelper, new Placement(), new Vec3(1, 0, 0), spacingH).setFlex(flexH);
            ResourceLocation setTexture = new ResourceLocation(Schism.NAMESPACE, "textures/gui/icons/" + elementSet.getKey() + ".png");
            MutableComponent setName = Component.translatable(Schism.NAMESPACE + ".element_set." + elementSet.getKey());
            IconWidget setIcon = new IconWidget(this.drawHelper, setPlacement, setTexture).addTooltip(setName);
            if (this.descriptions) {
                MutableComponent setDescription = Component.translatable(Schism.NAMESPACE + ".element_set." + elementSet.getKey() + ".description").withStyle(ChatFormatting.GREEN);
                setIcon.addTooltip(setDescription);
            }
            elementLayout.add(setIcon);

            for (ElementDefinition element : elementSet.getValue()) {
                IconWidget elementIcon = new IconWidget(this.drawHelper, iconPlacement, element.icon().orElse(null)).addTooltip(element.name());
                if (this.descriptions) {
                    elementIcon.addTooltip(element.description().copy().withStyle(ChatFormatting.GREEN));
                    elementIcon.setColor(element.color());
                }
                elementLayout.add(elementIcon);
            }

            this.layout.add(elementLayout);
        }

        this.layoutPlacement.setPosition(this.drawPosition());
        this.layout.draw(poseStack, widgetState, mouseInside);
    }
}
