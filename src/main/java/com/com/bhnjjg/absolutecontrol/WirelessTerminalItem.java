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
    public WirelessTerminalItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        
        if (!level.isClientSide) {
            // 👇 测试触发：拿着终端潜行+右键空气，强制触发一次红石输出 👇
            if (player.isShiftKeyDown()) {
                TerminalData.isSignalActive = true; 
                player.sendSystemMessage(Component.literal("§b[绝对控制] 测试触发：发送了一波红石信号！"));
                return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
            }

            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> new TerminalMenu(containerId, playerInventory, itemstack),
                    Component.literal("无线红石信号终端")
            ));
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
            if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
                TerminalData data = TerminalData.get(serverLevel);
                
                // 👇 修复：防止重复绑定 👇
                if (data.boundTerminals.containsKey(pos)) {
                    player.sendSystemMessage(Component.literal("§c[绝对控制] 该方块已经被绑定了终端！"));
                    return InteractionResult.FAIL;
                }

                CustomData customData = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag tag = customData.copyTag();
                
                data.boundTerminals.put(pos, tag);
                data.setDirty();
                
                itemStack.shrink(1);
                player.sendSystemMessage(Component.literal("§b[绝对控制] 终端已隐形绑定至该方块！"));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}