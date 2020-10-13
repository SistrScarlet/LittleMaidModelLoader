package net.sistr.lmml.resource.manager;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.sistr.lmml.config.LMMLConfig;
import net.sistr.lmml.util.ResourceHelper;
import net.sistr.lmml.util.TextureColor;
import net.sistr.lmml.util.TextureHolder;
import net.sistr.lmml.util.TextureIndexes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class TextureManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, TextureHolder> textures = new HashMap<>();

    public void addTexture(String fileName, String textureName, String modelName, int index, ResourceLocation texturePath) {
        TextureHolder textureHolder = textures.computeIfAbsent(textureName.toLowerCase(),
                k -> new TextureHolder(textureName, modelName));
        if (TextureIndexes.getTextureIndexes(index).isArmor()) {
            textureHolder.addArmorTexture(fileName.substring(0, fileName.indexOf('_')), index, texturePath);
        } else {
            textureHolder.addTexture(index, texturePath);
        }
        if (LMMLConfig.isDebugMode()) LOGGER.debug("Loaded Texture : " + texturePath);
    }

    public Optional<TextureHolder> getTexture(String textureName) {
        TextureHolder textureHolder = textures.get(textureName.toLowerCase());
        //サーバー側で読み込んでないテクスチャでもテクスチャ名だけは保持する
        if (textureHolder == null && FMLEnvironment.dist.isDedicatedServer()) {
            TextureHolder serverHolder = new TextureHolder(textureName, ResourceHelper.getModelName(textureName));
            textures.put(textureName.toLowerCase(), serverHolder);
            return Optional.of(serverHolder);
        }
        return Optional.ofNullable(textureHolder);
    }

    public Collection<TextureHolder> getAllTextures() {
        return textures.values();
    }
}
