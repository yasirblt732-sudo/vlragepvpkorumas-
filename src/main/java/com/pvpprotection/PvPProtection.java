package com.pvpprotection;

import org.bukkit.plugin.java.JavaPlugin;

public class PvPProtection extends JavaPlugin {

    private static PvPProtection instance;
    private ProtectionManager protectionManager;
    private ActionBarTask actionBarTask;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        protectionManager = new ProtectionManager(this);
        actionBarTask = new ActionBarTask(this, protectionManager);
        long interval = getConfig().getLong("protection.update-interval-ticks", 20L);
        actionBarTask.runTaskTimer(this, 0L, interval);
        getServer().getPluginManager().registerEvents(new PvPListener(this, protectionManager), this);
        PvPCommand commandHandler = new PvPCommand(this, protectionManager);
        getCommand("pvpprotect").setExecutor(commandHandler);
        getCommand("pvpprotect").setTabCompleter(commandHandler);
        getLogger().info("PvP Koruma Eklentisi aktif!");
    }

    @Override
    public void onDisable() {
        if (actionBarTask != null) actionBarTask.cancel();
        if (protectionManager != null) protectionManager.clearAll();
    }

    public static PvPProtection getInstance() { return instance; }
    public ProtectionManager getProtectionManager() { return protectionManager; }
}
