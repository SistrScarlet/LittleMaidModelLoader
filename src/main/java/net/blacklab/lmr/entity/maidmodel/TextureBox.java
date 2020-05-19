package net.blacklab.lmr.entity.maidmodel;

import net.minecraft.item.IArmorMaterial;
import net.minecraft.util.ResourceLocation;
import net.sistr.lmml.util.manager.ModelManager;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class TextureBox extends TextureBoxBase {

	/**
	 * テクスチャパックの名称、モデル指定詞の前までの文字列。
	 */
	public String packegeName;
	/**
	 * テクスチャファイルのファイル名リスト。
	 */
	public Map<Integer, ResourceLocation> textures;
	/**
	 * アーマーファイルのファイル名リスト。
	 */
	public Map<String, Map<Integer, ResourceLocation>> armors;
	/**
	 * モデル指定詞
	 */
	public String modelName;
	/**
	 * マルチモデルクラス
	 */
	public ModelMultiBase[] models;
	/**
	 * テクスチャの格納されているパックの名前（モデルに関係なし）
	 */
	public String fileName;



	public TextureBox() {
		textures = new HashMap<>();
		armors = new TreeMap<>();
		modelHeight = modelWidth = modelYOffset = modelMountedYOffset = 0.0F;
		contractColor = -1;
		wildColor = -1;
	}

	public TextureBox(String pTextureName, String[] pSearch) {
		this();
		textureName = pTextureName;
		fileName = pTextureName;
		int li = pTextureName.lastIndexOf("_");
		if (li > -1) {
			packegeName = pTextureName.substring(0, li);
			modelName = pTextureName.substring(li + 1);
		} else {
			packegeName = pTextureName;
			modelName = "";
		}
		//textureDir = pSearch;
	}

	public void setModels(String pModelName, ModelMultiBase[] pModels, ModelMultiBase[] pDefModels) {
		modelName = pModelName;
		models = pModels == null ? pDefModels : pModels;
		textureName = packegeName + "_" + modelName;
		isUpdateSize = (models != null && models[0] != null) && ModelCapsHelper.getCapsValueBoolean(models[0], IModelCaps.caps_isUpdateSize);
	}

	/**
	 * テクスチャのフルパスを返す。
	 * 登録インデックスが無い場合はNULLを返す。
	 */
	public ResourceLocation getTextureName(int pIndex) {
		if (textures.containsKey(pIndex)) {
			return textures.get(pIndex);
		} else if (pIndex >= ModelManager.tx_eyecontract && pIndex < (16 + ModelManager.tx_eyecontract)) {
			return getTextureName(ModelManager.tx_oldeye);
		} else if (pIndex >= ModelManager.tx_eyewild && pIndex < (16 + ModelManager.tx_eyewild)) {
			return getTextureName(ModelManager.tx_oldeye);
		}
		return null;
	}

	public ResourceLocation getArmorTextureName(int pIndex) {
		return getArmorTextureName(pIndex, ModelManager.armorFilenamePrefix[3], 0);
	}

	public static int getArmorRenderIndex(IArmorMaterial armorMaterial) {
		int index = 0;
		String name = armorMaterial.getName();
		switch (name) {
			case "chainmail":
				index = 1;
				break;
			case "iron":
				index = 2;
				break;
			case "diamond":
				index = 3;
				break;
			case "gold":
				index = 4;
				break;
		}
		return index;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ResourceLocation getArmorTextureName(int pIndex, String pArmorPrefix, int pDamage) {
		// indexは0x40,0x50番台
		if (armors.isEmpty() || pArmorPrefix == null) return null;

		Map<Integer, ResourceLocation> m = armors.get(pArmorPrefix);
		if (m == null) {
			m = armors.get("default");
			if (m == null) {
//				return null;
				m = (Map)armors.values().toArray()[0];
			}
		}
		ResourceLocation ls = null;
//		int lindex = pInner ? MMM_TextureManager.tx_armor1 : MMM_TextureManager.tx_armor2;
		for (int i = pIndex + pDamage; i >= pIndex; i--) {
			ls = m.get(i);
			if (ls != null) break;
		}
		return ls;
	}

	/**
	 * 契約色の有無をビット配列にして返す
	 */
	@Override
	public int getContractColorBits() {
		if (contractColor == -1) {
			int li = 0;
			for (Integer i : textures.keySet()) {
				if (i >= 0x00 && i <= 0x0f) {
					li |= 1 << (i & 0x0f);
				}
			}
			contractColor = li;
		}
		return contractColor;
	}
	/**
	 * 野生色の有無をビット配列にして返す
	 */
	@Override
	public int getWildColorBits() {
		if (wildColor == -1) {
			int li = 0;
			for (Integer i : textures.keySet()) {
				if (i >= ModelManager.tx_wild && i <= (ModelManager.tx_wild + 0x0f)) {
					li |= 1 << (i & 0x0f);
				}
			}
			wildColor = li;
		}
		return wildColor;
	}

	public boolean hasColor(int pIndex) {
		return textures.containsKey(pIndex);
	}

	public boolean hasColor(int pIndex, boolean pContract) {
		return textures.containsKey(pIndex + (pContract ? 0 : ModelManager.tx_wild));
	}

	public boolean hasArmor() {
		return !armors.isEmpty();
	}

	@Override
	public float getHeight(IModelCaps pEntityCaps) {
		return models != null ? models[0].getHeight(pEntityCaps) : modelHeight;
	}

	@Override
	public float getWidth(IModelCaps pEntityCaps) {
		return models != null ? models[0].getWidth(pEntityCaps) : modelWidth;
	}

	@Override
	public float getYOffset(IModelCaps pEntityCaps) {
		return models != null ? models[0].getyOffset(pEntityCaps) : modelYOffset;
	}

	@Override
	public float getMountedYOffset(IModelCaps pEntityCaps) {
		return models != null ? models[0].getMountedYOffset(pEntityCaps) : modelMountedYOffset;
	}

	public TextureBox duplicate() {
		TextureBox lbox = new TextureBox();
		lbox.textureName = textureName;
		lbox.packegeName = packegeName;
		lbox.fileName = fileName;
		lbox.modelName = modelName;
		//lbox.textureDir = textureDir;
		lbox.textures = textures;
		lbox.armors = armors;
		lbox.models = models;

		return lbox;
	}

	public boolean addTexture(int pIndex, String pLocation) {
		String ls = "/assets/minecraft/";
		if (pLocation.startsWith(ls)) {
			pLocation = pLocation.substring(ls.length());
		}
		switch ((pIndex & 0xfff0)) {
		case ModelManager.tx_armor1:
		case ModelManager.tx_armor2:
		case ModelManager.tx_armor1light:
		case ModelManager.tx_armor2light:
		case ModelManager.tx_oldarmor1:
		case ModelManager.tx_oldarmor2:
			ls = pLocation.substring(pLocation.lastIndexOf("/") + 1, pLocation.lastIndexOf("_"));
			Map<Integer, ResourceLocation> lmap;
			if (armors.containsKey(ls)) {
				lmap = armors.get(ls);
			} else {
				lmap = new HashMap<>();
				armors.put(ls, lmap);
			}
			lmap.put(pIndex, new ResourceLocation(pLocation.toLowerCase(Locale.ROOT)));
			break;

		default:
			textures.put(pIndex, new ResourceLocation(pLocation.toLowerCase(Locale.ROOT)));
			return true;
		}
		return false;
	}

	public ResourceLocation getTextureNameDefault() {
		for (Integer index : textures.keySet()) {
			return textures.get(index);
		}
		return null;
	}

}
