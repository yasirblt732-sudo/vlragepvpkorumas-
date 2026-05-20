# ⚔ PvP Koruma Plugin — Paper 1.20.2

## 📋 Özellikler

- ✅ Oyuncu sunucuya girince **otomatik 30 dakika PvP koruması** verilir
- ✅ Slot barın **üzerinde** mor renkte sayaç gösterilir
- ✅ Sayaç gerçek zamanlı **SS:DD** formatında geri sayar
- ✅ Koruma bitince oyuncuya **başlık + chat mesajı** ile uyarı verilir
- ✅ Ok atışları da dahil tüm PvP saldırıları engellenir
- ✅ Korumalıyken **başkasına da saldıramazsın**
- ✅ İsteğe bağlı korumanı `/pvpprotect remove` ile kaldırabilirsin
- ✅ Admin komutları ile manuel koruma ver/kontrol et

---

## 🖥 Action Bar Görünümü

```
✦ PVP KORUMASI AKTİF ✦     ← Koyu mor, kalın
⏱ 29:45 kaldı               ← Açık mor, sayaç
────────────────────────────
[  Slot Bar (envanter)  ]
```

---

## 🔨 Kurulum

### Gereksinimler
- Java 17+
- Maven 3.8+
- Paper 1.20.2

### Derleme (Build)
```bash
cd PvPProtection
mvn clean package
```
`target/PvPProtection-1.0.0.jar` dosyası oluşur.

### Sunucuya Kurma
1. `PvPProtection-1.0.0.jar` dosyasını sunucunun `plugins/` klasörüne koy
2. Sunucuyu yeniden başlat
3. `plugins/PvPProtection/config.yml` dosyasından ayarları düzenle

---

## ⚙ Konfigürasyon (config.yml)

```yaml
protection:
  duration-minutes: 30    # Koruma süresi (dakika)
  update-interval-ticks: 20  # Action bar güncelleme hızı (20 = 1sn)
```

---

## 💬 Komutlar

| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/pvpprotect remove` | Kendi korumanı kaldır | Herkes |
| `/pvpprotect give <oyuncu>` | Oyuncuya koruma ver | pvpprotection.admin |
| `/pvpprotect status` | Korumalı oyuncuları listele | pvpprotection.admin |
| `/pvpprotect reload` | Config'i yeniden yükle | pvpprotection.admin |

---

## 🔐 İzinler

| İzin | Açıklama | Varsayılan |
|------|----------|------------|
| `pvpprotection.admin` | Admin komutları | OP |
| `pvpprotection.bypass` | Korumalı oyunculara saldırabilir | false |

---

## 📁 Dosya Yapısı

```
PvPProtection/
├── pom.xml
└── src/main/
    ├── java/com/pvpprotection/
    │   ├── PvPProtection.java      ← Ana plugin sınıfı
    │   ├── ProtectionManager.java  ← Koruma süresi yönetimi
    │   ├── ActionBarTask.java      ← Mor sayaç göstergesi
    │   ├── PvPListener.java        ← Saldırı engelleme
    │   └── PvPCommand.java         ← /pvpprotect komutu
    └── resources/
        ├── plugin.yml
        └── config.yml
```
