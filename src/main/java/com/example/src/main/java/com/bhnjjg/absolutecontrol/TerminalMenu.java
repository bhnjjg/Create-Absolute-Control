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

    // 服务端创建
    public TerminalMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(2)); // 2个槽位
    }

    // 客户端创建
    public TerminalMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new SimpleContainer(2));
    }

    public TerminalMenu(int containerId, Inventory playerInventory, Container container) {
        super(AbsoluteControl.TERMINAL_MENU.get(), containerId);
        this.container = container;
        
        // 频率1槽位（坐标 x=20, y=20）
        this.addSlot(new Slot(container, 0, 20, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });
        // 频率2槽位（坐标 x=44, y=20）
        this.addSlot(new Slot(container, 1, 44, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        // 添加玩家自己的背包和快捷栏
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    // 👇 这就是刚才报错缺失的方法，专门处理 Shift+点击 快速移动 👇
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            // 如果点击的是自定义槽位（0和1），移动到玩家背包
            if (index < 2) {
                if (!this.moveItemStackTo(itemstack1, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } 
            // 如果点击的是玩家背包，移动到自定义槽位
            else if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }
}