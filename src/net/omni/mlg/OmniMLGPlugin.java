package net.omni.mlg;

import net.omni.mlg.commands.OmniMLGCommand;
import net.omni.mlg.handler.ItemHandler;
import net.omni.mlg.handler.MLGHandler;
import net.omni.mlg.handler.PlayerHandler;
import net.omni.mlg.listener.PlayerListener;
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

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.schematicHandler = new SchematicHandler(this);

        this.mlgHandler = new MLGHandler(this);

        mlgHandler.loadMLGs();

        this.playerHandler = new PlayerHandler(this);
        this.itemHandler = new ItemHandler(this);

        itemHandler.loadMLGItems();

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

        sendConsole("&aSuccessfully disabled OmniMLGPlugin");
    }

    public void sendMessage(CommandSender sender, String message) {
        sendMessage(sender, message, true);
    }

    public void sendMessage(CommandSender sender, String message, boolean prefix) {
        sender.sendMessage(translate(prefix ? "&3[&fOmni&bMLG&3]&r " + message : message));
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
}
