package com.bhnjjg.absolutecontrol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
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

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            CustomData customData = itemstack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag tag = customData.copyTag();

            if (tag.contains("boundX")) {
                // 👇 核心：打开咱们刚刚注册好的 TerminalMenu 面板 👇
                player.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, p) -> new TerminalMenu(containerId, playerInventory),
                        Component.literal("无线红石信号终端")
                ));
            } else {
                player.sendSystemMessage(Component.literal("§e[绝对控制] 你还没有把它贴在机器上，请对着机器按 Shift+右键。"));
            }
        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();

        if (player == null) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();

                tag.putInt("boundX", pos.getX());
                tag.putInt("boundY", pos.getY());
                tag.putInt("boundZ", pos.getZ());

                itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));

                player.sendSystemMessage(Component.literal("§b[绝对控制] 终端已贴附！"));
                player.sendSystemMessage(Component.literal("§7(拿着它右键空气，可以打开配置面板)"));
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}