package com.schism.core.client.gui.screens.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Schism;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.GuiSounds;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.client.gui.widgets.ButtonWidget;
import com.schism.core.client.gui.widgets.FixedLayoutWidget;
import com.schism.core.client.gui.widgets.TextWidget;
import com.schism.core.creatures.ICreature;
import com.schism.core.lore.RealmDefinition;
import com.schism.core.lore.RealmRepository;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;

public class RealmsPage extends AbstractPage
{
    protected final ICreature creature;
    protected RealmDefinition realmDefinition;
    protected String universeName;

    protected final ResourceLocation listBackground;
    protected final Placement listPlacement;
    protected final FixedLayoutWidget<ButtonWidget> list;
    protected Placement descriptionLayoutPlacement;
    protected FixedLayoutWidget<TextWidget> descriptionLayout;
    protected Placement descriptionPlacement;
    protected TextWidget description;

    protected final ResourceLocation secondaryListBackground;
    protected final Placement secondaryListPlacement;
    protected final FixedLayoutWidget<ButtonWidget> secondaryList;
    protected Placement secondaryDescriptionLayoutPlacement;
    protected FixedLayoutWidget<TextWidget> secondaryDescriptionLayout;
    protected Placement secondaryDescriptionPlacement;
    protected TextWidget secondaryDescription;

    public RealmsPage(DrawHelper drawHelper, Placement placement, ICreature creature)
    {
        super(drawHelper, placement, new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal/page_realms.png"));
        this.creature = creature;

        // Primary:
        this.descriptionLayoutPlacement = new Placement();
        this.descriptionLayout = new FixedLayoutWidget<>(
                drawHelper,
                this.descriptionLayoutPlacement,
                new Vec3(0, 1, 0),
                new Vec3(0, 10, 0),
                Vec2.ZERO,
                true
        );
        this.descriptionPlacement = new Placement();
        this.description = new TextWidget(drawHelper, this.descriptionPlacement);
        this.descriptionLayout.add(this.description);
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

        // Universes List:
        Vec2 iconSize = new Vec2(28, 28);
        ResourceLocation buttonTexture = new ResourceLocation(Schism.NAMESPACE, "textures/gui/spot.png");
        for (String universeName : RealmRepository.get().universes().stream().sorted().toList()) {
            ButtonWidget button = new ButtonWidget(this.drawHelper, new Placement(iconSize), buttonTexture, () -> this.selectUniverse(universeName));
            button.setSelectedSupplier(() -> universeName.equals(this.universeName));
            button.setForeground(new ResourceLocation(Schism.NAMESPACE, "textures/gui/universe/" + universeName + ".png"), iconSize.add(-2));
            Component name = Component.translatable(Schism.NAMESPACE + ".universe." + universeName).withStyle(ChatFormatting.BOLD);
            button.addTooltip(name);
            this.list.add(button);
        }

        // Secondary:
        this.secondaryDescriptionLayoutPlacement = new Placement();
        this.secondaryDescriptionLayout = new FixedLayoutWidget<>(
                drawHelper,
                this.secondaryDescriptionLayoutPlacement,
                new Vec3(0, 1, 0),
                new Vec3(0, 10, 0),
                Vec2.ZERO,
                true
        );
        this.secondaryDescriptionPlacement = new Placement();
        this.secondaryDescription = new TextWidget(drawHelper, this.secondaryDescriptionPlacement);
        this.secondaryDescriptionLayout.add(this.secondaryDescription);
        this.secondaryListBackground = new ResourceLocation(Schism.NAMESPACE, "textures/gui/list_background.png");
        this.secondaryListPlacement = new Placement();
        this.secondaryList = new FixedLayoutWidget<>(
                this.drawHelper,
                this.secondaryListPlacement,
                new Vec3(0, 1,0),
                new Vec3(0, 4, 0),
                new Vec2(32, this.drawHelper.screenY(0.85F)),
                true
        );
        this.secondaryList.setFlex(new Vec2(0.5F, 0));

        // Realms List:
        for (RealmDefinition realmDefinition : RealmRepository.get().definitions().stream().sorted().toList()) {
            if (realmDefinition.icon().isEmpty()) {
                continue;
            }
            ButtonWidget button = new ButtonWidget(this.drawHelper, new Placement(iconSize), buttonTexture, () -> this.selectRealm(realmDefinition));
            button.setSelectedSupplier(() -> this.realmDefinition == realmDefinition);
            button.setForeground(realmDefinition.icon().get(), iconSize.add(-2));
            button.addTooltip(realmDefinition.name().copy().withStyle(ChatFormatting.BOLD));
            this.secondaryList.add(button);
        }

        // Default Selections:
        this.selectUniverse(null);
        this.selectRealm(null);
    }

    /**
     * Selects the universe to display information about.
     * @param universeName The name of the universe to display.
     */
    protected void selectUniverse(String universeName)
    {
        // No Universe:
        if (universeName == null) {
            this.universeName = null;
            this.description.clear();

            Component title = Component.translatable(Schism.NAMESPACE + ".journal.title.universes").withStyle(ChatFormatting.BOLD);
            this.description.add(title, true, true, true);

            Component description = Component.translatable(Schism.NAMESPACE + ".universe.description");
            this.description.add(description, true, false, true);

            for (String realmTypeName : RealmRepository.get().realmTypes()) {
                Component type = Component.translatable(Schism.NAMESPACE + ".realm.type." + realmTypeName).withStyle(ChatFormatting.BOLD);
                this.description.add(type, true, true, true);

                Component typeDescription = Component.translatable(Schism.NAMESPACE + ".realm.type." + realmTypeName + ".description");
                this.description.add(typeDescription, true, false, true);
            }

            return;
        }

        // Set Universe:
        if (this.universeName == null || !this.universeName.equals(universeName)) {
            GuiSounds.get().play(Schism.NAMESPACE, "gui.page");
        }
        this.universeName = universeName;
        this.description.clear();

        Component name = Component.translatable(Schism.NAMESPACE + ".universe." + universeName).withStyle(ChatFormatting.BOLD);
        this.description.add(name, true, true, true);

        Component description = Component.translatable(Schism.NAMESPACE + ".universe." + universeName + ".description");
        this.description.add(description, true, false, true);
    }

    /**
     * Selects the realm to display information about.
     * @param realmDefinition The realm to display.
     */
    protected void selectRealm(RealmDefinition realmDefinition)
    {
        // No Realm:
        if (realmDefinition == null) {
            this.realmDefinition = null;
            this.secondaryDescription.clear();

            Component title = Component.translatable(Schism.NAMESPACE + ".journal.title.realms").withStyle(ChatFormatting.BOLD);
            this.secondaryDescription.add(title, true, true, true);

            Component description = Component.translatable(Schism.NAMESPACE + ".realms.description");
            this.secondaryDescription.add(description, true, false, true);

            return;
        }

        // Set Realm:
        if (this.realmDefinition != realmDefinition) {
            GuiSounds.get().play(Schism.NAMESPACE, "gui.page");
        }
        this.realmDefinition = realmDefinition;

        this.secondaryDescription.clear();
        this.secondaryDescription.add(this.realmDefinition.name().copy().withStyle(ChatFormatting.BOLD), true, true, true);
        this.secondaryDescription.add(this.realmDefinition.description(), true, false, true);
        Component universe = Component.translatable(Schism.NAMESPACE + ".realm.universe")
                .append(": ").append(Component.translatable(Schism.NAMESPACE + ".realm.universe." + universeName));
        this.secondaryDescription.add(universe, true, false, true);
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

        Vec2 listSizeMax = new Vec2(32, height);

        // Left Panel:
        float panelLeftX = left;
        float panelLeftWidth = (width / 2) - (padding * 2);

        Vec3 listPosition = new Vec3(panelLeftX, top, 0);
        this.drawHelper.drawTexture(poseStack, this.listBackground, listPosition, listSizeMax, Vec2.ONE, new Color(0.75F, 0.75F, 1F, 0.75F));
        this.listPlacement.setPosition(listPosition);
        this.list.setSizeMax(listSizeMax).draw(poseStack, widgetState, mouseInside);

        panelLeftX += listSizeMax.x() + 8 + padding;
        panelLeftWidth -= listSizeMax.x() + 8 + padding;
        this.descriptionLayoutPlacement.setSize(new Vec2(panelLeftWidth, height)).setPosition(new Vec3(panelLeftX, top, 0));
        this.descriptionPlacement.setSize(new Vec2(panelLeftWidth, height));
        this.descriptionLayout.setSizeMax(new Vec2(panelLeftWidth, height));
        this.descriptionLayout.draw(poseStack, widgetState, mouseInside);

        // Right Panel:
        float panelRightMargin = this.drawHelper.screenX(0.025F);
        float panelRightX = left + (width / 2) + padding + panelRightMargin;
        float panelRightWidth = (width / 2) - (padding * 2) - panelRightMargin;

        listPosition = new Vec3(panelRightX, top, 0);
        this.drawHelper.drawTexture(poseStack, this.secondaryListBackground, listPosition, listSizeMax, Vec2.ONE, new Color(0.75F, 0.75F, 1F, 0.75F));
        this.secondaryListPlacement.setPosition(listPosition);
        this.secondaryList.setSizeMax(listSizeMax).draw(poseStack, widgetState, mouseInside);

        panelRightX += listSizeMax.x() + 8 + padding;
        panelRightWidth -= listSizeMax.x() + 8 + padding;
        this.secondaryDescriptionLayoutPlacement.setSize(new Vec2(panelRightWidth, height)).setPosition(new Vec3(panelRightX, top, 0));
        this.secondaryDescriptionPlacement.setSize(new Vec2(panelRightWidth, height));
        this.secondaryDescriptionLayout.setSizeMax(new Vec2(panelRightWidth, height));
        this.secondaryDescriptionLayout.draw(poseStack, widgetState, mouseInside);
    }
}
