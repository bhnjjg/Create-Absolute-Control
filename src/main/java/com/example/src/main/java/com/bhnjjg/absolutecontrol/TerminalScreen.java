package com.bhnjjg.absolutecontrol;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;

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

        // 输入框：再往左靠一点
        this.rangeInput = new EditBox(this.font, x + 25, y + 70, 100, 15, Component.literal("范围"));
        this.rangeInput.setMaxLength(3);
        this.rangeInput.setValue("2");
        this.rangeInput.setFilter(text -> text.matches("\\d*"));
        this.rangeInput.setBordered(false);
        this.rangeInput.setTextColor(0x404040); // 改回深灰色，免得看不见
        this.addRenderableWidget(this.rangeInput);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;

        if (button == 0) {
            // 1. 打勾按钮区域 (根据你的图微调)
            if (localX >= 145 && localX <= 170 && localY >= 65 && localY <= 90) {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
                    this.minecraft.player.sendSystemMessage(Component.literal("§a[绝对控制] 保存成功！"));
                }
                return true; 
            }

            // 2. 频率槽位判定 👇 核心：直接用物理槽位坐标判定！ 👇
            for (int i = 0; i < 2; i++) {
                Slot slot = this.menu.slots.get(i);
                // 只要鼠标在这个物理槽位内，就认为点中了！
                if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                    toggleSelection(i);
                    return true;
                }
            }

            // 3. 背包判定：如果处于选中状态，点击背包区域
            if (selectedSlot != -1) {
                for (Slot slot : this.menu.slots) {
                    if (slot.index >= 2 && this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                        ItemStack source = slot.getItem();
                        if (!source.isEmpty()) {
                            ((TerminalMenu) this.menu).setFrequency(selectedSlot, source); 
                            if (this.minecraft != null && this.minecraft.player != null) {
                                this.minecraft.player.playSound(SoundEvents.ITEM_PICKUP, 0.5F, 1.5F);
                            }
                        }
                        selectedSlot = -1; // 完成分身，退出选中
                        return true; 
                    }
                }
                // 如果点到了空地，也取消选中
                selectedSlot = -1; 
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void toggleSelection(int slotIndex) {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
        }
        if (selectedSlot == slotIndex) selectedSlot = -1; 
        else selectedSlot = slotIndex; 
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, 187, 189, 187, 189);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // 画选中高亮框
        if (selectedSlot != -1) {
            Slot slot = this.menu.slots.get(selectedSlot);
            guiGraphics.fill(this.leftPos + slot.x, this.topPos + slot.y, this.leftPos + slot.x + 16, this.topPos + slot.y + 16, 0x88FFFFFF);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component titleText = this.getTitle();
        int textWidth = this.font.width(titleText);
        // 改回深色，如果还是看不见，就改回 0x404040 或者根据你的背景用 0xFFFFFF
        guiGraphics.drawString(this.font, titleText, (this.imageWidth - textWidth) / 2, 12, 0x404040, false);
    }
}