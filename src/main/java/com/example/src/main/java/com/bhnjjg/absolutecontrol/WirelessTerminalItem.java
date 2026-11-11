package com.bhnjjg.absolutecontrol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;

public class WirelessTerminalItem extends Item {

    public WirelessTerminalItem(Properties properties) {
        super(properties);
    }

    // 右键空气，打开配置面板
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> new TerminalMenu(containerId, playerInventory, itemstack),
                    Component.literal("无线红石信号终端")
            ));
        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    // 潜行+右键方块，隐形绑定并消耗物品
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();

        if (player == null) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                // 获取世界的上帝笔记本
                TerminalData data = TerminalData.get(serverLevel);
                
                // 获取手里的配置数据
                CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();
                
                // 记录坐标和配置
                data.boundTerminals.put(pos, tag);
                data.setDirty(); // 告诉游戏保存数据
                
                // 消耗终端物品
                itemStack.shrink(1);
                player.sendSystemMessage(Component.literal("§b[绝对控制] 终端已隐形绑定至该方块！"));
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}