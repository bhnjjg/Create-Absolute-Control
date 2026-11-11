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

        // 👇 频率槽位（未改动） 👇
        this.addSlot(new Slot(container, 0, 17, 29) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });
        this.addSlot(new Slot(container, 1, 40, 29) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        // ==========================================================
        // 👇 纯手工死数据排布（精确对应你图中的红线坐标） 👇
        // ==========================================================
        
        // 大背包 - 第一排 (Y=98)
        this.addSlot(new Slot(playerInventory, 9, 8, 98));
        this.addSlot(new Slot(playerInventory, 10, 26, 98));
        this.addSlot(new Slot(playerInventory, 11, 44, 98));
        this.addSlot(new Slot(playerInventory, 12, 62, 98));
        this.addSlot(new Slot(playerInventory, 13, 80, 98));
        this.addSlot(new Slot(playerInventory, 14, 98, 98));
        this.addSlot(new Slot(playerInventory, 15, 116, 98));
        this.addSlot(new Slot(playerInventory, 16, 134, 98));
        this.addSlot(new Slot(playerInventory, 17, 152, 98));

        // 大背包 - 第二排 (Y=116)
        this.addSlot(new Slot(playerInventory, 18, 8, 116));
        this.addSlot(new Slot(playerInventory, 19, 26, 116));
        this.addSlot(new Slot(playerInventory, 20, 44, 116));
        this.addSlot(new Slot(playerInventory, 21, 62, 116));
        this.addSlot(new Slot(playerInventory, 22, 80, 116));
        this.addSlot(new Slot(playerInventory, 23, 98, 116));
        this.addSlot(new Slot(playerInventory, 24, 116, 116));
        this.addSlot(new Slot(playerInventory, 25, 134, 116));
        this.addSlot(new Slot(playerInventory, 26, 152, 116));

        // 大背包 - 第三排 (Y=134)
        this.addSlot(new Slot(playerInventory, 27, 8, 134));
        this.addSlot(new Slot(playerInventory, 28, 26, 134));
        this.addSlot(new Slot(playerInventory, 29, 44, 134));
        this.addSlot(new Slot(playerInventory, 30, 62, 134));
        this.addSlot(new Slot(playerInventory, 31, 80, 134));
        this.addSlot(new Slot(playerInventory, 32, 98, 134));
        this.addSlot(new Slot(playerInventory, 33, 116, 134));
        this.addSlot(new Slot(playerInventory, 34, 134, 134));
        this.addSlot(new Slot(playerInventory, 35, 152, 134));

        // 快捷栏 (Y=152) - 注意：我根据你的红线，快捷栏Y直接推导为152，如果偏了，你直接把下面9行的 152 改掉即可
        this.addSlot(new Slot(playerInventory, 0, 8, 152));
        this.addSlot(new Slot(playerInventory, 1, 26, 152));
        this.addSlot(new Slot(playerInventory, 2, 44, 152));
        this.addSlot(new Slot(playerInventory, 3, 62, 152));
        this.addSlot(new Slot(playerInventory, 4, 80, 152));
        this.addSlot(new Slot(playerInventory, 5, 98, 152));
        this.addSlot(new Slot(playerInventory, 6, 116, 152));
        this.addSlot(new Slot(playerInventory, 7, 134, 152));
        this.addSlot(new Slot(playerInventory, 8, 152, 152));
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