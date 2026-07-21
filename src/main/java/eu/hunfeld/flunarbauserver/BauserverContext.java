package eu.hunfeld.flunarbauserver;

import eu.hunfeld.flunarbauserver.database.AutoloadRepository;
import eu.hunfeld.flunarbauserver.database.FeatureRepository;
import eu.hunfeld.flunarbauserver.database.ModerationRepository;
import eu.hunfeld.flunarbauserver.database.OnlineTimeRepository;
import eu.hunfeld.flunarbauserver.database.PrivateWorldRepository;
import eu.hunfeld.flunarbauserver.database.ProjectAccessRepository;
import eu.hunfeld.flunarbauserver.database.ProjectInfoRepository;
import eu.hunfeld.flunarbauserver.database.ProjectRepository;
import eu.hunfeld.flunarbauserver.manager.database.DatabaseManager;
import eu.hunfeld.flunarbauserver.service.BackupService;
import eu.hunfeld.flunarbauserver.service.TeleportService;
import eu.hunfeld.flunarbauserver.service.TpaService;
import eu.hunfeld.flunarbauserver.service.VanishService;
import eu.hunfeld.flunarbauserver.service.WorldService;
import eu.hunfeld.flunarbauserver.service.WorldTransferService;
import eu.hunfeld.flunarbauserver.settings.Settings;
import eu.hunfeld.flunarbauserver.utils.Messages;
import org.bukkit.plugin.Plugin;

public record BauserverContext(
    Plugin plugin,
    Settings settings,
    Messages messages,
    DatabaseManager database,
    ProjectRepository projects,
    ProjectAccessRepository projectAccess,
    ProjectInfoRepository projectInfos,
    AutoloadRepository autoload,
    PrivateWorldRepository privateWorlds,
    FeatureRepository features,
    ModerationRepository moderation,
    OnlineTimeRepository onlineTime,
    WorldService worlds,
    WorldTransferService worldTransfers,
    TeleportService teleports,
    TpaService tpa,
    VanishService vanish,
    BackupService backups) {}
