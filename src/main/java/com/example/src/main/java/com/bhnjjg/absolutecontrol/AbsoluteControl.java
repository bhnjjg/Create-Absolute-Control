package com.bhnjjg.absolutecontrol;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.resources.ResourceLocation;

@Mod(AbsoluteControl.MODID)
public class AbsoluteControl {
    public static final String MODID = "absolute_control";

    // 创建一个物品注册器
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    // 注册我们的“无线红石信号终端”物品
    public static final DeferredItem<Item> WIRELESS_TERMINAL = ITEMS.register("wireless_terminal", 
        () -> new WirelessTerminalItem(new Item.Properties()));

    public AbsoluteControl(IEventBus modEventBus) {
        // 把物品注册器绑定到模组总线上
        ITEMS.register(modEventBus);
    }
}