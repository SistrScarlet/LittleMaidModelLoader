package net.sistr.lmml.resource.manager;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final Map<String, Map<String, Map<String, String>>> configs = new HashMap<>();

    public void addConfig(String packName, String fileName, Map<String, String> settings) {
        Map<String, Map<String, String>> config = configs.computeIfAbsent(packName, k -> new HashMap<>());
        config.computeIfAbsent(fileName, k -> new HashMap<>())
                .putAll(settings);
    }
}
