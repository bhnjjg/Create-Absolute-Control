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

        // 👇 根据新图 (187x189) 精确定位频率槽位 👇
        // 频率 1 框
        this.addSlot(new Slot(container, 0, 18, 32) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return false; } // 禁止拖拽
        });
        // 频率 2 框
        this.addSlot(new Slot(container, 1, 48, 32) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return false; } // 禁止拖拽
        });

        // 👇 对齐下方的大背包框 👇
        // 玩家背包 (起始 X=8, Y=110)
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 110 + i * 18));
            }
        }
        // 快捷栏 (起始 Y=168)
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 168));
        }
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

        if (!this.container.getItem(0).isEmpty()) {
            tag.put("freq1", this.container.getItem(0).save(player.registryAccess()));
        } else {
            tag.remove("freq1");
        }
        if (!this.container.getItem(1).isEmpty()) {
            tag.put("freq2", this.container.getItem(1).save(player.registryAccess()));
        } else {
            tag.remove("freq2");
        }
        terminalStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; 
    }
}