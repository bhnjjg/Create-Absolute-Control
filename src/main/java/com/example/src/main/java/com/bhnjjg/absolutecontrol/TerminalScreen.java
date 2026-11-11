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

        // 👇 输入框：位置 X=45, Y=69，宽 50，高 12 (修复字体拉宽问题) 👇
        this.rangeInput = new EditBox(this.font, x + 45, y + 69, 50, 12, Component.literal("范围"));
        this.rangeInput.setMaxLength(3);
        this.rangeInput.setValue("2");
        this.rangeInput.setFilter(text -> text.matches("\\d*"));
        this.rangeInput.setBordered(false);
        this.rangeInput.setTextColor(0x000000); // 纯黑字
        this.addRenderableWidget(this.rangeInput);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;

        if (button == 0) {
            // 打勾按钮区域判定
            if (localX >= 135 && localX <= 165 && localY >= 58 && localY <= 85) {
                if (this.minecraft != null && this.minecraft.player != null) {
                    // 存入范围数据
                    String value = this.rangeInput.getValue();
                    int range = value.isEmpty() ? 2 : Integer.parseInt(value);
                    
                    ItemStack stack = ((TerminalMenu) this.menu).getTerminalStack();
                    net.minecraft.core.component.CustomData customData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.core.component.CustomData.EMPTY);
                    net.minecraft.nbt.CompoundTag tag = customData.copyTag();
                    tag.putInt("range", range);
                    stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.core.component.CustomData.of(tag));

                    this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
                    this.minecraft.player.sendSystemMessage(Component.literal("§a[绝对控制] 配置已保存！范围: " + range));
                }
                return true; 
            }

            // 频率槽位判定
            for (int i = 0; i < 2; i++) {
                Slot slot = this.menu.slots.get(i);
                if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                    toggleSelection(i);
                    return true;
                }
            }

            // 背包判定（分身魔法）
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
        
        // 选中高亮白框
        if (selectedSlot != -1) {
            Slot slot = this.menu.slots.get(selectedSlot);
            guiGraphics.fill(this.leftPos + slot.x, this.topPos + slot.y, this.leftPos + slot.x + 16, this.topPos + slot.y + 16, 0x88FFFFFF);
        }

        // 悬停提示（频率1/2）
        for (int i = 0; i < 2; i++) {
            Slot slot = this.menu.slots.get(i);
            if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                guiGraphics.renderTooltip(this.font, Component.literal("频率 " + (i + 1)), mouseX, mouseY);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component titleText = this.getTitle();
        // 黄色标题带阴影
        guiGraphics.drawString(this.font, titleText, 6, 3, 0xFFFF00, true);
    }
}