package net.omni.mlg.handler;

import net.omni.mlg.OmniMLGPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.stream.Collectors;

public class TopHandler {
    private final OmniMLGPlugin plugin;
    private final String[] tops = new String[5];
    private final transient Map<String, Integer> times = new HashMap<>();
    private final transient Map<Integer, String> topFiveString = new HashMap<>();

    public TopHandler(OmniMLGPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Updates the top 5 scores.
     */
    public void update() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("finished");

        if (section == null) {
            Arrays.fill(tops, "Unavailable");
            return;
        }

        times.clear();

        for (String key : section.getKeys(false)) {
            if (key != null)
                times.put(key, plugin.getConfig().getInt("finished." + key));
        }

        Map<String, Integer> topFive =
                times.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                        .limit(5)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        topFiveString.clear();

        int count = 1;

        for (Map.Entry<String, Integer> entry : topFive.entrySet())
            topFiveString.put(count++,
                    "&e" + entry.getKey() + ": &l&f" + entry.getValue());

        plugin.sendConsole("&l&3Updated Top " + (count - 1));

        for (Map.Entry<Integer, String> entry : topFiveString.entrySet())
            plugin.sendConsole("&3" + entry.getKey() + ") " + entry.getValue());

        tops[0] = topFiveString.get(1);
        tops[1] = topFiveString.get(2);
        tops[2] = topFiveString.get(3);
        tops[3] = topFiveString.get(4);
        tops[4] = topFiveString.get(5);

        topFive.clear();
        topFiveString.clear();
        times.clear();
    }

    /**
     * Gets the top according to the params.
     *
     * @param top {@code Integer} top to get
     * @return {@code String} returns the top converted to a string.
     */
    public String getTop(int top) {
        try {
            String text = tops[top - 1];

            return text == null ? "Unavailable" : text;
        } catch (ArrayIndexOutOfBoundsException e) {
            return "Unavailable";
        }
    }
}