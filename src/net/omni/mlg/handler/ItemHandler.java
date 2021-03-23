package net.omni.mlg.handler;

import net.omni.mlg.OmniMLGPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemHandler {

    private final OmniMLGPlugin plugin;

    private final List<ItemStack> mlgItems = new ArrayList<>();

    public ItemHandler(OmniMLGPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadMLGItems() {
        mlgItems.clear();

        for (String mlgItem : plugin.getConfig().getStringList("mlgItems")) {
            if (mlgItem == null)
                continue;

            String[] splice = mlgItem.toUpperCase().split(":");

            if (splice.length > 2) {
                plugin.sendConsole("&cToo many given parameters for " + mlgItem);
                continue;
            }

            Material material = Material.getMaterial(splice[0]);

            if (material == null) {
                plugin.sendConsole("&c" + splice[0] + " is not a material.");
                continue;
            }

            try {
                int amount = Integer.parseInt(splice[1]);

                mlgItems.add(new ItemStack(material, amount));
                plugin.sendConsole("&bLoaded " + amount + " " + material.name());
            } catch (NumberFormatException e) {
                plugin.sendConsole("&cCould not parse amount for " + mlgItem);
            }
        }
    }

    public void giveMLGItems(Player player) {
        if (player == null)
            return;

        if (mlgItems.isEmpty()) {
            plugin.sendConsole("&cItems not found.");
            return;
        }

        clear(player);

        int itemLevel = plugin.getLevelHandler().update(player);

        ItemStack item = mlgItems.get(itemLevel);

        player.getInventory().addItem(item);
    }

    public void clear(Player player) {
        if (player != null)
            player.getInventory().clear();
    }

    public boolean isMLGItem(ItemStack itemStack) {
        return itemStack != null && mlgItems.stream().anyMatch(itemStack::isSimilar);
    }

    public boolean isMLGItem(Material material) {
        return material != null && mlgItems.stream().anyMatch(item -> item.getType() == material);
    }

    public int size() {
        return mlgItems.size();
    }

    public void flush() {
        mlgItems.clear();
    }
}
