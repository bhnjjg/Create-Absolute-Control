package com.bhnjjg.absolutecontrol;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class TerminalScreen extends AbstractContainerScreen<TerminalMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("absolute_control", "textures/gui/terminal_gui.png");
    
    private EditBox rangeInput;

    public TerminalScreen(TerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // 因为你的图是 256x256，但实际 UI 大概只有 176x200，这里我们设大一点以包容全图
        this.imageWidth = 176;
        this.imageHeight = 200; 
        this.titleLabelX = -1000; // 隐藏默认标题
        this.inventoryLabelY = -1000; // 隐藏默认“物品栏”文字
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 👇 输入框（对齐你画的中间那个大框）
        // x+35, y+65 是大致坐标，宽 110，高 15
        this.rangeInput = new EditBox(this.font, x + 35, y + 65, 110, 15, Component.literal("范围"));
        this.rangeInput.setMaxLength(3);
        this.rangeInput.setValue("2"); // 默认值 2
        // 强制只允许输入数字
        this.rangeInput.setFilter(text -> text.matches("\\d*"));
        this.addRenderableWidget(this.rangeInput);

        // 👇 打勾按钮（对齐你画的那个打勾的框）
        // x+155, y+63, 宽 18, 高 18
        Button checkButton = Button.builder(Component.literal("✔"), (btn) -> {
            // 点击按钮后执行的操作（先打印到聊天框，待会再写复杂的网络包保存数据）
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("§a[绝对控制] 配置保存！当前范围: " + this.rangeInput.getValue()));
            }
        }).bounds(x + 155, y + 63, 18, 18).build();
        
        this.addRenderableWidget(checkButton);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        
        // 只渲染左上角 176x200 的区域（如果你画图主体靠左上，这样刚好）
        guiGraphics.blit(TEXTURE, x, y, 0, 0, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 👇 标题改到左上角对齐黄色区域
        Component titleText = this.getTitle();
        guiGraphics.drawString(this.font, titleText, 5, 5, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // 悬停提示跟随槽位
        if (isHovering(15, 25, 18, 18, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.literal("频率 1"), mouseX, mouseY);
        } else if (isHovering(40, 25, 18, 18, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.literal("频率 2"), mouseX, mouseY);
        }
        
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}