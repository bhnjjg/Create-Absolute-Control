package com.bhnjjg.absolutecontrol;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class TerminalScreen extends AbstractContainerScreen<TerminalMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("absolute_control", "textures/gui/terminal_gui.png");
    private EditBox rangeInput;
    private int selectedSlot = -1; 

    public TerminalScreen(TerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        // 你的图的准确尺寸
        this.imageWidth = 187;
        this.imageHeight = 189;
        this.titleLabelX = -1000; 
        this.inventoryLabelY = -1000; 
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 👇 根据截图估算，把输入框强行塞进你画的长条里 👇
        // 去掉了原版边框，并且把字数限制调大，让数字居中显示
        this.rangeInput = new EditBox(this.font, x + 50, y + 100, 80, 15, Component.literal("范围"));
        this.rangeInput.setMaxLength(3);
        this.rangeInput.setValue("2");
        this.rangeInput.setFilter(text -> text.matches("\\d*"));
        this.rangeInput.setBordered(false); // 彻底干掉原版黑框
        this.rangeInput.setTextColor(0x000000); // 如果背景是浅色，用黑字；如果是深色改回 0xFFFFFF
        this.addRenderableWidget(this.rangeInput);
        
        // 🚨 注意：这里不再 new 任何 Button 了！彻底删掉那个丑东西！
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;

        if (button == 0) {
            // 1. 判定是否点在了你画的“打勾”按钮上（根据截图，大概在 X:140, Y:100 附近）
            if (localX >= 130 && localX <= 170 && localY >= 90 && localY <= 120) {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendSystemMessage(Component.literal("§a[绝对控制] 点击了打勾按钮！范围：" + this.rangeInput.getValue()));
                }
                return true; // 拦截点击
            }

            // 2. 判定频率槽位（坐标得重新对齐你画的框）
            if (localX >= 35 && localX <= 65 && localY >= 65 && localY <= 95) {
                toggleSelection(0);
                return true;
            }
            else if (localX >= 75 && localX <= 105 && localY >= 65 && localY <= 95) {
                toggleSelection(1);
                return true;
            }

            // 3. 背包点击判定
            if (selectedSlot != -1) {
                // 适配你图里的背包位置（大概从 y=160 开始）
                if (mouseX >= this.leftPos + 20 && mouseX < this.leftPos + 20 + 9 * 18 &&
                    mouseY >= this.topPos + 160 && mouseY < this.topPos + 160 + 3 * 18) {
                    
                    int col = (int) ((mouseX - (this.leftPos + 20)) / 18);
                    int row = (int) ((mouseY - (this.topPos + 160)) / 18);
                    int slotIndex = 2 + col + row * 9; 

                    if (slotIndex >= 2 && slotIndex < this.menu.slots.size()) {
                        ItemStack source = this.menu.slots.get(slotIndex).getItem();
                        if (!source.isEmpty()) {
                            ((TerminalMenu) this.menu).setFrequency(selectedSlot, source); 
                        }
                    }
                    selectedSlot = -1; 
                    return true; 
                } else {
                    selectedSlot = -1; 
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void toggleSelection(int slotIndex) {
        if (selectedSlot == slotIndex) {
            selectedSlot = -1; 
        } else {
            selectedSlot = slotIndex; 
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, 187, 189);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // 选中高亮白框
        if (selectedSlot != -1) {
            int slotX = this.leftPos + (selectedSlot == 0 ? 35 : 75);
            int slotY = this.topPos + 65;
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 31, slotY + 31, 0xFFFFFFFF);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component titleText = this.getTitle();
        // 字居中于黄色区域
        guiGraphics.drawString(this.font, titleText, 55, 10, 0x404040, false);
    }
}