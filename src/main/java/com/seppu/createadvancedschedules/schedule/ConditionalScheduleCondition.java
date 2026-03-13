package com.seppu.createadvancedschedules.schedule;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.Create;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.Train;

import net.createmod.catnip.data.Couple;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;

public final class ConditionalScheduleCondition {
	private ConditionalScheduleCondition() {
	}

	public enum ConditionType {
		TIME,
		REDSTONE,
		CARGO,
		WEATHER,
		INVENTORY_PERCENT,
		RUNTIME_HOURS;

		public Component getDisplayName() {
			return switch (this) {
			case TIME -> Component.translatable("createadvancedschedules.schedule.condition.time");
			case REDSTONE -> Component.translatable("createadvancedschedules.schedule.condition.redstone");
			case CARGO -> Component.translatable("createadvancedschedules.schedule.condition.cargo");
			case WEATHER -> Component.translatable("createadvancedschedules.schedule.condition.weather");
			case INVENTORY_PERCENT -> Component.translatable("createadvancedschedules.schedule.condition.inventory");
			case RUNTIME_HOURS -> Component.translatable("createadvancedschedules.schedule.condition.runtime");
			};
		}
	}

	public enum WeatherState {
		CLEAR,
		RAIN,
		THUNDER
	}

	public enum Operator {
		GREATER(">"),
		LESS("<");

		public final String symbol;

		Operator(String symbol) {
			this.symbol = symbol;
		}

		public boolean test(int left, int right) {
			return this == GREATER ? left > right : left < right;
		}
	}

	public static boolean evaluate(ConditionType type, Operator operator, int value, boolean redstoneInverted,
								   Couple<Frequency> freq, Level level, Train train) {
		switch (type) {
		case TIME:
			int dayTime = (int) (level.getDayTime() % 24000);
			return operator.test(dayTime, value);
		case REDSTONE:
			boolean powered = Create.REDSTONE_LINK_NETWORK_HANDLER.hasAnyLoadedPower(freq);
			return powered != redstoneInverted;
		case CARGO:
			int totalItems = countCargoItems(train);
			return operator.test(totalItems, value);
		case WEATHER:
			return false;
		case INVENTORY_PERCENT:
			int percent = getInventoryFillPercent(train);
			return operator.test(percent, value);
		case RUNTIME_HOURS:
			int runtimeHours = getRuntimeHours(train);
			return operator.test(runtimeHours, value);
		default:
			return false;
		}
	}

	public static boolean evaluateWeather(WeatherState state, Level level) {
		return switch (state) {
		case CLEAR -> !level.isRaining() && !level.isThundering();
		case RAIN -> level.isRaining();
		case THUNDER -> level.isThundering();
		};
	}

	public static Component describe(ConditionType type, Operator operator, int value, boolean redstoneInverted) {
		switch (type) {
		case TIME:
			return Component.literal("time " + operator.symbol + " " + value);
		case REDSTONE:
			return Component.translatable(redstoneInverted
				? "createadvancedschedules.schedule.redstone.unpowered"
				: "createadvancedschedules.schedule.redstone.powered");
		case CARGO:
			return Component.literal("cargo " + operator.symbol + " " + value);
		case WEATHER:
			return Component.translatable("createadvancedschedules.schedule.condition.weather");
		case INVENTORY_PERCENT:
			return Component.literal("inventory " + operator.symbol + " " + value + "%");
		case RUNTIME_HOURS:
			return Component.literal("runtime " + operator.symbol + " " + value + "h");
		default:
			return Component.literal("?");
		}
	}

	public static ItemStack iconFor(ConditionType type) {
		return switch (type) {
		case TIME -> new ItemStack(Items.CLOCK);
		case REDSTONE -> AllBlocks.REDSTONE_LINK.asStack();
		case CARGO -> new ItemStack(Items.CHEST);
		case WEATHER -> new ItemStack(Items.WATER_BUCKET);
		case INVENTORY_PERCENT -> new ItemStack(Items.BARREL);
		case RUNTIME_HOURS -> new ItemStack(Items.COMPASS);
		};
	}

	private static int countCargoItems(Train train) {
		int total = 0;
		for (Carriage carriage : train.carriages) {
			IItemHandlerModifiable items = carriage.storage.getAllItems();
			if (items == null)
				continue;
			for (int i = 0; i < items.getSlots(); i++) {
				ItemStack stack = items.getStackInSlot(i);
				if (!stack.isEmpty())
					total += stack.getCount();
			}
		}
		return total;
	}

	private static int getInventoryFillPercent(Train train) {
		int totalItems = 0;
		int totalCapacity = 0;
		for (Carriage carriage : train.carriages) {
			IItemHandlerModifiable items = carriage.storage.getAllItems();
			if (items == null)
				continue;
			for (int i = 0; i < items.getSlots(); i++) {
				ItemStack stack = items.getStackInSlot(i);
				if (!stack.isEmpty())
					totalItems += stack.getCount();
				totalCapacity += items.getSlotLimit(i);
			}
		}
		if (totalCapacity <= 0)
			return 0;
		return (int) Math.floor((totalItems * 100.0) / totalCapacity);
	}

	private static int getRuntimeHours(Train train) {
		return com.seppu.createadvancedschedules.train.TrainRuntimeTracker.getRuntimeHours(train);
	}
}
