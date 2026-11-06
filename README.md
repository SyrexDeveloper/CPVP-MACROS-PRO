# CPVP MACROS PRO 3.6.2

**Minecraft 1.21.11 · Fabric · client-side**

`cpvp-macros-3.6-1.21.11.jar` tersine mühendislikle çözüldü, kaynak koduna çevrildi ve
**CPVP Macros Pro 3.6.2** olarak yeniden derlendi. Orijinal yazar: **priyme**. Bu sürüm
kristal hatalarını düzeltir, yeni PRO modülleri ekler ve yeni ayarlarla gelir.


---

## 🔴 ACİL DÜZELTME — açılış çökmesi (`Mixin transformation of class_746 failed`)

İlk Pro derlemesinde oyun açılırken şu hata alınıyordu:

```
java.lang.ClassCastException: class java.util.ArrayList cannot be cast to
class org.objectweb.asm.tree.AnnotationNode
  at com.llamalad7.mixinextras.wrapper.factory.FactoryRedirectWrapperMixinTransformer.transform(...)
Caused by: java.lang.RuntimeException: Mixin transformation of net.minecraft.class_746 failed
```

### Sebep (net)
MixinExtras'ın `FactoryRedirectWrapperMixinTransformer` sınıfı, `@Redirect` ek açıklamasının
`at` değerini **koşulsuz** olarak `AnnotationNode`'a cast ediyor:

```java
AnnotationNode at = (AnnotationNode) Annotations.getValue(redirect, "at"); // bytecode: checkcast AnnotationNode
```

Decompiler (Vineflower) tüm ek açıklamaları dizi formunda yazdığı için
(`at = {@At(...)}`) bizim derlememizde `at` **`ArrayList`** olarak kodlandı →
cast patladı → `ClientPlayerEntity` (`class_746`) mixinlenirken oyun çöktü.

Orijinal 3.6 jar'ında aynı alan **bare `AnnotationNode`** olarak kodlanmış (bu yüzden 3.6 çalışıyordu).
Doğrulama (ASM ile ek açıklama dökümü):

| Jar | `NoSlowMixin` → `@Redirect.at` |
|---|---|
| Orijinal 3.6 | `AnnotationNode` ✅ |
| İlk Pro derlemesi | `ArrayList` ❌ (çökme) |
| **Düzeltilmiş Pro 3.6.2** | `AnnotationNode` ✅ |

### Çözüm
Mixin sınıfları yeniden derlenmiyor; **orijinal 3.6 jar'ındaki mixin bytecode'ları**
`src/main/resources/mixin-original/` altında saklanıyor ve `jar` görevi paketleme
sırasında derlenmiş mixin sınıflarının yerine bunları koyuyor (Gradle `doLast`, build logunda
`PRO MIXIN FIX: 12 orijinal mixin sinifi geri yerlestirildi.`).

Doğrulama: çıktı jar'daki 12 mixin sınıfı, orijinal 3.6 jar'ıyla **MD5 olarak birebir aynı**.

> Yani Pro'nun tüm yeni özellikleri (Auto Totem Pro, Shield Swap Pro, Anchor Aura Pro,
> kristal bug fix'leri) korunurken mixin katmanı 3.6'daki çalışan hâliyle aynı.

---

## 📦 Ne değişti? (3.6 → 3.6.2 PRO)

### 1) Kristal bug'ları düzeltildi (`CrystalMacro`)

| # | Bug | Düzeltme |
|---|---|---|
| 1 | **Takılı kalma:** kullanım tuşu obsidyen yerleştirme bekleme süresindeyken bırakılırsa `placingObsidian` bayrağı sonsuza kadar `true` kalıyordu → makro tamamen kilitleniyordu | Tuş bırakıldığında bekleyen obsidyen işlemi iptal edilir, bayrak ve sayaçlar sıfırlanır, elindeki slot eski hâline döner |
| 2 | **Aynı tick'te çift aksiyon:** mevcut kristali kırarken aynı tick'te yeni kristal de yerleştiriliyordu (double-place / sunucu reddi / packet spam) | Kırma ve yerleştirme artık birbirini dışlıyor (`else if` zinciri) |
| 3 | **Elde obsidyen kalması:** obsidyen konup kristal bulunamazsa oyuncu obsidyen slotunda kalıyordu | Kristal hotbar'da yoksa otomatik olarak eski slota dönülür |
| 4 | **Kendini patlatma:** kristal ayağının dibine konabiliyordu | Yeni **Self Safe** koruması: patlama merkezi `Min Self Distance` (varsayılan 2.0 blok) içindeyse yerleştirme yapılmaz |

> Bu 4 madde, modun orijinal davranışını bozmadan eklendi; hepsi ayarlardan kapatılabilir.

### 2) Yeni PRO modülleri (CPvP sekmesinde)

| Modül | Ne yapar | Ayarlar |
|---|---|---|
| **Auto Totem Pro** | Can eşiğin altına düşünce hotbar'daki (yoksa envanterdeki) totemi offhand'e takar, sonra hotbar slotunu geri alır. Offhand'de totem varken dokunmaz. | Health Threshold, Swap Delay |
| **Shield Swap Pro** | Can düşükken kalkanı offhand'e alır ve kaldırır, can toplayınca indirir. Totem önceliklidir. | Health Threshold |
| **Anchor Aura Pro** | Çevredeki (1–6 blok) respawn anchor'larını otomatik **şarj eder** (glowstone) ve güvenli mesafedeyken **patlatır**. Kristal yerine anchor oynayanlar için tek tuş. | Radius, Delay, Safe Distance |

**Kullanım:** ClickGUI → **CPvP** sekmesi → modül adına tıkla (aç/kapa), sağındaki
kalemden tuş ata (keybind). Varsayılan olarak hepsi **kapalı** gelir.

### 3) Kristal modülüne eklenen 2 yeni ayar
- **Self Safe (PRO)** – kendine hasar koruması açık/kapalı
- **Min Self Distance (PRO)** – patlama merkezine minimum mesafe (0.1 blok hassasiyet, 0–8)

### 4) Marka / sürüm
- Ad: **CPVP Macros Pro**, sürüm: **3.6.2**
- Mod id aynı (`legitcrystalmacro`) → **mevcut config'in ve keybind'lerin korunur**
- `config/legitcrystalmacro.json` içinde tüm PRO ayarları otomatik olarak belirir

---

## ⚠️ Kurulum notları (önemli)

1. **Eski sürümü sil:** `mods` klasöründeki `cpvp-macros-3.6-1.21.11.jar`'ı kaldır,
   yerine `cpvp-macros-pro-3.6.2.jar` koy. İkisi birlikte **çalışmaz** (aynı mod id).
2. **Fabric API** `0.141.6+1.21.11` (veya daha yeni) kurulu olmalı.
3. `fabric.mod.json` içindeki, var olmayan iki jar-in-jar referansı (`config-0.27.2.jar`,
   `core-0.27.2.jar`) kaldırıldı — bunlar dosyada zaten yoktu ve bazı durumlarda
   başlatma hatasına yol açıyordu.
4. **ModMenu entegrasyonu** kaldırıldı (ModMenu API uyuşmazlığı). Ayar ekranına
   modun kendi tuşuyla girilir (varsayılan: ClickGUI tuşu, kontrollerden değiştirilebilir).

---

## 🔧 Derleme

```bash
./gradlew build
# build/libs/cpvp-macros-pro-3.6.2.jar
```

- Gradle 9.8.2 · Loom 1.19.3 · Yarn `1.21.11+build.6` · Java 21 hedefi
- Mixin hedefleri intermediary olarak korunur (üretim jar'ı doğrudan çalışır)
- `-Xmx750m` ile de derlenir (düşük bellekli makinede `gradle.properties`'teki
  `org.gradle.jvmargs` değerini kullan)

---

## 📁 Proje yapısı

```
src/main/java/dev/priyme/legitcrystalmacro/
├── LegitCrystalMacroClient.java      # giriş noktası, HUD, keybind'ler
├── config/MacroConfig.java           # tüm ayarlar (+ PRO alanları)
├── config/ConfigProfiles.java        # profil/preset sistemi
├── gui/ClickGuiScreen.java           # ClickGUI (+ 3 yeni PRO modülü)
├── gui/…                             # tema, palet, ikonlar, profil paneli
├── macro/
│   ├── CrystalMacro.java             # ← 4 bug fix + Self Safe
│   ├── AnchorMacro / SafeAnchor / DoubleAnchor
│   ├── AutoMaceMacro / MaceSwapMacro / sattela/* (mace seti olduğu gibi duruyor)
│   ├── pro/AutoTotemPro.java         # ★ YENİ
│   ├── pro/ShieldSwapPro.java        # ★ YENİ
│   ├── pro/AnchorAuraPro.java        # ★ YENİ
│   └── … (AimAssist, TriggerBot, TapReset, Trajectories, ShieldBreaker, …)
├── macro/MacroManager.java           # modül kayıt + keybind yönetimi (PRO modülleri eklendi)
├── mixin/…                           # 12 mixin (olduğu gibi)
└── util/…                            # InventoryUtil, BlockInteract, KeyInput, …
```

---

## 📌 Bilinen sınırlar

- `AnchorAuraPro` **Overworld**'de çalışır (Nether'de anchor patlamaz, bu yüzden orada
  sadece şarj etmez — mantık gereği atlanır).
- Self Safe koruması **mesafe** tabanlıdır; vanilla patlama hasar formülüyle tam
  hesap yapmaz (bu, patlatma gecikmesini artırırdı). Mesafeyi kendi oyun stiline
  göre ayarla (2.0 güvenli, 1.0 agresif).
- Otomatik makrolar PvP sunucularında **yasak** olabilir; tespit riski sana aittir.
- Orijinal modun lisansı *All Rights Reserved*. Bu Pro derlemesi kişisel kullanım
  içindir; yeniden dağıtım hakkı orijinal yazarındır.

---

## Tuşlar (varsayılan)

| Tuş | İşlev |
|---|---|
| ClickGUI tuşu | Menüyü aç (mod ayarlarından değiştirilebilir) |
| Modül başına atanan tuş | O modülü aç/kapa |

İlk açılışta `config/legitcrystalmacro.json` oluşur; tüm PRO ayarları orada görünür:

```json
{
  "proAutoTotemEnabled": false,
  "proAutoTotemHealth": 12,
  "proAutoTotemDelay": 1,
  "proShieldSwapEnabled": false,
  "proShieldHealth": 14,
  "proAnchorAuraEnabled": false,
  "proAnchorAuraRange": 4,
  "proAnchorAuraDelay": 2,
  "proAnchorAuraSafeDistance": 3,
  "proCrystalSelfSafe": true,
  "proCrystalMinSelfDistance": 2.0
}
```
