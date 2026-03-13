package com.seppu.createadvancedschedules.ponder;

import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.api.registration.SharedTextRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CreateAdvancedSchedulesPonderPlugin implements PonderPlugin {

	@Override
	public String getModId() {
		return CreateAdvancedSchedulesMod.MODID;
	}

	@Override
	public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		ModPonderScenes.register(helper);
	}

	@Override
	public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
	}

	@Override
	public void registerSharedText(SharedTextRegistrationHelper helper) {
	}
}
