package com.seppu.createadvancedschedules.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientEmergencyStopState {
	private static final Map<UUID, Boolean> ACTIVE = new HashMap<>();

	private ClientEmergencyStopState() {
	}

	public static void set(UUID id, boolean active) {
		ACTIVE.put(id, active);
	}

	public static boolean isActive(UUID id) {
		return ACTIVE.getOrDefault(id, false);
	}
}
