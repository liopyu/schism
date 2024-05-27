package com.schism.core.client.gui.components;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.client.gui.widgets.AbstractWidget;
import com.schism.core.client.gui.widgets.IconWidget;
import com.schism.core.client.gui.widgets.LayoutWidget;
import com.schism.core.creatures.ICreature;
import com.schism.core.gods.GodDefinition;
import com.schism.core.gods.PantheonDefinition;
import com.schism.core.gods.Worship;
import net.minecraft.ChatFormatting;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;

public class CreaturePietyStats extends AbstractWidget
{
    protected final ICreature creature;
    protected final Placement layoutPlacement;
    protected final LayoutWidget<AbstractWidget> layout;
    protected final boolean descriptions;

    public CreaturePietyStats(DrawHelper drawHelper, Placement placement, ICreature creature, boolean descriptions)
    {
        super(drawHelper, placement);
        this.creature = creature;
        this.layoutPlacement = new Placement();
        this.layout = new LayoutWidget<>(
                this.drawHelper,
                this.layoutPlacement,
                new Vec3(1, 0, 0),
                new Vec3(2, 0, 0)
        ).setFlex(new Vec2(0, 0.5F));
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
        if (this.creature.piety().pantheon().isEmpty()) {
            return;
        }

        this.layout.removeIf(abstractWidget -> true);
        Placement setPlacement = new Placement(new Vec2(24, 24));

        PantheonDefinition pantheonDefinition = this.creature.piety().pantheon().get();
        IconWidget pantheonIcon = new IconWidget(this.drawHelper, setPlacement, pantheonDefinition.icon().orElse(null)).addTooltip(pantheonDefinition.name());
        if (this.descriptions) {
            pantheonIcon.addTooltip(pantheonDefinition.description().withStyle(ChatFormatting.GREEN));
        }
        this.layout.add(pantheonIcon);

        Placement iconPlacement = new Placement(new Vec2(22, 22));
        for (Worship worship : creature.piety().worships()) {
            GodDefinition godDefinition = worship.god().definition();
            IconWidget godIcon = new IconWidget(this.drawHelper, iconPlacement, godDefinition.icon().orElse(null)).addTooltip(godDefinition.name());
            if (this.descriptions) {
                godIcon.addTooltip(godDefinition.description().withStyle(ChatFormatting.GREEN));
            }
            this.layout.add(godIcon);
        }

        this.layoutPlacement.setPosition(this.drawPosition());
        this.layout.draw(poseStack, widgetState, mouseInside);
    }
}
