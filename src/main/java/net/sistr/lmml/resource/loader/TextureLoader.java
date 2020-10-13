package net.sistr.lmml.resource.loader;

import net.minecraft.util.ResourceLocation;
import net.sistr.lmml.resource.ResourceWrapper;
import net.sistr.lmml.util.ResourceHelper;
import net.sistr.lmml.resource.manager.TextureManager;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 画像ファイルを読み込み、ゲームに登録するクラス
 */
public class TextureLoader implements ILoader {
    private final TextureManager textureManager;
    private final HashMap<String, String> converter = new HashMap<>();

    public TextureLoader(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public void addPathConverter(String target, String to) {
        converter.put(target, to);
    }

    @Override
    public boolean canLoad(String path, Path homePath, InputStream inputStream, boolean isArchive) {
        return path.endsWith(".png") && ResourceHelper.getTextureName(path) != null
                && ResourceHelper.getIndex(path) != -1;
    }

    @Override
    public void load(String path, Path homePath, InputStream inputStream, boolean isArchive) {
        ResourceLocation texturePath = getResourceLocation(path);
        String textureName = ResourceHelper.getTextureName(path);
        assert textureName != null;//canProcessでチェックしてるのでnullはありえない
        String modelName = ResourceHelper.getModelName(textureName);
        textureManager.addTexture(ResourceHelper.getFileName(path), textureName, modelName,
                ResourceHelper.getIndex(path), texturePath);
        ResourceWrapper.addResourcePath(texturePath, path, homePath, isArchive);
    }

    /**
     * パスをテクスチャのパスに変換
     * assets/minecraft/textures/entity/littlemaid/[texture]_[model]/xxxx_[index].png
     * または
     * mob/littlemaid/[texture]_[model]/xxxx_[index].png
     * を
     * minecraft:textures.entity.littlemaid.[texture]_[model].xxxx_[index].png
     * に変換
     */
    @Nullable
    private ResourceLocation getResourceLocation(String path) {
        //小文字にする
        String texturePath = path.toLowerCase();
        //すべてminecraftから始まるように変換
        for (Map.Entry<String, String> entry : converter.entrySet()) {
            texturePath = texturePath.replace(entry.getKey(), entry.getValue());
        }
        //minecraft/なら9
        int firstSplitter = texturePath.indexOf('/');
        //ファイル階層が無い場合はnullを返す
        if (firstSplitter == -1) {
            return null;
        }
        //使用不能文字を変換
        texturePath = texturePath.replaceAll("[^a-z0-9/._\\-]", "-");

        String nameSpace = texturePath.substring(0, firstSplitter);
        String namePath = texturePath.substring(firstSplitter + 1);
        return new ResourceLocation(nameSpace, namePath);
    }
}
