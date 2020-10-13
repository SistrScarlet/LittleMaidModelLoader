package net.sistr.lmml.resource.loader;

import net.minecraft.util.ResourceLocation;
import net.sistr.lmml.config.LMMLConfig;
import net.sistr.lmml.resource.ResourceWrapper;
import net.sistr.lmml.resource.manager.SoundManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.file.Path;

public class SoundLoader implements ILoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private final SoundManager soundManager;

    public SoundLoader(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    @Override
    public boolean canLoad(String path, Path homePath, InputStream inputStream, boolean isArchive) {
        return path.endsWith(".ogg");
    }

    @Override
    public void load(String path, Path homePath, InputStream inputStream, boolean isArchive) {
        String packName = getSoundPackName(path, homePath, isArchive);
        if (packName == null) return;
        String fileName = getFileName(path);
        ResourceLocation location = getLocation(packName, fileName);
        soundManager.addSound(packName, fileName, location);
        ResourceWrapper.addResourcePath(location, path, homePath, isArchive);
        if (LMMLConfig.isDebugMode()) LOGGER.debug("Loaded Sound : " + packName + " : " + fileName);
    }

    public ResourceLocation getLocation(String packName, String fileName) {
        packName = packName.toLowerCase().replaceAll("[^a-z0-9/._\\-]", "-");
        fileName = fileName.toLowerCase().replaceAll("[^a-z0-9/._\\-]", "-");
        return  new ResourceLocation("littlemaidmodelloader", packName + "/" + fileName);
    }

    public String getFileName(String path) {
        int lastSplitter = path.lastIndexOf('/');
        if (lastSplitter == -1) return path;
        return path.substring(lastSplitter + 1);
    }

    @Nullable
    public String getSoundPackName(String path, Path homePath, boolean isArchive) {
        if (isArchive) {
            //zipファイル名を取る
            String zipName = homePath.getFileName().toString();
            return zipName.substring(0, zipName.lastIndexOf('.'));
        } else {
            //一番外側のフォルダの名前を取る
            int firstSplitter = path.indexOf("/");
            if (firstSplitter == -1) return null;
            return path.substring(0, firstSplitter);
        }
    }

}
