package com.seppu.createadvancedschedules.schedule;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.gui.menu.HeldItemGhostItemMenu;
import com.seppu.createadvancedschedules.registry.ModMenuTypes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class AdvancedScheduleMenu extends HeldItemGhostItemMenu {
	public static final int MAX_WINDOWS = 12;
	public static final int VISIBLE_ROWS = 4;
	public static final int ROW_Y_START = 40;
	public static final int ROW_SPACING = 22;
	public static final int SLOT_X = 182;

	public int windowCount = 1;
	public int windowSlotStart = -1;
	public int scrollIndex = 0;
	public int visibleRowCount = VISIBLE_ROWS;

	public AdvancedScheduleMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
		this(ModMenuTypes.ADVANCED_SCHEDULE.get(), id, inv, extraData);
	}

	public AdvancedScheduleMenu(MenuType<?> type, int id, Inventory inv, FriendlyByteBuf extraData) {
		super(type, id, inv, extraData);
	}

	public AdvancedScheduleMenu(MenuType<?> type, int id, Inventory inv, ItemStack contentHolder) {
		super(type, id, inv, contentHolder);
	}

	@Override
	protected ItemStackHandler createGhostInventory() {
		return new ItemStackHandler(MAX_WINDOWS);
	}

	@Override
	protected boolean allowRepeats() {
		return true;
	}

	@Override
	protected void initAndReadInventory(ItemStack contentHolder) {
		super.initAndReadInventory(contentHolder);
		AdvancedScheduleData data = AdvancedScheduleData.fromItemTag(contentHolder.getTag());
		windowCount = Math.max(1, Math.min(MAX_WINDOWS, data.windows.size() == 0 ? 1 : data.windows.size()));
		for (int i = 0; i < data.windows.size() && i < MAX_WINDOWS; i++) {
			AdvancedScheduleData.Window window = data.windows.get(i);
			if (window.schedule == null)
				continue;
			ItemStack scheduleStack = AllItems.SCHEDULE.asStack();
			scheduleStack.getOrCreateTag().put("Schedule", window.schedule.write());
			ghostInventory.setStackInSlot(i, scheduleStack);
		}
	}

	@Override
	protected void addSlots() {
		addPlayerSlots(18, 140);
		windowSlotStart = slots.size();
		for (int i = 0; i < VISIBLE_ROWS; i++) {
			int y = ROW_Y_START + i * ROW_SPACING;
			addSlot(new WindowSlot(ghostInventory, i, SLOT_X, y));
		}
	}

	@Override
	protected Slot createPlayerSlot(Inventory inventory, int index, int x, int y) {
		return new Slot(inventory, index, x, y);
	}

	@Override
	protected void saveData(ItemStack contentHolder) {
	}

	@Override
	public void clicked(int slotId, int dragType, ClickType clickTypeIn, Player player) {
		if (slotId < 36 || slotId >= 36 + VISIBLE_ROWS) {
			super.clicked(slotId, dragType, clickTypeIn, player);
			return;
		}
		if (clickTypeIn == ClickType.THROW || clickTypeIn == ClickType.CLONE)
			return;

		int slot = slotId - 36;
		if (slot >= visibleRowCount || slot + scrollIndex >= windowCount)
			return;

		ItemStack held = getCarried();
		ItemStack existing = ghostInventory.getStackInSlot(slot);

		if (held.isEmpty()) {
			if (existing.isEmpty())
				return;
			setCarried(existing.copy());
			ghostInventory.setStackInSlot(slot, ItemStack.EMPTY);
			getSlot(slotId).setChanged();
			return;
		}

		if (!AllItems.SCHEDULE.isIn(held))
			return;

		ItemStack insert = held.copy();
		insert.setCount(1);
		if (!player.isCreative()) {
			held.shrink(1);
			setCarried(held);
		}
		if (!existing.isEmpty() && !player.isCreative())
			ItemHandlerHelper.giveItemToPlayer(player, existing.copy());

		ghostInventory.setStackInSlot(slot, insert);
		getSlot(slotId).setChanged();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		if (index < 36) {
			Slot slot = this.slots.get(index);
			ItemStack stackToInsert = slot.getItem();
			if (!AllItems.SCHEDULE.isIn(stackToInsert))
				return ItemStack.EMPTY;
			for (int i = 0; i < visibleRowCount; i++) {
				if (i + scrollIndex >= windowCount)
					break;
				ItemStack stack = ghostInventory.getStackInSlot(i);
				if (!stack.isEmpty())
					continue;
				ItemStack copy = stackToInsert.copy();
				copy.setCount(1);
				ghostInventory.setStackInSlot(i, copy);
				if (!playerIn.isCreative())
					stackToInsert.shrink(1);
				getSlot(i + 36).setChanged();
				break;
			}
		} else if (index < 36 + VISIBLE_ROWS) {
			ItemStack stack = ghostInventory.getStackInSlot(index - 36);
			if (!stack.isEmpty()) {
				ItemHandlerHelper.giveItemToPlayer(playerIn, stack.copy());
				ghostInventory.setStackInSlot(index - 36, ItemStack.EMPTY);
				getSlot(index).setChanged();
			}
		}
		return ItemStack.EMPTY;
	}

	class WindowSlot extends SlotItemHandler {
		private final int rowIndex;

		public WindowSlot(IItemHandler handler, int index, int xPosition, int yPosition) {
			super(handler, index, xPosition, yPosition);
			this.rowIndex = index;
		}

		@Override
		public boolean isActive() {
			if (rowIndex >= visibleRowCount)
				return false;
			return (rowIndex + scrollIndex) < windowCount;
		}
	}
}
