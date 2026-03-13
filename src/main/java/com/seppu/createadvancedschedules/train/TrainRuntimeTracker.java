package com.seppu.createadvancedschedules.train;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;

import net.minecraft.world.level.Level;

public class TrainRuntimeTracker {
	private static final Map<UUID, Long> RUNTIME_TICKS = new HashMap<>();

	private TrainRuntimeTracker() {
	}

	public static void tick(Level level) {
		if (level.isClientSide())
			return;
		if (level.dimension() != Level.OVERWORLD)
			return;

		for (Train train : Create.RAILWAYS.trains.values()) {
			if (train.runtime == null || train.runtime.getSchedule() == null) {
				RUNTIME_TICKS.remove(train.id);
				continue;
			}
			if (train.runtime.paused || train.runtime.completed)
				continue;
			RUNTIME_TICKS.merge(train.id, 1L, Long::sum);
		}

		Iterator<UUID> iterator = RUNTIME_TICKS.keySet().iterator();
		while (iterator.hasNext()) {
			UUID id = iterator.next();
			if (!Create.RAILWAYS.trains.containsKey(id))
				iterator.remove();
		}
	}

	public static int getRuntimeHours(Train train) {
		long ticks = RUNTIME_TICKS.getOrDefault(train.id, 0L);
		return (int) (ticks / 72000L);
	}
}
