package com.bhnjjg.absolutecontrol;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class TerminalMenu extends AbstractContainerMenu {
    private final SimpleContainer container;
    private final ItemStack terminalStack;

    public TerminalMenu(int containerId, Inventory playerInventory, ItemStack terminalStack) {
        super(AbsoluteControl.TERMINAL_MENU.get(), containerId);
        this.terminalStack = terminalStack;
        this.container = new SimpleContainer(2);

        CustomData customData = terminalStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("freq1")) {
            this.container.setItem(0, ItemStack.parseOptional(playerInventory.player.registryAccess(), tag.getCompound("freq1")));
        }
        if (tag.contains("freq2")) {
            this.container.setItem(1, ItemStack.parseOptional(playerInventory.player.registryAccess(), tag.getCompound("freq2")));
        }

        // 频率槽位（未改动）
        this.addSlot(new Slot(container, 0, 17, 29) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        this.addSlot(new Slot(container, 1, 40, 29) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        // ==========================================================
        // 👇 百分百按照你给的 Y轴 (105, 123, 141, 163) 手敲 👇
        // ==========================================================
        
        // 大背包 - 第一排 (Y=105)
        this.addSlot(new Slot(playerInventory, 9, 8, 105));
        this.addSlot(new Slot(playerInventory, 10, 26, 105));
        this.addSlot(new Slot(playerInventory, 11, 44, 105));
        this.addSlot(new Slot(playerInventory, 12, 62, 105));
        this.addSlot(new Slot(playerInventory, 13, 80, 105));
        this.addSlot(new Slot(playerInventory, 14, 98, 105));
        this.addSlot(new Slot(playerInventory, 15, 116, 105));
        this.addSlot(new Slot(playerInventory, 16, 134, 105));
        this.addSlot(new Slot(playerInventory, 17, 152, 105));

        // 大背包 - 第二排 (Y=123)
        this.addSlot(new Slot(playerInventory, 18, 8, 123));
        this.addSlot(new Slot(playerInventory, 19, 26, 123));
        this.addSlot(new Slot(playerInventory, 20, 44, 123));
        this.addSlot(new Slot(playerInventory, 21, 62, 123));
        this.addSlot(new Slot(playerInventory, 22, 80, 123));
        this.addSlot(new Slot(playerInventory, 23, 98, 123));
        this.addSlot(new Slot(playerInventory, 24, 116, 123));
        this.addSlot(new Slot(playerInventory, 25, 134, 123));
        this.addSlot(new Slot(playerInventory, 26, 152, 123));

        // 大背包 - 第三排 (Y=141)
        this.addSlot(new Slot(playerInventory, 27, 8, 141));
        this.addSlot(new Slot(playerInventory, 28, 26, 141));
        this.addSlot(new Slot(playerInventory, 29, 44, 141));
        this.addSlot(new Slot(playerInventory, 30, 62, 141));
        this.addSlot(new Slot(playerInventory, 31, 80, 141));
        this.addSlot(new Slot(playerInventory, 32, 98, 141));
        this.addSlot(new Slot(playerInventory, 33, 116, 141));
        this.addSlot(new Slot(playerInventory, 34, 134, 141));
        this.addSlot(new Slot(playerInventory, 35, 152, 141));

        // 快捷栏 (Y=163)
        this.addSlot(new Slot(playerInventory, 0, 8, 163));
        this.addSlot(new Slot(playerInventory, 1, 26, 163));
        this.addSlot(new Slot(playerInventory, 2, 44, 163));
        this.addSlot(new Slot(playerInventory, 3, 62, 163));
        this.addSlot(new Slot(playerInventory, 4, 80, 163));
        this.addSlot(new Slot(playerInventory, 5, 98, 163));
        this.addSlot(new Slot(playerInventory, 6, 116, 163));
        this.addSlot(new Slot(playerInventory, 7, 134, 163));
        this.addSlot(new Slot(playerInventory, 8, 152, 163));
    }

    public TerminalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, playerInventory.player.getMainHandItem());
    }

    public void setFrequency(int slotIndex, ItemStack stack) {
        if (stack.isEmpty()) {
            this.container.setItem(slotIndex, ItemStack.EMPTY);
        } else {
            ItemStack ghost = stack.copy();
            ghost.setCount(1); 
            this.container.setItem(slotIndex, ghost);
        }
        this.broadcastChanges(); 
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        CustomData customData = terminalStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();

        if (!this.container.getItem(0).isEmpty()) tag.put("freq1", this.container.getItem(0).save(player.registryAccess()));
        else tag.remove("freq1");
        
        if (!this.container.getItem(1).isEmpty()) tag.put("freq2", this.container.getItem(1).save(player.registryAccess()));
        else tag.remove("freq2");

        terminalStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    public ItemStack getTerminalStack() {
        return this.terminalStack;
    }
}