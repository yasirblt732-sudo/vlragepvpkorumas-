package com.pvpprotection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        protectionManager.giveProtection(player);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(Component.text("\n")
                    .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n", PURPLE_DARK))
                    .append(Component.text("  ✦ PVP KORUMASI AKTİF ✦\n", PURPLE_LIGHT).decorate(TextDecoration.BOLD))
                    .append(Component.text("  Sunucuya hoş geldin! 30 dakika\n", NamedTextColor.GRAY))
                    .append(Component.text("  PvP koruması verildi.\n", NamedTextColor.GRAY))
                    .append(Component.text("  Süre bitince saldırıya açık olacaksın!\n", NamedTextColor.YELLOW))
                    .append(Component.text("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━", PURPLE_DARK)));
            }
        }, 40L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) return;
        Player attacker = getAttacker(event);
        if (attacker == null || attacker.equals(target)) return;

        if (!target.hasPermission("pvpprotection.bypass") && protectionManager.isProtected(target)) {
            event.setCancelled(true);
            attacker.sendMessage(Component.text("⚔ ", PURPLE_DARK)
                .append(Component.text(target.getName(), PURPLE_LIGHT).decorate(TextDecoration.BOLD))
                .append(Component.text(" henüz PvP koruması altında! ", NamedTextColor.GRAY))
                .append(Component.text("(" + protectionManager.getFormattedTime(target) + " kaldı)", PURPLE_DARK)));
            return;
        }

        if (!attacker.hasPermission("pvpprotection.bypass") && protectionManager.isProtected(attacker)) {
            event.setCancelled(true);
            attacker.sendMessage(Component.text("✦ ", PURPLE_LIGHT)
                .append(Component.text("PvP koruması aktifken saldıramazsın!", PURPLE_DARK)));
        }
    }

    private Player getAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p) return p;
        if (event.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player s) return s;
        return null;
    }
}
