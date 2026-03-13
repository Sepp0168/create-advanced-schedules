package com.seppu.createadvancedschedules.registry;

import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
	public static final DeferredRegister<CreativeModeTab> TABS =
		DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateAdvancedSchedulesMod.MODID);

	public static final RegistryObject<CreativeModeTab> CREATE_CONDITIONAL =
		TABS.register("createadvancedschedules", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup.createadvancedschedules"))
			.icon(() -> new ItemStack(ModItems.EMERGENCY_STOP_BUTTON.get()))
			.displayItems((params, output) -> {
				output.accept(ModItems.EMERGENCY_STOP_BUTTON.get());
				output.accept(ModItems.ADVANCED_SCHEDULE.get());
			})
			.build());

	private ModCreativeTabs() {
	}

	public static void register(IEventBus modEventBus) {
		TABS.register(modEventBus);
	}
}
