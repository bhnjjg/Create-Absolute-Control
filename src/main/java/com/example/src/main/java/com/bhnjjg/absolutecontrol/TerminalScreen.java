package com.bhnjjg.absolutecontrol;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TerminalScreen extends AbstractContainerScreen<TerminalMenu> {
    // 👇 确保这里是 absolute_control，千万不要是 minecraft
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("absolute_control", "textures/gui/terminal_gui.png");

    public TerminalScreen(TerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = -1000; // 隐藏原版左上角的字
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // 把你画的图贴上去
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 强行把标题画在中间黄色区域（y+8 的位置）
        Component titleText = this.getTitle();
        int textWidth = this.font.width(titleText);
        guiGraphics.drawString(this.font, titleText, (this.imageWidth - textWidth) / 2, 8, 0xFFFFFF, true);
        
        // 画物品栏的字
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, this.imageHeight - 96 + 2, 0xFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // 悬停提示
        if (isHovering(20, 20, 18, 18, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.literal("频率 1"), mouseX, mouseY);
        } else if (isHovering(44, 20, 18, 18, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.literal("频率 2"), mouseX, mouseY);
        }
        
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}