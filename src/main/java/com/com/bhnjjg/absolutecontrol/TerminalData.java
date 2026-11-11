package com.bhnjjg.absolutecontrol;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class TerminalData extends SavedData {
    public final Map<BlockPos, CompoundTag> boundTerminals = new HashMap<>();

    public static TerminalData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new Factory<>(TerminalData::new, TerminalData::load),
                "absolute_control_terminals"
        );
    }

    public static TerminalData load(CompoundTag tag, HolderLookup.Provider registries) {
        TerminalData data = new TerminalData();
        ListTag list = tag.getList("terminals", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            BlockPos pos = NbtUtils.readBlockPos(entry, "pos").orElse(null);
            if (pos != null) {
                data.boundTerminals.put(pos, entry.getCompound("config"));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, CompoundTag> entry : boundTerminals.entrySet()) {
            CompoundTag item = new CompoundTag();
            item.put("pos", NbtUtils.writeBlockPos(entry.getKey()));
            item.put("config", entry.getValue());
            list.add(item);
        }
        tag.put("terminals", list);
        return tag;
    }
}