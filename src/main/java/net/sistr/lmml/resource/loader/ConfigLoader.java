package net.sistr.lmml.resource.loader;

import net.sistr.lmml.config.LMMLConfig;
import net.sistr.lmml.resource.manager.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ConfigLoader implements ILoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ConfigManager configManager;

    public ConfigLoader(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public boolean canLoad(String path, Path homePath, InputStream inputStream, boolean isArchive) {
        return path.endsWith(".cfg");
    }

    @Override
    public void load(String path, Path homePath, InputStream inputStream, boolean isArchive) {
        Map<String, String> settings = new HashMap<>();
        try {
            getTextStream(inputStream).forEach(s -> addSettings(settings, s));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        String packName = getPackName(path, homePath, isArchive);
        String fileName = getFileName(path);
        configManager.addConfig(packName, fileName, settings);
        if (LMMLConfig.isDebugMode())
            LOGGER.debug("Loaded Config : " + packName + " : " + fileName + " : Total " + settings.size());
    }

    public void addSettings(Map<String, String> settings, String text) {
        int firstComment = text.indexOf('#');
        if (firstComment != -1)
            text = text.substring(0, firstComment);
        int firstSplitter = text.indexOf('=');
        if (firstSplitter == -1) return;
        String firstText = text.substring(0, firstSplitter);
        String secondText = text.substring(firstSplitter + 1);
        settings.put(firstText, secondText);
    }

    public Stream<String> getTextStream(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.lines();
    }

    @Nullable
    public String getPackName(String path, Path homePath, boolean isArchive) {
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

    public String getFileName(String path) {
        int lastSplitter = path.lastIndexOf('/');
        if (lastSplitter == -1) return path;
        return path.substring(lastSplitter + 1);
    }

}
