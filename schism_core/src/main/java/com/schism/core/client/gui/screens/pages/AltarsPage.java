package com.schism.core.client.gui.screens.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Log;
import com.schism.core.Schism;
import com.schism.core.altars.AltarDefinition;
import com.schism.core.altars.AltarRepository;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.client.gui.widgets.ButtonWidget;
import com.schism.core.client.gui.widgets.FixedLayoutWidget;
import com.schism.core.creatures.ICreature;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import com.schism.core.util.Vec2;
import com.schism.core.util.Vec3;

import java.awt.*;

public class AltarsPage extends AbstractPage
{
    protected final ICreature creature;
    protected AltarDefinition altarDefinition;

    protected final ResourceLocation listBackground;
    protected final Placement listPlacement;
    protected final FixedLayoutWidget<ButtonWidget> list;

    public AltarsPage(DrawHelper drawHelper, Placement placement, ICreature creature)
    {
        super(drawHelper, placement, new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal/page_altars.png"));
        this.creature = creature;
        this.altarDefinition = null;

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

        // Add Altars to List:
        Vec2 iconSize = new Vec2(28, 28);
        ResourceLocation buttonTexture = new ResourceLocation(Schism.NAMESPACE, "textures/gui/spot.png");
        for (AltarDefinition altarDefinition : AltarRepository.get().definitions().stream().sorted().toList()) {
            if (altarDefinition.coreBlocks().isEmpty()) {
                continue;
            }
            ButtonWidget button = new ButtonWidget(this.drawHelper, new Placement(iconSize), buttonTexture, () -> this.selectAltar(altarDefinition));
            button.setSelectedSupplier(() -> this.altarDefinition == altarDefinition);
            button.setItemStack(new ItemStack(altarDefinition.coreBlocks().get(0)), 1.5F);
            button.addTooltip(altarDefinition.name().withStyle(ChatFormatting.BOLD));
            this.list.add(button);
        }
    }

    /**
     * Selects the altar to display information about.
     * @param altarDefinition The altar to display.
     */
    protected void selectAltar(AltarDefinition altarDefinition)
    {
        this.altarDefinition = altarDefinition;
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
        float middle = top + (this.drawSize().y() / 2);

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

        if (this.altarDefinition != null) {
            this.drawHelper.drawTextCentered(poseStack, this.altarDefinition.name().withStyle(ChatFormatting.BOLD), new Vec2(panelLeftCenter, panelLeftY), 0xFFFFFF, true);
            panelLeftY += 10 + padding;
            this.drawHelper.drawTextWrapped(poseStack, this.altarDefinition.description(), new Vec2(panelLeftX, panelLeftY), (int)panelLeftWidth, 0xFFFFFF, true);
        }

        // Right Panel:
        float panelRightMargin = this.drawHelper.screenX(0.025F);
        float panelRightX = left + (width / 2) + padding + panelRightMargin;
        float panelRightY = top;
        float panelRightWidth = (width / 2) - (padding * 2) - panelRightMargin;
        float panelRightCenter = panelRightX + (panelRightWidth / 2);

        if (this.altarDefinition != null) {
            Vec3 blueprintPosition = new Vec3(
                    panelRightCenter - (this.altarDefinition.structure().size().x() / 2),
                    middle - (this.altarDefinition.structure().size().y() / 2),
                    100 + (this.altarDefinition.structure().size().z())
            );
            float blockSpacing = 18;
            this.altarDefinition.structure().layout().forEach((key, value) -> {
                if (value == null || value.isEmpty() || value.get(0).isEmpty()) {
                    return;
                }
                Vec3 renderPosition = blueprintPosition.add(
                        (key.x() * blockSpacing) + (-key.z() * blockSpacing),
                        -key.y() * blockSpacing + (-key.x() * (blockSpacing / 2)) + (-key.z() * (blockSpacing / 2)),
                        -key.z() * blockSpacing
                );
                Block block = value.get(0).get();
                ItemStack itemStack = new ItemStack(block.asItem());
                if (block instanceof LiquidBlock liquidBlock) {
                    itemStack = liquidBlock.getFluid().getBucket().getDefaultInstance();
                }
                this.drawHelper.drawItemStack(poseStack, itemStack, renderPosition, 1.5F);
            });
        }
    }
}
