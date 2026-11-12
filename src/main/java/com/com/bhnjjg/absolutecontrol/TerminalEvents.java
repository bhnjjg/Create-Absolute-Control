package com.bhnjjg.absolutecontrol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock; // 👈 修复了大小写
import net.minecraft.world.level.block.state.BlockState;
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

        for (ServerLevel level : event.getServer().getAllLevels()) {
            TerminalData data = TerminalData.get(level);
            if (data.boundTerminals.isEmpty()) continue;

            for (ServerPlayer player : level.players()) {
                ItemStack mainHand = player.getMainHandItem();
                
                if (BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString().equals("create:linked_controller") && player.isShiftKeyDown()) {
                    
                    for (Map.Entry<BlockPos, CompoundTag> entry : data.boundTerminals.entrySet()) {
                        BlockPos targetPos = entry.getKey();
                        CompoundTag config = entry.getValue();

                        int range = config.contains("range") ? config.getInt("range") : 2;
                        if (player.blockPosition().distSqr(targetPos) > (range * range)) continue;

                        BlockState state = level.getBlockState(targetPos);
                        
                        // 1. 如果是红石灯，直接点亮（修复了 RedstoneLampBlock 的大小写）
                        if (state.is(Blocks.REDSTONE_LAMP)) {
                            level.setBlockAndUpdate(targetPos, state.setValue(RedstoneLampBlock.LIT, true));
                        }

                        // 2. 给周围所有方块发送红石更新
                        for (Direction dir : Direction.values()) {
                            BlockPos neighborPos = targetPos.relative(dir);
                            BlockState neighborState = level.getBlockState(neighborPos);
                            
                            if (neighborState.is(Blocks.REDSTONE_WIRE)) {
                                level.setBlockAndUpdate(neighborPos, neighborState.setValue(net.minecraft.world.level.block.RedStoneWireBlock.POWER, 15));
                            }
                            
                            // 👇 删掉了报错的 state.neighborChanged，改用下面的强制更新 👇
                            level.updateNeighborsAt(neighborPos, Blocks.REDSTONE_BLOCK);
                        }

                        level.updateNeighborsAt(targetPos, Blocks.REDSTONE_BLOCK);
                        
                        player.sendSystemMessage(Component.literal("§b[绝对控制] 强制注入红石信号！"));
                    }
                }
            }
        }
    }
}