package com.bhnjjg.absolutecontrol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
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

    // 1. 当玩家对着空气右键时（未绑定）
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                player.sendSystemMessage(Component.literal("§a[绝对控制] 请对着需要绑定的机器方块按潜行+右键进行绑定。"));
            } else {
                player.sendSystemMessage(Component.literal("§e[绝对控制] 请潜行（蹲下）右键来配置终端。"));
            }
        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    // 2. 当玩家对着方块右键时（核心绑定逻辑）
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();

        if (player == null) return InteractionResult.PASS;

        // 如果玩家按下了潜行键（Shift）右键方块
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                // 1.21.1 新版保存数据的方式：获取 CustomData（自定义标签）
                CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();

                // 把方块的 X, Y, Z 坐标存进去
                tag.putInt("boundX", pos.getX());
                tag.putInt("boundY", pos.getY());
                tag.putInt("boundZ", pos.getZ());

                // 保存回物品里
                itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                // 给玩家发送提示，并且显示坐标
                player.sendSystemMessage(Component.literal(
                        "§b[绝对控制] 绑定成功！\n坐标: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()
                ));
            }
            return InteractionResult.SUCCESS; // 拦截动作，防止把方块放下去或者触发机器本身的功能
        }

        return InteractionResult.PASS;
    }
}