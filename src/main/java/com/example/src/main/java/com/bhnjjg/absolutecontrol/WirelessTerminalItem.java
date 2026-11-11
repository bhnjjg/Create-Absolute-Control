package com.bhnjjg.absolutecontrol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.nbt.CompoundTag;

public class WirelessTerminalItem extends Item {

    public WirelessTerminalItem(Properties properties) {
        super(properties);
    }

    // 右键空气，打开配置面板
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            // 直接打开面板，不用非得绑定方块了，因为是要先配置
            player.openMenu(new SimpleMenuProvider(
                    (containerId, playerInventory, p) -> new TerminalMenu(containerId, playerInventory, itemstack),
                    Component.literal("无线红石信号终端")
            ));
        }
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }

    // 潜行+右键方块，把它贴上去
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();

        if (player == null) return InteractionResult.PASS;

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                // 把贴片放上去（这里先临时用原版按钮代替，为了测试）
                BlockPos placePos = pos.relative(face);
                level.setBlockAndUpdate(placePos, Blocks.STONE_BUTTON.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.ButtonBlock.FACING, face)
                        .setValue(net.minecraft.world.level.block.ButtonBlock.FACE, 
                                face == Direction.UP ? net.minecraft.world.level.block.state.properties.AttachFace.FLOOR : 
                                face == Direction.DOWN ? net.minecraft.world.level.block.state.properties.AttachFace.CEILING : 
                                net.minecraft.world.level.block.state.properties.AttachFace.WALL));
                
                // 消耗终端物品
                itemStack.shrink(1);
                player.sendSystemMessage(Component.literal("§b[绝对控制] 终端已安装！用扳手可以拆除。"));
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}