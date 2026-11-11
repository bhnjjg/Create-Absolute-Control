package com.bhnjjg.absolutecontrol;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TerminalSyncPacket(int range, ItemStack freq1, ItemStack freq2) implements CustomPacketPayload {
    public static final Type<TerminalSyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AbsoluteControl.MODID, "sync"));
    public static final StreamCodec<FriendlyByteBuf, TerminalSyncPacket> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT, TerminalSyncPacket::range,
        ItemStack.OPTIONAL_STREAM_CODEC, TerminalSyncPacket::freq1,
        ItemStack.OPTIONAL_STREAM_CODEC, TerminalSyncPacket::freq2,
        TerminalSyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(TerminalSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack stack = player.getMainHandItem();
                if (stack.getItem() instanceof WirelessTerminalItem) {
                    CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                    CompoundTag tag = customData.copyTag();
                    tag.putInt("range", packet.range());
                    if (!packet.freq1().isEmpty()) tag.put("freq1", packet.freq1().save(player.registryAccess()));
                    else tag.remove("freq1");
                    if (!packet.freq2().isEmpty()) tag.put("freq2", packet.freq2().save(player.registryAccess()));
                    else tag.remove("freq2");
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                }
            }
        });
    }
}