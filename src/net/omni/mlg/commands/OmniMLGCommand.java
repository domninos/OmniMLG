package net.omni.mlg.commands;

import net.omni.mlg.OmniMLGPlugin;
import net.omni.mlg.schematic.MLGSchematic;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

public class OmniMLGCommand implements CommandExecutor {

    private final OmniMLGPlugin plugin;
    private final String msg;

    public OmniMLGCommand(OmniMLGPlugin plugin) {
        this.plugin = plugin;

        String[] text = {
                "&0&l&m------------- &8[&fOmni&bMLG&8] &0&l&m-------------",
                "&bAlias: /omnim, /mlg",
                "&b/omnimlg join |player| &7» Sends the player to the schematic world.",
                "&b/omnimlg leave |player| &7» Sends the player back to the main world.",
                "&b/omnimlg update <player> &7» Updates the level of the player.",
                "&0&l&m---------------------------------"
        };

        this.msg = plugin.translate(String.join("\n", text));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!sender.hasPermission("omnimlg.use"))
            return noPerms(sender);

        if (args.length == 0) {
            plugin.sendMessage(sender, msg, false);
            return true;
        } else if (args.length == 1) {
            if (!(args[0].equalsIgnoreCase("join")
                    || args[0].equalsIgnoreCase("leave"))) {
                plugin.sendMessage(sender, msg, false);
                return true;
            }

            if (!(sender instanceof Player)) {
                plugin.sendMessage(sender, "&cOnly players can use this command.");
                return true;
            }

            Player player = (Player) sender;

            if (args[0].equalsIgnoreCase("join")) {
                if (!sender.hasPermission("omnimlg.join"))
                    return noPerms(sender);

                if (plugin.getMLGHandler().inMLG(player)) {
                    plugin.sendMessage(player,
                            "&cYou are already in an mlg. Do /omnimlg leave to leave.");
                    return true;
                }

                plugin.getPlayerHandler().setLastLocation(player, player.getLocation());
                plugin.getMLGHandler().sendPlayer(player);
            } else if (args[0].equalsIgnoreCase("leave")) {
                if (!sender.hasPermission("omnimlg.leave"))
                    return noPerms(sender);

                if (!plugin.getMLGHandler().inMLG(player)) {
                    plugin.sendMessage(player,
                            "&cYou are not in an mlg. Use /omnimlg join to go to one.");
                    return true;
                }

                Location lastLocation = plugin.getPlayerHandler().getLastLocation(player);

                if (lastLocation == null) {
                    plugin.sendMessage(sender, "Your last location was not found.");
                    return true;
                }

                player.teleport(lastLocation);

                plugin.getPlayerHandler().removePlayer(player);
                plugin.getMLGHandler().removePlayer(player);
            }

            return true;
        } else if (args.length == 2) {
            if (!(args[0].equalsIgnoreCase("join")
                    || args[0].equalsIgnoreCase("leave")
                    || args[0].equalsIgnoreCase("update"))) {
                plugin.sendMessage(sender, msg, false);
                return true;
            }

            String playerName = args[1];

            Player target = Bukkit.getPlayer(playerName);

            if (target == null) {
                plugin.sendMessage(sender, "&c" + playerName + " not found.");
                return true;
            }

            if (args[0].equalsIgnoreCase("join")) {
                if (!sender.hasPermission("omnimlg.join.other"))
                    return noPerms(sender);

                if (plugin.getMLGHandler().inMLG(target)) {
                    plugin.sendMessage(sender, "&c" + playerName + " is already in an mlg.");
                    return true;
                }

                plugin.getPlayerHandler().setLastLocation(target, target.getLocation());
                plugin.getMLGHandler().sendPlayer(target);
            } else if (args[0].equalsIgnoreCase("Leave")) {
                if (!sender.hasPermission("omnimlg.leave.other"))
                    return noPerms(sender);

                if (!plugin.getMLGHandler().inMLG(target)) {
                    plugin.sendMessage(target, "&c" + playerName
                            + " is not in an mlg. Use /omnimlg join <player> to send the player to an mlg.");
                    return true;
                }

                Location lastLocation = plugin.getPlayerHandler().getLastLocation(target);

                if (lastLocation == null) {
                    plugin.sendMessage(sender, "&c" + playerName + "'s last location was not found.");
                    return true;
                }

                target.teleport(lastLocation);

                plugin.getPlayerHandler().removePlayer(target);
                plugin.getMLGHandler().removePlayer(target);

            } else if (args[0].equalsIgnoreCase("update")) {
                if (!sender.hasPermission("omnimlg.update"))
                    return noPerms(sender);

                if (!plugin.getMLGHandler().inMLG(target)) {
                    plugin.sendMessage(target, "&c" + playerName
                            + " is not in an mlg. Use /omnimlg join <player> to send the player to an mlg.");
                    return true;
                }

                MLGSchematic mlgSchematic = plugin.getMLGHandler().getMLGFromPlayer(target);

                mlgSchematic.update();

                plugin.getItemHandler().setLevel(target, mlgSchematic.getLevel());

                plugin.getMLGHandler().clear(target);

                target.teleport(mlgSchematic.getSpawn());
                plugin.getItemHandler().giveMLGItems(target);
            }

            return true;
        }

        return true;
    }

    public void register() {
        PluginCommand pluginCommand = plugin.getCommand("omnimlg");

        if (pluginCommand != null)
            pluginCommand.setExecutor(this);
        else
            plugin.sendConsole("&cCould not register /omnimlg.");
    }

    private boolean noPerms(CommandSender sender) {
        plugin.sendMessage(sender, "&cNo permissions.");
        return true;
    }
}
