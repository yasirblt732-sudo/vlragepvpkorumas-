package com.pvpprotection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PvPListener implements Listener {

    private final PvPProtection plugin;
    private final ProtectionManager protectionManager;

    private static final TextColor PURPLE_LIGHT = TextColor.color(0xFF55FF);
    private static final TextColor PURPLE_DARK  = TextColor.color(0xAA00AA);

    public PvPListener(PvPProtection plugin, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.protectionManager = protectionManager;
    }

    /**
     * Oyuncu sunucuya girdiğinde 30 dakika koruma ver
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        protectionManager.giveProtection(player);

        // Hoşgeldin mesajı
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                Component welcomeMsg = Component.newline()
                        .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", PURPLE_DARK))
                        .append(Component.newline())
                        .append(Component.text("  ✦ PVP KORUMASI AKTİF ✦", PURPLE_LIGHT).decorate(TextDecoration.BOLD))
                        .append(Component.newline())
                        .append(Component.text("  Sunucuya hoş geldin! Sana 30 dakika", NamedTextColor.GRAY))
                        .append(Component.newline())
                        .append(Component.text("  PvP koruması verildi.", NamedTextColor.GRAY))
                        .append(Component.newline())
                        .append(Component.text("  Süre bitince saldırıya açık olacaksın!", NamedTextColor.YELLOW))
                        .append(Component.newline())
                        .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", PURPLE_DARK))
                        .append(Component.newline());
                player.sendMessage(welcomeMsg);
            }
        }, 40L); // 2 saniye bekle (spawn ekranı geçsin diye)
    }

    /**
     * Hasar olayını yakala - korumalı oyunculara hasar verilmesini engelle
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Hedef bir oyuncu mu?
        if (!(event.getEntity() instanceof Player target)) return;

        // Saldıranı bul (direkt veya ok ile)
        Player attacker = getAttacker(event);
        if (attacker == null) return;

        // Aynı oyuncu kendine zarar vermesin (splash potion gibi şeyler)
        if (attacker.equals(target)) return;

        // Bypass izni var mı?
        boolean attackerBypass = attacker.hasPermission("pvpprotection.bypass");
        boolean targetBypass   = target.hasPermission("pvpprotection.bypass");

        // HEDEF korumalı mı?
        if (!targetBypass && protectionManager.isProtected(target)) {
            event.setCancelled(true);

            String timeLeft = protectionManager.getFormattedTime(target);

            // Saldırana mesaj
            Component msgAttacker = Component.text("⚔ ", PURPLE_DARK)
                    .append(Component.text(target.getName(), PURPLE_LIGHT).decorate(TextDecoration.BOLD))
                    .append(Component.text(" henüz PvP koruması altında! ", NamedTextColor.GRAY))
                    .append(Component.text("(" + timeLeft + " kaldı)", PURPLE_DARK));
            attacker.sendMessage(msgAttacker);
            return;
        }

        // SALDIRAN korumalı mı? (korumalıyken saldırmasın)
        if (!attackerBypass && protectionManager.isProtected(attacker)) {
            event.setCancelled(true);

            Component msgAttacker = Component.text("✦ ", PURPLE_LIGHT)
                    .append(Component.text("PvP koruması aktifken saldıramazsın!", PURPLE_DARK))
                    .append(Component.text(" Korumanı kaldırmak için /pvpprotect remove yaz.", NamedTextColor.GRAY));
            attacker.sendMessage(msgAttacker);
        }
    }

    /**
     * Event'ten saldıran oyuncuyu çıkar (direkt vuruş veya projectile)
     */
    private Player getAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p) {
            return p;
        }
        if (event.getDamager() instanceof Projectile proj) {
            if (proj.getShooter() instanceof Player shooter) {
                return shooter;
            }
        }
        return null;
    }
}
