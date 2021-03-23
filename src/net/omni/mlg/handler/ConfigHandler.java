package net.omni.mlg.handler;

import net.omni.mlg.OmniMLGPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ConfigHandler {
    private final OmniMLGPlugin plugin;

    public ConfigHandler(OmniMLGPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(Player player) {
        if (getPersonalBest(player.getName()) <= 0)
            setPersonalBest(player.getName(), 0);
    }

    public void finish(Player player) {
        int level = plugin.getLevelHandler().getLevel(player);
        int personal_best = getPersonalBest(player.getName());

        setFinished(player.getName(), level);

        // level is greater than personal best
        if (level > personal_best)
            setPersonalBest(player.getName(), level);
    }

    public int getPersonalBest(String name) {
        return isInConfig(name) ? plugin.getConfig().getInt("personal_best." + name) : 0;
    }

    /**
     * Returns the time from config.
     *
     * @param name - {@code String} to check from config
     * @return {@code Integer} the time from config
     */
    public int getLevelInConfig(String name) {
        return isInConfig(name) ? plugin.getConfig().getInt("finished." + name) : 0;
    }

    /**
     * Use this only if the player is in config.
     *
     * @param name - {@code String} of to check
     * @return true - if name is in config
     */
    public boolean isInConfig(String name) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("finished");

        if (section == null)
            return false;

        for (String key : section.getKeys(false)) {
            if (key == null)
                continue;

            if (key.equalsIgnoreCase(name))
                return true;
        }

        return false;
    }

    public void setPlayers(String name, int level) {
        set("players", name, level);
    }

    public void setFinished(String name, int level) {
        set("finished", name, level);
    }

    public void setPersonalBest(String name, int level) {
        set("personal_best", name, level);
    }

    public void set(String path, String name, int level) {
        plugin.getConfig().set(path + "." + name, level);
        plugin.saveConfig();
    }
}
