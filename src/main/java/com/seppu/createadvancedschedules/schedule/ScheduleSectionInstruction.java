package com.seppu.createadvancedschedules.schedule;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;
import com.simibubi.create.content.trains.graph.DiscoveredPath;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime;
import com.simibubi.create.content.trains.schedule.ScheduleRuntime.State;
import com.simibubi.create.content.trains.schedule.destination.TextScheduleInstruction;

import net.createmod.catnip.data.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ScheduleSectionInstruction extends TextScheduleInstruction {
	public ScheduleSectionInstruction() {
		super();
	}

	public ScheduleSectionInstruction(String name) {
		super();
		data.putString("Text", name);
	}

	@Override
	public Pair<ItemStack, Component> getSummary() {
		return Pair.of(icon(), Component.literal(getLabelText()));
	}

	@Override
	public boolean supportsConditions() {
		return false;
	}

	@Override
	public ResourceLocation getId() {
		return ResourceLocation.fromNamespaceAndPath(CreateAdvancedSchedulesMod.MODID, "section");
	}

	@Override
	public ItemStack getSecondLineIcon() {
		return icon();
	}

	public String getSectionName() {
		return getLabelText();
	}

	@Override
	public List<Component> getTitleAs(String type) {
		return ImmutableList.of(
			Component.translatable("createadvancedschedules.schedule.instruction.section.title")
				.withStyle(ChatFormatting.GOLD),
			Component.translatable("generic.in_quotes", Component.literal(getLabelText()))
		);
	}

	private ItemStack icon() {
		return new ItemStack(Items.OAK_SIGN);
	}

	@Override
	@Nullable
	public DiscoveredPath start(ScheduleRuntime runtime, Level level) {
		runtime.state = State.PRE_TRANSIT;
		runtime.currentEntry++;
		return null;
	}
}
