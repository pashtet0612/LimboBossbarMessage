package fun.pashtet_fallout.limboBosbarMessage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class LimboBosbarMessage extends JavaPlugin {

    private BossBarmanager bossBarManager;

    @Override
    public void onEnable() {
        // Создаёт config.yml при первом запуске
        saveDefaultConfig();

        bossBarManager = new BossBarmanager(this);
        bossBarManager.start();

        // Регистрируем команду
        getCommand("limbobossbar").setExecutor(this);
    }

    @Override
    public void onDisable() {
        if (bossBarManager != null) {
            bossBarManager.stop();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("limbobossbar")) return false;

        if (args.length != 1 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage("§cUsing: /limbobossbar reload");
            return true;
        }

        if (!sender.hasPermission("limbobossbar.reload")) {
            sender.sendMessage("§cYou don't have permissions");
            return true;
        }

        // Перезагружаем
        bossBarManager.stop();
        reloadConfig(); // ← ВАЖНО: перечитываем config.yml
        bossBarManager = new BossBarmanager(this); // ← создаём новый экземпляр с новыми данными
        bossBarManager.start();

        sender.sendMessage("§aReloaded!");

        return true;
    }
}