package com.seppu.createadvancedschedules.network;

import com.seppu.createadvancedschedules.registry.ModItems;
import com.seppu.createadvancedschedules.schedule.AdvancedScheduleData;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent.Context;

public class AdvancedScheduleEditPacket extends SimplePacketBase {
	private final CompoundTag data;

	public AdvancedScheduleEditPacket(CompoundTag data) {
		this.data = data;
	}

	public AdvancedScheduleEditPacket(FriendlyByteBuf buffer) {
		CompoundTag tag = buffer.readNbt();
		this.data = tag == null ? new CompoundTag() : tag;
	}

	public static AdvancedScheduleEditPacket decode(FriendlyByteBuf buffer) {
		return new AdvancedScheduleEditPacket(buffer);
	}

	public static void encode(AdvancedScheduleEditPacket packet, FriendlyByteBuf buffer) {
		packet.write(buffer);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeNbt(data);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> {
			ServerPlayer sender = context.getSender();
			if (sender == null)
				return;
			ItemStack mainHandItem = sender.getMainHandItem();
			if (!mainHandItem.is(ModItems.ADVANCED_SCHEDULE.get()))
				return;

			CompoundTag tag = mainHandItem.getOrCreateTag();
			if (data.isEmpty() || !data.contains("Windows")) {
				tag.remove(AdvancedScheduleData.TAG_KEY);
				if (tag.isEmpty())
					mainHandItem.setTag(null);
			} else {
				tag.put(AdvancedScheduleData.TAG_KEY, data);
			}

			sender.getCooldowns().addCooldown(mainHandItem.getItem(), 5);
		});
		return true;
	}
}
