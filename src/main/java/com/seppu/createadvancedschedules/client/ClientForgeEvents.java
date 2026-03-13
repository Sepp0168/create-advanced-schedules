package com.seppu.createadvancedschedules.client;

import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;
import com.seppu.createadvancedschedules.network.EmergencyStopTogglePacket;
import com.seppu.createadvancedschedules.network.ModNetwork;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CreateAdvancedSchedulesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {

	@SubscribeEvent
	public static void onKeyInput(InputEvent.Key event) {
		if (ClientModEvents.ESTOP_KEY.consumeClick()) {
			if (EmergencyStopOverlay.isInTrain()) {
				ModNetwork.CHANNEL.sendToServer(new EmergencyStopTogglePacket());
			}
		}
	}

	@SubscribeEvent
	public static void onMouseInput(InputEvent.MouseButton.Pre event) {
		if (event.getButton() != 0 || event.getAction() != 1)
			return;
		if (!EmergencyStopOverlay.isInTrain())
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.screen != null)
			return;

		int width = mc.getWindow()
			.getGuiScaledWidth();
		int height = mc.getWindow()
			.getGuiScaledHeight();
		int x = EmergencyStopOverlay.getX(width);
		int y = EmergencyStopOverlay.getY(height);
		double mx = mc.mouseHandler.xpos() * width / mc.getWindow().getScreenWidth();
		double my = mc.mouseHandler.ypos() * height / mc.getWindow().getScreenHeight();

		if (mx >= x && mx <= x + EmergencyStopOverlay.WIDTH && my >= y && my <= y + EmergencyStopOverlay.HEIGHT) {
			ModNetwork.CHANNEL.sendToServer(new EmergencyStopTogglePacket());
			event.setCanceled(true);
		}
	}
}
