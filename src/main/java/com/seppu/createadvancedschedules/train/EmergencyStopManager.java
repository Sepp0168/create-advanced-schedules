package com.seppu.createadvancedschedules.train;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.trains.entity.Train;

import net.minecraft.world.level.Level;

public class EmergencyStopManager {
	private static final Map<UUID, Boolean> ACTIVE = new HashMap<>();
	private static final Map<UUID, Boolean> PREV_PAUSED = new HashMap<>();

	private EmergencyStopManager() {
	}

	public static boolean isActive(Train train) {
		return ACTIVE.getOrDefault(train.id, false);
	}

	public static void setActive(Train train, boolean active) {
		ACTIVE.put(train.id, active);
	}

	public static boolean toggle(Train train) {
		boolean next = !isActive(train);
		setActive(train, next);
		return next;
	}

	public static void tick(Level level) {
		if (level.isClientSide())
			return;
		if (level.dimension() != Level.OVERWORLD)
			return;

		for (Train train : Create.RAILWAYS.trains.values()) {
			boolean active = isActive(train);
			if (!active) {
				Boolean prev = PREV_PAUSED.remove(train.id);
				if (prev != null && !prev) {
					train.runtime.paused = false;
				}
				continue;
			}

			if (!PREV_PAUSED.containsKey(train.id))
				PREV_PAUSED.put(train.id, train.runtime.paused);

			train.runtime.paused = true;
			train.navigation.cancelNavigation();
			train.speed = 0;
			train.targetSpeed = 0;
		}

		Iterator<UUID> iterator = ACTIVE.keySet().iterator();
		while (iterator.hasNext()) {
			UUID id = iterator.next();
			if (!Create.RAILWAYS.trains.containsKey(id)) {
				iterator.remove();
				PREV_PAUSED.remove(id);
			}
		}
	}
}
