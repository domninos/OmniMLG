package net.omni.mlg;

import net.omni.mlg.commands.OmniMLGCommand;
import net.omni.mlg.handler.*;
import net.omni.mlg.listener.PlayerListener;
import net.omni.mlg.placeholder.MLGPlaceholder;
import net.omni.mlg.schematic.SchematicHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;

public class OmniMLGPlugin extends JavaPlugin {

    private MLGHandler mlgHandler;
    private PlayerHandler playerHandler;
    private SchematicHandler schematicHandler;
    private ItemHandler itemHandler;
    private TopHandler topHandler;
    private PotionHandler potionHandler;
    private LevelHandler levelHandler;
    private ConfigHandler configHandler;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.configHandler = new ConfigHandler(this);
        this.levelHandler = new LevelHandler(this);
        this.topHandler = new TopHandler(this);
        this.potionHandler = new PotionHandler(this);

        potionHandler.loadPotions();

        this.schematicHandler = new SchematicHandler(this);
        this.mlgHandler = new MLGHandler(this);

        mlgHandler.loadMLGs();

        this.playerHandler = new PlayerHandler(this);
        this.itemHandler = new ItemHandler(this);

        itemHandler.loadMLGItems();

        topHandler.update();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new MLGPlaceholder(this).register();

            sendConsole("&aPlaceholderAPI found, registered placeholder.");
        }

        // [+] LISTENERS [+]
        new PlayerListener(this).register();

        // [+] COMMANDS [+]
        new OmniMLGCommand(this).register();

        sendConsole("&aSuccessfully enabled OmniMLGPlugin v-" + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        mlgHandler.flush();
        playerHandler.flush();
        itemHandler.flush();
        levelHandler.flush();

        sendConsole("&aSuccessfully disabled OmniMLGPlugin");
    }

    public void sendMessage(CommandSender sender, String message) {
        sendMessage(sender, message, true);
    }

    public void sendMessage(CommandSender sender, String message, boolean prefix) {
        sender.sendMessage(translate(prefix ? "&2&lS&a&lP &f?? " + message : message));
    }

    public void sendConsole(String message) {
        sendMessage(Bukkit.getConsoleSender(), message);
    }

    public String getNextMLG() {
        String fileName = "mlg-1";

        try {
            fileName = "mlg-" + (Files.list(mlgHandler.getMLGDir().toPath()).count() + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileName;
    }

    public String translate(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public SchematicHandler getSchematicHandler() {
        return schematicHandler;
    }

    public MLGHandler getMLGHandler() {
        return mlgHandler;
    }

    public ItemHandler getItemHandler() {
        return itemHandler;
    }

    public PlayerHandler getPlayerHandler() {
        return playerHandler;
    }

    public PotionHandler getPotionHandler() {
        return potionHandler;
    }

    public TopHandler getTopHandler() {
        return topHandler;
    }

    public LevelHandler getLevelHandler() {
        return levelHandler;
    }

    public ConfigHandler getConfigHandler() {
        return configHandler;
    }
}
