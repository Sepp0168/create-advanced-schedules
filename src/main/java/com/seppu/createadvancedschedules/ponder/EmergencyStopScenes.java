package com.seppu.createadvancedschedules.ponder;

import com.seppu.createadvancedschedules.registry.ModItems;

import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.world.item.ItemStack;

public class EmergencyStopScenes {

	public static void intro(SceneBuilder scene, SceneBuildingUtil util) {
		scene.title("emergency_stop_button", "Emergency Stop");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		scene.idle(10);

		scene.overlay().showControls(util.vector().topOf(util.grid().at(2, 1, 2)), Pointing.DOWN, 40)
			.withItem(new ItemStack(ModItems.EMERGENCY_STOP_BUTTON.get()));
		scene.idle(20);

		scene.overlay().showText(80)
			.placeNearTarget()
			.pointAt(util.vector().topOf(util.grid().at(2, 1, 2)))
			.text("Use the Emergency Stop to immediately halt a train's schedule.");
		scene.idle(90);

		scene.overlay().showText(80)
			.placeNearTarget()
			.pointAt(util.vector().topOf(util.grid().at(2, 1, 2)))
			.text("Activate it again to resume operation.");
		scene.idle(90);
	}
}
