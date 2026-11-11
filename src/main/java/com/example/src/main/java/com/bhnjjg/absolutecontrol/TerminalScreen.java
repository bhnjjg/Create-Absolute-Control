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

        // 输入框：根据新布局大概在 X=40, Y=68 的位置
        this.rangeInput = new EditBox(this.font, x + 40, y + 68, 90, 15, Component.literal("范围"));
        this.rangeInput.setMaxLength(3);
        this.rangeInput.setValue("2");
        this.rangeInput.setFilter(text -> text.matches("\\d*"));
        this.rangeInput.setBordered(false); // 去黑框
        this.rangeInput.setTextColor(0xFFFFFF); // 白字
        this.addRenderableWidget(this.rangeInput);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;

        if (button == 0) {
            // 1. 判定按钮 (X=140, Y=68 附近)
            if (localX >= 140 && localX <= 170 && localY >= 65 && localY <= 90) {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.sendSystemMessage(Component.literal("§a[绝对控制] 保存！范围：" + this.rangeInput.getValue()));
                }
                return true; 
            }

            // 2. 判定频率槽位 (必须严格对齐 Menu 里的 35,45 和 65,45)
            // 注意：这里用 isHovering 判断，确保 100% 贴合
            if (this.isHovering(35, 45, 18, 18, mouseX, mouseY)) {
                toggleSelection(0);
                return true;
            }
            else if (this.isHovering(65, 45, 18, 18, mouseX, mouseY)) {
                toggleSelection(1);
                return true;
            }

            // 3. 判定背包
            if (selectedSlot != -1) {
                if (mouseX >= this.leftPos + 8 && mouseX < this.leftPos + 8 + 9 * 18 &&
                    mouseY >= this.topPos + 110 && mouseY < this.topPos + 110 + 3 * 18) {
                    
                    int col = (int) ((mouseX - (this.leftPos + 8)) / 18);
                    int row = (int) ((mouseY - (this.topPos + 110)) / 18);
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
        if (selectedSlot == slotIndex) selectedSlot = -1; 
        else selectedSlot = slotIndex; 
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        // 严格按 187x189 渲染
        guiGraphics.blit(TEXTURE, x, y, 0, 0, 187, 189, 187, 189);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // 画选中白框
        if (selectedSlot != -1) {
            int slotX = this.leftPos + (selectedSlot == 0 ? 35 : 65);
            int slotY = this.topPos + 45;
            guiGraphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFFFFFFFF);
        }
    }
}