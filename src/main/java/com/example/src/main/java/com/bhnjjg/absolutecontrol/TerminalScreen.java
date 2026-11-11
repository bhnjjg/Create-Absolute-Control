package com.bhnjjg.absolutecontrol;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TerminalScreen extends AbstractContainerScreen<TerminalMenu> {
    // 这里指向你画的那张图
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("absolute_control", "textures/gui/terminal_gui.png");

    public TerminalScreen(TerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // 把你画的图贴上去
        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 悬停提示（对应咱们在 Menu 里设置的坐标：20,20 和 44,20）
        // 只有槽位没有物品的时候，才显示“频率 1”
if (this.menu.slots.get(0).getItem().isEmpty() && isHovering(20, 20, 18, 18, mouseX, mouseY)) {
    guiGraphics.renderTooltip(this.font, Component.literal("频率 1"), mouseX, mouseY);
} 
// 只有槽位没有物品的时候，才显示“频率 2”
else if (this.menu.slots.get(1).getItem().isEmpty() && isHovering(44, 20, 18, 18, mouseX, mouseY)) {
    guiGraphics.renderTooltip(this.font, Component.literal("频率 2"), mouseX, mouseY);
}
        
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}