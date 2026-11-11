package com.bhnjjg.absolutecontrol;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
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
        
        // 添加面板里的槽位：对应你画的两个小框框
        // 假设频率1的坐标是 x=20, y=20
        this.addSlot(new Slot(container, 0, 20, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true; // 允许放任何物品
            }
        });
        // 假设频率2的坐标是 x=44, y=20
        this.addSlot(new Slot(container, 1, 44, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        // 添加玩家自己的背包和快捷栏（这样你才能把物品拿进去）
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
}