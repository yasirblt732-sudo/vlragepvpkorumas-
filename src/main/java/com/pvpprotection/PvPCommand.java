package com.pvpprotection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PvPCommand implements CommandExecutor, TabCompleter {

    private final PvPProtection plugin;
    private final ProtectionManager protectionManager;

    private static final TextColor PURPLE_LIGHT = TextColor.color(0xFF55FF);
    private static final TextColor PURPLE_DARK  = TextColor.color(0xAA00AA);

    public PvPCommand(PvPProtection plugin, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.protectionManager = protectionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                if (!sender.hasPermission("pvpprotection.admin")) {
                    sender.sendMessage(Component.text("Bu komutu kullanma yetkin yok!", NamedTextColor.RED));
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(Component.text("✦ Ayarlar yeniden yüklendi!", PURPLE_LIGHT));
            }

            case "remove" -> {
                // Oyuncu kendi korumasını kapatabilir
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(Component.text("Bu komutu sadece oyuncular kullanabilir!", NamedTextColor.RED));
                    return true;
                }
                if (!protectionManager.isProtected(player)) {
                    player.sendMessage(Component.text("Zaten aktif bir koruман yok.", NamedTextColor.YELLOW));
                    return true;
                }
                protectionManager.removeProtection(player);
                player.sendMessage(Component.text("⚔ PvP koruман kaldırıldı. Artık saldırıya açıksın!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
            }

            case "give" -> {
                if (!sender.hasPermission("pvpprotection.admin")) {
                    sender.sendMessage(Component.text("Bu komutu kullanma yetkin yok!", NamedTextColor.RED));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Kullanım: /pvpprotect give <oyuncu>", NamedTextColor.RED));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(Component.text("Oyuncu bulunamadı: " + args[1], NamedTextColor.RED));
                    return true;
                }
                protectionManager.giveProtection(target);
                sender.sendMessage(Component.text("✦ " + target.getName() + " oyuncusuna 30 dakika koruma verildi.", PURPLE_LIGHT));
                target.sendMessage(Component.text("✦ Sana 30 dakika PvP koruması verildi!", PURPLE_LIGHT).decorate(TextDecoration.BOLD));
            }

            case "status" -> {
                if (!sender.hasPermission("pvpprotection.admin")) {
                    sender.sendMessage(Component.text("Bu komutu kullanma yetkin yok!", NamedTextColor.RED));
                    return true;
                }
                Map<UUID, Long> protectedMap = protectionManager.getProtectedPlayers();
                if (protectedMap.isEmpty()) {
                    sender.sendMessage(Component.text("Şu anda korumalı oyuncu yok.", NamedTextColor.YELLOW));
                    return true;
                }
                sender.sendMessage(Component.text("━━ Korumalı Oyuncular ━━", PURPLE_DARK).decorate(TextDecoration.BOLD));
                for (Map.Entry<UUID, Long> entry : protectedMap.entrySet()) {
                    Player p = Bukkit.getPlayer(entry.getKey());
                    String name = (p != null) ? p.getName() : entry.getKey().toString().substring(0, 8) + "...";
                    long remaining = Math.max(0, entry.getValue() - System.currentTimeMillis());
                    long mins = remaining / 60000;
                    long secs = (remaining % 60000) / 1000;
                    sender.sendMessage(Component.text("  • ", PURPLE_LIGHT)
                            .append(Component.text(name, NamedTextColor.WHITE))
                            .append(Component.text(" — ", NamedTextColor.GRAY))
                            .append(Component.text(String.format("%02d:%02d", mins, secs), PURPLE_LIGHT)));
                }
            }

            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("━━━━━ PvP Koruma ━━━━━", PURPLE_DARK).decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text("/pvpprotect remove", PURPLE_LIGHT)
                .append(Component.text(" — Korumanı kaldır", NamedTextColor.GRAY)));
        if (sender.hasPermission("pvpprotection.admin")) {
            sender.sendMessage(Component.text("/pvpprotect give <oyuncu>", PURPLE_LIGHT)
                    .append(Component.text(" — Oyuncuya koruma ver", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/pvpprotect status", PURPLE_LIGHT)
                    .append(Component.text(" — Korumalı oyuncuları listele", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/pvpprotect reload", PURPLE_LIGHT)
                    .append(Component.text(" — Ayarları yeniden yükle", NamedTextColor.GRAY)));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            if (sender.hasPermission("pvpprotection.admin")) {
                return Arrays.asList("remove", "give", "status", "reload");
            }
            return List.of("remove");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("pvpprotection.admin")) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
