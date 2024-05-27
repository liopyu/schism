package com.schism.core.client.gui.screens.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Schism;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.client.gui.widgets.ButtonWidget;
import com.schism.core.client.gui.widgets.FixedLayoutWidget;
import com.schism.core.creatures.ICreature;
import com.schism.core.resolvers.EffectResolver;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.awt.*;
import java.util.Comparator;

public class EffectsPage extends AbstractPage
{
    protected final ICreature creature;

    protected final ResourceLocation listBackground;
    protected final Placement listPlacement;
    protected final FixedLayoutWidget<ButtonWidget> list;

    protected MobEffect effect;

    public EffectsPage(DrawHelper drawHelper, Placement placement, ICreature creature)
    {
        super(drawHelper, placement, new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal/page_effects.png"));
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

        // Add Effects to List:
        Vec2 iconSize = new Vec2(28, 28);
        ResourceLocation buttonTexture = new ResourceLocation(Schism.NAMESPACE, "textures/gui/spot.png");
        for (MobEffect effect : ForgeRegistries.MOB_EFFECTS.getValues().stream().sorted(Comparator.comparing(a -> a.getDisplayName().getString())).toList()) {
            if (effect.getRegistryName() == null) {
                continue;
            }
            String descriptionKey = effect.getDescriptionId() + ".description";
            if (Component.translatable(descriptionKey).getString().equals(descriptionKey)) {
                continue;
            }
            ButtonWidget button = new ButtonWidget(this.drawHelper, new Placement(iconSize), buttonTexture, () -> this.selectEffect(effect));
            button.setSelectedSupplier(() -> this.effect == effect);
            button.setForeground(new ResourceLocation(effect.getRegistryName().getNamespace(), "textures/mob_effect/" + effect.getRegistryName().getPath() + ".png"), iconSize.add(-2));
            button.setColor(new Color(effect.getColor()));
            button.setForegroundColor(Color.WHITE);
            button.addTooltip(effect.getDisplayName());
            this.list.add(button);
        }
    }

    /**
     * Selects the effect to display information about.
     * @param effect The effect to display.
     */
    protected void selectEffect(MobEffect effect)
    {
        this.effect = effect;
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

        if (this.effect != null) {
            Component title = new TextComponent("").append(this.effect.getDisplayName()).withStyle(ChatFormatting.BOLD);
            this.drawHelper.drawTextCentered(poseStack, title, new Vec2(panelLeftCenter, panelLeftY), 0xFFFFFF, true);

            panelLeftY += 10 + padding;
            String descriptionKey = effect.getDescriptionId() + ".description";
            Component description = Component.translatable(descriptionKey);
            if (description.getString().equals(descriptionKey)) {
                description = Component.translatable(Schism.NAMESPACE + ".effects.no_description");
            }
            panelLeftY += this.drawHelper.drawTextWrapped(poseStack, description, new Vec2(panelLeftX, panelLeftY), (int)panelLeftWidth, 0xFFFFFF, true);

            panelLeftY += padding;
            String groupName = EffectResolver.get().getGroup(effect);
            Component group = Component.translatable(Schism.NAMESPACE + ".effects.group").append(": ").append(Component.translatable(Schism.NAMESPACE + ".effects.group." + groupName));
            this.drawHelper.drawTextWrapped(poseStack, group, new Vec2(panelLeftX, panelLeftY), (int)panelLeftWidth, 0xFFFFFF, true);
        }
    }
}
