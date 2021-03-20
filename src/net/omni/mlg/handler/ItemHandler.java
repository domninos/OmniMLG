package net.omni.mlg.handler;

import net.omni.mlg.OmniMLGPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemHandler {

    private final OmniMLGPlugin plugin;

    private final List<ItemStack> mlgItems = new ArrayList<>();
    private final Map<Player, Integer> playerLevels = new HashMap<>();
    private final Map<Player, Integer> playerItems = new HashMap<>();

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

        if (!hasLevels(player))
            setLevel(player, 0);

        int itemLevel = playerItems.get(player);

        if (itemLevel > (mlgItems.size() - 1)) {
            itemLevel = 0;
            playerItems.put(player, 0);
        }

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
        playerLevels.remove(player);
        playerItems.remove(player);
    }

    public void flush() {
        mlgItems.clear();
        playerLevels.clear();
        playerItems.clear();
    }
}
