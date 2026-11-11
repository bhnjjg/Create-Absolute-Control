package com.bhnjjg.absolutecontrol;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.network.FriendlyByteBuf;

public class TerminalMenu extends AbstractContainerMenu {
    private final Container container;

    public TerminalMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(2));
    }

    public TerminalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(2));
    }

    public TerminalMenu(int containerId, Inventory playerInventory, Container container) {
        super(AbsoluteControl.TERMINAL_MENU.get(), containerId);
        this.container = container;
        
        // 👇 根据你画的图估算的槽位坐标
        // 频率1框（偏左上）
        this.addSlot(new Slot(container, 0, 15, 25) {
            @Override public int getMaxStackSize() { return 1; }
        });
        // 频率2框（紧挨着）
        this.addSlot(new Slot(container, 1, 40, 25) {
            @Override public int getMaxStackSize() { return 1; }
        });

        // 👇 玩家背包（对齐你画的灰色大框，9列3行）
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                // 起始坐标大概在 x=8, y=110 的位置
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 110 + i * 18));
            }
        }
        // 快捷栏（对齐最下面那一排）
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 168));
        }
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}