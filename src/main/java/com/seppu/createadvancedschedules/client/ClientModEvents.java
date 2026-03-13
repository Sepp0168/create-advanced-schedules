package com.seppu.createadvancedschedules.client;

import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;
import com.seppu.createadvancedschedules.client.AdvancedScheduleScreen;
import com.seppu.createadvancedschedules.registry.ModMenuTypes;
import com.seppu.createadvancedschedules.ponder.CreateAdvancedSchedulesPonderPlugin;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.KeyMapping;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = CreateAdvancedSchedulesMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
	public static final KeyMapping ESTOP_KEY = new KeyMapping(
		"key.createadvancedschedules.estop",
		GLFW.GLFW_KEY_O,
		"key.categories.createadvancedschedules"
	);

	@SubscribeEvent
	public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
		event.register(ESTOP_KEY);
	}

	@SubscribeEvent
	public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
		event.registerAboveAll("createadvancedschedules_estop", EmergencyStopOverlay.OVERLAY);
	}

	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(ModMenuTypes.ADVANCED_SCHEDULE.get(), AdvancedScheduleScreen::new);
			PonderIndex.addPlugin(new CreateAdvancedSchedulesPonderPlugin());
		});
	}
}
