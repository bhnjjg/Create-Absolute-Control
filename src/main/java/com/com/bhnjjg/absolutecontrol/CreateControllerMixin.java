package com.bhnjjg.absolutecontrol.mixin;

import com.bhnjjg.absolutecontrol.TerminalData;
import com.simibubi.create.content.logistics.item.LinkedControllerServerHandler;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(LinkedControllerServerHandler.class)
public class CreateControllerMixin {

    // 精准拦截 receivePressed 方法
    @Inject(method = "receivePressed", at = @At("HEAD"), require = 0)
    private static void absolute_control$onReceivePressed(LevelAccessor world, BlockPos pos, UUID uniqueID,
                                                           List<Couple<LinkedControllerServerHandler.Frequency>> collect,
                                                           boolean pressed, CallbackInfo ci) {
        if (world instanceof ServerLevel level) {
            // 只要收到按键包，就遍历所有绑定的终端
            TerminalData data = TerminalData.get(level);
            for (BlockPos targetPos : data.boundTerminals.keySet()) {
                // 强行告诉引擎：这个方块有15级红石信号！（由 SignalGetterMixin 来响应）
                level.updateNeighborsAt(targetPos, net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK);
            }
        }
    }
}