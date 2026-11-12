package com.bhnjjg.absolutecontrol.mixin;

import com.bhnjjg.absolutecontrol.TerminalData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.SignalGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignalGetter.class)
public interface SignalGetterMixin {
    @Inject(method = "getBestNeighborSignal", at = @At("HEAD"), cancellable = true)
    default void absolute_control$getBestNeighborSignal(BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        if ((Object)this instanceof ServerLevel level) {
            TerminalData data = TerminalData.get(level);
            if (TerminalData.isSignalActive && data.boundTerminals.containsKey(pos)) {
                cir.setReturnValue(15);
            }
        }
    }
}