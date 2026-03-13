package com.seppu.createadvancedschedules.ponder;

import com.seppu.createadvancedschedules.registry.ModItems;

import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class ModPonderScenes {
	public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		PonderSceneRegistrationHelper<net.minecraft.world.item.Item> HELPER =
			helper.withKeyFunction(ForgeRegistries.ITEMS::getKey);

		HELPER.addStoryBoard(
			ModItems.ADVANCED_SCHEDULE.get(),
			"advanced_schedule",
			AdvancedScheduleScenes::intro
		);
		HELPER.addStoryBoard(
			ModItems.EMERGENCY_STOP_BUTTON.get(),
			"emergency_stop_button",
			EmergencyStopScenes::intro
		);
	}
}
