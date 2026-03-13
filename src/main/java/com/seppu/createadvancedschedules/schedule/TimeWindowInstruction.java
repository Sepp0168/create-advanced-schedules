package com.seppu.createadvancedschedules.schedule;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;
import com.simibubi.create.content.trains.graph.DiscoveredPath;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleEntry;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime.State;
import com.simibubi.create.content.trains.schedule.destination.ScheduleInstruction;

import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class TimeWindowInstruction extends ScheduleInstruction {
	private static final String TAG_START = "Start";
	private static final String TAG_END = "End";
	private static final String TAG_SECTION = "Section";

	public TimeWindowInstruction() {
		super();
	}

	public TimeWindowInstruction(int start, int end, String section) {
		super();
		data.putInt(TAG_START, AdvancedScheduleData.clampTime(start));
		data.putInt(TAG_END, AdvancedScheduleData.clampTime(end));
		data.putString(TAG_SECTION, section);
	}

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(icon(), Component.literal("Between " + getStart() + " and " + getEnd()));
	}

	@Override
	public boolean supportsConditions() {
		return false;
	}

	@Override
	public ResourceLocation getId() {
		return ResourceLocation.fromNamespaceAndPath(CreateAdvancedSchedulesMod.MODID, "time_window");
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return icon();
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			Component.translatable("createadvancedschedules.schedule.instruction.time_window.title")
				.withStyle(ChatFormatting.GOLD),
			Component.literal(getStart() + " - " + getEnd())
		);
	}

	@Override
	@Nullable
	public DiscoveredPath start(ScheduleRuntime runtime, Level level) {
		Schedule schedule = runtime.schedule;
		if (schedule == null || schedule.entries.isEmpty()) {
			runtime.currentEntry++;
			return null;
		}

		int now = (int) (level.getDayTime() % 24000);
		boolean inRange = isInRange(now, getStart(), getEnd());
		if (inRange) {
			int target = findSectionTargetIndex(schedule, getSection());
			if (target != -1) {
				runtime.currentEntry = target;
				runtime.state = State.PRE_TRANSIT;
				return null;
			}
		}

		runtime.currentEntry = resolveNextIndex(runtime.currentEntry, schedule.entries.size(), schedule.cyclic);
		runtime.state = State.PRE_TRANSIT;
		return null;
	}

	public int getStart() {
		return data.contains(TAG_START) ? data.getInt(TAG_START) : 0;
	}

	public int getEnd() {
		return data.contains(TAG_END) ? data.getInt(TAG_END) : 0;
	}

	public String getSection() {
		return data.getString(TAG_SECTION);
	}

	private ItemStack icon() {
		return new ItemStack(Items.CLOCK);
	}

	private boolean isInRange(int now, int start, int end) {
		if (start == end)
			return true;
		if (start < end)
			return now >= start && now < end;
		return now >= start || now < end;
	}

	private int resolveNextIndex(int currentIndex, int size, boolean cyclic) {
		if (size <= 0)
			return currentIndex;
		int target = currentIndex + 1;
		if (cyclic)
			return net.minecraft.util.Mth.positiveModulo(target, size);
		return Math.min(target, size - 1);
	}

	private int findSectionTargetIndex(Schedule schedule, String sectionName) {
		for (int i = 0; i < schedule.entries.size(); i++) {
			ScheduleEntry entry = schedule.entries.get(i);
			if (!(entry.instruction instanceof ScheduleSectionInstruction section))
				continue;
			if (sectionName.equals(section.getSectionName()))
				return i;
		}
		return -1;
	}
}
