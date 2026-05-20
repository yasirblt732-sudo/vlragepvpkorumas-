package com.pvpprotection;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProtectionManager {

    private final PvPProtection plugin;
    // UUID -> koruma bitiş zamanı (ms cinsinden System.currentTimeMillis())
    private final Map<UUID, Long> protectedPlayers = new HashMap<>();

    public ProtectionManager(PvPProtection plugin) {
        this.plugin = plugin;
    }

    /**
     * Oyuncuya koruma ver (config'deki süre kadar)
     */
    public void giveProtection(Player player) {
        long durationMinutes = plugin.getConfig().getLong("protection.duration-minutes", 30L);
        long expiryTime = System.currentTimeMillis() + (durationMinutes * 60 * 1000L);
        protectedPlayers.put(player.getUniqueId(), expiryTime);
    }

    /**
     * Oyuncunun koruması var mı?
     */
    public boolean isProtected(Player player) {
        UUID uuid = player.getUniqueId();
        if (!protectedPlayers.containsKey(uuid)) return false;

        long expiry = protectedPlayers.get(uuid);
        if (System.currentTimeMillis() >= expiry) {
            // Süre dolmuş, kaldır
            protectedPlayers.remove(uuid);
            return false;
        }
        return true;
    }

    /**
     * Oyuncunun kalan koruma süresi (milisaniye)
     */
    public long getRemainingMillis(Player player) {
        UUID uuid = player.getUniqueId();
        if (!protectedPlayers.containsKey(uuid)) return 0L;
        long remaining = protectedPlayers.get(uuid) - System.currentTimeMillis();
        return Math.max(0L, remaining);
    }

    /**
     * Kalan süreyi okunabilir formata çevir: SS:dd
     */
    public String getFormattedTime(Player player) {
        long millis = getRemainingMillis(player);
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Koruması az önce dolmuş muydu? (expire kontrolü için)
     * Bu metod expire olan UUID'leri döner ve onları temizler.
     */
    public java.util.List<UUID> getAndClearExpired() {
        java.util.List<UUID> expired = new java.util.ArrayList<>();
        long now = System.currentTimeMillis();
        protectedPlayers.entrySet().removeIf(entry -> {
            if (now >= entry.getValue()) {
                expired.add(entry.getKey());
                return true;
            }
            return false;
        });
        return expired;
    }

    /**
     * Oyuncunun korumasını manuel kaldır
     */
    public void removeProtection(Player player) {
        protectedPlayers.remove(player.getUniqueId());
    }

    /**
     * Tüm korumaları temizle (plugin kapatılırken)
     */
    public void clearAll() {
        protectedPlayers.clear();
    }

    /**
     * Tüm korumalı oyuncuların UUID listesi
     */
    public Map<UUID, Long> getProtectedPlayers() {
        return protectedPlayers;
    }
}
