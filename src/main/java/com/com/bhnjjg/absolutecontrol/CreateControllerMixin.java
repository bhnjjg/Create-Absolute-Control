package com.bhnjjg.absolutecontrol.mixin;

import com.bhnjjg.absolutecontrol.TerminalData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 直接注入 Create 的遥控器服务端处理器
@Mixin(targets = "com.simibubi.create.content.equipment.linkedcontroller.LinkedControllerServerHandler")
public class CreateControllerMixin {

    // 拦截 Create 处理按键包的方法
    @Inject(method = "receivePressed", at = @At("HEAD"), remap = false)
    private static void absolute_control$onReceivePressed(ServerPlayer player, int[] pressedKeys, CallbackInfo ci) {
        // 只要收到遥控器按键包，就遍历世界激活咱们绑定的终端
        ServerLevel level = (ServerLevel) player.level();
        TerminalData data = TerminalData.get(level);

        for (BlockPos targetPos : data.boundTerminals.keySet()) {
            // 触发方块更新，唤醒机器。LevelMixin 会强制告诉它“你通电了”
            level.updateNeighborsAt(targetPos, Blocks.REDSTONE_BLOCK);
        }
    }
}