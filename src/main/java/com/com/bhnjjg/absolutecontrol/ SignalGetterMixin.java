package com.bhnjjg.absolutecontrol.mixin;

import com.bhnjjg.absolutecontrol.TerminalData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.SignalGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// 👇 精准注入到 SignalGetter 接口！不加 require = 0，如果找错就让它崩！ 👇
@Mixin(SignalGetter.class)
public interface SignalGetterMixin {

    @Inject(method = "getBestNeighborSignal", at = @At("HEAD"), cancellable = true)
    default void absolute_control$getBestNeighborSignal(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if ((Object)this instanceof ServerLevel level) {
            TerminalData data = TerminalData.get(level);
            
            // 只要全局信号状态是激活的，且这个坐标正是咱们绑定的终端，直接强行返回 15 级红石！
            if (TerminalData.isSignalActive && data.boundTerminals.containsKey(pos)) {
                cir.setReturnValue(15);
            }
        }
    }
}