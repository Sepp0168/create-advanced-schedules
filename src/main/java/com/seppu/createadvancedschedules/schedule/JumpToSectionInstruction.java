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

public class JumpToSectionInstruction extends ScheduleInstruction {
	private static final String TAG_SECTION = "Section";

	public JumpToSectionInstruction() {
		super();
	}

	public JumpToSectionInstruction(String section) {
		super();
		data.putString(TAG_SECTION, section);
	}

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(icon(), Component.literal("Jump to " + getSection()));
	}

	@Override
	public boolean supportsConditions() {
		return false;
	}

	@Override
	public ResourceLocation getId() {
		return ResourceLocation.fromNamespaceAndPath(CreateAdvancedSchedulesMod.MODID, "jump_section");
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return icon();
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			Component.translatable("createadvancedschedules.schedule.instruction.jump_section.title")
				.withStyle(ChatFormatting.GOLD),
			Component.translatable("generic.in_quotes", Component.literal(getSection()))
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
		int target = findSectionTargetIndex(schedule, getSection());
		if (target == -1)
			target = resolveNextIndex(runtime.currentEntry, schedule.entries.size(), schedule.cyclic);
		runtime.currentEntry = target;
		runtime.state = State.PRE_TRANSIT;
		return null;
	}

	public String getSection() {
		return data.getString(TAG_SECTION);
	}

	private ItemStack icon() {
		return new ItemStack(Items.ARROW);
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
