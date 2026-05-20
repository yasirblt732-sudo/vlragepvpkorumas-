package com.pvpprotection;

import org.bukkit.entity.Player;
import java.util.*;

public class ProtectionManager {

    private final PvPProtection plugin;
    private final Map<UUID, Long> protectedPlayers = new HashMap<>();

    public ProtectionManager(PvPProtection plugin) {
        this.plugin = plugin;
    }

    public void giveProtection(Player player) {
        long durationMinutes = plugin.getConfig().getLong("protection.duration-minutes", 30L);
        long expiryTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L);
        protectedPlayers.put(player.getUniqueId(), expiryTime);
    }

    public boolean isProtected(Player player) {
        UUID uuid = player.getUniqueId();
        if (!protectedPlayers.containsKey(uuid)) return false;
        long expiry = protectedPlayers.get(uuid);
        if (System.currentTimeMillis() >= expiry) {
            protectedPlayers.remove(uuid);
            return false;
        }
        return true;
    }

    public long getRemainingMillis(Player player) {
        UUID uuid = player.getUniqueId();
        if (!protectedPlayers.containsKey(uuid)) return 0L;
        return Math.max(0L, protectedPlayers.get(uuid) - System.currentTimeMillis());
    }

    public String getFormattedTime(Player player) {
        long totalSeconds = getRemainingMillis(player) / 1000;
        return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60);
    }

    public List<UUID> getAndClearExpired() {
        List<UUID> expired = new ArrayList<>();
        long now = System.currentTimeMillis();
        protectedPlayers.entrySet().removeIf(entry -> {
            if (now >= entry.getValue()) { expired.add(entry.getKey()); return true; }
            return false;
        });
        return expired;
    }

    public void removeProtection(Player player) { protectedPlayers.remove(player.getUniqueId()); }
    public void clearAll() { protectedPlayers.clear(); }
    public Map<UUID, Long> getProtectedPlayers() { return protectedPlayers; }
}
