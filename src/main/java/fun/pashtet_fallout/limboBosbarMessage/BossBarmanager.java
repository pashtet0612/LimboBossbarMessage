package fun.pashtet_fallout.limboBosbarMessage;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BossBarmanager {

    private final LimboBosbarMessage plugin;
    private List<String> messages;
    private List<BarColor> colors;
    private int currentIndex = 0;

    private BukkitRunnable cycleTask;
    private BukkitRunnable fadeTask;

    private BossBar bossBar;

    public BossBarmanager(LimboBosbarMessage plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.reloadConfig();

        ConfigurationSection section = plugin.getConfig().getConfigurationSection("bossbar");

        if (section == null) {
            useDefaults();
            return;
        }

        List<String> rawMessages = section.getStringList("messages");

        if (rawMessages.isEmpty()) {
            useDefaults();
            return;
        }

        this.messages = new ArrayList<>();
        for (String msg : rawMessages) {
            messages.add(ChatColor.translateAlternateColorCodes('&', msg));
        }

        this.colors = new ArrayList<>();
        List<String> rawColors = section.getStringList("colors");
        for (String colorName : rawColors) {
            try {
                colors.add(BarColor.valueOf(colorName.toUpperCase()));
            } catch (IllegalArgumentException e) {
                colors.add(BarColor.WHITE);
            }
        }

        while (colors.size() < messages.size()) {
            colors.add(colors.get(colors.size() - 1));
        }
    }

    private void useDefaults() {
        this.messages = List.of(
                ChatColor.GREEN + "Default message 1",
                ChatColor.YELLOW + "Default message 2",
                ChatColor.RED + "Default message 3",
                ChatColor.AQUA + "Default message 4"
        );
        this.colors = List.of(BarColor.GREEN, BarColor.YELLOW, BarColor.RED, BarColor.BLUE);
    }

    public void start() {
        if (messages.isEmpty()) return;

        bossBar = Bukkit.createBossBar("", BarColor.WHITE, BarStyle.SOLID);

        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }

        Bukkit.getPluginManager().registerEvents(new org.bukkit.event.Listener() {
            @org.bukkit.event.EventHandler
            public void onJoin(org.bukkit.event.player.PlayerJoinEvent e) {
                bossBar.addPlayer(e.getPlayer());
            }
        }, plugin);

        cycleTask = new BukkitRunnable() {
            @Override
            public void run() {
                showNextMessage();
            }
        };

        cycleTask.runTaskTimer(plugin, 0L, 100L);
    }

    private void showNextMessage() {
        String title = messages.get(currentIndex);
        BarColor color = colors.get(currentIndex);

        bossBar.setTitle(title);
        bossBar.setColor(color);
        bossBar.setProgress(1.0);

        if (fadeTask != null) {
            fadeTask.cancel();
        }

        fadeTask = new BukkitRunnable() {
            double progress = 1.0;
            final double step = 0.02;
            final int steps = 50;

            @Override
            public void run() {
                progress -= step;
                if (progress <= 0) {
                    progress = 0;
                    this.cancel();
                }
                bossBar.setProgress(progress);
            }
        };

        fadeTask.runTaskTimer(plugin, 0L, 2L);

        currentIndex = (currentIndex + 1) % messages.size();
    }

    public void stop() {
        if (cycleTask != null) {
            cycleTask.cancel();
        }
        if (fadeTask != null) {
            fadeTask.cancel();
        }
        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }
    }
}