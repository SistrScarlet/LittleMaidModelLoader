package net.sistr.lmml.util.manager;

import net.blacklab.lmr.entity.maidmodel.IHasMultiModel;
import net.blacklab.lmr.entity.maidmodel.ModelMultiBase;
import net.blacklab.lmr.entity.maidmodel.TextureBox;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.sistr.lmml.LittleMaidModelLoader;
import net.sistr.lmml.config.LMRConfig;
import net.sistr.lmml.util.loader.LMMultiModelHandler;
import net.sistr.lmml.util.loader.LMTextureHandler;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.Map.Entry;

public class ModelManager {

    /**
     * 継承クラスで置き換えることを考慮。
     */
    public static ModelManager instance = new ModelManager();

    public static String defaultModelName = "Orign";

    public static final int tx_oldwild = 0x10; //16;
    public static final int tx_oldarmor1 = 0x11; //17;
    public static final int tx_oldarmor2 = 0x12; //18;
    public static final int tx_oldeye = 0x13; //19;
    public static final int tx_gui = 0x20; //32;
    public static final int tx_wild = 0x30; //48;
    public static final int tx_armor1 = 0x40; //64;
    public static final int tx_armor2 = 0x50; //80;
    public static final int tx_eye = 0x60; //96;
    public static final int tx_eyecontract = 0x60; //96;
    public static final int tx_eyewild = 0x70; //112;
    public static final int tx_armor1light = 0x80; //128;
    public static final int tx_armor2light = 0x90; //144;

    public static String[] armorFilenamePrefix = new String[]{
            "leather", "chainmail", "iron", "diamond", "gold"};

    /**
     * ローカルで保持しているモデルのリスト
     */
    protected Map<String, ModelMultiBase[]> modelMap = new TreeMap<>();

    /**
     * ローカルで保持しているテクスチャパック
     */
    private final List<TextureBox> textures = new ArrayList<>();

    /**
     * Entity毎にデフォルトテクスチャを参照。
     * 構築方法はEntityListを参照のこと。
     */
    protected Map<Class<?>, TextureBox> defaultTextures = new HashMap<>();

    /**
     * テクスチャ名称の一致する物を返す。
     */
    @Nullable
    public TextureBox getTextureBox(String pName) {
        for (TextureBox ltb : getTextureList()) {
            if (ltb.textureName.equals(pName)) {
                return ltb;
            }
        }
        return null;
    }

    public static List<TextureBox> getTextureList() {
        return instance.textures;
    }

    public void buildCrafterTexture() {
        // TODO:実験コード標準モデルテクスチャで構築
        TextureBox lbox = new TextureBox("Crafter_Steve", new String[]{"", "", ""});
        lbox.fileName = "";

        lbox.addTexture(0x0c, "/assets/minecraft/textures/entity/lmsteve/steve.png");
        if (armorFilenamePrefix != null && armorFilenamePrefix.length > 0) {
            for (String ls : armorFilenamePrefix) {
                Map<Integer, ResourceLocation> lmap = new HashMap<>();
                lmap.put(tx_armor1, new ResourceLocation(
                        "textures/models/armor/" + ls + "_layer_2.png"));
                lmap.put(tx_armor2, new ResourceLocation(
                        "textures/models/armor/" + ls + "_layer_1.png"));
                lbox.armors.put(ls, lmap);
            }
        }

        textures.add(lbox);
    }

    public TextureBox getNextPackege(TextureBox pNowBox, int pColor) {
        // 次のテクスチャパッケージの名前を返す
        boolean f = false;
        TextureBox lreturn = null;
        for (TextureBox ltb : getTextureList()) {
            if (ltb.hasColor(pColor)) {
                if (f) {
                    return ltb;
                }
                if (lreturn == null) {
                    lreturn = ltb;
                }
            }
            if (ltb == pNowBox) {
                f = true;
            }
        }
        return lreturn;
    }

    public TextureBox getPrevPackege(TextureBox pNowBox, int pColor) {
        // 前のテクスチャパッケージの名前を返す
        TextureBox lreturn = null;
        for (TextureBox ltb : getTextureList()) {
            if (ltb == pNowBox) {
                if (lreturn != null) {
                    break;
                }
            }
            if (ltb.hasColor(pColor)) {
                lreturn = ltb;
            }
        }
        return lreturn;
    }

    /**
     * ローカルで読み込まれているテクスチャパックの数。
     */
    public int getTextureCount() {
        return getTextureList().size();
    }

    public TextureBox getNextArmorPackege(TextureBox pNowBox) {
        // 次のテクスチャパッケージの名前を返す
        boolean f = false;
        TextureBox lreturn = null;
        for (TextureBox ltb : getTextureList()) {
            if (ltb.hasArmor()) {
                if (f) {
                    return ltb;
                }
                if (lreturn == null) {
                    lreturn = ltb;
                }
            }
            if (ltb == pNowBox) {
                f = true;
            }
        }
        return lreturn;
    }

    public TextureBox getPrevArmorPackege(TextureBox pNowBox) {
        // 前のテクスチャパッケージの名前を返す
        TextureBox lreturn = null;
        for (TextureBox ltb : getTextureList()) {
            if (ltb == pNowBox) {
                if (lreturn != null) {
                    break;
                }
            }
            if (ltb.hasArmor()) {
                lreturn = ltb;
            }
        }
        return lreturn;
    }

    /**
     * スポーン用モデル名をランダムに取得する
     */
    public String getRandomTextureString(Random pRand) {
        //return getRandomTexture(pRand).textureName;

        // 野生色があるものをリストアップ
        //List<TextureBox> llist = new ArrayList<>();
        //for (TextureBox lbox : this.textures) {
        //    if (lbox.getWildColorBits() > 0) {
        //        llist.add(lbox);
        //    }
        //}
        //防具モデルのみのモデルが選ばれる場合があるため若干変更
        while (true) {
            TextureBox wild = this.textures.get(pRand.nextInt(this.textures.size()));
            if (0 < wild.textures.size()) {
                return wild.textureName;
            }
        }
    }

    public void setDefaultTexture(Class<?> pEntityClass, TextureBox pBox) {
        defaultTextures.put(pEntityClass, pBox);
        LittleMaidModelLoader.Debug("appendDefaultTexture:%s(%s)",
                pEntityClass.getSimpleName(), pBox == null ? "NULL" : pBox.textureName);
    }

    /**
     * Entityに対応するデフォルトモデルを返す。
     */
    public TextureBox getDefaultTexture(IHasMultiModel pEntity) {
        return getDefaultTexture(pEntity.getClass());
    }

    public TextureBox getDefaultTexture(Class<?> pEntityClass) {
        if (defaultTextures.containsKey(pEntityClass)) {
            return defaultTextures.get(pEntityClass);
        }
        Class<?> lsuper = pEntityClass.getSuperclass();
        if (lsuper != null) {
            TextureBox lbox = getDefaultTexture(lsuper);
            if (lbox != null) {
                setDefaultTexture(pEntityClass, lbox);
            }
            return lbox;
        }
        return null;
    }

    /**
     * LMFileLoaderで読み込んだ情報をもとにメイドさんモデルをセットアップする
     */
    public void createLittleMaidModels() {

        LittleMaidModelLoader.LOGGER.info("createLittleMaidModels : start");

        //マルチモデル生成
        for (String key : LMMultiModelHandler.multiModelClassMap.keySet()) {

            Class<? extends ModelMultiBase> clazz;

            try {

                //マルチモデルクラスをインスタンス化
                clazz = LMMultiModelHandler.multiModelClassMap.get(key);

                ModelMultiBase[] mmBase = new ModelMultiBase[3];

                Constructor<? extends ModelMultiBase> constructorMMBase = clazz.getConstructor(float.class);
                mmBase[0] = constructorMMBase.newInstance(0.0F);
                float[] lsize = mmBase[0].getArmorModelsSize();
                mmBase[1] = constructorMMBase.newInstance(lsize[0]);
                mmBase[2] = constructorMMBase.newInstance(lsize[1]);

                modelMap.put(key, mmBase);

                //モデルロードメッセージ
                LittleMaidModelLoader.LOGGER.info(String.format("ModelManager-Load-MultiModel : %s", key));

            } catch (Exception e) {
                LittleMaidModelLoader.LOGGER.error(String.format("ModelManager-MultiModelInstanceException : %s", ""));
                if (LMRConfig.cfg_PrintDebugMessage) e.printStackTrace();
            }

        }

        //テクスチャを生成
        for (String key : LMTextureHandler.textureMap.keySet()) {

            //TextureBox生成
            TextureBox textureBox = new TextureBox(key, new String[]{"", "", ""});
            textures.add(textureBox);

            //TextureBoxにテクスチャを登録
            for (String texturePath : LMTextureHandler.textureMap.get(key)) {
                //bind用texture
                String bindTexturePath = texturePath.replace("assets/minecraft/", "");
                int colorIndex = LMTextureHandler.getColorIndex(texturePath);

                //colorごとに追加
                textureBox.addTexture(colorIndex, bindTexturePath);

            }

            //モデルロードメッセージ
            LittleMaidModelLoader.LOGGER.info(String.format("ModelManager-Load-Texture : %s", key));

        }

        //スティーブモデルの登録
        buildCrafterTexture();

        //TextureBoxとMultiModelの紐づけ
        //デフォルトモデル設定
        ModelMultiBase[] defaultModel = modelMap.get(defaultModelName);
        if (defaultModel == null && !modelMap.isEmpty()) {
            defaultModel = (ModelMultiBase[]) modelMap.values().toArray()[0];
        }

        //TextureBoxベースでメイドモデルを紐づける
        for (TextureBox textureBox : textures) {
            if (!textureBox.modelName.isEmpty()) {
                for (String key : modelMap.keySet()) {
                    //モデル名に一致するモデルをTextureBoxへ設定する
                    if (key.toLowerCase().equals(textureBox.modelName.toLowerCase())) {
                        textureBox.setModels(textureBox.modelName, modelMap.get(key), defaultModel);
                        break;
                    }
                }
            } else {
                //モデル名の設定がない場合は標準モデルを設定する
                textureBox.setModels(defaultModelName, null, defaultModel);
            }
        }

        //マルチモデル側からテクスチャを設定する場合に利用する
        //基本的に使用されていない
        for (Entry<String, ModelMultiBase[]> entryModelMap : modelMap.entrySet()) {
            String modelTexture = entryModelMap.getValue()[0].getUsingTexture();
            if (modelTexture != null) {
                if (getTextureBox(modelTexture + "_" + entryModelMap.getKey()) == null) {
                    TextureBox textureBox = null;
                    for (TextureBox ltb : textures) {
                        if (ltb.packegeName.equals(modelTexture)) {
                            textureBox = ltb;
                            break;
                        }
                    }
                    if (textureBox != null) {
                        textureBox = textureBox.duplicate();
                        textureBox.setModels(entryModelMap.getKey(), null, entryModelMap.getValue());
                        textures.add(textureBox);
                    }
                }
            }
        }

        //TextureBoxのモデルがnullのものを削除する
        textures.removeIf(textureBox -> textureBox.models == null);

        //野生持ちTextureBoxをdefaultModelに設定する
        for (TextureBox lbox : textures) {
            if (lbox.getWildColorBits() > 0) {
                setDefaultTexture(LivingEntity.class, lbox);
            }
        }

        //defaultモデルの設定
        setDefaultTexture(LivingEntity.class, getTextureBox("default_" + defaultModelName));

        LittleMaidModelLoader.LOGGER.info("createLittleMaidModels : end");

    }

}
