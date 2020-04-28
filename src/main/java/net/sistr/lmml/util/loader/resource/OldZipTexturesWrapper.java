package net.sistr.lmml.util.loader.resource;

import com.google.common.collect.ImmutableSet;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.sistr.lmml.config.LMRConfig;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Predicate;

/**
 * 旧方式テクスチャのロード用リソースパック
 *
 * @author firis-games
 */
public class OldZipTexturesWrapper implements IResourcePack {

    protected static ArrayList<String> keys = new ArrayList<>();

    @Override
    public InputStream getRootResourceStream(String fileName) throws IOException {
        return null;
    }

    @Override
    public InputStream getResourceStream(ResourcePackType type, ResourceLocation location) throws IOException {
        if (resourceExists(type, location)) {
            String key = texturesResourcePath(location);
            key = containsKey(key);
            InputStream stream = getInputStreamFromResoucepacks(key);
            if (stream == null) {
                stream = getClass().getClassLoader().getResourceAsStream(key);
            }
            return stream;
        }
        return null;
    }

    @Override
    public Collection<ResourceLocation> getAllResourceLocations(ResourcePackType type, String namespaceIn, String pathIn, int maxDepthIn, Predicate<String> filterIn) {
        return null;
    }

    @Override
    public boolean resourceExists(ResourcePackType type, ResourceLocation location) {
        String key = texturesResourcePath(location);
        if (key == null) {
            return false;
        }

        return containsKey(key) != null;
    }

    @Override
    public Set<String> getResourceNamespaces(ResourcePackType type) {
        return ImmutableSet.of("minecraft");
    }

    @Nullable
    @Override
    public <T> T getMetadata(IMetadataSectionSerializer<T> deserializer) throws IOException {
        return null;
    }

    @Override
    public String getName() {
        return "OldTexturesLoader";
    }

    @Override
    public void close() throws IOException {

    }

    /**
     * テクスチャパックのリソースパスへ変換する
     *
     * @param path
     * @return
     */
    public String texturesResourcePath(ResourceLocation path) {

        String key = path.getNamespace();

        if (!key.endsWith(".png")) return null;

        if (key.startsWith("/")) key = key.substring(1);

        //旧式用の判定処理
        if (key.toLowerCase().startsWith("mob/modelmulti")
                || key.toLowerCase().startsWith("mob/littlemaid")) {
            //旧方式は何も加工しない
        } else {
            key = "assets/minecraft/" + key;
        }

        return key;
    }

    /**
     * テクスチャリストの中に対象テクスチャが含まれるかチェックする
     * 大文字小文字は区別しない
     *
     * @param path
     * @return
     */
    public String containsKey(String path) {

        String ret = null;

        for (String key : keys) {
            if (key.toLowerCase().equals(path.toLowerCase())) {
                ret = key;
                break;
            }
        }

        return ret;

    }

    /**
     * Textureを追加する
     *
     * @param texture
     */
    public static void addTexturePath(String texture) {
        keys.add(texture);
    }

    /**
     * 機能が有効な場合
     *
     * @return
     * @throws IOException
     */
    protected InputStream getInputStreamFromResoucepacks(String key) throws IOException {

        //設定が無効の場合は何もしない
        if (!LMRConfig.cfg_loader_texture_load_from_resoucepack) return null;

        //設定系の初期化
        createResourcepacksConfig();

        Path path = Paths.get(resourcepacksPath.toString(), key);
        if (Files.exists(path)) {
            return Files.newInputStream(path);
        }
        return null;
    }

    private static final Path resourcepacksPath = Paths.get("resourcepacks", "LittleMaidResource");

    /**
     * テクスチャの直接読込処理用設定を作成する
     */
    public static void createResourcepacksConfig() {

        //設定が無効の場合は何もしない
        if (!LMRConfig.cfg_loader_texture_load_from_resoucepack) return;

        //フォルダを作成する
        Path basePath = resourcepacksPath;

        //ベースフォルダが存在しない場合
        if (Files.notExists(basePath)) {
            //必要ファイルを作成する
            try {
                //ベースフォルダ作成
                Files.createDirectories(basePath);

                //作業用パス作成
                Path workPath = Paths.get(basePath.toString(), "assets/minecraft/textures/entity");
                Files.createDirectories(workPath);

                //リソースパック用設定ファイル出力
                Path packmetaPath = Paths.get(basePath.toString(), "pack.mcmeta");
                String packmeta = "{\"pack\": {\"pack_format\": 3,\"description\": \"LittleMaidReengaged Developer Resourcepack\"}}";
                Files.write(packmetaPath, Collections.singletonList(packmeta), StandardCharsets.UTF_8, StandardOpenOption.CREATE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
