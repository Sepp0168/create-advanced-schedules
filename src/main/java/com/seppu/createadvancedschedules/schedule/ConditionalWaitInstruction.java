package com.seppu.createadvancedschedules.schedule;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.content.trains.entity.Train;
import com.simibubi.create.content.trains.graph.DiscoveredPath;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleEntry;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime.State;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;
import com.simibubi.create.foundation.gui.ModularGuiLine;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ConditionalWaitInstruction extends ScheduleInstruction {

	private Couple<Frequency> freq = Couple.create(() -> Frequency.EMPTY);

	public ConditionalWaitInstruction() {
		data.putInt("ConditionType", ConditionalScheduleCondition.ConditionType.TIME.ordinal());
		data.putInt("Operator", ConditionalScheduleCondition.Operator.GREATER.ordinal());
		data.putInt("Value", 18000);
		data.putInt("WeatherState", ConditionalScheduleCondition.WeatherState.RAIN.ordinal());
		data.putInt("TrueJump", 1);
		data.putInt("FalseJump", 1);
		data.putString("TrueSection", "");
		data.putString("FalseSection", "");
		data.putInt("RedstoneInverted", 0);
	}

	@Override
	public boolean supportsConditions() {
		return false;
	}

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(getIcon(), Component.translatable(
			"createadvancedschedules.schedule.instruction.conditional.summary",
			getConditionSummary(), Component.literal(String.valueOf(getTrueJump())),
			Component.literal(String.valueOf(getFalseJump()))
		));
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			Component.translatable("createadvancedschedules.schedule.instruction.conditional.title", getConditionSummary()),
			Component.translatable("createadvancedschedules.schedule.instruction.conditional.jumps",
				Component.literal(String.valueOf(getTrueJump())),
				Component.literal(String.valueOf(getFalseJump())))
		);
	}

	@Override
	public ResourceLocation getId() {
		return ResourceLocation.fromNamespaceAndPath(CreateAdvancedSchedulesMod.MODID, "conditional");
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return getIcon();
	}

	@Override
	public int slotsTargeted() {
		return getConditionType() == ConditionalScheduleCondition.ConditionType.REDSTONE ? 2 : 0;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		if (getConditionType() == ConditionalScheduleCondition.ConditionType.REDSTONE) {
			freq.set(slot == 0, Frequency.of(stack));
		}
	}

	@Override
	public ItemStack getItem(int slot) {
		if (getConditionType() == ConditionalScheduleCondition.ConditionType.REDSTONE) {
			return freq.get(slot == 0)
				.getStack();
		}
		return ItemStack.EMPTY;
	}

	@Override
	protected void writeAdditional(net.minecraft.nbt.CompoundTag tag) {
		tag.put("Frequency", freq.serializeEach(f -> f.getStack()
			.serializeNBT()));
	}

	@Override
	protected void readAdditional(net.minecraft.nbt.CompoundTag tag) {
		if (tag.contains("Frequency")) {
			freq = Couple.deserializeEach(tag.getList("Frequency", net.minecraft.nbt.Tag.TAG_COMPOUND),
				c -> Frequency.of(ItemStack.of(c)));
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initConfigurationWidgets(ModularGuiLineBuilder builder) {
		try {
			int baseX = createadvancedschedules$getBuilderInt(builder, "x");
			int baseY = createadvancedschedules$getBuilderInt(builder, "y");
			ModularGuiLine target = createadvancedschedules$getBuilderTarget(builder);
			if (target == null)
				return;

			IconButton config = new IconButton(baseX + 122, baseY - 4, AllIcons.I_CONFIG_OPEN);
			config.setToolTip(Component.translatable("createadvancedschedules.ui.configure"));
			config.withCallback(() -> {
				net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
				mc.setScreen(new com.seppu.createadvancedschedules.client.ConditionalBranchScreen(mc.screen, this));
			});
			target.add(net.createmod.catnip.data.Pair.of(config, "Config"));
		} catch (ReflectiveOperationException ignored) {
		}
	}

	@OnlyIn(Dist.CLIENT)
	private static int createadvancedschedules$getBuilderInt(ModularGuiLineBuilder builder, String name)
		throws ReflectiveOperationException {
		java.lang.reflect.Field field = ModularGuiLineBuilder.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.getInt(builder);
	}

	@OnlyIn(Dist.CLIENT)
	private static ModularGuiLine createadvancedschedules$getBuilderTarget(ModularGuiLineBuilder builder)
		throws ReflectiveOperationException {
		java.lang.reflect.Field field = ModularGuiLineBuilder.class.getDeclaredField("target");
		field.setAccessible(true);
		return (ModularGuiLine) field.get(builder);
	}


	@Override
	@Nullable
	public DiscoveredPath start(ScheduleRuntime runtime, Level level) {
		Schedule schedule = runtime.schedule;
		if (schedule == null || schedule.entries.isEmpty()) {
			runtime.currentEntry++;
			return null;
		}

		// Integrates with Create's schedule runtime by redirecting the currentEntry
		// before any navigation is started.
		boolean passed = evaluate(level, runtime.train);
		String section = passed ? getTrueSection() : getFalseSection();
		int nextIndex = -1;

		if (!section.isBlank())
			nextIndex = findSectionTargetIndex(schedule, section);

		if (nextIndex == -1) {
			int jump = passed ? getTrueJump() : getFalseJump();
			nextIndex = resolveTargetIndex(runtime.currentEntry, jump, schedule.entries.size(), schedule.cyclic);
		}

		// If the jump would stall on this entry, move forward once to avoid infinite loops.
		if (nextIndex == runtime.currentEntry)
			nextIndex = runtime.currentEntry + 1;

		runtime.currentEntry = nextIndex;
		runtime.state = State.PRE_TRANSIT;
		return null;
	}

	private boolean evaluate(Level level, Train train) {
		if (getConditionType() == ConditionalScheduleCondition.ConditionType.WEATHER) {
			return ConditionalScheduleCondition.evaluateWeather(getWeatherState(), level);
		}
		return ConditionalScheduleCondition.evaluate(
			getConditionType(),
			getOperator(),
			getValue(),
			isRedstoneInverted(),
			freq,
			level,
			train
		);
	}

	private int resolveTargetIndex(int currentIndex, int jump, int size, boolean cyclic) {
		if (size <= 0)
			return currentIndex;

		int target = currentIndex + jump;
		if (cyclic) {
			target = Mth.positiveModulo(target, size);
		} else if (target < 0) {
			target = 0;
		}
		return target;
	}

	public ConditionalScheduleCondition.ConditionType getConditionType() {
		return enumData("ConditionType", ConditionalScheduleCondition.ConditionType.class);
	}

	public ConditionalScheduleCondition.Operator getOperator() {
		return enumData("Operator", ConditionalScheduleCondition.Operator.class);
	}

	public int getValue() {
		return data.contains("Value") ? data.getInt("Value") : 0;
	}

	public ConditionalScheduleCondition.WeatherState getWeatherState() {
		return enumData("WeatherState", ConditionalScheduleCondition.WeatherState.class);
	}

	public int getTrueJump() {
		return Math.max(1, data.getInt("TrueJump"));
	}

	public int getFalseJump() {
		return Math.max(1, data.getInt("FalseJump"));
	}

	private String getTrueSection() {
		return data.getString("TrueSection");
	}

	private String getFalseSection() {
		return data.getString("FalseSection");
	}

	public boolean isRedstoneInverted() {
		return data.getInt("RedstoneInverted") == 1;
	}

	public void setConditionType(ConditionalScheduleCondition.ConditionType type) {
		data.putInt("ConditionType", type.ordinal());
	}

	public void setOperator(ConditionalScheduleCondition.Operator operator) {
		data.putInt("Operator", operator.ordinal());
	}

	public void setValue(int value) {
		data.putInt("Value", value);
	}

	public void setWeatherState(ConditionalScheduleCondition.WeatherState state) {
		data.putInt("WeatherState", state.ordinal());
	}

	public void setRedstoneInverted(boolean inverted) {
		data.putInt("RedstoneInverted", inverted ? 1 : 0);
	}

	public void setTrueJump(int value) {
		data.putInt("TrueJump", Math.max(1, value));
	}

	public void setFalseJump(int value) {
		data.putInt("FalseJump", Math.max(1, value));
	}

	private Component getConditionSummary() {
		if (getConditionType() == ConditionalScheduleCondition.ConditionType.WEATHER) {
			return Component.translatable("createadvancedschedules.schedule.weather.state",
				Component.translatable("createadvancedschedules.schedule.weather." + getWeatherState()
					.name()
					.toLowerCase()));
		}
		return ConditionalScheduleCondition.describe(getConditionType(), getOperator(), getValue(), isRedstoneInverted());
	}

	private ItemStack getIcon() {
		return ConditionalScheduleCondition.iconFor(getConditionType());
	}

	private int findSectionTargetIndex(Schedule schedule, String sectionName) {
		for (int i = 0; i < schedule.entries.size(); i++) {
			ScheduleEntry entry = schedule.entries.get(i);
			if (!(entry.instruction instanceof ScheduleSectionInstruction section))
				continue;
			if (!sectionName.equalsIgnoreCase(section.getSectionName()))
				continue;
			return i + 1;
		}
		return -1;
	}
}
