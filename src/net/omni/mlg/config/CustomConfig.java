package net.omni.mlg.config;

import net.omni.mlg.OmniMLGPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CustomConfig {

    private final File file;
    private FileConfiguration config;

    public CustomConfig(OmniMLGPlugin plugin, String fileName) {
        this(plugin, fileName, plugin.getDataFolder());
    }

    public CustomConfig(OmniMLGPlugin plugin, String fileName, File directory) {
        if (!fileName.endsWith(".yml"))
            fileName += ".yml";

        this.file = new File(directory, fileName);

        if (!file.exists()) {
            try {
                if (file.createNewFile())
                    plugin.sendConsole("&aSuccessfully created " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        reload();
    }

    public void set(String path, Object obj, boolean save) {
        config.set(path, obj);

        if (save)
            save();
    }

    public void setNoSave(String path, Object obj) {
        set(path, obj, false);
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public void reload() {
        this.config = YamlConfiguration.loadConfiguration(file);
    }
}
