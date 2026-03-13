package com.seppu.createadvancedschedules.world;

import com.seppu.createadvancedschedules.network.EmergencyStopStatusPacket;
import com.seppu.createadvancedschedules.network.ModNetwork;
import com.seppu.createadvancedschedules.train.EmergencyStopManager;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

public class EmergencyStopButtonInteraction extends MovingInteractionBehaviour {
	@Override
	public boolean handlePlayerInteraction(Player player, InteractionHand hand, BlockPos localPos,
		AbstractContraptionEntity contraptionEntity) {
		if (player.level().isClientSide())
			return true;

		if (!(contraptionEntity instanceof CarriageContraptionEntity cce))
			return false;

		Carriage carriage = cce.getCarriage();
		if (carriage == null)
			return false;

		Train train = carriage.train;
		boolean active = EmergencyStopManager.toggle(train);
		ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new EmergencyStopStatusPacket(train.id, active));
		return true;
	}
}
