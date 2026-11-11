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
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;

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

        this.rangeInput = new EditBox(this.font, x + 28, y + 67, 100, 12, Component.literal("范围"));
        this.rangeInput.setMaxLength(10);
        this.rangeInput.setValue(String.valueOf(((TerminalMenu)this.menu).currentRange));
        this.rangeInput.setFilter(text -> text.matches("\\d*"));
        this.rangeInput.setBordered(false); 
        this.rangeInput.setTextColor(0x000000); 
        this.rangeInput.setCanLoseFocus(true); 
        this.rangeInput.setCursorPosition(0); 
        this.rangeInput.setHighlightPos(0);
        this.addRenderableWidget(this.rangeInput);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double localX = mouseX - this.leftPos;
        double localY = mouseY - this.topPos;

        if (button == 0) {
            if (localX >= 135 && localX <= 165 && localY >= 58 && localY <= 85) {
                if (this.minecraft != null && this.minecraft.player != null) {
                    String value = this.rangeInput.getValue();
                    int range = 2;
                    try { range = Integer.parseInt(value); } catch (Exception ignored) {}
                    if (range < 2) range = 2;
                    ((TerminalMenu) this.menu).currentRange = range;
                    this.minecraft.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
                    this.minecraft.player.sendSystemMessage(Component.literal("§a[绝对控制] 配置已保存！范围: " + range));
                    this.minecraft.player.closeContainer(); 
                }
                return true; 
            }

            for (int i = 0; i < 2; i++) {
                Slot slot = this.menu.slots.get(i);
                if (this.isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                    toggleSelection(i);
                    return true;
                }
            }

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

    @Override
    public void removed() {
        super.removed();
        if (this.rangeInput != null) {
            String value = this.rangeInput.getValue();
            int range = 2;
            try { range = Integer.parseInt(value); } catch (Exception ignored) {}
            if (range < 2) range = 2;
            ((TerminalMenu) this.menu).currentRange = range;
        }
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
        
        if (selectedSlot != -1) {
            Slot slot = this.menu.slots.get(selectedSlot);
            guiGraphics.fill(this.leftPos + slot.x, this.topPos + slot.y, this.leftPos + slot.x + 16, this.topPos + slot.y + 16, 0x88FFFFFF);
        }

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
        guiGraphics.drawString(this.font, titleText, 6, 3, 0xFFFF00, true);
    }
}