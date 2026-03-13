package com.seppu.createadvancedschedules.world;

import com.seppu.createadvancedschedules.network.EmergencyStopStatusPacket;
import com.seppu.createadvancedschedules.network.ModNetwork;
import com.seppu.createadvancedschedules.train.EmergencyStopManager;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.PacketDistributor;

public class EmergencyStopButtonBlock extends Block {
	public EmergencyStopButtonBlock(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult hit) {
		if (level.isClientSide())
			return InteractionResult.SUCCESS;

		Train train = findPlayerTrain(player);
		if (train == null)
			return InteractionResult.PASS;

		boolean active = EmergencyStopManager.toggle(train);
		ModNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new EmergencyStopStatusPacket(train.id, active));
		return InteractionResult.SUCCESS;
	}

	private Train findPlayerTrain(Player player) {
		Entity root = player.getRootVehicle();
		if (!(root instanceof CarriageContraptionEntity cce))
			return null;
		Carriage carriage = cce.getCarriage();
		if (carriage == null)
			return null;
		return carriage.train;
	}
}
