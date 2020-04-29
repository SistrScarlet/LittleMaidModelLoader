package net.sistr.lmml.util.loader;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.resources.data.PackMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


//外部から読み込んだリソースをマイクラに送るラッパー
public class ResourceWrapper implements IResourcePack {
    public static final ResourceWrapper INSTANCE = new ResourceWrapper();
    private static final PackMetadataSection PACK_INFO = new PackMetadataSection(new StringTextComponent("LittleMaidModelLoader!!!"), 5);
    //textures/entity/...と、c:\...mods\<pack_name>.zipまたは.jar
    protected static final HashMap<String, String> PATHS = Maps.newHashMap();

    @Override
    public InputStream getRootResourceStream(String fileName) throws IOException {
        return null;
    }

    //引数のResourceLocationはminecraft:textures/...の形式
    @Override
    public InputStream getResourceStream(ResourcePackType type, ResourceLocation location) throws IOException {
        String path = location.getPath();
        String packFullPath = PATHS.get(path);
        String zipPath = "assets/minecraft/".concat(location.getPath());
        //zipまたはjarを開き、pathと一致するものを探す
        ZipInputStream zipStream = new ZipInputStream(Files.newInputStream(Paths.get(packFullPath)));
        ZipEntry zipEntry;
        while ((zipEntry = zipStream.getNextEntry()) != null) {
            //assets/minecraft/textures...
            String zPath = zipEntry.getName().toLowerCase().replaceAll("[^A-Za-z0-9/._\\-]", "-");
            if (zipPath.equals(zPath)) {
                return zipStream;
            }
        }
        return null;
    }

    @Override
    public Collection<ResourceLocation> getAllResourceLocations(ResourcePackType type, String namespaceIn, String pathIn, int maxDepthIn, Predicate<String> filterIn) {
        List<ResourceLocation> list = Lists.newLinkedList();
        PATHS.forEach((key, value) -> list.add(new ResourceLocation("minecraft", key)));
        return list;
    }

    @Override
    public boolean resourceExists(ResourcePackType type, ResourceLocation location) {
        return PATHS.containsKey(location.getPath());
    }

    @Override
    public Set<String> getResourceNamespaces(ResourcePackType type) {
        return ImmutableSet.of("minecraft");
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
    public void close() throws IOException {

    }

    //pathはzip内のassetsから始まるテクスチャのパス
    //packFullPathはc:/から始まるパックのファイルパス
    public static void addTexturePath(String path, String packFullPath) {
        PATHS.put(path.toLowerCase().replace("assets/minecraft/", ""), packFullPath.toLowerCase());
    }

    public static ImmutableMap<String, String> getReadOnlyPaths() {
        return ImmutableMap.copyOf(PATHS);
    }


}
