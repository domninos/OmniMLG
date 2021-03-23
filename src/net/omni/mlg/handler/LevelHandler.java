package net.omni.mlg.handler;

import net.omni.mlg.OmniMLGPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LevelHandler {
    private final OmniMLGPlugin plugin;
    private final ConcurrentMap<Player, Integer> playerLevels = new ConcurrentHashMap<>();
    private final Map<Player, Integer> playerItems = new HashMap<>();

    public LevelHandler(OmniMLGPlugin plugin) {
        this.plugin = plugin;
    }

    public int update(Player player) {
        if (!hasLevels(player))
            setLevel(player, 0);

        int itemLevel = playerItems.get(player);

        if (itemLevel > (plugin.getItemHandler().size() - 1)) {
            itemLevel = 0;
            playerItems.put(player, 0);
        }

        return itemLevel;
    }

    public boolean hasLevels(Player player) {
        return playerLevels.containsKey(player);
    }

    public int getLevel(Player player) {
        return playerLevels.getOrDefault(player, 0);
    }

    public void setLevel(Player player, int level) {
        playerLevels.put(player, level);

        if (!playerItems.containsKey(player))
            playerItems.put(player, 0);
        else
            playerItems.put(player, playerItems.get(player) + 1);

        plugin.getPotionHandler().addEffects(player, level);
    }

    public void removeLevels(Player player) {
        plugin.getConfigHandler().finish(player);

        playerLevels.remove(player);
        playerItems.remove(player);
    }

    public void flush() {
        playerLevels.clear();
        playerItems.clear();
    }
}
