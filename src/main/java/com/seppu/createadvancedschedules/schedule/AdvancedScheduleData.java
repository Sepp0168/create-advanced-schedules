package com.seppu.createadvancedschedules.schedule;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.content.trains.schedule.ScheduleEntry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

public class AdvancedScheduleData {
	public static final String TAG_KEY = "AdvancedSchedule";
	private static final String TAG_WINDOWS = "Windows";
	private static final String TAG_START = "Start";
	private static final String TAG_END = "End";
	private static final String TAG_SCHEDULE = "Schedule";

	public static class Window {
		public int start;
		public int end;
		public Schedule schedule;

		public Window(int start, int end, Schedule schedule) {
			this.start = start;
			this.end = end;
			this.schedule = schedule;
		}
	}

	public final List<Window> windows = new ArrayList<>();

	public static AdvancedScheduleData fromItemTag(CompoundTag root) {
		AdvancedScheduleData data = new AdvancedScheduleData();
		if (root == null || !root.contains(TAG_KEY, Tag.TAG_COMPOUND))
			return data;
		CompoundTag tag = root.getCompound(TAG_KEY);
		ListTag list = tag.getList(TAG_WINDOWS, Tag.TAG_COMPOUND);
		for (Tag t : list) {
			if (!(t instanceof CompoundTag windowTag))
				continue;
			int start = clampTime(windowTag.getInt(TAG_START));
			int end = clampTime(windowTag.getInt(TAG_END));
			Schedule schedule = null;
			if (windowTag.contains(TAG_SCHEDULE, Tag.TAG_COMPOUND))
				schedule = Schedule.fromTag(windowTag.getCompound(TAG_SCHEDULE));
			data.windows.add(new Window(start, end, schedule));
		}
		return data;
	}

	public CompoundTag write() {
		CompoundTag tag = new CompoundTag();
		ListTag list = new ListTag();
		for (Window window : windows) {
			CompoundTag w = new CompoundTag();
			w.putInt(TAG_START, clampTime(window.start));
			w.putInt(TAG_END, clampTime(window.end));
			if (window.schedule != null)
				w.put(TAG_SCHEDULE, window.schedule.write());
			list.add(w);
		}
		tag.put(TAG_WINDOWS, list);
		return tag;
	}

	public Schedule compileToSchedule() {
		Schedule compiled = new Schedule();
		compiled.entries.clear();
		compiled.cyclic = true;
		compiled.savedProgress = 0;

		List<Window> valid = windows.stream()
			.filter(w -> w.schedule != null && w.schedule.entries != null && !w.schedule.entries.isEmpty())
			.toList();
		if (valid.isEmpty())
			return compiled;

		String router = "advanced_router";
		compiled.entries.add(sectionEntry(router));

		int index = 0;
		for (Window window : valid) {
			String section = "advanced_window_" + index++;
			compiled.entries.add(timeWindowEntry(window.start, window.end, section));
		}
		compiled.entries.add(jumpEntry(router));

		index = 0;
		for (Window window : valid) {
			String section = "advanced_window_" + index++;
			compiled.entries.add(sectionEntry(section));
			for (ScheduleEntry entry : window.schedule.entries)
				compiled.entries.add(entry.clone());
			compiled.entries.add(jumpEntry(router));
		}

		return compiled;
	}

	public static AdvancedScheduleData fromCompiledSchedule(Schedule schedule) {
		if (schedule == null || schedule.entries == null || schedule.entries.isEmpty())
			return null;

		Map<String, Window> windowsBySection = new LinkedHashMap<>();
		for (ScheduleEntry entry : schedule.entries) {
			if (entry.instruction instanceof TimeWindowInstruction timeWindow) {
				String section = timeWindow.getSection();
				windowsBySection.put(section, new Window(timeWindow.getStart(), timeWindow.getEnd(), null));
			}
		}

		if (windowsBySection.isEmpty())
			return null;

		for (int i = 0; i < schedule.entries.size(); i++) {
			ScheduleEntry entry = schedule.entries.get(i);
			if (!(entry.instruction instanceof ScheduleSectionInstruction section))
				continue;
			String sectionName = section.getSectionName();
			Window window = windowsBySection.get(sectionName);
			if (window == null)
				continue;

			Schedule windowSchedule = new Schedule();
			windowSchedule.entries.clear();
			windowSchedule.cyclic = true;
			windowSchedule.savedProgress = 0;

			for (int j = i + 1; j < schedule.entries.size(); j++) {
				ScheduleEntry nested = schedule.entries.get(j);
				if (nested.instruction instanceof JumpToSectionInstruction jump
					&& "advanced_router".equals(jump.getSection()))
					break;
				if (nested.instruction instanceof ScheduleSectionInstruction nestedSection
					&& windowsBySection.containsKey(nestedSection.getSectionName()))
					break;
				windowSchedule.entries.add(nested.clone());
			}
			window.schedule = windowSchedule;
		}

		AdvancedScheduleData data = new AdvancedScheduleData();
		for (Window window : windowsBySection.values())
			data.windows.add(window);
		return data;
	}

	private static ScheduleEntry sectionEntry(String name) {
		ScheduleEntry entry = new ScheduleEntry();
		entry.instruction = new ScheduleSectionInstruction(name);
		return entry;
	}

	private static ScheduleEntry timeWindowEntry(int start, int end, String targetSection) {
		ScheduleEntry entry = new ScheduleEntry();
		entry.instruction = new TimeWindowInstruction(start, end, targetSection);
		return entry;
	}

	private static ScheduleEntry jumpEntry(String targetSection) {
		ScheduleEntry entry = new ScheduleEntry();
		entry.instruction = new JumpToSectionInstruction(targetSection);
		return entry;
	}

	public static int clampTime(int time) {
		if (time < 0)
			return 0;
		if (time > 23999)
			return 23999;
		return time;
	}
}
