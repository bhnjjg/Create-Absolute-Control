package com.bhnjjg.absolutecontrol;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;

public class WirelessTerminalItem extends Item {

    public WirelessTerminalItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 只在服务端执行逻辑，防止客户端和服务端刷两次提示
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                // 玩家潜行右键（蹲下右键）
                // 这里以后是打开面板和绑定的逻辑，现在先做个测试
                player.sendSystemMessage(Component.literal("§a[绝对控制] 正在打开终端面板... (测试中)"));
            } else {
                // 玩家普通右键
                player.sendSystemMessage(Component.literal("§e[绝对控制] 请潜行（蹲下）右键来配置终端。"));
            }
        }

        // 告诉游戏这个动作被我们处理了
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}