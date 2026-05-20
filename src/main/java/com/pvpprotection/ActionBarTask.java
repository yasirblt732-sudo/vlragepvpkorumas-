package com.pvpprotection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.UUID;

public class ActionBarTask extends BukkitRunnable {

    private final PvPProtection plugin;
    private final ProtectionManager protectionManager;
    private static final TextColor PURPLE_DARK  = TextColor.color(0xAA00AA);
    private static final TextColor PURPLE_LIGHT = TextColor.color(0xFF55FF);
    private static final TextColor PURPLE_MID   = TextColor.color(0xCC44CC);

    public ActionBarTask(PvPProtection plugin, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.protectionManager = protectionManager;
    }

    @Override
    public void run() {
        List<UUID> expired = protectionManager.getAndClearExpired();
        for (UUID uuid : expired) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) notifyExpired(p);
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (protectionManager.isProtected(player)) sendActionBar(player);
        }
    }

    private void sendActionBar(Player player) {
        String timeLeft = protectionManager.getFormattedTime(player);
        Component line1 = Component.text("✦ PVP KORUMASI AKTİF ✦")
                .color(PURPLE_DARK).decorate(TextDecoration.BOLD);
        Component line2 = Component.text("⏱ ").color(PURPLE_LIGHT)
                .append(Component.text(timeLeft).color(PURPLE_LIGHT).decorate(TextDecoration.BOLD))
                .append(Component.text(" kaldı").color(PURPLE_MID));
        player.sendActionBar(line1.append(Component.newline()).append(line2));
    }

    private void notifyExpired(Player player) {
        player.sendActionBar(Component.text("⚔ PVP KORUMASI SONA ERDİ ⚔")
                .color(NamedTextColor.RED).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("⚠ ", NamedTextColor.YELLOW)
                .append(Component.text("PvP koruması sona erdi! ", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .append(Component.text("Artık saldırıya açıksın!", NamedTextColor.GRAY)));
        player.showTitle(net.kyori.adventure.title.Title.title(
                Component.text("⚔ PVP AKTİF ⚔").color(NamedTextColor.RED).decorate(TextDecoration.BOLD),
                Component.text("Koruman sona erdi!").color(NamedTextColor.YELLOW),
                net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(500),
                        java.time.Duration.ofMillis(3000),
                        java.time.Duration.ofMillis(500))));
    }
}
