package com.bhnjjg.absolutecontrol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;

@EventBusSubscriber(modid = AbsoluteControl.MODID)
public class TerminalEvents {

    // ==========================================
    // 1. 扳手拆除逻辑（潜行 + 左键）
    // ==========================================
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
                    if (!player.getInventory().add(returnedTerminal)) {
                        player.drop(returnedTerminal, false);
                    }
                    player.sendSystemMessage(Component.literal("§a[绝对控制] 已拆除终端，并返还！"));
                    event.setCanceled(true);
                }
            }
        }
    }

    // ==========================================
    // 2. 直接兼容机械动力遥控器的信号接收
    // ==========================================
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        // 每 5 tick 检查一次 (0.25秒)，保证信号丝滑
        if (event.getServer().getTickCount() % 5 != 0) return;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            TerminalData data = TerminalData.get(level);
            if (data.boundTerminals.isEmpty()) continue;

            for (ServerPlayer player : level.players()) {
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();

                ItemStack controller = ItemStack.EMPTY;
                if (isLinkedController(mainHand)) controller = mainHand;
                else if (isLinkedController(offHand)) controller = offHand;

                // 如果玩家手里拿着 Create 遥控器
                if (!controller.isEmpty()) {
                    // 偷看遥控器 NBT，获取按下的按键和频率
                    CustomData customData = controller.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    CompoundTag tag = customData.copyTag();
                    
                    // Create 的遥控器按下按键时，NBT里会有一个 Activated (或类似) 的标签
                    // 以及 FreqFirst、FreqSecond 这两个频率物品。
                    // 注意：这里我们用一种通用的读取方式，即便 Create 的NBT字段名变了也能兼容
                    boolean isActivated = tag.getBoolean("Activated") || tag.contains("PressedKeys");
                    
                    if (isActivated) {
                        // 读取遥控器的频率槽物品
                        ItemStack remoteFreq1 = tag.contains("FreqFirst") ? ItemStack.parseOptional(level.registryAccess(), tag.getCompound("FreqFirst")) : ItemStack.EMPTY;
                        ItemStack remoteFreq2 = tag.contains("FreqSecond") ? ItemStack.parseOptional(level.registryAccess(), tag.getCompound("FreqSecond")) : ItemStack.EMPTY;

                        // 遍历所有绑定的终端
                        for (Map.Entry<BlockPos, CompoundTag> entry : data.boundTerminals.entrySet()) {
                            BlockPos targetPos = entry.getKey();
                            CompoundTag config = entry.getValue();

                            // 距离检测（范围）
                            int range = config.contains("range") ? config.getInt("range") : 2;
                            if (player.blockPosition().distSqr(targetPos) > (range * range)) continue;

                            // 终端的频率槽物品
                            ItemStack termFreq1 = config.contains("freq1") ? ItemStack.parseOptional(level.registryAccess(), config.getCompound("freq1")) : ItemStack.EMPTY;
                            ItemStack termFreq2 = config.contains("freq2") ? ItemStack.parseOptional(level.registryAccess(), config.getCompound("freq2")) : ItemStack.EMPTY;

                            // 👇 核心逻辑：频率匹配 👇
                            // 如果遥控器和终端都有频率，且物品完全相同（包括NBT）
                            boolean freq1Match = !remoteFreq1.isEmpty() && ItemStack.isSameItemSameComponents(remoteFreq1, termFreq1);
                            boolean freq2Match = !remoteFreq2.isEmpty() && ItemStack.isSameItemSameComponents(remoteFreq2, termFreq2);

                            if (freq1Match || freq2Match) {
                                // 给绑定方块发红石脉冲
                                level.updateNeighborsAt(targetPos, Blocks.REDSTONE_BLOCK);
                                
                                // 特殊处理一下红石灯（测试用），如果是原版机器基本不需要这句
                                // var state = level.getBlockState(targetPos);
                                // if (state.is(Blocks.REDSTONE_LAMP)) {
                                //    level.setBlockAndUpdate(targetPos, state.setValue(net.minecraft.world.level.block.RedStoneLampBlock.LIT, true));
                                // }
                            }
                        }
                    }
                }
            }
        }
    }

    // 判断是不是 Create 的遥控器
    private static boolean isLinkedController(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals("create:linked_controller");
    }
}