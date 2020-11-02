package net.sistr.lmml.client;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.resources.data.PackMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


//外部から読み込んだリソースをマイクラに送るラッパー
@OnlyIn(Dist.CLIENT)
public class ResourceWrapper implements IResourcePack {
    public static final ResourceWrapper INSTANCE = new ResourceWrapper();
    public static final PackMetadataSection PACK_INFO =
            new PackMetadataSection(new StringTextComponent("LittleMaidModelLoader!!!"), 6);
    protected static final HashMap<ResourceLocation, Resource> PATHS = Maps.newHashMap();

    //いつ呼ばれるのか不明
    //少なくとも起動時とリロード時は動かず
    @Override
    public InputStream getRootResourceStream(String fileName) throws IOException {
        return null;
    }

    //引数のResourceLocationはminecraft:textures/...の形式
    @Override
    public InputStream getResourceStream(ResourcePackType type, ResourceLocation location) throws IOException {
        return PATHS.get(location).getInputStream();
    }

    //リロード時に呼ばれる
    //pathInが重要で、これと一致するリソースのみ渡すこと
    //そうでないと画像リソースがフォントに飛んでってクソ時間を食う
    @Override
    public Collection<ResourceLocation> getAllResourceLocations(ResourcePackType type, String namespaceIn,
                                                                String pathIn, int maxDepthIn, Predicate<String> filterIn) {
        return PATHS.keySet().stream()
                .filter(location -> location.getPath().startsWith(pathIn))
                .filter(location -> filterIn.test(location.getPath()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean resourceExists(ResourcePackType type, ResourceLocation location) {
        return PATHS.containsKey(location);
    }

    //初期化時に読み込まれる
    @Override
    public Set<String> getResourceNamespaces(ResourcePackType type) {
        return Sets.newHashSet("littlemaidmodelloader");
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getMetadata(IMetadataSectionSerializer<T> deserializer) throws IOException {
        if (deserializer.getSectionName().equals("pack")) {
            return (T) PACK_INFO;
        }
        return null;
    }

    @Override
    public String getName() {
        return "LMModelLoader";
    }

    @Override
    public void close() {

    }

    public static void addResourcePath(ResourceLocation resourcePath, String path, Path homePath, boolean isArchive) {
        PATHS.put(resourcePath, new Resource(path, homePath, isArchive));
    }

    private static class Resource {
        private final String path;
        private final Path homePath;
        private final boolean isArchive;

        private Resource(String path, Path homePath, boolean isArchive) {
            this.path = path;
            this.homePath = homePath;
            this.isArchive = isArchive;
        }

        public InputStream getInputStream() throws IOException {
            if (isArchive) {
                String resourcePath = homePath.toString();
                ZipFile zipfile = new ZipFile(resourcePath);
                ZipEntry zipentry = zipfile.getEntry(path);
                if (zipentry != null) {
                    return zipfile.getInputStream(zipentry);
                }
                //上記より遅い
                /*ZipInputStream zipStream = new ZipInputStream(Files.newInputStream(homePath));
                ZipEntry entry;
                while ((entry = zipStream.getNextEntry()) != null) {
                    if (entry.getName().equals(path)) {
                        return zipStream;
                    }
                }*/
                throw new NoSuchFileException(path);
            } else {
                return Files.newInputStream(Paths.get(homePath.toString(), path));
            }
        }

    }

}
