package eu.hunfeld.flunarBauserver;

import eu.hunfeld.flunarBauserver.database.AutoloadRepository;
import eu.hunfeld.flunarBauserver.database.FeatureRepository;
import eu.hunfeld.flunarBauserver.database.ModerationRepository;
import eu.hunfeld.flunarBauserver.database.OnlineTimeRepository;
import eu.hunfeld.flunarBauserver.database.PrivateWorldRepository;
import eu.hunfeld.flunarBauserver.database.ProjectAccessRepository;
import eu.hunfeld.flunarBauserver.database.ProjectInfoRepository;
import eu.hunfeld.flunarBauserver.database.ProjectRepository;
import eu.hunfeld.flunarBauserver.manager.database.DatabaseManager;
import eu.hunfeld.flunarBauserver.service.BackupService;
import eu.hunfeld.flunarBauserver.service.TeleportService;
import eu.hunfeld.flunarBauserver.service.TpaService;
import eu.hunfeld.flunarBauserver.service.VanishService;
import eu.hunfeld.flunarBauserver.service.WorldService;
import eu.hunfeld.flunarBauserver.service.WorldTransferService;
import eu.hunfeld.flunarBauserver.settings.Settings;
import eu.hunfeld.flunarBauserver.utils.Messages;

public record BauserverContext(
    FlunarBauserver plugin,
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
