# Flunar-Bauserver

Java-Plugin-Port der Bauserver-Skripte für **Paper 26.1.2 und neuer**. Das Projekt wird mit Java 25 gebaut. `16_troll.sk` ist auf Wunsch nicht enthalten.

## Projektstruktur

| Paket | Aufgabe |
| --- | --- |
| `api` | Öffentliche Java-API für weitere Flunar-Plugins |
| `chat` | Chat, Prefix, Tablist und PlaceholderAPI-Anbindung |
| `commands` | Befehle nach Fachbereich; `/projekt` besitzt einzelne Subcommand-Klassen |
| `database` | Repositories, Schema und thread-sichere Caches |
| `gui` | Projekt-, Feature- und Werkzeug-Inventare |
| `listener` | Ereignisbasierter Zugriffsschutz und Spielmechaniken |
| `manager` | Lebenszyklus und zentrale Infrastruktur, insbesondere der DB-Manager |
| `model` | Unveränderliche Datenmodelle |
| `service` | Fachlogik für Welten, Backup, Onlinezeit, TPA, Fix und BlockDisplays |
| `settings` | Laden und Validieren von `settings.yml`, `database.yml` und `labymod.yml` |
| `utils` | Nachrichten- und Spieler-Hilfen |

Kleine zusammengehörige Befehle sind bewusst gebündelt. So verarbeitet `WeatherTimeCommands` `/sun`, `/rain`, `/storm`, `/day` und `/night`. Umfangreiche Router wie `/projekt` delegieren hingegen jeden Unterbefehl an eine eigene Klasse.

## Installation

1. Paper 26.1.2+ mit Java 25 verwenden.
2. `mvn package` ausführen.
3. `target/Flunar-Bauserver-2.0.4.jar` in den Plugin-Ordner kopieren.
4. Server einmal starten und anschließend `plugins/Flunar-Bauserver/database.yml` konfigurieren.
5. Server vollständig neu starten.

Ohne vollständig erreichbare Datenbank deaktiviert sich das Plugin beim Start. Dadurch kann kein Spieler einen halb initialisierten Zustand erreichen.

## Konfiguration

- `settings.yml`: Servertexte, Spawn, Pfade, Templates, Onlinezeit und Ban-Gründe.
- `database.yml`: ausschließlich MariaDB-Zugang und Pool-Einstellungen.
- `labymod.yml`: optionale LabyMod-Funktionen wie Fancy Font, Discord RPC, Spielmodus und Subtitle.

## Optionale Integrationen

- PlaceholderAPI 2.12.3 und LuckPerms API 5.5 sind in Maven als `provided` eingebunden.
- LabyMod Server API 1.0.10 ist ebenfalls als optionale `provided`-Abhängigkeit eingebunden.
- Alle drei Plugins bleiben optionale Laufzeitabhängigkeiten und werden nicht in das Plugin-JAR kopiert.
- LuckPerms-Prefix und Gruppenpriorität werden über die LuckPerms-Erweiterung von PlaceholderAPI gelesen.
- Paper lädt PlaceholderAPI, LuckPerms und LabyModServerAPI – sofern vorhanden – vor dem Bauserver-Plugin. Images und FancyNpcs werden erst nach abgeschlossenem Projekt-Autoload geladen.

Für die LuckPerms-Platzhalter muss neben PlaceholderAPI auch dessen LuckPerms-Expansion auf dem Server installiert sein.

## Locator-Bar

Das Feature `locatorbar` ist standardmäßig deaktiviert. Die Gamerule wird beim Laden jeder Welt gesetzt, also auch für neu erstellte oder automatisch geladene Projekt- und Privatwelten. Über `/feature` kann die Locator-Bar serverweit ein- oder ausgeschaltet werden.

Das Datenbankschema bleibt kompatibel zu den bisherigen Skripten. Die vorhandenen Tabellen und Daten können weiterverwendet werden.

Beim Start protokolliert das Plugin Verbindung, Schema-Prüfung, Cache-Laden und Dauer getrennt. Alte URLs mit `jdbc:mysql://` werden automatisch auf den MariaDB-Treiber umgestellt. Erst nach erfolgreichem Datenbank- und Welt-Autoload meldet es sich als vollständig betriebsbereit.

## Datenbank und Performance

Laufende SQL-Operationen laufen über einen begrenzten HikariCP-Pool auf eigenen Threads. Nur der einmalige, zeitlich begrenzte Datenbankstart wird abgewartet, damit Caches und Autoload vor anderen Plugins garantiert bereit sind. Minecraft-Events führen keine synchronen SQL-Abfragen aus. Projektzugriff, Bans, Whitelist, Features, Autoload und Onlinezeit werden aus thread-sicheren Speichercaches gelesen.

Onlinezeiten werden spielerübergreifend gesammelt und standardmäßig alle 60 Sekunden mit einem gemeinsamen JDBC-Batch über eine Verbindung gespeichert. Der MariaDB-Treiber schreibt diesen Batch als gebündeltes Multi-Value-Statement. Das Intervall ist über `online-time.database-save-seconds` konfigurierbar; beim Stoppen wird der letzte Batch vollständig abgewartet.

Welten, Inventare, Entities und Bukkit-Spielerobjekte werden ausschließlich auf dem Server-Thread verändert. Große `/fix`-Bereiche werden auf 20.000 Prüfungen pro Tick begrenzt und blockieren den Server daher nicht in einer einzigen langen Schleife.
