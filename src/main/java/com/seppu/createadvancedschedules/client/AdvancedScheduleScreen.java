package com.seppu.createadvancedschedules.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.seppu.createadvancedschedules.network.ModNetwork;
import com.seppu.createadvancedschedules.network.AdvancedScheduleEditPacket;
import com.seppu.createadvancedschedules.schedule.AdvancedScheduleData;
import com.seppu.createadvancedschedules.schedule.AdvancedScheduleMenu;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.gui.UIRenderHelper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.Mth;

public class AdvancedScheduleScreen extends AbstractSimiContainerScreen<AdvancedScheduleMenu> {
	private static final int LABEL_Y = 22;
	private static final int START_X = 78;
	private static final int END_X = 130;
	private static final int BOX_W = 38;
	private static final int ROW_BG_X = 24;
	private static final int ROW_BG_W = 198;
	private static final int ROW_BG_H = 18;
	private static final int LABEL_COLOR = 0xF2D16B;
	private static final int PLAYER_INV_X = 10;
	private static final int PLAYER_INV_Y = 122;
	private static final int SCROLL_STEP = 1;

	private final List<EditBox> startBoxes = new ArrayList<>();
	private final List<EditBox> endBoxes = new ArrayList<>();
	private final List<IconButton> deleteButtons = new ArrayList<>();
	private final List<Integer> startValues = new ArrayList<>();
	private final List<Integer> endValues = new ArrayList<>();
	private final List<ItemStack> scheduleStacks = new ArrayList<>();
	private IconButton addButton;
	private IconButton confirmButton;
	private IconButton cancelButton;
	private int scrollIndex = 0;
	private int maxScrollIndex = 0;
	private boolean refreshVisible = true;

	public AdvancedScheduleScreen(AdvancedScheduleMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
		imageWidth = 256;
		imageHeight = 226;
	}

	@Override
	protected void init() {
		super.init();
		startBoxes.clear();
		endBoxes.clear();
		deleteButtons.clear();
		startValues.clear();
		endValues.clear();
		scheduleStacks.clear();

		AdvancedScheduleData data = AdvancedScheduleData.fromItemTag(menu.contentHolder.getTag());
		menu.windowCount = Math.max(1, Math.min(AdvancedScheduleMenu.MAX_WINDOWS,
			data.windows.size() == 0 ? 1 : data.windows.size()));
		for (int i = 0; i < menu.windowCount; i++) {
			AdvancedScheduleData.Window window = i < data.windows.size()
				? data.windows.get(i)
				: null;
			int start = window != null ? window.start : 0;
			int end = window != null ? window.end : 12000;
			startValues.add(start);
			endValues.add(end);
			if (window != null && window.schedule != null) {
				ItemStack scheduleStack = AllItems.SCHEDULE.asStack();
				scheduleStack.getOrCreateTag().put("Schedule", window.schedule.write());
				scheduleStacks.add(scheduleStack);
			} else {
				scheduleStacks.add(ItemStack.EMPTY);
			}
		}

		for (int i = 0; i < AdvancedScheduleMenu.VISIBLE_ROWS; i++) {
			int y = topPos + AdvancedScheduleMenu.ROW_Y_START + i * AdvancedScheduleMenu.ROW_SPACING + 3;
			EditBox start = new EditBox(font, leftPos + START_X, y, BOX_W, 14,
				Component.translatable("createadvancedschedules.advanced_schedule.start"));
			EditBox end = new EditBox(font, leftPos + END_X, y, BOX_W, 14,
				Component.translatable("createadvancedschedules.advanced_schedule.end"));
			start.setMaxLength(5);
			end.setMaxLength(5);
			start.setFilter(this::isNumber);
			end.setFilter(this::isNumber);
			start.setValue("0");
			end.setValue("12000");
			startBoxes.add(start);
			endBoxes.add(end);
			addRenderableWidget(start);
			addRenderableWidget(end);

			IconButton delete = new IconButton(leftPos + ROW_BG_X + ROW_BG_W - 18, y - 2, AllIcons.I_MTD_CLOSE);
			final int deleteIndex = i;
			delete.withCallback(() -> removeRow(deleteIndex));
			deleteButtons.add(delete);
			addRenderableWidget(delete);
		}

		addButton = new IconButton(leftPos + 26, topPos + 46, AllIcons.I_ADD);
		addButton.withCallback(this::addRow);
		addRenderableWidget(addButton);

		cancelButton = new IconButton(leftPos + imageWidth - 44, topPos + imageHeight - 24, AllIcons.I_CONFIG_BACK);
		cancelButton.withCallback(() -> onClose());
		addRenderableWidget(cancelButton);

		confirmButton = new IconButton(leftPos + imageWidth - 22, topPos + imageHeight - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(this::confirm);
		addRenderableWidget(confirmButton);

		scrollIndex = 0;
		refreshVisible = true;
		updateVisibleRows();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		AllGuiTextures.SCHEDULE.render(graphics, leftPos, topPos);

		for (int row = 0; row < getVisibleRowCount(); row++) {
			int y = getRowTop(row);
			int windowIndex = windowIndexForRow(row);
			UIRenderHelper.drawStretched(graphics, leftPos + ROW_BG_X, y, ROW_BG_W, ROW_BG_H, 0,
				AllGuiTextures.SCHEDULE_CARD_MEDIUM);
			UIRenderHelper.drawStretched(graphics, leftPos + ROW_BG_X + 1, y + 1, ROW_BG_W - 2, ROW_BG_H - 2, 0,
				AllGuiTextures.SCHEDULE_CARD_LIGHT);

			Item iconItem = getPeriodIconItem(windowIndex);
			if (iconItem != null) {
				GuiGameElement.of(new ItemStack(iconItem))
					.at(leftPos + ROW_BG_X + 4, y + 2)
					.render(graphics);
			}
		}

		graphics.drawString(font, Component.translatable("createadvancedschedules.advanced_schedule.between"),
			leftPos + 30, topPos + LABEL_Y, LABEL_COLOR, false);
		graphics.drawString(font, Component.translatable("createadvancedschedules.advanced_schedule.and"),
			leftPos + 120, topPos + LABEL_Y, LABEL_COLOR, false);
		graphics.drawString(font, Component.translatable("createadvancedschedules.advanced_schedule.follow"),
			leftPos + 158, topPos + LABEL_Y, LABEL_COLOR, false);

		AllGuiTextures.PLAYER_INVENTORY.render(graphics, leftPos + PLAYER_INV_X, topPos + PLAYER_INV_Y);
		graphics.drawString(font, playerInventoryTitle, leftPos + PLAYER_INV_X + 8,
			topPos + PLAYER_INV_Y + 6, 0x505050, false);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		updateVisibleRows();
		super.render(graphics, mouseX, mouseY, partialTicks);
		renderTooltip(graphics, mouseX, mouseY);
	}

	private void addRow() {
		if (menu.windowCount >= AdvancedScheduleMenu.MAX_WINDOWS)
			return;
		syncVisibleToData();
		menu.windowCount++;
		startValues.add(0);
		endValues.add(12000);
		scheduleStacks.add(ItemStack.EMPTY);
		if (menu.windowCount > AdvancedScheduleMenu.VISIBLE_ROWS)
			scrollIndex = menu.windowCount - AdvancedScheduleMenu.VISIBLE_ROWS;
		refreshVisible = true;
		updateVisibleRows();
	}

	private void updateVisibleRows() {
		updateScrollLimits();
		int visibleRows = getVisibleRowCount();
		menu.visibleRowCount = visibleRows;
		menu.scrollIndex = scrollIndex;
		for (int row = 0; row < AdvancedScheduleMenu.VISIBLE_ROWS; row++) {
			boolean visible = row < visibleRows;
			int rowTop = getRowTop(row);
			int rowY = rowTop + 5;

			startBoxes.get(row).setVisible(visible);
			endBoxes.get(row).setVisible(visible);
			startBoxes.get(row).setX(leftPos + START_X);
			startBoxes.get(row).setY(rowY);
			endBoxes.get(row).setX(leftPos + END_X);
			endBoxes.get(row).setY(rowY);

			deleteButtons.get(row).visible = visible && menu.windowCount > 1;
			deleteButtons.get(row).setX(leftPos + ROW_BG_X + ROW_BG_W - 18);
			deleteButtons.get(row).setY(rowTop + 1);
		}

		if (refreshVisible) {
			loadVisibleFromData();
			refreshVisible = false;
		}

		addButton.visible = menu.windowCount < AdvancedScheduleMenu.MAX_WINDOWS;
		if (addButton.visible) {
			int y = getRowTop(visibleRows) + 4;
			addButton.setX(leftPos + 26);
			addButton.setY(y);
		}
	}

	private void removeRow(int index) {
		if (menu.windowCount <= 1)
			return;
		if (index < 0 || index >= getVisibleRowCount())
			return;

		syncVisibleToData();
		int windowIndex = windowIndexForRow(index);
		if (windowIndex < 0 || windowIndex >= menu.windowCount)
			return;

		startValues.remove(windowIndex);
		endValues.remove(windowIndex);
		scheduleStacks.remove(windowIndex);
		menu.windowCount--;
		scrollIndex = Mth.clamp(scrollIndex, 0, Math.max(0, menu.windowCount - AdvancedScheduleMenu.VISIBLE_ROWS));
		refreshVisible = true;
		updateVisibleRows();
	}

	private void confirm() {
		syncVisibleToData();
		AdvancedScheduleData data = new AdvancedScheduleData();
		for (int i = 0; i < menu.windowCount; i++) {
			int start = AdvancedScheduleData.clampTime(startValues.get(i));
			int end = AdvancedScheduleData.clampTime(endValues.get(i));
			Schedule schedule = null;
			ItemStack stack = scheduleStacks.get(i);
			if (!stack.isEmpty() && stack.getOrCreateTag().contains("Schedule")) {
				schedule = Schedule.fromTag(stack.getOrCreateTag().getCompound("Schedule"));
			}
			data.windows.add(new AdvancedScheduleData.Window(start, end, schedule));
		}
		ModNetwork.CHANNEL.sendToServer(new AdvancedScheduleEditPacket(data.write()));
		onClose();
	}

	private int parseTime(String value) {
		try {
			return AdvancedScheduleData.clampTime(Integer.parseInt(value.trim()));
		} catch (NumberFormatException ex) {
			return 0;
		}
	}

	private boolean isNumber(String value) {
		return value.isEmpty() || value.chars().allMatch(Character::isDigit);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (isOverList(mouseX, mouseY) && maxScrollIndex > 0) {
			syncVisibleToData();
			scrollIndex = Mth.clamp(scrollIndex - (int) (delta * SCROLL_STEP), 0, maxScrollIndex);
			refreshVisible = true;
			updateVisibleRows();
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, delta);
	}

	private void updateScrollLimits() {
		maxScrollIndex = Math.max(0, menu.windowCount - AdvancedScheduleMenu.VISIBLE_ROWS);
		scrollIndex = Mth.clamp(scrollIndex, 0, maxScrollIndex);
	}

	private boolean isOverList(double mouseX, double mouseY) {
		return mouseX >= getListLeft() && mouseX < getListRight()
			&& mouseY >= getListTop() && mouseY < getListBottom();
	}

	private int getListLeft() {
		return leftPos + ROW_BG_X;
	}

	private int getListRight() {
		return leftPos + ROW_BG_X + ROW_BG_W;
	}

	private int getListTop() {
		return topPos + AdvancedScheduleMenu.ROW_Y_START - 2;
	}

	private int getListBottom() {
		return topPos + PLAYER_INV_Y - 6;
	}

	private int getRowTop(int row) {
		return topPos + AdvancedScheduleMenu.ROW_Y_START + row * AdvancedScheduleMenu.ROW_SPACING - 2;
	}

	private int getVisibleRowCount() {
		return Math.min(AdvancedScheduleMenu.VISIBLE_ROWS, menu.windowCount);
	}

	private int windowIndexForRow(int row) {
		return row + scrollIndex;
	}

	private void loadVisibleFromData() {
		int visibleRows = getVisibleRowCount();
		for (int row = 0; row < AdvancedScheduleMenu.VISIBLE_ROWS; row++) {
			if (row >= visibleRows) {
				startBoxes.get(row).setValue("");
				endBoxes.get(row).setValue("");
				menu.ghostInventory.setStackInSlot(row, ItemStack.EMPTY);
				continue;
			}
			int windowIndex = windowIndexForRow(row);
			startBoxes.get(row).setValue(Integer.toString(startValues.get(windowIndex)));
			endBoxes.get(row).setValue(Integer.toString(endValues.get(windowIndex)));
			menu.ghostInventory.setStackInSlot(row, scheduleStacks.get(windowIndex));
		}
	}

	private void syncVisibleToData() {
		int visibleRows = getVisibleRowCount();
		for (int row = 0; row < visibleRows; row++) {
			int windowIndex = windowIndexForRow(row);
			startValues.set(windowIndex, parseTime(startBoxes.get(row).getValue()));
			endValues.set(windowIndex, parseTime(endBoxes.get(row).getValue()));
			scheduleStacks.set(windowIndex, menu.ghostInventory.getStackInSlot(row).copy());
		}
	}

	private Item getPeriodIconItem(int index) {
		if (index < 0 || index >= menu.windowCount)
			return null;
		int start = parseTime(startBoxes.get(index).getValue());
		int end = parseTime(endBoxes.get(index).getValue());

		boolean coversDay = overlaps(start, end, 0, 12000);
		boolean coversNight = overlaps(start, end, 12000, 24000);

		if (coversDay && coversNight)
			return Items.CLOCK;
		if (coversDay)
			return Items.SUNFLOWER;
		return Items.ENDER_PEARL;
	}

	private boolean overlaps(int start, int end, int rangeStart, int rangeEnd) {
		if (start == end)
			return true;
		if (start < end)
			return start < rangeEnd && end > rangeStart;
		return (start < rangeEnd) || (end > rangeStart);
	}
}
