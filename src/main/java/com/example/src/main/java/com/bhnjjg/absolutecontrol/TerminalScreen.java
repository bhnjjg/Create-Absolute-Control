package com.bhnjjg.absolutecontrol;
package com.bhnjjg.absolutecontrol;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class TerminalScreen extends AbstractContainerScreen<TerminalMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("absolute_control", "textures/gui/terminal_gui.png");
    private EditBox rangeInput;
    private int selectedSlot = -1;

    public TerminalScreen(TerminalMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 200; 
        this.titleLabelX = -1000; 
        this.inventoryLabelY = -1000; 
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        this.rangeInput = new EditBox(this.font, x + 35, y + 65, 110, 15, Component.literal("范围"));
        this.rangeInput.setMaxLength(3);
        this.rangeInput.setValue("2");
        this.rangeInput.setFilter(text -> text.matches("\\d*"));
        this.addRenderableWidget(this.rangeInput);

        Button checkButton = Button.builder(Component.literal("✔"), (btn) -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.sendSystemMessage(Component.literal("§a[绝对控制] 配置保存！当前范围: " + this.rangeInput.getValue()));
            }
        }).bounds(x + 155, y + 63, 18, 18).build();
        this.addRenderableWidget(checkButton);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;

        if (button == 0) { // 左键
            // 1. 选中频率
            if (localX >= 15 && localX <= 33 && localY >= 25 && localY <= 43) {
                handleSlotClick(0);
                return true;
            }
            else if (localX >= 40 && localX <= 58 && localY >= 25 && localY <= 43) {
                handleSlotClick(1);
                return true;
            }

            // 2. 选中状态下点击背包
            if (selectedSlot != -1) {
                // 👇 手写遍历，不用原版方法 👇
                Slot clickedSlot = null;
                for (Slot slot : this.menu.slots) {
                    if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                        clickedSlot = slot;
                        break;
                    }
                }

                // 如果是背包（index >= 2）
                if (clickedSlot != null && clickedSlot.index >= 2) {
                    ItemStack source = clickedSlot.getItem();
                    if (!source.isEmpty()) {
                        this.menu.setFrequency(selectedSlot, source);
                        if (this.minecraft != null && this.minecraft.player != null) {
                            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.ITEM_PICKUP, 0.5F, 1.5F);
                        }
                    }
                    selectedSlot = -1; // 完成分身
                    return true; 
                } else {
                    selectedSlot = -1; // 点空取消
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleSlotClick(int slotIndex) {
        if (this.minecraft == null || this.minecraft.player == null) return;
        if (selectedSlot == slotIndex) {
            selectedSlot = -1;
            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
        } else {
            selectedSlot = slotIndex;
            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        if (selectedSlot != -1) {
            int slotX = this.leftPos + (selectedSlot == 0 ? 15 : 40);
            int slotY = this.topPos + 25;
            guiGraphics.fill(slotX - 1, slotY - 1, slotX + 17, slotY + 17, 0xFFFFFFFF);
        }

        if (this.isHovering(15, 25, 18, 18, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.literal("频率 1 (点击选中)"), mouseX, mouseY);
        } else if (this.isHovering(40, 25, 18, 18, mouseX, mouseY)) {
            guiGraphics.renderTooltip(this.font, Component.literal("频率 2 (点击选中)"), mouseX, mouseY);
        }
        
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Component titleText = this.getTitle();
        guiGraphics.drawString(this.font, titleText, 5, 5, 0x404040, false);
    }
}