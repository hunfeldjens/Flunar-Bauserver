<div align="center">

# Flunar-Bauserver

**Das zentrale Paper-Plugin für Projektwelten, Bauteams und Serververwaltung auf Flunar.de.**

![Version](https://img.shields.io/badge/Version-3.1.11-1FADFF?style=for-the-badge)
![Paper](https://img.shields.io/badge/Paper-26.1.2%2B-F7A81B?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-Async-003545?style=for-the-badge&logo=mariadb&logoColor=white)

</div>

Flunar-Bauserver bündelt die komplette Verwaltung des Bauserver-Netzwerks in einem modularen Paper-Plugin. Es verwaltet Projekte und Privatwelten, schützt Welten und Zugänge, stellt Team- und Moderationswerkzeuge bereit und hält häufig benötigte Daten in schnellen, thread-sicheren Caches.

## Highlights

- Vollständiges Projekt- und Weltmanagement mit GUI
- Eigene Privatwelt pro Spieler mit verzögertem, sicherem Auto-Unload
- Projekt-Whitelist, Projekt-Bans, Autoload, Export und Info-Seiten
- Zentrales Bauserver-Menü für alle verfügbaren Untermenüs
- Serverweite Feature-Schalter inklusive Gamerules und Locator-Bar
- Moderationssystem mit Grund-IDs, Ban-/Kick-Historie und Sicherheitsprüfungen
- LuckPerms-Prefixe, gewichtete Tab-Sortierung und Nametag-Anzeige
- Private Nachrichten mit `/msg` und `/r` sowie Builder-Teamchat
- TPA-System mit anklickbarer Annahme und Soundfeedback
- Onlinezeit-, AFK- und Aktivitätsverwaltung
- Optionale PlaceholderAPI- und LabyMod-Integration
- Asynchrone MariaDB-Zugriffe mit HikariCP, Caches und gebündelten Writes
- Öffentliche Java-API für weitere Flunar-Plugins

## Funktionen

### Projekte und Welten

- Projektwelten als `normal`, `flat` oder `air` erstellen
- Projekte über ein Inventar-Menü suchen, öffnen und betreten
- Besitzer, Beschreibung, Icon und Whitelist-Status anzeigen
- Projektinformationen als eigene GUI-Seiten verwalten
- Spieler hinzufügen, entfernen, bannen, entbannen oder aus einer Welt werfen
- Autoload für einzelne Projekte steuern
- Projekte und Privatwelten über externe Scripts exportieren
- Gelöschte Projekte sicher entladen und anwesende Spieler zurücksetzen
- Einheitliche Gamerules direkt beim Erstellen und Laden einer Welt anwenden
- Projektzugriffe vollständig aus dem Cache prüfen

### Privatwelten

Jeder berechtigte Spieler erhält über `/privat` eine eigene, persistente Welt. Verlässt der letzte Spieler eine Privatwelt, wird sie gespeichert und nach fünf Minuten entladen. Betritt während der Wartezeit erneut jemand die Welt, wird das Entladen automatisch abgebrochen.

### Schutz und Features

- Zutritt zum Bauserver nur mit `bauserver.access`
- Hauptwelt `world` gegen Bauen ohne `bauserver.builder` geschützt
- Laufende Kontrolle bei Änderungen der LuckPerms-Rechte
- Projekt-Whitelist und Projekt-Bans werden auch bei bereits anwesenden Spielern durchgesetzt
- Locator-Bar und Advancements standardmäßig deaktivierbar
- Weltregeln werden auf Haupt-, Projekt- und Privatwelten synchron gehalten
- Netherite-Aufzug: Springen nach oben, Schleichen nach unten
- Entity-Sichtbarkeit ohne Spieler oder Tiere unsichtbar zu machen

Folgende Features können zentral über `/feature` geschaltet werden:

```text
damage, break, place, inventory, pickup, hunger, weather, craft,
drop, farmland, explosion, blockbreak, blockplace, blockdamage,
mobspawn, summon, operator, firespread, leafdecay, liquidflow,
itemdamage, portal, gravity, falldamage, daynight, commandblocks,
mobgriefing, keepinventory, locatorbar, advancements
```

### Benutzeroberflächen und Bauwerkzeuge

- `/bs` als zentrales, permission-basiertes Bauserver-Menü
- Projekt-, Projektinfo-, Feature-, Tools-, Backup- und Onlinezeit-GUI
- Ban- und Kick-Historie mit Spielerköpfen und Details
- Operator-Items und spezielle Minecart-/Block-Werkzeuge
- Werkbank, Amboss und Enderkiste als mobile Menüs
- `/fix 1`, `/fix 2` und Partikelmarkierungen für große Bereiche
- BlockDisplay-Auswahl und Bearbeitung

### Chat, Tablist und Team

- LuckPerms-Prefixe im Chat, in der Tablist und über Spielerköpfen
- Tab-Sortierung nach der höchsten LuckPerms-Gruppengewichtung
- PlaceholderAPI-Unterstützung für Prefix und Gruppenpriorität
- Private Nachrichten über `/msg` mit Antwortfunktion `/r`
- Teamchat über `/tc` für Spieler mit `bauserver.builder`
- Formatierte Broadcasts, Chat-Clear und persönliche Chat-Bereinigung
- Join-Nachricht in der Actionbar
- Einheitliche MiniMessage-Ausgaben und konfigurierbarer Server-Prefix

### Moderation und Onlinezeit

- Kick- und Ban-System mit konfigurierbaren Grund-IDs
- Schutz vor Selbst-Kick und Selbst-Ban
- Bypass-Permissions für geschützte Teammitglieder
- Blitz-Effekt beim erfolgreichen Ban eines Online-Spielers
- Sicherheitsprüfung vor Ban-, Unban- und Whitelist-Änderungen
- GUI-basierte Ban- und Kick-Historie mit Zeitpunkt, Grund und Ausführer
- Aktive Spielzeit mit AFK-Erkennung und konfigurierbaren Intervallen
- Onlinezeit-GUI, Detailansicht, Reset und Export
- TPS-Anzeige für die aktuelle Last sowie die letzten 5 und 15 Minuten

## Anforderungen

| Komponente | Version |
| --- | --- |
| Paper | `26.1.2` oder neuer |
| Java | `25` oder neuer |
| MariaDB | erforderlich |
| Maven | `3.9+` zum Bauen |

## Installation

1. Repository klonen oder als ZIP herunterladen.
2. Im Projektordner die Plugin-JAR bauen:

   ```bash
   mvn clean package
   ```

3. `target/Flunar-Bauserver-3.1.11.jar` in den `plugins`-Ordner des Servers kopieren.
4. Den Server einmal starten, damit die Konfigurationsdateien erstellt werden.
5. MariaDB-Zugang und Serverpfade konfigurieren.
6. Den Server vollständig neu starten.

Kann die Datenbank beim Start nicht vollständig initialisiert werden, deaktiviert sich das Plugin. Dadurch arbeiten Befehle und Listener niemals mit nur teilweise geladenen Caches.

## Konfiguration

| Datei | Inhalt |
| --- | --- |
| `settings.yml` | Prefix, Titel, Hauptwelt, Spawn, Pfade, Onlinezeit und Ban-Gründe |
| `database.yml` | MariaDB-URL, Zugangsdaten und HikariCP-Einstellungen |
| `labymod.yml` | Optionale LabyMod-Funktionen, Texte, Banner und Serverlisten-Icon |
| `commands.yml` | Befehle, Aliasse und Permission-Nodes innerhalb der Plugin-JAR |

Die produktive Datenbankkonfiguration befindet sich nach dem ersten Start unter:

```text
plugins/Flunar-Bauserver/database.yml
```

Reale Datenbank-Zugangsdaten gehören niemals in ein öffentliches Git-Repository.

## Optionale Integrationen

| Plugin/API | Verwendung |
| --- | --- |
| LuckPerms | Prefixe, Gruppen und gewichtete Tab-Sortierung |
| PlaceholderAPI | Platzhalter in Chat-, Tab- und LabyMod-Texten |
| LabyMod Server API | Discord RPC, Spielstatus, Schriftart, Subtitle und Addon-Steuerung |
| Images | Wird bei Installation nach dem Bauserver-Autoload geladen |
| FancyNpcs | Wird bei Installation nach dem Bauserver-Autoload geladen |

Alle Integrationen sind optional. Fehlt eine davon, startet das Bauserver-Plugin weiterhin. Für `%luckperms_...%`-Platzhalter muss zusätzlich die LuckPerms-Erweiterung von PlaceholderAPI installiert sein.

## Befehle und Permissions

Die Permission-Stufen sind für LuckPerms-Gruppen gedacht und sollten dort hierarchisch vererbt werden.

### Projekte und Welten

| Befehl | Beschreibung | Permission |
| --- | --- | --- |
| `/projekt` | Projektübersicht öffnen | `bauserver.team` |
| `/projekt join <Projekt>` | Projekt betreten | `bauserver.team` |
| `/projekt kick <Spieler>` | Spieler aus der Projektwelt entfernen | `bauserver.team` |
| `/projekt info` | Projektinformationen öffnen oder verwalten | `bauserver.builder` |
| `/projekt ban <Spieler>` | Spieler auf dem aktuellen Projekt bannen | `bauserver.builder` |
| `/projekt create <Name> <Typ> <Icon> <Beschreibung>` | Projekt erstellen | `bauserver.srbuilder` |
| `/projekt remove <Projekt>` | Projekt löschen | `bauserver.srbuilder` |
| `/projekt whitelist <on\|off\|add\|remove>` | Projekt-Whitelist verwalten | `bauserver.srbuilder` |
| `/projekt unban <Spieler>` | Projekt-Ban aufheben | `bauserver.srbuilder` |
| `/projekt autoload <Welt> [on\|off]` | Welt-Autoload steuern | `bauserver.srbuilder` |
| `/projekt seticon <Item>` | Projekt-Icon ändern | `bauserver.srbuilder` |
| `/projekt admin` | Projektverwaltung öffnen | `bauserver.srbuilder` |
| `/projekt tp <Welt>` | Direkt zu einer Projektwelt teleportieren | `bauserver.admin` |
| `/projekt export <projekt\|privat> <Name>` | Welt exportieren | `bauserver.admin` |
| `/privat` | Eigene Privatwelt öffnen | `bauserver.privat` |
| `/world` | Zur Hauptwelt zurückkehren | `bauserver.team` |
| `/unloadallworlds` | Zusätzliche Welten sicher entladen | `bauserver.admin` |

### Teleport, Chat und Spieler

| Befehl | Beschreibung | Permission |
| --- | --- | --- |
| `/tpa <Spieler>` | Teleport-Anfrage senden | `bauserver.tpa` |
| `/tpaccept` | Teleport-Anfrage annehmen | `bauserver.tpa` |
| `/tpadeny` | Teleport-Anfrage ablehnen | `bauserver.tpa` |
| `/back` | Zur letzten Position zurückkehren | `bauserver.builder` |
| `/msg <Spieler> <Nachricht>` | Private Nachricht senden | `bauserver.team` |
| `/r <Nachricht>` | Auf die letzte private Nachricht antworten | `bauserver.team` |
| `/tc <Nachricht>` | Builder-Teamchat | `bauserver.builder` |
| `/fly [Spieler]` | Flugmodus umschalten; Fremdziel benötigt Builder-Rechte | `bauserver.team` |
| `/gm <Modus> [Spieler]` | Spielmodus ändern; Fremdziel benötigt Builder-Rechte | `bauserver.gamemode` |
| `/speed <0-10\|off>` | Flug-/Laufgeschwindigkeit ändern | `bauserver.builder` |
| `/size <Wert>` | Spielergröße ändern | `bauserver.builder` |
| `/ping [Spieler]` | Ping anzeigen | `bauserver.team` |
| `/tps` | TPS der letzten 1, 5 und 15 Minuten anzeigen | keine |
| `/me [Spieler]` | Spielerinformationen anzeigen | `bauserver.team` |

### Bauwerkzeuge

| Befehl | Beschreibung | Permission |
| --- | --- | --- |
| `/bs` | Zentrales Bauserver-Menü-Item erhalten | `bauserver.access` |
| `/tools` | Werkzeug- und Operator-Items öffnen | `bauserver.builder` |
| `/workbench` | Werkbank öffnen | `bauserver.builder` |
| `/anvil` | Amboss öffnen | `bauserver.builder` |
| `/enderchest` | Eigene Enderkiste öffnen | `bauserver.builder` |
| `/invsee <Spieler>` | Spielerinventar ansehen | `bauserver.team` |
| `/endersee <Spieler>` | Enderkiste eines Spielers ansehen | `bauserver.team` |
| `/hideentity` | Unterstützte Entity unsichtbar machen | `bauserver.builder` |
| `/showentity` | Unterstützte Entity sichtbar machen | `bauserver.builder` |
| `/fix` | Bereich markieren, reparieren und zurücksetzen | `bauserver.builder` |
| `/blockdisplay` | BlockDisplay-Werkzeuge verwenden | `bauserver.builder` |

### Weltzeit und Wetter

| Befehl | Beschreibung | Permission |
| --- | --- | --- |
| `/sun` | Sonniges Wetter setzen | `bauserver.builder` |
| `/rain` | Regen setzen | `bauserver.builder` |
| `/storm` | Gewitter setzen | `bauserver.builder` |
| `/tag` oder `/day` | Tag setzen | `bauserver.builder` |
| `/nacht` oder `/night` | Nacht setzen | `bauserver.builder` |

### Moderation und Administration

| Befehl | Beschreibung | Permission |
| --- | --- | --- |
| `/reasons` | Konfigurierte Moderationsgründe anzeigen | `bauserver.team` |
| `/kick <Spieler> <ID>` | Spieler mit Grund kicken | `bauserver.team` |
| `/ban <Spieler> <ID>` | Spieler permanent bannen | `bauserver.team` |
| `/unban <Spieler>` | Spieler entbannen | `bauserver.team` |
| `/banhistory` | Ban-Historie als GUI öffnen | `bauserver.team` |
| `/kickhistory` | Kick-Historie als GUI öffnen | `bauserver.team` |
| `/onlinezeit` | Eigene Onlinezeit anzeigen | `bauserver.team` |
| `/onlinezeit <Spieler>` | Onlinezeit eines Spielers anzeigen | `bauserver.admin` |
| `/onlinezeit reset <Spieler>` | Onlinezeit zurücksetzen | `bauserver.admin` |
| `/onlinezeit export` | Onlinezeitdaten exportieren | `bauserver.admin` |
| `/cc` | Chat für alle Spieler leeren | `bauserver.team` |
| `/pcc` | Eigenen Chat leeren | `bauserver.team` |
| `/broadcast <Nachricht>` | Formatierte Servernachricht senden | `bauserver.admin` |
| `/vanish` | Vollständigen Vanish umschalten | `bauserver.admin` |
| `/backup` | Backup-Menü öffnen | `bauserver.builder` |
| `/feature` | Feature-Menü öffnen | `bauserver.srbuilder` |
| `/featuredebug [Feature]` | Aktuelle Feature-Zustände prüfen | `bauserver.srbuilder` |
| `/wldebug [Spieler]` | Projektzugriff der aktuellen Welt prüfen | `bauserver.admin` |
| `/serverrestart` | Server mit Countdown stoppen | `bauserver.srbuilder` |
| `/prefix reload` | Prefixe und Tablist aktualisieren | `bauserver.admin` |
| `/reset confirm` | Persistente Datenbank-Caches neu laden | `bauserver.admin` |
| `/help [Kategorie]` | Permission-gefilterte Hilfe öffnen | `bauserver.team` |

### Permission-Nodes

| Permission | Zweck | Standard |
| --- | --- | --- |
| `bauserver.access` | Zutritt zum Bauserver | `false` |
| `bauserver.tpa` | TPA-System | `false` |
| `bauserver.privat` | Zugriff auf `/privat` | `false` |
| `bauserver.team` | Grundlegende Teamfunktionen | `false` |
| `bauserver.builder` | Bauwerkzeuge und erweiterte Projektfunktionen | `false` |
| `bauserver.srbuilder` | Projekt- und Featureverwaltung | `false` |
| `bauserver.admin` | Administrative Funktionen | `op` |
| `bauserver.gamemode` | Zugriff auf `/gamemode` und `/gm` | `false` |
| `bauserver.admin.bypass` | Schutz vor administrativen Eingriffen | `op` |
| `bauserver.ban.bypass` | Schutz vor Bans | `op` |

Beispiel mit LuckPerms:

```text
/lp group builder permission set bauserver.access true
/lp group builder permission set bauserver.tpa true
/lp group builder permission set bauserver.privat true
/lp group builder permission set bauserver.team true
/lp group builder permission set bauserver.builder true
```

## Datenbank und Performance

Zur Laufzeit blockiert keine SQL-Abfrage den Minecraft-Hauptthread:

- HikariCP verwaltet einen begrenzten Verbindungspool.
- Projekt-, Zugriffs-, Ban-, Whitelist-, Feature- und Autoload-Daten liegen in thread-sicheren Caches.
- Schreibvorgänge laufen über dedizierte Datenbank-Threads.
- Onlinezeiten werden gesammelt und als gemeinsamer JDBC-Batch gespeichert.
- Bukkit-Objekte wie Spieler, Welten und Inventare werden ausschließlich auf dem Server-Thread verändert.
- Große `/fix`-Bereiche werden über mehrere Ticks verteilt verarbeitet.

Nur die Datenbankinitialisierung wird beim Pluginstart kontrolliert abgewartet. Damit stehen Caches und Projekt-Autoload vollständig bereit, bevor Spieler oder abhängige Plugins auf das System zugreifen.

## Java-API

Andere Paper-Plugins können direkt auf die stabile `BauserverApi` zugreifen. Cache-basierte Lesezugriffe sind thread-sicher und führen keine SQL-Abfrage aus.

### Maven

Das Bauserver-Plugin muss zunächst im lokalen oder gemeinsamen Maven-Repository verfügbar sein:

```bash
mvn install
```

Danach im konsumierenden Plugin einbinden:

```xml
<dependency>
    <groupId>eu.hunfeld</groupId>
    <artifactId>flunar-bauserver</artifactId>
    <version>3.1.11</version>
    <scope>provided</scope>
</dependency>
```

In dessen `paper-plugin.yml`:

```yaml
dependencies:
  server:
    Flunar-Bauserver:
      load: BEFORE
      required: true
      join-classpath: true
```

### API abrufen

```java
import eu.hunfeld.flunarbauserver.FlunarBauserver;
import eu.hunfeld.flunarbauserver.api.BauserverApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

Plugin dependency = Bukkit.getPluginManager().getPlugin("Flunar-Bauserver");

if (!(dependency instanceof FlunarBauserver bauserver) || !bauserver.isEnabled()) {
    throw new IllegalStateException("Flunar-Bauserver ist nicht verfügbar");
}

BauserverApi api = bauserver.api();
```

### Prefix und Titel

```java
String prefixMiniMessage = api.prefix();
String titleMiniMessage = api.title();

Component prefix = api.prefixComponent();
Component title = api.titleComponent();
```

Die Adventure-Komponenten sind bereits geparst und können direkt verwendet werden:

```java
player.sendMessage(
    api.prefixComponent()
        .append(Component.space())
        .append(Component.text("Nachricht aus meinem Plugin"))
);
```

### Projekte und Features lesen

```java
boolean databaseReady = api.databaseReady();
List<Project> projects = api.projects();
Optional<Project> lobby = api.projectByWorld("projekt_Lobby");

boolean allowed = api.canEnterProject(
    player.getUniqueId(),
    "projekt_Lobby",
    false
);

boolean explosions = api.featureEnabled("explosion");
```

Ein `Project` stellt folgende Werte bereit:

```java
project.name();
project.description();
project.worldName();
project.owner();
project.whitelistActive();
project.icon();
```

### Projektzugriffe asynchron ändern

```java
api.setProjectWhitelist(playerId, "projekt_Lobby", true)
    .thenAccept(saved ->
        Bukkit.getScheduler().runTask(myPlugin, () -> {
            if (saved) {
                player.sendMessage("Whitelist-Eintrag gespeichert.");
            }
        })
    );
```

Verfügbare Schreibmethoden:

```java
CompletableFuture<Boolean> setProjectBan(
    UUID playerId,
    String worldName,
    boolean banned
);

CompletableFuture<Boolean> setProjectWhitelist(
    UUID playerId,
    String worldName,
    boolean whitelisted
);
```

Das Future liefert `true`, wenn eine Änderung erfolgreich gespeichert wurde. `false` bedeutet, dass der gewünschte Zustand bereits bestand oder der Datenbankvorgang nicht gespeichert werden konnte.

Callbacks laufen nicht automatisch auf dem Bukkit-Hauptthread. Änderungen an Spielern, Welten, Entities oder Inventaren müssen deshalb mit `Bukkit.getScheduler().runTask(...)` zurück auf den Server-Thread wechseln.

## Projektstruktur

```text
src/main/java/eu/hunfeld/flunarbauserver/
├── api/          Öffentliche Java-API
├── chat/         Chat, Tablist und Placeholder-Anbindung
├── commands/     Befehle nach Fachbereich
├── database/     Repositories und thread-sichere Caches
├── gui/          Inventar-Menüs
├── listener/     Event- und Schutzlogik
├── manager/      Zentrale Infrastruktur
├── model/        Datenmodelle
├── service/      Welt-, Backup- und Fachlogik
├── settings/     Konfigurationsverwaltung
└── utils/        Nachrichten und Hilfsfunktionen
```

Größere Systeme wie `/projekt` besitzen einen schlanken Router und jeweils eine eigene Klasse pro Unterbefehl. Kleine, eng zusammengehörige Funktionen bleiben gebündelt.

## Entwicklung

Projekt kompilieren:

```bash
mvn compile
```

Plugin-JAR bauen:

```bash
mvn clean package
```

Bauserver-API im lokalen Maven-Repository installieren:

```bash
mvn install
```

---

<div align="center">

**Flunar-Bauserver 3.1.11**<br>
Entwickelt für das Flunar.de Bauserver-Netzwerk

[hunfeld.eu](https://www.hunfeld.eu)

</div>
