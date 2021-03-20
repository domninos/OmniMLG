package net.omni.mlg.schematic;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import net.omni.mlg.OmniMLGPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class SchematicHandler {
    private final OmniMLGPlugin plugin;
    private final File schematicFile;
    private String worldName;
    private World schematicWorld;

    public SchematicHandler(OmniMLGPlugin plugin) {
        this.schematicFile = new File(plugin.getDataFolder(), "mlg.schem");
        this.plugin = plugin;

        setSchematicWorld(updateSchematicWorld());
    }

    public boolean pasteSchematic(int x, int y, int z) {
        if (!schematicFile.exists()) {
            plugin.sendConsole("&cCould not find mlg schematic.");
            return false;
        }

        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(schematicWorld);

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);

        if (format == null) {
            plugin.sendConsole("&cCould not find schematic from file.");
            return false;
        }

        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            Clipboard clipboard = reader.read();

            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().
                    getEditSession(adaptedWorld, -1)) {
                Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                        .to(BlockVector3.at(x, y, z)).ignoreAirBlocks(false).build();

                try {
                    Operations.complete(operation);
                    return true;
                } catch (WorldEditException e) {
                    e.printStackTrace();
                    plugin.sendConsole("&cSomething went wrong pasting mlg  schematic. [WorldEdit]");
                    return false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            plugin.sendConsole("&cSomething went wrong pasting mlg schematic.");
            return false;
        }
    }

    public MLGSchematic pasteSchem(int x, int y, int z) {
        if (!schematicFile.exists()) {
            plugin.sendConsole("&cCould not find lava pool schematic.");
            return null;
        }

        if (!pasteSchematic(x, y, z))
            return null;

        MLGSchematic mlgSchematic = new MLGSchematic(plugin, schematicWorld.getName(), x, y, z);

        mlgSchematic.loadConfig(plugin.getNextMLG());
        mlgSchematic.setConfigValues();

        plugin.getMLGHandler().add(mlgSchematic);

        return mlgSchematic;
    }

    public World updateSchematicWorld() {
        String worldName = plugin.getConfig().getString("schematicWorld");

        if (worldName == null) {
            plugin.sendConsole("&cCould not find world in config.");
            return null;
        }

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            plugin.sendConsole("'" + worldName + "' is not found.");
            return null;
        }

        this.worldName = worldName;

        plugin.sendConsole("&aLoaded schematic world: " + worldName);

        return world;
    }

    public Location getRandomLocation(Location origin, double radius) {
        double randomRadius = ThreadLocalRandom.current().nextDouble() * radius;
        double theta = Math.toRadians(ThreadLocalRandom.current().nextDouble() * 360);
        double phi = Math.toRadians(ThreadLocalRandom.current().nextDouble() * 180 - 90);

        double x = randomRadius * Math.cos(theta) * Math.sin(phi);
        double y = randomRadius * Math.sin(theta) * Math.cos(phi);
        double z = randomRadius * Math.cos(phi);

        Location newLoc = origin.add(x, origin.getY(), z);
        newLoc.add(0, y, 0);

        if (newLoc.getBlockY() <= 5)
            newLoc.add(0, 10, 0);

        newLoc.add(0, 3, 0);

        return newLoc;
    }

    public World getSchematicWorld() {
        return schematicWorld;
    }

    public void setSchematicWorld(World world) {
        if (world != null)
            this.schematicWorld = world;
        else
            plugin.sendConsole("&cWorld is null");
    }

    public String getWorldName() {
        return worldName;
    }
}
