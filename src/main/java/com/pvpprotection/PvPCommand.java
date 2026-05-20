package com.pvpprotection;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import java.util.*;

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
        if (args.length == 0) { sendHelp(sender); return true; }
        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("pvpprotection.admin")) { sender.sendMessage(Component.text("Yetkin yok!", NamedTextColor.RED)); return true; }
                plugin.reloadConfig();
                sender.sendMessage(Component.text("✦ Ayarlar yeniden yüklendi!", PURPLE_LIGHT));
            }
            case "remove" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage(Component.text("Sadece oyuncular!", NamedTextColor.RED)); return true; }
                if (!protectionManager.isProtected(player)) { player.sendMessage(Component.text("Aktif koruман yok.", NamedTextColor.YELLOW)); return true; }
                protectionManager.removeProtection(player);
                player.sendMessage(Component.text("⚔ PvP koruман kaldırıldı!", NamedTextColor.RED).decorate(TextDecoration.BOLD));
            }
            case "give" -> {
                if (!sender.hasPermission("pvpprotection.admin")) { sender.sendMessage(Component.text("Yetkin yok!", NamedTextColor.RED)); return true; }
                if (args.length < 2) { sender.sendMessage(Component.text("Kullanım: /pvpprotect give <oyuncu>", NamedTextColor.RED)); return true; }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { sender.sendMessage(Component.text("Oyuncu bulunamadı!", NamedTextColor.RED)); return true; }
                protectionManager.giveProtection(target);
                sender.sendMessage(Component.text("✦ " + target.getName() + " oyuncusuna koruma verildi.", PURPLE_LIGHT));
                target.sendMessage(Component.text("✦ Sana 30 dakika PvP koruması verildi!", PURPLE_LIGHT).decorate(TextDecoration.BOLD));
            }
            case "status" -> {
                if (!sender.hasPermission("pvpprotection.admin")) { sender.sendMessage(Component.text("Yetkin yok!", NamedTextColor.RED)); return true; }
                Map<UUID, Long> map = protectionManager.getProtectedPlayers();
                if (map.isEmpty()) { sender.sendMessage(Component.text("Korumalı oyuncu yok.", NamedTextColor.YELLOW)); return true; }
                sender.sendMessage(Component.text("━━ Korumalı Oyuncular ━━", PURPLE_DARK).decorate(TextDecoration.BOLD));
                map.forEach((uuid, expiry) -> {
                    Player p = Bukkit.getPlayer(uuid);
                    String name = p != null ? p.getName() : uuid.toString().substring(0, 8);
                    long rem = Math.max(0, expiry - System.currentTimeMillis());
                    sender.sendMessage(Component.text("  • ", PURPLE_LIGHT)
                        .append(Component.text(name, NamedTextColor.WHITE))
                        .append(Component.text(" — ", NamedTextColor.GRAY))
                        .append(Component.text(String.format("%02d:%02d", rem/60000, (rem%60000)/1000), PURPLE_LIGHT)));
                });
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("━━━━━ PvP Koruma ━━━━━", PURPLE_DARK).decorate(TextDecoration.BOLD));
        sender.sendMessage(Component.text("/pvpprotect remove", PURPLE_LIGHT).append(Component.text(" — Korumanı kaldır", NamedTextColor.GRAY)));
        if (sender.hasPermission("pvpprotection.admin")) {
            sender.sendMessage(Component.text("/pvpprotect give <oyuncu>", PURPLE_LIGHT).append(Component.text(" — Koruma ver", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/pvpprotect status", PURPLE_LIGHT).append(Component.text(" — Listeye bak", NamedTextColor.GRAY)));
            sender.sendMessage(Component.text("/pvpprotect reload", PURPLE_LIGHT).append(Component.text(" — Ayarları yükle", NamedTextColor.GRAY)));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return sender.hasPermission("pvpprotection.admin") ? Arrays.asList("remove","give","status","reload") : List.of("remove");
        if (args.length == 2 && args[0].equalsIgnoreCase("give") && sender.hasPermission("pvpprotection.admin"))
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase())).toList();
        return List.of();
    }
}
