package net.omni.mlg.listener;

import net.omni.mlg.OmniMLGPlugin;
import net.omni.mlg.schematic.MLGSchematic;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;

public class PlayerListener implements Listener {

    private final OmniMLGPlugin plugin;

    public PlayerListener(OmniMLGPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getSchematicHandler().getWorldName()))
            return;

        if (player.getGameMode() == GameMode.CREATIVE)
            return;

        event.setCancelled(true);
        plugin.sendMessage(player, "&cYou cannot break blocks here.");
    }

    @EventHandler
    public void onPlayerEmptyBucket(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getSchematicHandler().getWorldName()))
            return;


        if (event.getBucket() == Material.WATER_BUCKET) {

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                Block placed = event.getBlockClicked().getRelative(event.getBlockFace());

                plugin.getMLGHandler().add(player, placed);
            }, 1L);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getSchematicHandler().getWorldName()))
            return;

        MLGSchematic mlgSchematic = plugin.getMLGHandler().getMLGFromPlayer(player);

        if (mlgSchematic == null) {
            plugin.sendConsole("&cCould not find mlg from " + player.getName());
            return;
        }

        if (!mlgSchematic.isStart()) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlock();

        if (!plugin.getItemHandler().isMLGItem(block.getType())) {
            if (player.getGameMode() == GameMode.CREATIVE)
                return;

            event.setCancelled(true);
            plugin.sendMessage(player, "&cYou cannot place blocks here.");
            return;
        }

        if (!block.getType().name().endsWith("_BOAT"))
            plugin.getMLGHandler().add(player, block);
    }

    @EventHandler
    public void onVehicleEnter(VehicleEnterEvent event) {
        if (!(event.getEntered() instanceof Player))
            return;

        if (!(event.getVehicle() instanceof Boat))
            return;

        Player player = (Player) event.getEntered();

        if (!player.getWorld().getName().equalsIgnoreCase(plugin.getSchematicHandler().getWorldName()))
            return;

        Boat boat = (Boat) event.getVehicle();

        plugin.getMLGHandler().add(player, boat);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getPlayerHandler().tpToLastLocation(player);
        plugin.getPlayerHandler().removePlayer(player);

        if (plugin.getMLGHandler().inMLG(player))
            plugin.getMLGHandler().removePlayer(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.getWorld().getName().equalsIgnoreCase(plugin.getSchematicHandler().getWorldName())) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                player.spigot().respawn();

                plugin.getPlayerHandler().tpToLastLocation(player);
                plugin.getPlayerHandler().removePlayer(player);

                if (plugin.getMLGHandler().inMLG(player))
                    plugin.getMLGHandler().removePlayer(player);
            }, 20L);
        }
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}
