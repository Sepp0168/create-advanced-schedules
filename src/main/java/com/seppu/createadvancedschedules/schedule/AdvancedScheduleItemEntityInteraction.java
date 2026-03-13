package com.seppu.createadvancedschedules.schedule;

import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;
import com.seppu.createadvancedschedules.registry.ModItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;

import net.createmod.catnip.data.Couple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = CreateAdvancedSchedulesMod.MODID)
public class AdvancedScheduleItemEntityInteraction {

	@SubscribeEvent
	public static void interactWithConductor(EntityInteractSpecific event) {
		Entity entity = event.getTarget();
		Player player = event.getEntity();
		if (player == null || entity == null)
			return;
		if (player.isSpectator())
			return;

		if (!(entity instanceof LivingEntity living))
			return;

		Entity rootVehicle = entity.getRootVehicle();
		if (!(rootVehicle instanceof CarriageContraptionEntity cce))
			return;

		if (player.getCooldowns().isOnCooldown(ModItems.ADVANCED_SCHEDULE.get()))
			return;

		ItemStack itemStack = event.getItemStack();
		if (itemStack.getItem() instanceof AdvancedScheduleItem advanced) {
			InteractionResult result = advanced.handScheduleTo(itemStack, player, living, event.getHand());
			if (!result.consumesAction())
				return;

			player.getCooldowns().addCooldown(ModItems.ADVANCED_SCHEDULE.get(), 5);
			event.setCancellationResult(result);
			event.setCanceled(true);
			return;
		}

		if (event.getHand() == InteractionHand.OFF_HAND)
			return;

		if (!itemStack.isEmpty())
			return;

		Contraption contraption = cce.getContraption();
		if (!(contraption instanceof CarriageContraption cc))
			return;

		Train train = cce.getCarriage().train;
		if (train == null || train.runtime.getSchedule() == null)
			return;

		Integer seatIndex = contraption.getSeatMapping().get(entity.getUUID());
		if (seatIndex == null)
			return;
		BlockPos seatPos = contraption.getSeats().get(seatIndex);
		Couple<Boolean> directions = cc.conductorSeats.get(seatPos);
		if (directions == null)
			return;

		AdvancedScheduleData data = AdvancedScheduleData.fromCompiledSchedule(train.runtime.getSchedule());
		if (data == null || data.windows.isEmpty())
			return;

		if (!event.getLevel().isClientSide) {
			ItemStack advancedStack = new ItemStack(ModItems.ADVANCED_SCHEDULE.get());
			advancedStack.getOrCreateTag().put(AdvancedScheduleData.TAG_KEY, data.write());
			train.runtime.discardSchedule();
			player.getInventory().placeItemBackInInventory(advancedStack);
			AllSoundEvents.playItemPickup(player);
			player.displayClientMessage(Component.translatable("createadvancedschedules.advanced_schedule.removed"), true);
		}

		player.getCooldowns().addCooldown(ModItems.ADVANCED_SCHEDULE.get(), 5);
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}
}
