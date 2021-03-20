package net.omni.mlg.handler;

import net.omni.mlg.OmniMLGPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class PotionHandler {

    private final OmniMLGPlugin plugin;
    private final Map<Integer, Set<PotionEffect>> potionEffects = new HashMap<>();

    public PotionHandler(OmniMLGPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadPotions() {
        // TODO

        flush();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("levelPotionEffects");

        if (section == null) {
            plugin.sendConsole("&cSection 'levelPotionEffects' not found.");
            return;
        }

        for (String key : section.getKeys(false)) {
            if (key == null)
                continue;

            List<String> potions = plugin.getConfig().
                    getStringList("levelPotionEffects." + key);

            int level;

            try {
                level = Integer.parseInt(key);
            } catch (NumberFormatException e) {
                plugin.sendConsole("&cCould not parse level: " + key);
                continue;
            }

            Set<PotionEffect> potionsSet = new HashSet<>();

            for (String potion : potions) {
                if (potion == null)
                    continue;

                String[] splice = potion.split(":");

                if (splice.length > 2) {
                    plugin.sendConsole("&cCould not parse potion effects for " + key);
                    continue;
                }

                String effectType = splice[0].toUpperCase();

                PotionEffectType type = PotionEffectType.getByName(effectType);

                if (type == null) {
                    plugin.sendConsole("&cCould not find potion effect " + effectType);
                    continue;
                }

                int amplifier;

                try {
                    amplifier = Integer.parseInt(splice[1]);
                } catch (NumberFormatException e) {
                    plugin.sendConsole("&cCould not parse amplifier for " + effectType);
                    continue;
                }

                amplifier -= 1;

                if (amplifier < 0)
                    amplifier = 0;

                PotionEffect potionEffect = new PotionEffect(type, Integer.MAX_VALUE, amplifier);

                potionsSet.add(potionEffect);
            }

            potionEffects.put(level, potionsSet);
            plugin.sendConsole("&aLoaded " + key + "'s potion effects");
        }
    }

    public void addEffects(Player player, int level) {
        if (!potionEffects.containsKey(level))
            return;

        removeEffects(player);

        Set<PotionEffect> effects = potionEffects.get(level);

        player.addPotionEffects(effects);
    }

    public void removeEffects(Player player) {
        // clear potion effects

        for (PotionEffect activeEffect : player.getActivePotionEffects())
            player.removePotionEffect(activeEffect.getType());
    }

    public void flush() {
        potionEffects.forEach((key, value) -> value.clear());
        potionEffects.clear();
    }
}