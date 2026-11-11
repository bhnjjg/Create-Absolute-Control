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

        // 👇 输入框计算：你需要左下角 Y=11，MC字体高度约8，所以输入框组件顶部应该在 Y=3 左右 👇
        // X=29 保持你给的
        this.rangeInput = new EditBox(this.font, x + 29, y + 3, 100, 12, Component.literal("范围"));
        this.rangeInput.setMaxLength(3);
        this.rangeInput.setValue("2");
        this.rangeInput.setFilter(text -> text.matches("\\d*"));
        this.rangeInput.setBordered(false);
        this.rangeInput.setTextColor(0x000000); // 黑字
        this.addRenderableWidget(this.rangeInput);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;

        if (button == 0) {
            // 打勾按钮区域 (保持你之前的估算)
            if (localX >= 135 && localX <= 165 && localY >= 58 && localY <= 85) {
                if (this.minecraft != null && this.minecraft.player != null) {
                    this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
                    this.minecraft.player.sendSystemMessage(Component.literal("§a[绝对控制] 保存成功！"));
                }
                return true; 
            }

            // 频率槽位判定（直接读 Menu 里的绝对坐标）
            for (int i = 0; i < 2; i++) {
                Slot slot = this.menu.slots.get(i);
                if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                    toggleSelection(i);
                    return true;
                }
            }

            // 背包判定（同样依赖绝对坐标）
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
                        selectedSlot = -1; 
                        return true; 
                    }
                }
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
        
        // 👇 精准计算中心点，画高亮框，保证刚好覆盖 16x16 👇
        if (selectedSlot != -1) {
            Slot slot = this.menu.slots.get(selectedSlot);
            int centerX = this.leftPos + slot.x + 8;
            int centerY = this.topPos + slot.y + 8;
            
            // 以中心点反推，画出 16x16 的框
            guiGraphics.fill(centerX - 8, centerY - 8, centerX + 8, centerY + 8, 0x88FFFFFF);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component titleText = this.getTitle();
        // 👇 文字计算：你给的左下角是 6,11。减去字体高度 8，所以左上角画在 Y=3 👇
        guiGraphics.drawString(this.font, titleText, 6, 3, 0xFFFF00, true);
    }
}