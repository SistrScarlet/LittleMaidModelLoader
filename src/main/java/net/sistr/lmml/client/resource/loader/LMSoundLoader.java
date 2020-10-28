package net.sistr.lmml.client.resource.loader;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.sistr.lmml.client.ResourceWrapper;
import net.sistr.lmml.client.resource.manager.LMSoundManager;
import net.sistr.lmml.config.LMMLConfig;
import net.sistr.lmml.resource.loader.LMLoader;
import net.sistr.lmml.resource.util.ResourceHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Path;

//サーバーでは読み込む必要性が無いため読み込まない
@OnlyIn(Dist.CLIENT)
public class LMSoundLoader implements LMLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private final LMSoundManager soundManager;

    public LMSoundLoader(LMSoundManager soundManager) {
        this.soundManager = soundManager;
    }

    @Override
    public boolean canLoad(String path, Path homePath, InputStream inputStream, boolean isArchive) {
        return path.endsWith(".ogg") && ResourceHelper.getParentFolderName(path).isPresent();
    }

    @Override
    public void load(String path, Path homePath, InputStream inputStream, boolean isArchive) {
        String packName = ResourceHelper.getParentFolderName(path)
                .orElseThrow(() -> new IllegalArgumentException("引数が不正です。"));
        String fileName = getFileName(path);
        ResourceLocation location = getLocation(packName, fileName);
        fileName = ResourceHelper.removeExtension(fileName);
        fileName = ResourceHelper.removeNameLastIndex(fileName);
        soundManager.addSound(packName, fileName, location);
        ResourceWrapper.addResourcePath(location, path, homePath, isArchive);
        if (LMMLConfig.isDebugMode()) LOGGER.debug("Loaded Sound : " + packName + " : " + fileName);
    }

    /**
     * ResourceWrapperに登録するファイル名を取得する。
     * */
    public ResourceLocation getLocation(String packName, String fileName) {
        packName = packName.toLowerCase().replaceAll("[^a-z0-9/._\\-]", "-");
        fileName = fileName.toLowerCase().replaceAll("[^a-z0-9/._\\-]", "-");
        return new ResourceLocation("littlemaidmodelloader", packName + "/" + fileName);
    }

    public String getFileName(String path) {
        int lastSplitter = path.lastIndexOf('/');
        if (lastSplitter == -1) return path;
        return path.substring(lastSplitter + 1);
    }

}
