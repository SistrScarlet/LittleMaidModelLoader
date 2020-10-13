package net.sistr.lmml.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber
public class LMMLConfig {

	public static final String CATEGORY_COMMON = "common";
	public static final String CATEGORY_CLIENT = "client";
	public static final String SUBCATEGORY_DEBUG = "debug";

	public static ForgeConfigSpec COMMON_CONFIG;
	public static ForgeConfigSpec CLIENT_CONFIG;

	public static ForgeConfigSpec.BooleanValue DEBUG_MODE;

	static {
		ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();


		COMMON_BUILDER.comment("Common settings").push(CATEGORY_COMMON);
		setupMiscConfig(COMMON_BUILDER);
		COMMON_BUILDER.pop();

		CLIENT_BUILDER.comment("Client settings").push(CATEGORY_CLIENT);
		CLIENT_BUILDER.pop();

		COMMON_CONFIG = COMMON_BUILDER.build();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}

	private static void setupMiscConfig(ForgeConfigSpec.Builder COMMON_BUILDER) {
		COMMON_BUILDER.comment("Debug settings").push(SUBCATEGORY_DEBUG);
		DEBUG_MODE = COMMON_BUILDER.comment("デバッグモードを有効にするか否か / Enable debug mode or not")
				.define("debug_mode", false);
		COMMON_BUILDER.pop();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {

	}

	@SubscribeEvent
	public static void onReload(final ModConfig.Reloading configEvent) {
	}

	public static boolean isDebugMode() {
		return DEBUG_MODE.get();
	}

}
