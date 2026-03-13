package com.seppu.createadvancedschedules.registry;

import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;
import com.seppu.createadvancedschedules.schedule.AdvancedScheduleItem;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS =
		DeferredRegister.create(ForgeRegistries.ITEMS, CreateAdvancedSchedulesMod.MODID);

	public static final RegistryObject<Item> EMERGENCY_STOP_BUTTON =
		ITEMS.register("emergency_stop_button",
			() -> new BlockItem(ModBlocks.EMERGENCY_STOP_BUTTON.get(), new Item.Properties()));
	public static final RegistryObject<Item> ADVANCED_SCHEDULE =
		ITEMS.register("advanced_schedule",
			() -> new AdvancedScheduleItem(new Item.Properties().stacksTo(1)));

	private ModItems() {
	}

	public static void register(IEventBus modEventBus) {
		ITEMS.register(modEventBus);
	}
}
