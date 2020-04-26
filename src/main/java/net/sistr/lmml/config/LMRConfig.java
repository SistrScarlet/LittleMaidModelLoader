package net.sistr.lmml.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class LMRConfig {

	public static final String CATEGORY_COMMON = "common";
	public static final String CATEGORY_CLIENT = "client";
	public static final String SUBCATEGORY_SPAWN = "spawn";

	public static ForgeConfigSpec COMMON_CONFIG;
	public static ForgeConfigSpec CLIENT_CONFIG;

	public static ForgeConfigSpec.BooleanValue CAN_SPAWN_LM;
	public static ForgeConfigSpec.IntValue SPAWN_WEIGHT_LM;
	public static ForgeConfigSpec.IntValue SPAWN_LIMIT_LM;
	public static ForgeConfigSpec.IntValue SPAWN_MIN_GROUP_SIZE_LM;
	public static ForgeConfigSpec.IntValue SPAWN_MAX_GROUP_SIZE_LM;
	public static ForgeConfigSpec.BooleanValue CAN_DESPAWN_LM;

	// @MLProp(info="Relative spawn weight. The lower the less common. 10=pigs. 0=off")
	public static int cfg_spawnWeight = 5;
	// @MLProp(info="Maximum spawn count in the World.")
	public static int cfg_spawnLimit = 20;
	// @MLProp(info="Minimum spawn group count.")
	public static int cfg_minGroupSize = 1;
	// @MLProp(info="Maximum spawn group count.")
	public static int cfg_maxGroupSize = 3;
	// @MLProp(info="It will despawn, if it lets things go. ")
	public static boolean cfg_canDespawn = false;

	// @MLProp(info="Print Debug Massages.")
	public static boolean cfg_PrintDebugMessage = false;
	// @MLProp(info="Print Death Massages.")
	public static boolean cfg_DeathMessage = true;
	// @MLProp(info="Spawn Anywhere.")
	public static boolean cfg_Dominant = false;
	// アルファブレンド
	public static boolean cfg_isModelAlphaBlend = false;
	
	// 野生テクスチャ
	public static boolean cfg_isFixedWildMaid = false;

	// VoiceRate
	public static float cfg_voiceRate = 0.2f;
	
	/** メイドの土産 */
	public static boolean cfg_isResurrection = true;
	
	/** 砂糖アイテムID */
	private static List<String> cfg_sugar_item_ids = null;
	
	/** 砂糖アイテムID */
	public static Map<String, Integer> cfg_sugar_item_ids_map = null;

	/** ケーキアイテムID */
	public static List<String> cfg_cake_item_ids = null;
	
	/** 原木ブロックID */
	public static List<String> cfg_lj_log_block_ids = null;

	/** 葉ブロックID */
	public static List<String> cfg_lj_leaf_block_ids = null;

	/** 苗アイテムID */
	public static List<String> cfg_lj_sapling_item_ids = null;
	
	/** デフォルトスポーン有効化設定 */
	public static boolean cfg_spawn_default_enable = true;
	
	/** カスタムスポーン バイオームID or バイオーム名 */
	public static List<String> cfg_spawn_biomes = null;
	
	/** メイドミルク */
	public static boolean cfg_custom_maid_milk = false;
	
	/** 騎乗高さ調整 */
	public static float cfg_custom_riding_height_adjustment = 0.0F;
	
	/** 弓（銃）アイテムID */
	public static List<String> cfg_ac_bow_item_ids = null;
	
	/** 矢（弾丸）アイテムID */
	public static List<String> cfg_ac_arrow_item_ids = null;
	
	/** かまどの料理対象外アイテム */
	public static List<String> cfg_cock_no_cooking_item_ids = null;
	
	/** アニマルメイド判定 */
	public static List<String> cfg_custom_animal_maid_mob_ids = null;
	
	/** hwyla連携 */
	public static boolean cfg_plugin_hwyla = true;
	
	/** trigger 判断用 アイテムID */
	public static List<String> cfg_trigger_item_ids = null;
	
	/** メイドミルクの特殊表示 */
	public static boolean cfg_secret_maid_milk = false;
	public static String cfg_secret_maid_milk_producer_default = "";
	public static String cfg_secret_maid_milk_producer_label = "";
	
	/** みんなのメイドさん */
	public static boolean cfg_cstm_everyones_maid = false;
	
	/** 開発用テストモジュールの有効化設定 */
	public static boolean cfg_developer_test_module = false;
	
	/** メイドアバター */
	public static boolean cfg_lmabatar_maid_avatar = false;
	
	/** ファイルローダー機能のキャッシュ機能のON/OFF設定 */
	public static boolean cfg_loader_is_cache = true;
	
	/** sounds.jsonファイルの出力設定 */
	public static boolean cfg_loader_output_sounds_json = false;
	
	/** テクスチャのリソースパックロードモード設定 */
	public static boolean cfg_loader_texture_load_from_resoucepack = false;
	
	/** LittleMaidAvatarに登録するLayer設定 */
	public static List<String> cfg_lmavatar_include_layer = null;


	static {
		ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
		ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();


		COMMON_BUILDER.comment("Common settings").push(CATEGORY_COMMON);

		setupSpawnConfig(COMMON_BUILDER);

		COMMON_BUILDER.pop();

		CLIENT_BUILDER.comment("Client settings").push(CATEGORY_CLIENT);
		CLIENT_BUILDER.pop();

		COMMON_CONFIG = COMMON_BUILDER.build();
		CLIENT_CONFIG = CLIENT_BUILDER.build();
	}

	private static void setupSpawnConfig(ForgeConfigSpec.Builder COMMON_BUILDER) {
		COMMON_BUILDER.comment("Spawn settings").push(SUBCATEGORY_SPAWN);
		CAN_SPAWN_LM = COMMON_BUILDER.comment("Whether LittleMaid can spawn or not")
				.define("canSpawnLM", true);
		SPAWN_WEIGHT_LM = COMMON_BUILDER.comment("LittleMaid spawn weight")
				.defineInRange("spawnWeightLM", 5, 1, 50);
		SPAWN_LIMIT_LM = COMMON_BUILDER.comment("LittleMaid spawn limit")
				.defineInRange("spawnLimitLM", 20, 1, 200);
		SPAWN_MIN_GROUP_SIZE_LM = COMMON_BUILDER.comment("LittleMaid min group size")
				.defineInRange("spawnMinGroupSizeLM", 1, 1, 30);
		SPAWN_MAX_GROUP_SIZE_LM = COMMON_BUILDER.comment("LittleMaid max group size")
				.defineInRange("spawnMaxGroupSizeLM", 3, 1, 30);
		CAN_DESPAWN_LM = COMMON_BUILDER.comment("Whether LittleMaid can despawn or not")
				.define("canDespawnLM", false);
		COMMON_BUILDER.pop();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {

	}

	@SubscribeEvent
	public static void onReload(final ModConfig.Reloading configEvent) {
	}

}
