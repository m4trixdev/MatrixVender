package br.com.matrixdev.matrixvender.utils;

import br.com.matrixdev.matrixvender.MatrixVender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigUtil {

    private final MatrixVender plugin;
    private final String fileName;
    private File configFile;
    private FileConfiguration config;

    public ConfigUtil(MatrixVender plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        loadConfig();
    }

    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), fileName);
        }
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Nao foi possivel salvar " + fileName + ": " + e.getMessage());
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public File getConfigFile() {
        return configFile;
    }
}
