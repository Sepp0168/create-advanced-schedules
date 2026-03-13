package com.seppu.createadvancedschedules.schedule;

import java.util.List;

import com.seppu.createadvancedschedules.registry.ModMenuTypes;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.trains.entity.CarriageContraption;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.content.trains.schedule.Schedule;

import net.createmod.catnip.data.Couple;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;

public class AdvancedScheduleItem extends Item implements MenuProvider, SupportsItemCopying {

	public AdvancedScheduleItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		if (context.getPlayer() == null)
			return InteractionResult.PASS;
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (!player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
			if (!world.isClientSide && player instanceof ServerPlayer)
				NetworkHooks.openScreen((ServerPlayer) player, this, buf -> buf.writeItem(heldItem));
			return InteractionResultHolder.success(heldItem);
		}
		return InteractionResultHolder.pass(heldItem);
	}

	public InteractionResult handScheduleTo(ItemStack stack, Player player, LivingEntity target,
											InteractionHand usedHand) {
		InteractionResult pass = InteractionResult.PASS;

		AdvancedScheduleData data = getAdvancedSchedule(stack);
		if (data == null)
			return pass;
		if (target == null)
			return pass;
		Entity rootVehicle = target.getRootVehicle();
		if (!(rootVehicle instanceof CarriageContraptionEntity entity))
			return pass;
		if (player.level().isClientSide)
			return InteractionResult.SUCCESS;

		Contraption contraption = entity.getContraption();
		if (contraption instanceof CarriageContraption cc) {
			Train train = entity.getCarriage().train;
			if (train == null)
				return InteractionResult.SUCCESS;

			Integer seatIndex = contraption.getSeatMapping().get(target.getUUID());
			if (seatIndex == null)
				return InteractionResult.SUCCESS;
			BlockPos seatPos = contraption.getSeats().get(seatIndex);
			Couple<Boolean> directions = cc.conductorSeats.get(seatPos);
			if (directions == null) {
				player.displayClientMessage(CreateLang.translateDirect("schedule.non_controlling_seat"), true);
				AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
				return InteractionResult.SUCCESS;
			}

			if (train.runtime.getSchedule() != null) {
				AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
				player.displayClientMessage(CreateLang.translateDirect("schedule.remove_with_empty_hand"), true);
				return InteractionResult.SUCCESS;
			}

			Schedule compiled = data.compileToSchedule();
			if (compiled.entries.isEmpty()) {
				AllSoundEvents.DENY.playOnServer(player.level(), player.blockPosition(), 1, 1);
				player.displayClientMessage(
					Component.translatable("createadvancedschedules.advanced_schedule.no_windows"),
					true
				);
				return InteractionResult.SUCCESS;
			}

			train.runtime.setSchedule(compiled, false);
			AllAdvancements.CONDUCTOR.awardTo(player);
			AllSoundEvents.CONFIRM.playOnServer(player.level(), player.blockPosition(), 1, 1);
			player.displayClientMessage(Component.translatable("createadvancedschedules.advanced_schedule.applied")
				.withStyle(ChatFormatting.GREEN), true);
			stack.shrink(1);
			player.setItemInHand(usedHand, stack.isEmpty() ? ItemStack.EMPTY : stack);
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
		AdvancedScheduleData data = getAdvancedSchedule(stack);
		if (data == null || data.windows.isEmpty())
			return;
		MutableComponent caret = Component.literal("> ").withStyle(ChatFormatting.GRAY);
		for (AdvancedScheduleData.Window window : data.windows) {
			tooltip.add(caret.copy().append(Component.literal(window.start + " - " + window.end)));
		}
	}

	public static AdvancedScheduleData getAdvancedSchedule(ItemStack stack) {
		if (!stack.hasTag())
			return new AdvancedScheduleData();
		return AdvancedScheduleData.fromItemTag(stack.getTag());
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		ItemStack heldItem = player.getMainHandItem();
		return new AdvancedScheduleMenu(ModMenuTypes.ADVANCED_SCHEDULE.get(), id, inv, heldItem);
	}

	@Override
	public Component getDisplayName() {
		return getDescription();
	}
}
