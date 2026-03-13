package com.seppu.createadvancedschedules.network;

import java.util.Optional;

import com.seppu.createadvancedschedules.train.EmergencyStopManager;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import java.util.function.Supplier;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class EmergencyStopTogglePacket {

	public static EmergencyStopTogglePacket decode(FriendlyByteBuf buf) {
		return new EmergencyStopTogglePacket();
	}

	public static void encode(EmergencyStopTogglePacket packet, FriendlyByteBuf buf) {
	}

	public static void handle(EmergencyStopTogglePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
		NetworkEvent.Context context = contextSupplier.get();
		context.setPacketHandled(true);
		ServerPlayer player = context.getSender();
		if (player == null)
			return;

		Entity root = player.getRootVehicle();
		if (!(root instanceof CarriageContraptionEntity cce))
			return;

		Carriage carriage = cce.getCarriage();
		if (carriage == null)
			return;

		Train train = carriage.train;
		boolean active = EmergencyStopManager.toggle(train);
		ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new EmergencyStopStatusPacket(train.id, active));
	}
}
