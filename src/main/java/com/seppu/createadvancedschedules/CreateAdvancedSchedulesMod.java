package com.seppu.createadvancedschedules;

import com.mojang.logging.LogUtils;
import com.seppu.createadvancedschedules.schedule.ConditionalWaitInstruction;
import com.seppu.createadvancedschedules.schedule.JumpToSectionInstruction;
import com.seppu.createadvancedschedules.schedule.ScheduleSectionInstruction;
import com.seppu.createadvancedschedules.schedule.TimeWindowInstruction;
import com.seppu.createadvancedschedules.network.ModNetwork;
import com.seppu.createadvancedschedules.registry.ModBlocks;
import com.seppu.createadvancedschedules.registry.ModCreativeTabs;
import com.seppu.createadvancedschedules.registry.ModItems;
import com.seppu.createadvancedschedules.registry.ModMenuTypes;
import com.seppu.createadvancedschedules.world.EmergencyStopButtonInteraction;
import com.simibubi.create.content.trains.schedule.Schedule;
import com.simibubi.create.api.behaviour.interaction.MovingInteractionBehaviour;

import net.createmod.catnip.data.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(CreateAdvancedSchedulesMod.MODID)
public class CreateAdvancedSchedulesMod {
	public static final String MODID = "createadvancedschedules";

	private static final Logger LOGGER = LogUtils.getLogger();

	public CreateAdvancedSchedulesMod(FMLJavaModLoadingContext context) {
		IEventBus modEventBus = context.getModEventBus();
		modEventBus.addListener(this::commonSetup);
		ModBlocks.register(modEventBus);
		ModCreativeTabs.register(modEventBus);
		ModItems.register(modEventBus);
		ModMenuTypes.register(modEventBus);
		ModNetwork.register();
	}

	private void commonSetup(final FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			// Register custom schedule instruction with Create's schedule runtime lists.
			Schedule.INSTRUCTION_TYPES.add(
				Pair.of(ResourceLocation.fromNamespaceAndPath(MODID, "conditional"), ConditionalWaitInstruction::new)
			);
			Schedule.INSTRUCTION_TYPES.add(
				Pair.of(ResourceLocation.fromNamespaceAndPath(MODID, "section"), ScheduleSectionInstruction::new)
			);
			Schedule.INSTRUCTION_TYPES.add(
				Pair.of(ResourceLocation.fromNamespaceAndPath(MODID, "time_window"), TimeWindowInstruction::new)
			);
			Schedule.INSTRUCTION_TYPES.add(
				Pair.of(ResourceLocation.fromNamespaceAndPath(MODID, "jump_section"), JumpToSectionInstruction::new)
			);
			MovingInteractionBehaviour.REGISTRY.register(
				ModBlocks.EMERGENCY_STOP_BUTTON.get(),
				new EmergencyStopButtonInteraction()
			);
			LOGGER.info("Registered Create Advanced Schedules schedule instruction.");
		});
	}
}
