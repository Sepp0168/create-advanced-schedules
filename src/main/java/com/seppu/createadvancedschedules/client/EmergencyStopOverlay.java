package com.seppu.createadvancedschedules.client;

import com.simibubi.create.content.contraptions.actors.trainControls.ControlsHandler;
import com.simibubi.create.content.trains.entity.Carriage;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class EmergencyStopOverlay {
	public static final int WIDTH = 44;
	public static final int HEIGHT = 14;

	public static final IGuiOverlay OVERLAY = EmergencyStopOverlay::render;

	private EmergencyStopOverlay() {
	}

	public static boolean isInTrain() {
		return ControlsHandler.getContraption() instanceof CarriageContraptionEntity;
	}

	public static Carriage getCarriage() {
		if (!(ControlsHandler.getContraption() instanceof CarriageContraptionEntity cce))
			return null;
		return cce.getCarriage();
	}

	public static int getX(int width) {
		return width / 2 + 96;
	}

	public static int getY(int height) {
		return height - 23;
	}

	private static void render(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
		if (!isInTrain())
			return;

		Carriage carriage = getCarriage();
		if (carriage == null)
			return;

		boolean active = ClientEmergencyStopState.isActive(carriage.train.id);
		int x = getX(width);
		int y = getY(height);

		int bg = active ? 0xFFB00000 : 0xFF8A0000;
		int border = 0xFF2A0000;

		graphics.fill(x, y, x + WIDTH, y + HEIGHT, bg);
		graphics.fill(x, y, x + WIDTH, y + 1, border);
		graphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, border);
		graphics.fill(x, y, x + 1, y + HEIGHT, border);
		graphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, border);

		Font font = Minecraft.getInstance().font;
		Component label = Component.literal(active ? "E-STOP" : "STOP");
		graphics.drawString(font, label, x + 4, y + 3, 0xFFF2E8E8, false);
	}
}
