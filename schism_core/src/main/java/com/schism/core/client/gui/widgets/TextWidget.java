package com.schism.core.client.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import com.schism.core.client.gui.DrawHelper;
import com.schism.core.client.gui.Placement;
import com.schism.core.client.gui.WidgetState;
import com.schism.core.util.Vec2;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TextWidget extends AbstractWidget
{
    protected record TextEntry(DrawHelper drawHelper, TextWidget textWidget, Component component, boolean wrap, boolean center, boolean shadow)
    {
        public Vec2 size() {
            float width = this.textWidget().baseSize().x();
            float height = 10;
            if (this.wrap() && !this.center()) {
                height = this.drawHelper().wrappedHeight(this.component(), (int)width);
            }
            return new Vec2(width, height);
        }
    }

    protected final List<TextEntry> textEntries;
    protected final int lineHeight;
    protected final int padding;

    /**
     * A widget that displays text.
     * @param drawHelper The draw helper to render with.
     * @param placement The placement to position and size this widget with.
     */
    public TextWidget(DrawHelper drawHelper, Placement placement)
    {
        super(drawHelper, placement);

        this.textEntries = new ArrayList<>();
        this.lineHeight = 10;
        this.padding = 2;
    }

    @Override
    public Vec2 drawSize()
    {
        return this.textEntries.stream()
                .map(TextEntry::size)
                .map(size -> new Vec2(size.x(), size.y() + this.padding))
                .reduce(Vec2.ZERO, Vec2::add);
    }

    /**
     * Adds a text component to display.
     * @param component The text component to add.
     * @param wrap If true, the text will be wrapped when exceeding this widget's width.
     * @param center If true, the text will be centered across the width instead.
     * @param shadow If true, the text will have a drop shadow.
     * @return The current instance.
     */
    public TextWidget add(Component component, boolean wrap, boolean center, boolean shadow)
    {
        this.textEntries.add(new TextEntry(this.drawHelper, this, component, wrap, center, shadow));
        return this;
    }

    /**
     * Clears all text components.
     * @return The current instance.
     */
    public TextWidget clear()
    {
        this.textEntries.clear();
        return this;
    }

    @Override
    public void draw(PoseStack poseStack, WidgetState widgetState, boolean mouseInside)
    {
        float y = this.drawPosition().y();
        for (TextEntry textEntry : this.textEntries) {
            if (textEntry.center()) {
                this.drawHelper.drawTextCentered(poseStack, textEntry.component(), new Vec2(this.drawPosition().x() + (this.baseSize().x() / 2), y), 0xFFFFFF, textEntry.shadow());
                y += 10;
            } else if (textEntry.wrap()) {
                y += this.drawHelper.drawTextWrapped(poseStack, textEntry.component(), new Vec2(this.drawPosition().x(), y), (int)this.baseSize().x(), 0xFFFFFF, textEntry.shadow());
            } else {
                this.drawHelper.drawText(poseStack, textEntry.component(), new Vec2(this.drawPosition().x(), y), 0xFFFFFF, textEntry.shadow());
                y += 10;
            }
            y += this.padding;
        }
    }
}
