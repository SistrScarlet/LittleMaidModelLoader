package net.sistr.lmml.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class LMMLConfig {

	public static final String CATEGORY_COMMON = "common";
	public static final String CATEGORY_CLIENT = "client";

	public static ForgeConfigSpec COMMON_CONFIG;
	public static ForgeConfigSpec CLIENT_CONFIG;

	public static boolean cfg_PrintDebugMessage = false;

	static {
		ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();


		COMMON_BUILDER.comment("Common settings").push(CATEGORY_COMMON);
		COMMON_BUILDER.pop();

		CLIENT_BUILDER.comment("Client settings").push(CATEGORY_CLIENT);
		CLIENT_BUILDER.pop();

		COMMON_CONFIG = COMMON_BUILDER.build();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {

	}

	@SubscribeEvent
	public static void onReload(final ModConfig.Reloading configEvent) {
	}

}
