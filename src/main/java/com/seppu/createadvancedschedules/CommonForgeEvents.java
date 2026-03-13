package com.seppu.createadvancedschedules;

import com.seppu.createadvancedschedules.train.EmergencyStopManager;
import com.seppu.createadvancedschedules.train.TrainRuntimeTracker;

import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateAdvancedSchedulesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonForgeEvents {

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END)
			return;
		Level level = event.level;
		TrainRuntimeTracker.tick(level);
		EmergencyStopManager.tick(level);
	}
}
