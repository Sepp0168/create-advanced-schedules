package com.seppu.createadvancedschedules.client;

import java.util.List;

import com.seppu.createadvancedschedules.schedule.ConditionalScheduleCondition;
import com.seppu.createadvancedschedules.schedule.ConditionalWaitInstruction;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.content.trains.schedule.Schedule;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ConditionalBranchScreen extends Screen {
	private static final int WIDTH = 220;
	private static final int HEIGHT = 140;

	private final Screen parent;
	private final ConditionalWaitInstruction instruction;

	private Button conditionButton;
	private Button detailButton;
	private EditBox valueBox;
	private EditBox trueJumpBox;
	private EditBox falseJumpBox;
	private int detailX;
	private int detailY;
	private ConditionalScheduleCondition.ConditionType currentCondition;
	private List<String> currentDetailOptions;
	private int detailIndex;

	public ConditionalBranchScreen(Screen parent, ConditionalWaitInstruction instruction) {
		super(Component.translatable("createadvancedschedules.ui.conditional.title"));
		this.parent = parent;
		this.instruction = instruction;
	}

	@Override
	protected void init() {
		int x = (width - WIDTH) / 2;
		int y = (height - HEIGHT) / 2;

		currentCondition = instruction.getConditionType();
		conditionButton = addRenderableWidget(Button.builder(conditionLabel(currentCondition), btn -> {
			currentCondition = nextCondition(currentCondition);
			conditionButton.setMessage(conditionLabel(currentCondition));
			updateDetailOptions(currentCondition);
		}).bounds(x + 10, y + 24, 200, 20).build());

		detailX = x + 10;
		detailY = y + 50;
		detailButton = addRenderableWidget(Button.builder(Component.literal(""), btn -> {
			if (currentDetailOptions == null || currentDetailOptions.isEmpty())
				return;
			detailIndex = (detailIndex + 1) % currentDetailOptions.size();
			detailButton.setMessage(detailLabel());
		}).bounds(detailX, detailY, 200, 20).build());

		valueBox = new EditBox(font, x + 10, y + 76, 98, 18, Component.translatable("createadvancedschedules.ui.value"));
		valueBox.setValue(String.valueOf(instruction.getValue()));
		addRenderableWidget(valueBox);

		trueJumpBox = new EditBox(font, x + 120, y + 76, 40, 18, Component.translatable("createadvancedschedules.ui.true_jump"));
		trueJumpBox.setValue(String.valueOf(instruction.getTrueJump()));
		addRenderableWidget(trueJumpBox);

		falseJumpBox = new EditBox(font, x + 170, y + 76, 40, 18, Component.translatable("createadvancedschedules.ui.false_jump"));
		falseJumpBox.setValue(String.valueOf(instruction.getFalseJump()));
		addRenderableWidget(falseJumpBox);

		IconButton cancel = new IconButton(x + WIDTH - 44, y + HEIGHT - 24, AllIcons.I_CONFIG_BACK);
		cancel.withCallback(() -> Minecraft.getInstance().setScreen(parent));
		addRenderableWidget(cancel);

		IconButton confirm = new IconButton(x + WIDTH - 22, y + HEIGHT - 24, AllIcons.I_CONFIRM);
		confirm.withCallback(this::applyAndClose);
		addRenderableWidget(confirm);

		updateDetailOptions(currentCondition);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics);
		int x = (width - WIDTH) / 2;
		int y = (height - HEIGHT) / 2;

		graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xFF2A2A2A);
		graphics.fill(x + 1, y + 1, x + WIDTH - 1, y + HEIGHT - 1, 0xFF3A3A3A);

		graphics.drawString(font, title, x + 10, y + 8, 0xFFECECEC, false);
		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	private void updateDetailOptions(ConditionalScheduleCondition.ConditionType type) {
		currentDetailOptions = null;
		detailIndex = 0;
		if (type == ConditionalScheduleCondition.ConditionType.REDSTONE) {
			currentDetailOptions = List.of(
				Component.translatable("createadvancedschedules.schedule.redstone.powered").getString(),
				Component.translatable("createadvancedschedules.schedule.redstone.unpowered").getString()
			);
			detailIndex = instruction.isRedstoneInverted() ? 1 : 0;
			valueBox.visible = false;
			valueBox.active = false;
		}

		if (type == ConditionalScheduleCondition.ConditionType.WEATHER) {
			currentDetailOptions = List.of(
				Component.translatable("createadvancedschedules.schedule.weather.clear").getString(),
				Component.translatable("createadvancedschedules.schedule.weather.rain").getString(),
				Component.translatable("createadvancedschedules.schedule.weather.thunder").getString()
			);
			detailIndex = instruction.getWeatherState().ordinal();
			valueBox.visible = false;
			valueBox.active = false;
		}

		if (currentDetailOptions == null) {
			currentDetailOptions = List.of(
				Component.translatable("createadvancedschedules.schedule.operator.gt").getString(),
				Component.translatable("createadvancedschedules.schedule.operator.lt").getString()
			);
			detailIndex = instruction.getOperator() == ConditionalScheduleCondition.Operator.GREATER ? 0 : 1;
			valueBox.visible = true;
			valueBox.active = true;
		}

		detailButton.setMessage(detailLabel());
		if (type != ConditionalScheduleCondition.ConditionType.REDSTONE
			&& type != ConditionalScheduleCondition.ConditionType.WEATHER) {
			valueBox.visible = true;
			valueBox.active = true;
		}
		if (type == ConditionalScheduleCondition.ConditionType.REDSTONE
			|| type == ConditionalScheduleCondition.ConditionType.WEATHER) {
			valueBox.visible = false;
			valueBox.active = false;
		}
	}

	private void applyAndClose() {
		ConditionalScheduleCondition.ConditionType type = currentCondition;
		instruction.setConditionType(type);

		if (type == ConditionalScheduleCondition.ConditionType.REDSTONE) {
			boolean inverted = detailIndex == 1;
			instruction.setRedstoneInverted(inverted);
		} else if (type == ConditionalScheduleCondition.ConditionType.WEATHER) {
			instruction.setWeatherState(ConditionalScheduleCondition.WeatherState.values()[detailIndex]);
		} else {
			instruction.setOperator(detailIndex == 0
				? ConditionalScheduleCondition.Operator.GREATER
				: ConditionalScheduleCondition.Operator.LESS);
			instruction.setValue(parseInt(valueBox.getValue(), instruction.getValue()));
		}

		instruction.setTrueJump(parseInt(trueJumpBox.getValue(), instruction.getTrueJump()));
		instruction.setFalseJump(parseInt(falseJumpBox.getValue(), instruction.getFalseJump()));

		createadvancedschedules$commitScheduleChanges();
		Minecraft.getInstance().setScreen(parent);
	}

	private Component conditionLabel(ConditionalScheduleCondition.ConditionType type) {
		return Component.translatable("createadvancedschedules.schedule.label.condition")
			.append(": ")
			.append(type.getDisplayName());
	}

	private Component detailLabel() {
		if (currentDetailOptions == null || currentDetailOptions.isEmpty())
			return Component.translatable("createadvancedschedules.ui.detail");
		return Component.translatable("createadvancedschedules.ui.detail")
			.append(": ")
			.append(Component.literal(currentDetailOptions.get(detailIndex)));
	}

	private ConditionalScheduleCondition.ConditionType nextCondition(
		ConditionalScheduleCondition.ConditionType current
	) {
		ConditionalScheduleCondition.ConditionType[] values = ConditionalScheduleCondition.ConditionType.values();
		return values[(current.ordinal() + 1) % values.length];
	}
	private String weatherLabel(ConditionalScheduleCondition.WeatherState state) {
		return switch (state) {
			case CLEAR -> Component.translatable("createadvancedschedules.schedule.weather.clear").getString();
			case RAIN -> Component.translatable("createadvancedschedules.schedule.weather.rain").getString();
			case THUNDER -> Component.translatable("createadvancedschedules.schedule.weather.thunder").getString();
		};
	}

	private int parseInt(String text, int fallback) {
		try {
			return Integer.parseInt(text.trim());
		} catch (NumberFormatException ex) {
			return fallback;
		}
	}

	private void createadvancedschedules$commitScheduleChanges() {
		if (!(parent instanceof com.simibubi.create.content.trains.schedule.ScheduleScreen scheduleScreen))
			return;
		try {
			java.util.function.Consumer<Boolean> onEditorClose =
				(java.util.function.Consumer<Boolean>) createadvancedschedules$findFieldValue(scheduleScreen, "onEditorClose", null);
			if (onEditorClose != null)
				onEditorClose.accept(true);

			java.lang.reflect.Method stopEditing =
				scheduleScreen.getClass().getDeclaredMethod("stopEditing");
			stopEditing.setAccessible(true);
			stopEditing.invoke(scheduleScreen);

			Schedule schedule = (Schedule) createadvancedschedules$findFieldValue(scheduleScreen, "schedule", Schedule.class);
			if (schedule == null)
				return;
			Object menu = createadvancedschedules$findFieldValue(scheduleScreen, "menu", null);
			if (menu == null)
				return;
			ItemStack contentHolder =
				(ItemStack) createadvancedschedules$findFieldValue(menu, "contentHolder", ItemStack.class);
			if (contentHolder == null)
				return;
			contentHolder.getOrCreateTag().put("Schedule", schedule.write());
		} catch (ReflectiveOperationException ignored) {
		}
	}

	private static Object createadvancedschedules$findFieldValue(Object target, String name, Class<?> type)
		throws ReflectiveOperationException {
		Class<?> current = target.getClass();
		while (current != null) {
			try {
				java.lang.reflect.Field field = current.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(target);
			} catch (NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
		}
		if (type != null) {
			current = target.getClass();
			while (current != null) {
				for (java.lang.reflect.Field field : current.getDeclaredFields()) {
					if (type.isAssignableFrom(field.getType())) {
						field.setAccessible(true);
						return field.get(target);
					}
				}
				current = current.getSuperclass();
			}
		}
		return null;
	}
}
