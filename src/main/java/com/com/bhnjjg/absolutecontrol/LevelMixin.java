package com.bhnjjg.absolutecontrol.mixin;

import com.bhnjjg.absolutecontrol.TerminalData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public class LevelMixin {

    // 👇 拦截原版获取最佳红石信号的方法 👇
    @Inject(method = "getBestNeighborSignal", at = @At("HEAD"), cancellable = true)
    private void absolute_control$getBestNeighborSignal(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        Level level = (Level) (Object) this;
        if (level instanceof ServerLevel serverLevel) {
            TerminalData data = TerminalData.get(serverLevel);
            // 如果全局信号激活，且这个坐标正是咱们绑定的终端，直接强行返回 15 级红石！
            if (TerminalData.isSignalActive && data.boundTerminals.containsKey(pos)) {
                cir.setReturnValue(15);
            }
        }
    }
}