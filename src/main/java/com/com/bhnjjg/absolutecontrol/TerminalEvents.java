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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneLampBlock;
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
        // 极简测试：不检查频率，只要手里拿遥控器并且按 Shift 就触发
        if (event.getServer().getTickCount() % 5 != 0) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            TerminalData data = TerminalData.get(level);
            if (data.boundTerminals.isEmpty()) continue;

            for (ServerPlayer player : level.players()) {
                ItemStack mainHand = player.getMainHandItem();
                
                // 判断手里是不是遥控器
                if (BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString().equals("create:linked_controller")) {
                    
                    // 👇 暴力测试：按 Shift 直接触发，不再管频率 👇
                    if (player.isShiftKeyDown()) {
                        for (Map.Entry<BlockPos, CompoundTag> entry : data.boundTerminals.entrySet()) {
                            BlockPos targetPos = entry.getKey();
                            CompoundTag config = entry.getValue();

                            // 距离检测
                            int range = config.contains("range") ? config.getInt("range") : 2;
                            if (player.blockPosition().distSqr(targetPos) > (range * range)) continue;

                            // 👇 核心：强制红石激活 👇
                            BlockState state = level.getBlockState(targetPos);
                            
                            // 如果是红石灯，直接给它点亮！
                            if (state.is(Blocks.REDSTONE_LAMP)) {
                                level.setBlockAndUpdate(targetPos, state.setValue(RedStoneLampBlock.LIT, true));
                            }
                            
                            // 不管什么方块，都给一次邻居更新
                            level.updateNeighborsAt(targetPos, Blocks.REDSTONE_BLOCK);
                            
                            player.sendSystemMessage(Component.literal("§b[绝对控制] 信号已触发！"));
                        }
                    }
                }
            }
        }
    }
}