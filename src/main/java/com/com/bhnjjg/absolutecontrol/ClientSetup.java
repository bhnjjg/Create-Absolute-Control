package com.bhnjjg.absolutecontrol;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = AbsoluteControl.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        // 把 TerminalMenu 和 TerminalScreen 绑在一起
        event.register(AbsoluteControl.TERMINAL_MENU.get(), TerminalScreen::new);
    }
}