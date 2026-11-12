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
        if (event.getServer().getTickCount() % 10 != 0) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            TerminalData data = TerminalData.get(level);
            if (data.boundTerminals.isEmpty()) continue;

            for (ServerPlayer player : level.players()) {
                ItemStack mainHand = player.getMainHandItem();
                
                // 测试条件：拿着遥控器按 Shift
                if (BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString().equals("create:linked_controller") && player.isShiftKeyDown()) {
                    
                    for (Map.Entry<BlockPos, CompoundTag> entry : data.boundTerminals.entrySet()) {
                        BlockPos targetPos = entry.getKey();
                        CompoundTag config = entry.getValue();

                        int range = config.contains("range") ? config.getInt("range") : 2;
                        if (player.blockPosition().distSqr(targetPos) > (range * range)) continue;

                        BlockState state = level.getBlockState(targetPos);
                        
                        // 👇 终极物理骗术 👇
                        // 1. 如果是红石灯，直接点燃
                        if (state.is(Blocks.REDSTONE_LAMP)) {
                            level.setBlockAndUpdate(targetPos, state.setValue(RedStoneLampBlock.LIT, true));
                        }

                        // 2. 强制更新周围所有的邻居（骗它外面通电了）
                        for (Direction dir : Direction.values()) {
                            level.updateNeighborsAt(targetPos.relative(dir), Blocks.REDSTONE_BLOCK);
                        }
                        
                        // 3. 核心神技：向机器强行注入红石更新事件，让 Create 机器以为自己接到了红石信号！
                        state.neighborChanged(level, targetPos, Blocks.REDSTONE_BLOCK, targetPos, false);
                        
                        // 4. 针对机械动力，很多机器是看“方块更新”和“状态”的，我们甚至可以暴力刷新它
                        level.blockUpdated(targetPos, state.getBlock());

                        player.sendSystemMessage(Component.literal("§b[绝对控制] 已向机器内部注射红石信号！"));
                    }
                }
            }
        }
    }
}