package com.schism.core.client.gui.screens.pages;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.Schism;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.client.gui.widgets.TextWidget;
import com.schism.core.creatures.ICreature;
import com.schism.core.util.Vec2;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.schism.core.util.Vec3;

public class IndexPage extends AbstractPage
{
    protected final ICreature creature;

    protected final Placement textPlacement;
    protected final TextWidget text;

    public IndexPage(DrawHelper drawHelper, Placement placement, ICreature creature)
    {
        super(drawHelper, placement, new ResourceLocation(Schism.NAMESPACE, "textures/gui/journal/page_index.png"));
        this.creature = creature;

        this.textPlacement = new Placement();
        this.text = new TextWidget(this.drawHelper, this.textPlacement);
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
        float center = left + (width / 2);

        this.text.clear();

        Component title = Component.literal(Schism.MOD_NAME + " Alpha").withStyle(ChatFormatting.BOLD);
        this.text.add(title, true, true, true);

        Component info = Component.literal("Schism is still under early development, test away and enjoy!");
        this.text.add(info, true, false, true);

        // Stats:
        if (this.creature != null) {
            this.text.add(Component.literal(""), true, false, true);

            MutableComponent stats = Component.translatable(Schism.NAMESPACE + ".journal.title.stats").withStyle(ChatFormatting.BOLD);
            this.text.add(stats, true, false, true);

            MutableComponent level = Component.translatable(Schism.NAMESPACE + ".stats.level");
            level.append(": " + this.creature.stats().level());
            this.text.add(level, true, false, true);

            MutableComponent experience = Component.translatable(Schism.NAMESPACE + ".stats.experience");
            experience.append(": " + this.creature.stats().experience() + "/" + this.creature.stats().experienceTarget());
            this.text.add(experience, true, false, true);

            MutableComponent health = Component.translatable(Schism.NAMESPACE + ".stats.health");
            health.append(": " + String.format("%.2f", this.creature.livingEntity().getHealth()) + "/" + String.format("%.2f", this.creature.livingEntity().getMaxHealth())
                    + " (+" + String.format("%.2f", this.creature.stats().health()) + ")"
            );
            this.text.add(health, true, false, true);

            MutableComponent attack = Component.translatable(Schism.NAMESPACE + ".stats.attack");
            attack.append(": " + String.format("%.2f", this.creature.livingEntity().getAttributeValue(Attributes.ATTACK_DAMAGE) + this.creature.stats().attack())
                    + " (+" + String.format("%.2f", this.creature.stats().attack()) + ")"
            );
            this.text.add(attack, true, false, true);

            MutableComponent defense = Component.translatable(Schism.NAMESPACE + ".stats.defense");
            defense.append(": " + this.creature.stats().defense());
            this.text.add(defense, true, false, true);
        }

        this.textPlacement.setSize(new Vec2(width, height)).setPosition(new Vec3(left, top, 0));
        this.text.draw(poseStack, widgetState, mouseInside);
    }
}
