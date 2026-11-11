package com.bhnjjg.absolutecontrol;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(AbsoluteControl.MODID)
public class AbsoluteControl {
    public static final String MODID = "absolute_control";

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, MODID);

    public static final DeferredItem<Item> WIRELESS_TERMINAL = ITEMS.register("wireless_terminal", 
        () -> new WirelessTerminalItem(new Item.Properties()));

    public static final DeferredHolder<MenuType<?>, MenuType<TerminalMenu>> TERMINAL_MENU = MENUS.register("terminal_menu",
        () -> IMenuTypeExtension.create(TerminalMenu::new));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ABSOLUTE_CONTROL_TAB = CREATIVE_MODE_TABS.register("absolute_control_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.absolute_control"))
            .icon(() -> new ItemStack(WIRELESS_TERMINAL.get()))
            .displayItems((parameters, output) -> {
                output.accept(WIRELESS_TERMINAL.get());
            })
            .build()
    );

    public AbsoluteControl(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        MENUS.register(modEventBus);
    }
}