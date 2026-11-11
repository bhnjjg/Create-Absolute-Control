package com.bhnjjg.absolutecontrol;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class WrenchItem extends Item {
    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        var player = context.getPlayer();

        if (player != null && player.isShiftKeyDown() && !level.isClientSide) {
            // 这里我们暂时用原版的“石头按钮”作为咱们的贴片占位符
            // 以后咱们可以自己注册一个透明的方块
            if (level.getBlockState(pos).is(Blocks.STONE_BUTTON)) {
                level.removeBlock(pos, false); // 拆掉贴片
                player.sendSystemMessage(Component.literal("§a[绝对控制] 已拆除终端，并返还！"));
                // 返还终端物品（这里用指令或直接给物品的逻辑）
                player.getInventory().add(new net.minecraft.world.item.ItemStack(AbsoluteControl.WIRELESS_TERMINAL.get()));
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}