package com.bhnjjg.absolutecontrol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;

@EventBusSubscriber(modid = AbsoluteControl.MODID)
public class TerminalEvents {

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        var player = event.getEntity();
        var level = event.getLevel();
        var pos = event.getPos();
        ItemStack heldItem = event.getItemStack();

        if (player.isShiftKeyDown() && !level.isClientSide && level instanceof ServerLevel serverLevel) {
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(heldItem.getItem());
            if (itemId.toString().equals("create:wrench")) {
                TerminalData data = TerminalData.get(serverLevel);
                if (data.boundTerminals.containsKey(pos)) {
                    CompoundTag config = data.boundTerminals.remove(pos);
                    data.setDirty();
                    ItemStack returnedTerminal = new ItemStack(AbsoluteControl.WIRELESS_TERMINAL.get());
                    returnedTerminal.set(DataComponents.CUSTOM_DATA, CustomData.of(config));
                    if (!player.getInventory().add(returnedTerminal)) player.drop(returnedTerminal, false);
                    player.sendSystemMessage(Component.literal("§a[绝对控制] 已拆除终端，并返还！"));
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 10 != 0) return;

        boolean anySignalActive = false; // 记录当前是否有信号在发射

        for (ServerLevel level : event.getServer().getAllLevels()) {
            TerminalData data = TerminalData.get(level);
            if (data.boundTerminals.isEmpty()) continue;

            for (ServerPlayer player : level.players()) {
                ItemStack mainHand = player.getMainHandItem();
                
                // 测试用：拿着遥控器按Shift
                if (BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString().equals("create:linked_controller") && player.isShiftKeyDown()) {
                    anySignalActive = true;
                    // 单纯的方块更新，唤醒机器去问“我通电了吗”
                    for (BlockPos targetPos : data.boundTerminals.keySet()) {
                        level.updateNeighborsAt(targetPos, net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK);
                    }
                }
            }
            // 把状态存给数据字典，Mixin 注入的时候要用
            data.isSignalActive = anySignalActive;
        }
    }
}