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

    // Mor tonları
    private static final TextColor PURPLE_DARK  = TextColor.color(0xAA00AA); // §5
    private static final TextColor PURPLE_LIGHT = TextColor.color(0xFF55FF); // §d
    private static final TextColor PURPLE_MID   = TextColor.color(0xCC44CC);

    public ActionBarTask(PvPProtection plugin, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.protectionManager = protectionManager;
    }

    @Override
    public void run() {
        // Süresi dolanları kontrol et ve bildir
        List<UUID> expired = protectionManager.getAndClearExpired();
        for (UUID uuid : expired) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                notifyExpired(p);
            }
        }

        // Koruması devam eden oyunculara action bar gönder
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (protectionManager.isProtected(player)) {
                sendActionBar(player);
            }
        }
    }

    /**
     * Action bar'ı gönder: slot barın ÜSTÜNDE görünür
     * İki satır efekti için \n kullanıyoruz (Adventure API destekliyor)
     */
    private void sendActionBar(Player player) {
        String timeLeft = protectionManager.getFormattedTime(player);

        // Satır 1: ✦ PVP KORUMASI AKTİF ✦  (koyu mor, kalın)
        Component line1 = Component.text("✦ PVP KORUMASI AKTİF ✦")
                .color(PURPLE_DARK)
                .decorate(TextDecoration.BOLD);

        // Satır 2: ⏱ XX:XX kaldı  (açık mor)
        Component line2 = Component.text("⏱ ")
                .color(PURPLE_LIGHT)
                .append(Component.text(timeLeft)
                        .color(PURPLE_LIGHT)
                        .decorate(TextDecoration.BOLD))
                .append(Component.text(" kaldı")
                        .color(PURPLE_MID));

        // İki satırı birleştir (\n ile)
        Component actionBarMsg = line1
                .append(Component.newline())
                .append(line2);

        player.sendActionBar(actionBarMsg);
    }

    /**
     * Koruma bittiğinde oyuncuyu bilgilendir
     */
    private void notifyExpired(Player player) {
        // Action bar'da kırmızı uyarı
        Component expiredBar = Component.text("⚔ PVP KORUMASI SONA ERDİ ⚔")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD);
        player.sendActionBar(expiredBar);

        // Chat mesajı
        Component chatMsg = Component.text("⚠ ", NamedTextColor.YELLOW)
                .append(Component.text("PvP koruması sona erdi! ", NamedTextColor.RED).decorate(TextDecoration.BOLD))
                .append(Component.text("Artık saldırıya açıksın!", NamedTextColor.GRAY));
        player.sendMessage(chatMsg);

        // Başlık (kısa, dikkat çekici)
        Component title = Component.text("⚔ PVP AKTİF ⚔")
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD);
        Component subtitle = Component.text("Koruman sona erdi!")
                .color(NamedTextColor.YELLOW);
        player.showTitle(net.kyori.adventure.title.Title.title(title, subtitle,
                net.kyori.adventure.title.Title.Times.times(
                        java.time.Duration.ofMillis(500),
                        java.time.Duration.ofMillis(3000),
                        java.time.Duration.ofMillis(500)
                )));
    }
}
