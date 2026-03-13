package com.seppu.createadvancedschedules.registry;

import com.seppu.createadvancedschedules.CreateAdvancedSchedulesMod;
import com.seppu.createadvancedschedules.world.EmergencyStopButtonBlock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS =
		DeferredRegister.create(ForgeRegistries.BLOCKS, CreateAdvancedSchedulesMod.MODID);

	public static final RegistryObject<Block> EMERGENCY_STOP_BUTTON =
		BLOCKS.register("emergency_stop_button",
			() -> new EmergencyStopButtonBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_BLOCK)));

	private ModBlocks() {
	}

	public static void register(IEventBus modEventBus) {
		BLOCKS.register(modEventBus);
	}
}
