package com.seppu.createadvancedschedules.registry;

import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;
import com.seppu.createadvancedschedules.schedule.AdvancedScheduleMenu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
	public static final DeferredRegister<MenuType<?>> MENUS =
		DeferredRegister.create(ForgeRegistries.MENU_TYPES, CreateAdvancedSchedulesMod.MODID);

	public static final RegistryObject<MenuType<AdvancedScheduleMenu>> ADVANCED_SCHEDULE =
		MENUS.register("advanced_schedule",
			() -> IForgeMenuType.create(AdvancedScheduleMenu::new));

	private ModMenuTypes() {
	}

	public static void register(IEventBus modEventBus) {
		MENUS.register(modEventBus);
	}
}
