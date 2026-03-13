package com.seppu.createadvancedschedules.network;

import java.util.UUID;

import com.seppu.createadvancedschedules.client.ClientEmergencyStopState;

import net.minecraft.network.FriendlyByteBuf;
import java.util.function.Supplier;

import net.minecraftforge.network.NetworkEvent;

public class EmergencyStopStatusPacket {
	private final UUID trainId;
	private final boolean active;

	public EmergencyStopStatusPacket(UUID trainId, boolean active) {
		this.trainId = trainId;
		this.active = active;
	}

	public static EmergencyStopStatusPacket decode(FriendlyByteBuf buf) {
		return new EmergencyStopStatusPacket(buf.readUUID(), buf.readBoolean());
	}

	public static void encode(EmergencyStopStatusPacket packet, FriendlyByteBuf buf) {
		buf.writeUUID(packet.trainId);
		buf.writeBoolean(packet.active);
	}

	public static void handle(EmergencyStopStatusPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.enqueueWork(() -> ClientEmergencyStopState.set(packet.trainId, packet.active));
		context.setPacketHandled(true);
	}
}
