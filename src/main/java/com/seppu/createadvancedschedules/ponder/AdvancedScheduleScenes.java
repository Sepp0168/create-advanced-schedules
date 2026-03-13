package com.seppu.createadvancedschedules.ponder;

import com.seppu.createadvancedschedules.registry.ModItems;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.world.item.ItemStack;

public class AdvancedScheduleScenes {

	public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("advanced_schedule", "Advanced Schedule");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(10);

		scene.overlay().showControls(util.vector().topOf(util.grid().at(2, 1, 2)), Pointing.DOWN, 40)
			.withItem(new ItemStack(ModItems.ADVANCED_SCHEDULE.get()));
		scene.idle(20);

		scene.overlay().showText(80)
			.placeNearTarget()
			.pointAt(util.vector().topOf(util.grid().at(2, 1, 2)))
			.text("Use an Advanced Schedule to assign different schedules per time window.");
		scene.idle(90);

		scene.overlay().showText(80)
			.placeNearTarget()
			.pointAt(util.vector().topOf(util.grid().at(2, 1, 2)))
			.text("Add periods, set start and end times, then place a Schedule item in each slot.");
		scene.idle(90);

		scene.overlay().showText(80)
			.placeNearTarget()
			.pointAt(util.vector().topOf(util.grid().at(2, 1, 2)))
			.text("Give it to a conductor to automate trains by time of day.");
		scene.idle(90);
	}
}
