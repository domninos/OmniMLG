package net.omni.mlg.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.omni.mlg.OmniMLGPlugin;
import org.bukkit.entity.Player;

public class MLGPlaceholder extends PlaceholderExpansion {
    private final OmniMLGPlugin plugin;

    public MLGPlaceholder(OmniMLGPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "mlg";
    }

    @Override
    public String getAuthor() {
        return "omni";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        String result = "Unavailable";

        if (params.startsWith("top")) {
            switch (params) {
                case "top_1":
                    result = plugin.getTopHandler().getTop(1);
                    break;
                case "top_2":
                    result = plugin.getTopHandler().getTop(2);
                    break;
                case "top_3":
                    result = plugin.getTopHandler().getTop(3);
                    break;
                case "top_4":
                    result = plugin.getTopHandler().getTop(4);
                    break;
                case "top_5":
                    result = plugin.getTopHandler().getTop(5);
                    break;
            }

            if (result == null || result.isEmpty())
                result = "Unavailable";
        } else if (params.equalsIgnoreCase("level")) {
            int level = 0;

            if (plugin.getLevelHandler().hasLevels(player))
                level = plugin.getLevelHandler().getLevel(player);
            else if (plugin.getConfigHandler().isInConfig(player.getName()))
                level = plugin.getConfigHandler().getLevelInConfig(player.getName());

            result = String.valueOf(level);
        } else if (params.equalsIgnoreCase("personal_best")) {

            if (plugin.getConfigHandler().isInConfig(player.getName())) {
                int personal_best = plugin.getConfigHandler().getPersonalBest(player.getName());

                result = String.valueOf(personal_best);
            } else
                result = "Not found";

        }

        return result;
    }
}
