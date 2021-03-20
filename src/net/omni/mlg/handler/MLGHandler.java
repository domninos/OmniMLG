package net.omni.mlg.handler;

import net.omni.mlg.OmniMLGPlugin;
import net.omni.mlg.schematic.MLGSchematic;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MLGHandler {

    private final OmniMLGPlugin plugin;
    private final Set<MLGSchematic> mlgSchematics = new HashSet<>();
    private final Map<Player, MLGSchematic> playerMLG = new HashMap<>();
    private final Map<Player, List<Object>> playerPlaced = new HashMap<>();
    private final File mlgDir;

    public MLGHandler(OmniMLGPlugin plugin) {
        this.plugin = plugin;
        this.mlgDir = new File(plugin.getDataFolder(), "MLG");

        if (!mlgDir.exists()) {
            if (mlgDir.mkdirs())
                plugin.sendConsole("&aSuccessfully created MLG directory");
        }
    }

    public void loadMLGs() {
        flush();

        try {
            World schemWorld = plugin.getSchematicHandler().getSchematicWorld();

            if (schemWorld == null) {
                plugin.sendConsole("&cCould not load mlgs because schematic world is not found.");
                return;
            }

            Files.list(mlgDir.toPath()).map(Path::toFile)
                    .filter(file -> file.getName().endsWith(".yml"))
                    .forEach(file -> {
                        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

                        String worldName = config.getString("world");
                        int x = config.getInt("spawn.x");
                        int y = config.getInt("spawn.y");
                        int z = config.getInt("spawn.z");

                        MLGSchematic mlgSchematic = new MLGSchematic(plugin, worldName, x, y, z);

                        mlgSchematic.loadConfig(file.getName());

                        add(mlgSchematic);

                        plugin.sendConsole("&aLoaded " + file.getName());
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPlayer(Player player) {
        MLGSchematic mlgSchematic = getAvailableMLG();

        if (mlgSchematic == null) {
            plugin.sendConsole("&cCould not find an available mlg.");
            return;
        }

        plugin.getItemHandler().clear(player);

        mlgSchematic.occupy(true);

        player.teleport(mlgSchematic.getSpawn());

        new BukkitRunnable() {
            int count = 3;

            @Override
            public void run() {
                if (!mlgSchematic.isOccupied()) {
                    cancel();
                    return;
                }

                if (count > 0)
                    player.sendTitle(ChatColor.GOLD + "Starting in ",
                            ChatColor.GOLD + String.valueOf(count--), 10, 10, 10);
                else {
                    plugin.getItemHandler().giveMLGItems(player);
                    mlgSchematic.start();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        playerMLG.put(player, mlgSchematic);
    }

    public void removePlayer(Player player) {
        MLGSchematic mlgSchematic = getMLGFromPlayer(player);

        if (mlgSchematic == null) {
            plugin.sendConsole("&cCould not find mlg from " + player.getName());
            return;
        }

        plugin.getItemHandler().clear(player);

        mlgSchematic.occupy(false);

        mlgSchematic.reset();

        playerMLG.remove(player);
        plugin.getItemHandler().removeLevels(player);
        clear(player);
    }

    public MLGSchematic getMLGFromPlayer(Player player) {
        return playerMLG.getOrDefault(player, null);
    }

    public boolean inMLG(Player player) {
        return playerMLG.containsKey(player);
    }

    public MLGSchematic getAvailableMLG() {
        Optional<MLGSchematic> optional = mlgSchematics.stream().filter(lavaPool -> !lavaPool.isOccupied())
                .findFirst();

        if (optional.isPresent())
            return optional.get();
        else {
            Location randomLocationOrigin = plugin.getSchematicHandler().
                    getRandomLocation(plugin.getSchematicHandler().getSchematicWorld().getSpawnLocation(),
                            100);

            Location randomLocation = plugin.getSchematicHandler().
                    getRandomLocation(randomLocationOrigin, 1000);

            randomLocation.setY(plugin.getSchematicHandler().getSchematicWorld()
                    .getHighestBlockYAt(randomLocation) + 30);

            int x = randomLocation.getBlockX();
            int y = randomLocation.getBlockY();
            int z = randomLocation.getBlockZ();

            return plugin.getSchematicHandler().pasteSchem(x, y, z);
        }
    }

    public void add(MLGSchematic mlgSchematic) {
        this.mlgSchematics.add(mlgSchematic);
    }

    public void add(Player player, Block block) {
        List<Object> placed = getPlaced(player);

        if (placed == null)
            placed = new ArrayList<>();

        placed.add(block);

        playerPlaced.put(player, placed);
    }

    public void add(Player player, Entity entity) {
        List<Object> placed = getPlaced(player);

        if (placed == null)
            placed = new ArrayList<>();

        placed.add(entity);

        playerPlaced.put(player, placed);
    }

    public void clear(Player player) {
        if (!playerPlaced.containsKey(player))
            return;

        List<Object> placed = getPlaced(player);

        for (Object object : placed) {
            if (object instanceof Block) {
                Block block = (Block) object;

                if (block.getType() != Material.AIR)
                    block.setType(Material.AIR);
            } else if (object instanceof Entity) {
                Entity entity = (Entity) object;

                entity.remove();
            }
        }

        playerPlaced.remove(player);
    }

    public List<Object> getPlaced(Player player) {
        return playerPlaced.getOrDefault(player, null);
    }

    public boolean isOccupied(MLGSchematic mlgSchematic) {
        return mlgSchematic.isOccupied();
    }

    public void flush() {
        mlgSchematics.clear();
        playerMLG.clear();
        playerPlaced.clear();
    }

    public File getMLGDir() {
        return mlgDir;
    }
}
