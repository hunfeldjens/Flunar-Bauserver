package eu.hunfeld.flunarbauserver;

import eu.hunfeld.flunarbauserver.api.BauserverApi;
import eu.hunfeld.flunarbauserver.api.DefaultBauserverApi;
import eu.hunfeld.flunarbauserver.chat.TabListener;
import eu.hunfeld.flunarbauserver.chat.TabService;
import eu.hunfeld.flunarbauserver.commands.CommandRegistry;
import eu.hunfeld.flunarbauserver.commands.admin.*;
import eu.hunfeld.flunarbauserver.commands.backup.BackupCommand;
import eu.hunfeld.flunarbauserver.commands.chat.MessageCommand;
import eu.hunfeld.flunarbauserver.commands.chat.ReplyCommand;
import eu.hunfeld.flunarbauserver.commands.chat.TeamChatCommand;
import eu.hunfeld.flunarbauserver.commands.feature.FeatureCommand;
import eu.hunfeld.flunarbauserver.commands.feature.FeatureDebugCommand;
import eu.hunfeld.flunarbauserver.commands.moderation.*;
import eu.hunfeld.flunarbauserver.commands.onlinetime.OnlineTimeCommand;
import eu.hunfeld.flunarbauserver.commands.player.*;
import eu.hunfeld.flunarbauserver.commands.project.ProjectCommand;
import eu.hunfeld.flunarbauserver.commands.teleport.*;
import eu.hunfeld.flunarbauserver.commands.tools.BlockDisplayCommand;
import eu.hunfeld.flunarbauserver.commands.tools.FixCommand;
import eu.hunfeld.flunarbauserver.commands.world.*;
import eu.hunfeld.flunarbauserver.database.*;
import eu.hunfeld.flunarbauserver.database.FeatureRepository;
import eu.hunfeld.flunarbauserver.database.ModerationRepository;
import eu.hunfeld.flunarbauserver.database.OnlineTimeRepository;
import eu.hunfeld.flunarbauserver.database.PrivateWorldRepository;
import eu.hunfeld.flunarbauserver.gui.BackupMenu;
import eu.hunfeld.flunarbauserver.gui.BuilderServerMenu;
import eu.hunfeld.flunarbauserver.gui.FeatureMenu;
import eu.hunfeld.flunarbauserver.gui.ModerationHistoryMenu;
import eu.hunfeld.flunarbauserver.gui.OnlineTimeMenu;
import eu.hunfeld.flunarbauserver.gui.ProjectInfoMenu;
import eu.hunfeld.flunarbauserver.gui.ProjectMenu;
import eu.hunfeld.flunarbauserver.gui.ToolsMenu;
import eu.hunfeld.flunarbauserver.listener.*;
import eu.hunfeld.flunarbauserver.listener.BackupProtectionListener;
import eu.hunfeld.flunarbauserver.listener.BlockDisplayListener;
import eu.hunfeld.flunarbauserver.listener.FeatureListener;
import eu.hunfeld.flunarbauserver.listener.FixListener;
import eu.hunfeld.flunarbauserver.listener.OnlineTimeListener;
import eu.hunfeld.flunarbauserver.manager.database.DatabaseManager;
import eu.hunfeld.flunarbauserver.service.BackupCoordinator;
import eu.hunfeld.flunarbauserver.service.BackupService;
import eu.hunfeld.flunarbauserver.service.BlockDisplayService;
import eu.hunfeld.flunarbauserver.service.FeatureService;
import eu.hunfeld.flunarbauserver.service.FixService;
import eu.hunfeld.flunarbauserver.service.LabyModIntegration;
import eu.hunfeld.flunarbauserver.service.OnlineTimeService;
import eu.hunfeld.flunarbauserver.service.PrivateMessageService;
import eu.hunfeld.flunarbauserver.service.ServerListIconService;
import eu.hunfeld.flunarbauserver.service.TeleportService;
import eu.hunfeld.flunarbauserver.service.TpaService;
import eu.hunfeld.flunarbauserver.service.VanishService;
import eu.hunfeld.flunarbauserver.service.WorldService;
import eu.hunfeld.flunarbauserver.service.WorldTransferService;
import eu.hunfeld.flunarbauserver.settings.ConfigFiles;
import eu.hunfeld.flunarbauserver.settings.Settings;
import eu.hunfeld.flunarbauserver.utils.Messages;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FlunarBauserver extends JavaPlugin {
  private DatabaseManager database;
  private BackupService backups;
  private OnlineTimeService onlineTime;
  private WorldTransferService worldTransfers;
  private BauserverApi api;

  @Override
  public void onEnable() {
    ConfigFiles.Loaded config = ConfigFiles.load(this);
    Settings settings = config.settings();
    Messages messages = new Messages(settings);

    database = new DatabaseManager(this, config.database());
    ProjectRepository projects = new ProjectRepository(database);
    ProjectAccessRepository projectAccess = new ProjectAccessRepository(database, projects);
    ProjectInfoRepository projectInfos = new ProjectInfoRepository(database);
    AutoloadRepository autoload = new AutoloadRepository(database);
    PrivateWorldRepository privateWorlds = new PrivateWorldRepository(database);
    FeatureRepository features = new FeatureRepository(database);
    ModerationRepository moderation = new ModerationRepository(database);
    OnlineTimeRepository onlineTimes = new OnlineTimeRepository(database);

    FeatureService featureService = new FeatureService(features);
    WorldService worlds = new WorldService(this, settings, projectAccess, featureService, messages);
    worldTransfers = new WorldTransferService(this, settings);
    TeleportService teleports = new TeleportService(this);
    TpaService tpa = new TpaService();
    VanishService vanish = new VanishService(this);
    backups = new BackupService(this, settings);
    BauserverContext context =
        new BauserverContext(
            this,
            settings,
            messages,
            database,
            projects,
            projectAccess,
            projectInfos,
            autoload,
            privateWorlds,
            features,
            moderation,
            onlineTimes,
            worlds,
            worldTransfers,
            teleports,
            tpa,
            vanish,
            backups);
    api = new DefaultBauserverApi(context);

    BackupCoordinator backupCoordinator = new BackupCoordinator(context);
    BackupMenu backupMenu = new BackupMenu(context, backupCoordinator);
    ProjectMenu projectMenu = new ProjectMenu(context);
    ProjectInfoMenu projectInfoMenu = new ProjectInfoMenu(context);
    FeatureMenu featureMenu = new FeatureMenu(context, featureService);
    TabService tab = new TabService(this, messages);
    onlineTime = new OnlineTimeService(this, settings.onlineTime(), onlineTimes);
    OnlineTimeMenu onlineTimeMenu = new OnlineTimeMenu(context, onlineTime);
    ModerationHistoryMenu moderationHistoryMenu = new ModerationHistoryMenu(context);
    FixService fix = new FixService(this, messages);
    BlockDisplayService blockDisplays = new BlockDisplayService();
    ToolsMenu toolsMenu = new ToolsMenu(context);
    PrivateMessageService privateMessages = new PrivateMessageService();
    BuilderServerMenu builderServerMenu =
        new BuilderServerMenu(
            context,
            projectMenu,
            projectInfoMenu,
            toolsMenu,
            featureMenu,
            backupMenu,
            onlineTimeMenu,
            moderationHistoryMenu);

    /*
     * The initial connection is intentionally awaited during plugin startup. Runtime SQL remains
     * asynchronous, but no player or AFTER dependency may observe half-filled caches.
     */
    boolean databaseReady = database.initialise().join();
    if (!databaseReady) {
      getLogger()
          .severe(
              "Flunar-Bauserver wird deaktiviert, weil MariaDB nicht vollständig gestartet werden konnte.");
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    featureService.applyAll();
    int loadedWorlds = 0;
    for (String worldName : autoload.all()) {
      try {
        worlds.createProject(worldName, "normal");
        loadedWorlds++;
      } catch (RuntimeException exception) {
        getLogger()
            .severe("Autoload fehlgeschlagen für " + worldName + ": " + exception.getMessage());
      }
    }
    getLogger()
        .info(
            "Projekt-Autoload abgeschlossen: "
                + loadedWorlds
                + "/"
                + autoload.all().size()
                + " Welten geladen.");

    registerListeners(
        context,
        backupMenu,
        projectMenu,
        projectInfoMenu,
        featureMenu,
        onlineTimeMenu,
        featureService,
        tab,
        fix,
        blockDisplays,
        toolsMenu,
        builderServerMenu,
        moderationHistoryMenu);
    getServer()
        .getPluginManager()
        .registerEvents(new ServerListIconService(this, config.labyMod().serverListIcon()), this);
    LabyModIntegration.registerIfAvailable(this, config.labyMod());
    registerCommands(
        context,
        backupCoordinator,
        backupMenu,
        projectMenu,
        projectInfoMenu,
        featureMenu,
        onlineTimeMenu,
        tab,
        onlineTime,
        fix,
        blockDisplays,
        toolsMenu,
        builderServerMenu,
        privateMessages,
        moderationHistoryMenu);

    Bukkit.getScheduler().runTaskLater(this, tab::updateAll, 40L);
    onlineTime.start();
    getLogger().info("Flunar-Bauserver für Paper 26.1.2+ ist vollständig betriebsbereit.");
  }

  private void registerListeners(
      BauserverContext context,
      BackupMenu backupMenu,
      ProjectMenu projectMenu,
      ProjectInfoMenu projectInfoMenu,
      FeatureMenu featureMenu,
      OnlineTimeMenu onlineTimeMenu,
      FeatureService features,
      TabService tab,
      FixService fix,
      BlockDisplayService blockDisplays,
      ToolsMenu toolsMenu,
      BuilderServerMenu builderServerMenu,
      ModerationHistoryMenu moderationHistoryMenu) {
    PluginManager manager = getServer().getPluginManager();
    manager.registerEvents(new AccessListener(context), this);
    manager.registerEvents(
        new MainWorldBuildProtectionListener(context.worlds(), context.messages()), this);
    manager.registerEvents(new PrivateWorldUnloadListener(this), this);
    manager.registerEvents(new BackupProtectionListener(context.backups()), this);
    manager.registerEvents(backupMenu, this);
    manager.registerEvents(new TeleportHistoryListener(context.teleports()), this);
    manager.registerEvents(new ElevatorListener(this), this);
    manager.registerEvents(new UnknownCommandListener(context.messages()), this);
    manager.registerEvents(new FeatureListener(features, context.messages()), this);
    manager.registerEvents(projectMenu, this);
    manager.registerEvents(projectInfoMenu, this);
    manager.registerEvents(featureMenu, this);
    manager.registerEvents(onlineTimeMenu, this);
    manager.registerEvents(new TabListener(this, tab), this);
    manager.registerEvents(new OnlineTimeListener(onlineTime), this);
    manager.registerEvents(new FixListener(fix), this);
    manager.registerEvents(new BlockDisplayListener(blockDisplays), this);
    manager.registerEvents(toolsMenu, this);
    manager.registerEvents(builderServerMenu, this);
    manager.registerEvents(moderationHistoryMenu, this);
  }

  private void registerCommands(
      BauserverContext c,
      BackupCoordinator backupCoordinator,
      BackupMenu backupMenu,
      ProjectMenu projectMenu,
      ProjectInfoMenu projectInfoMenu,
      FeatureMenu featureMenu,
      OnlineTimeMenu onlineTimeMenu,
      TabService tab,
      OnlineTimeService onlineTime,
      FixService fix,
      BlockDisplayService blockDisplays,
      ToolsMenu toolsMenu,
      BuilderServerMenu builderServerMenu,
      PrivateMessageService privateMessages,
      ModerationHistoryMenu moderationHistoryMenu) {
    CommandRegistry commands = new CommandRegistry(this);
    commands.register("projekt", new ProjectCommand(c, projectMenu, projectInfoMenu));
    commands.register("privat", new PrivateWorldCommand(c));
    commands.register("unloadallworlds", new UnloadAllWorldsCommand(c));
    commands.register("world", new MainWorldCommand(c));
    commands.register("wldebug", new WorldDebugCommand(c));
    commands.register("tpa", new TpaCommand(c));
    commands.register("tpaccept", new TpaAcceptCommand(c));
    commands.register("tpadeny", new TpaDenyCommand(c));
    commands.register("back", new BackCommand(c));
    commands.register("bs", new BauserverMenuCommand(c, builderServerMenu));
    MessageCommand messageCommand = new MessageCommand(c, privateMessages);
    commands.register("msg", messageCommand);
    commands.register("r", new ReplyCommand(c, privateMessages, messageCommand));
    commands.register("tc", new TeamChatCommand(c));
    commands.register("fly", new FlyCommand(c));
    commands.register("gamemode", new GameModeCommand(c));
    WeatherTimeCommands weatherTime = new WeatherTimeCommands(c);
    commands.register("sun", weatherTime);
    commands.register("rain", weatherTime);
    commands.register("storm", weatherTime);
    commands.register("tag", weatherTime);
    commands.register("nacht", weatherTime);
    commands.register("speed", new SpeedCommand(c));
    commands.register("size", new SizeCommand(c));
    commands.register("hideentity", new HideEntityCommand(c));
    commands.register("showentity", new ShowEntityCommand(c));
    commands.register("tools", new ToolsCommand(c, toolsMenu));
    commands.register("workbench", new WorkbenchCommand(c));
    commands.register("anvil", new AnvilCommand(c));
    commands.register("enderchest", new EnderChestCommand(c));
    commands.register("invsee", new InventorySeeCommand(c));
    commands.register("endersee", new EnderSeeCommand(c));
    commands.register("reasons", new ReasonsCommand(c));
    commands.register("kick", new KickCommand(c));
    commands.register("ban", new BanCommand(c));
    commands.register("unban", new UnbanCommand(c));
    commands.register("banhistory", new BanHistoryCommand(c, moderationHistoryMenu));
    commands.register("kickhistory", new KickHistoryCommand(c, moderationHistoryMenu));
    commands.register("broadcast", new BroadcastCommand(c));
    commands.register("cc", new ChatClearCommand(c));
    commands.register("pcc", new PersonalChatClearCommand(c));
    commands.register("me", new PlayerInfoCommand(c));
    commands.register("tps", new TpsCommand(c));
    commands.register("ping", new PingCommand(c));
    commands.register("vanish", new VanishCommand(c));
    commands.register("serverrestart", new ServerRestartCommand(c));
    commands.register("help", new HelpCommand(c));
    commands.register("feature", new FeatureCommand(c, featureMenu));
    commands.register("featuredebug", new FeatureDebugCommand(c));
    commands.register("onlinezeit", new OnlineTimeCommand(c, onlineTime, onlineTimeMenu));
    commands.register("backup", new BackupCommand(c, backupCoordinator, backupMenu));
    commands.register("prefix", new PrefixReloadCommand(c, tab::updateAll));
    commands.register("reset", new ResetCommand(c));
    commands.register("fix", new FixCommand(c, fix));
    commands.register("blockdisplay", new BlockDisplayCommand(c, blockDisplays));
  }

  @Override
  public void onDisable() {
    if (onlineTime != null) onlineTime.close();
    if (backups != null) backups.close();
    if (worldTransfers != null) worldTransfers.close();
    if (database != null) database.close();
  }

  public BauserverApi api() {
    if (api == null) throw new IllegalStateException("Plugin ist noch nicht vollständig geladen");
    return api;
  }
}
