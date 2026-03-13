package com.seppu.createadvancedschedules.network;

import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
	private static final String PROTOCOL = "1";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
		ResourceLocation.fromNamespaceAndPath(CreateAdvancedSchedulesMod.MODID, "main"),
		() -> PROTOCOL,
		PROTOCOL::equals,
		PROTOCOL::equals
	);

	private ModNetwork() {
	}

	public static void register() {
		int id = 0;
		CHANNEL.messageBuilder(AdvancedScheduleEditPacket.class, id++)
			.encoder(AdvancedScheduleEditPacket::encode)
			.decoder(AdvancedScheduleEditPacket::decode)
			.consumerMainThread((packet, ctx) -> packet.handle(ctx.get()))
			.add();
		CHANNEL.messageBuilder(EmergencyStopTogglePacket.class, id++)
			.encoder(EmergencyStopTogglePacket::encode)
			.decoder(EmergencyStopTogglePacket::decode)
			.consumerMainThread(EmergencyStopTogglePacket::handle)
			.add();
		CHANNEL.messageBuilder(EmergencyStopStatusPacket.class, id++)
			.encoder(EmergencyStopStatusPacket::encode)
			.decoder(EmergencyStopStatusPacket::decode)
			.consumerMainThread(EmergencyStopStatusPacket::handle)
			.add();
	}
}
