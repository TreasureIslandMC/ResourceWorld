package me.nik.resourceworld;

import me.nik.resourceworld.commands.CommandManager;
import me.nik.resourceworld.files.Config;
import me.nik.resourceworld.files.Data;
import me.nik.resourceworld.files.Lang;
import me.nik.resourceworld.files.commentedfiles.CommentedFileConfiguration;
import me.nik.resourceworld.gui.GuiListener;
import me.nik.resourceworld.managers.MsgType;
import me.nik.resourceworld.managers.PapiHook;
import me.nik.resourceworld.managers.UpdateChecker;
import me.nik.resourceworld.managers.custom.CustomWorld;
import me.nik.resourceworld.managers.custom.ResourceWorldType;
import me.nik.resourceworld.metrics.MetricsLite;
import me.nik.resourceworld.modules.ListenerModule;
import me.nik.resourceworld.modules.impl.Drowning;
import me.nik.resourceworld.modules.impl.LeaveWorld;
import me.nik.resourceworld.modules.impl.disabledcommands.CommandsEnd;
import me.nik.resourceworld.modules.impl.disabledcommands.CommandsNether;
import me.nik.resourceworld.modules.impl.disabledcommands.CommandsWorld;
import me.nik.resourceworld.modules.impl.entityspawning.EntitySpawning;
import me.nik.resourceworld.modules.impl.entityspawning.EntitySpawningEnd;
import me.nik.resourceworld.modules.impl.entityspawning.EntitySpawningNether;
import me.nik.resourceworld.modules.impl.explosion.Explosion;
import me.nik.resourceworld.modules.impl.explosion.ExplosionEnd;
import me.nik.resourceworld.modules.impl.explosion.ExplosionNether;
import me.nik.resourceworld.modules.impl.portals.PortalEnd;
import me.nik.resourceworld.modules.impl.portals.PortalNether;
import me.nik.resourceworld.modules.impl.suffocation.Suffocation;
import me.nik.resourceworld.modules.impl.suffocation.SuffocationEnd;
import me.nik.resourceworld.modules.impl.suffocation.SuffocationNether;
import me.nik.resourceworld.tasks.AlwaysDay;
import me.nik.resourceworld.tasks.ResetEndWorld;
import me.nik.resourceworld.tasks.ResetNetherWorld;
import me.nik.resourceworld.tasks.ResetWorld;
import me.nik.resourceworld.utils.Messenger;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ResourceWorld extends JavaPlugin {

    private static ResourceWorld plugin;
    private static Economy econ = null;

    private Config config;
    private Data data;
    private Lang lang;

    public static ResourceWorld getInstance() {
        return plugin;
    }

    private final String[] STARTUP_MESSAGE = {
            " ",
            ChatColor.GREEN + "Resource World v" + this.getDescription().getVersion(),
            " ",
            ChatColor.WHITE + "     Author: Nik",
            " "
    };

    private final Map<ResourceWorldType, CustomWorld> resourceWorlds = new HashMap<>();

    private final List<ListenerModule> modules = new ArrayList<>();

    @Override
    public void onDisable() {

        //DisInitialize modules
        this.modules.forEach(ListenerModule::disInit);

        //Store Time Left
        storeTimeLeft();

        //Reload Files
        config.reset();
        lang.reload();
        lang.save();

        HandlerList.unregisterAll(this);
        this.getServer().getScheduler().cancelTasks(this);
        plugin = null;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public Map<ResourceWorldType, CustomWorld> getResourceWorlds() {
        return resourceWorlds;
    }

    @Override
    public void onEnable() {
        plugin = this;
        this.config = new Config(this);
        this.data = new Data();
        this.lang = new Lang();

        //Startup Message
        this.getServer().getConsoleSender().sendMessage(STARTUP_MESSAGE);

        //Load Files
        loadFiles();

        //Load Vault if found
        setupEconomy();

        getCommand("resource").setExecutor(new CommandManager(this));

        initializeListeners();

        initWorlds();

        manageMillis();

        startIntervals();

        initializeTasks();

        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PapiHook(this).register();
        }

        new Metrics(this, 6981);
    }

    private void initWorlds() {
        this.resourceWorlds.clear();

        if (Config.Setting.WORLD_ENABLED.getBoolean()) {
            this.resourceWorlds.put(
                    ResourceWorldType.RESOURCE_WORLD,
                    new CustomWorld(Config.Setting.WORLD_NAME.getString(),
                            Difficulty.valueOf(Config.Setting.WORLD_DIFFICULTY.getString()),
                            WorldType.valueOf(Config.Setting.WORLD_TYPE.getString()),
                            World.Environment.valueOf(Config.Setting.WORLD_ENVIRONMENT.getString()),
                            Config.Setting.WORLD_GENERATE_STRUCTURES.getBoolean(),
                            Config.Setting.WORLD_SEED_ENABLED.getBoolean(),
                            Config.Setting.WORLD_SEED.getLong(),
                            Config.Setting.WORLD_BORDER_ENABLED.getBoolean(),
                            Config.Setting.WORLD_BORDER_SIZE.getInt(),
                            Config.Setting.WORLD_PVP.getBoolean(),
                            Config.Setting.WORLD_KEEP_INVENTORY.getBoolean(),
                    ResourceWorldType.RESOURCE_WORLD));
        }

        if (Config.Setting.NETHER_ENABLED.getBoolean()) {
            this.resourceWorlds.put(
                    ResourceWorldType.RESOURCE_NETHER,
                    new CustomWorld(Config.Setting.NETHER_NAME.getString(),
                            Difficulty.valueOf(Config.Setting.NETHER_DIFFICULTY.getString()),
                            WorldType.valueOf(Config.Setting.NETHER_TYPE.getString()),
                            World.Environment.valueOf(Config.Setting.NETHER_ENVIRONMENT.getString()),
                            Config.Setting.NETHER_GENERATE_STRUCTURES.getBoolean(),
                            Config.Setting.NETHER_SEED_ENABLED.getBoolean(),
                            Config.Setting.NETHER_SEED.getLong(),
                            Config.Setting.NETHER_BORDER_ENABLED.getBoolean(),
                            Config.Setting.NETHER_BORDER_SIZE.getInt(),
                            Config.Setting.NETHER_PVP.getBoolean(),
                            Config.Setting.NETHER_KEEP_INVENTORY.getBoolean(),
                    ResourceWorldType.RESOURCE_NETHER));
        }

        if (Config.Setting.END_ENABLED.getBoolean()) {
            this.resourceWorlds.put(
                    ResourceWorldType.RESOURCE_END,
                    new CustomWorld(Config.Setting.END_NAME.getString(),
                            Difficulty.valueOf(Config.Setting.END_DIFFICULTY.getString()),
                            WorldType.valueOf(Config.Setting.END_TYPE.getString()),
                            World.Environment.valueOf(Config.Setting.END_ENVIRONMENT.getString()),
                            Config.Setting.END_GENERATE_STRUCTURES.getBoolean(),
                            Config.Setting.END_SEED_ENABLED.getBoolean(),
                            Config.Setting.END_SEED.getLong(),
                            Config.Setting.END_BORDER_ENABLED.getBoolean(),
                            Config.Setting.END_BORDER_SIZE.getInt(),
                            Config.Setting.END_PVP.getBoolean(),
                            Config.Setting.END_KEEP_INVENTORY.getBoolean(),
                    ResourceWorldType.RESOURCE_END));
        }

        this.resourceWorlds.values().forEach(CustomWorld::generate);
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return;
        econ = rsp.getProvider();
    }

    public CustomWorld getResourceWorld(ResourceWorldType type) {
        return this.resourceWorlds.get(type);
    }

    public CommentedFileConfiguration getConfiguration() {
        return config.getConfig();
    }

    public FileConfiguration getLang() {
        return lang.get();
    }

    public FileConfiguration getData() {
        return data.get();
    }

    public void saveData() {
        data.save();
    }

    public void reloadData() {
        data.reload();
    }

    private void initializeTasks() {
        if (Config.Setting.SETTINGS_CHECK_FOR_UPDATES.getBoolean()) {
            new UpdateChecker(this).runTaskAsynchronously(this);
        } else Messenger.consoleMessage(MsgType.UPDATE_DISABLED.getMessage());

        if (Config.Setting.WORLD_ALWAYS_DAY.getBoolean()) new AlwaysDay().runTaskTimer(this, 1200, 1200);
    }

    private void manageMillis() {
        if (Config.Setting.WORLD_ENABLED.getBoolean() && Config.Setting.WORLD_STORE_TIME.getBoolean() && data.get().getLong("world.millis") <= 0) {
            data.get().set("world.millis", System.currentTimeMillis());
            data.save();
            data.reload();
        }
        if (Config.Setting.NETHER_ENABLED.getBoolean() && Config.Setting.NETHER_STORE_TIME.getBoolean() && data.get().getLong("nether.millis") <= 0) {
            data.get().set("nether.millis", System.currentTimeMillis());
            data.save();
            data.reload();
        }
        if (Config.Setting.END_ENABLED.getBoolean() && Config.Setting.END_STORE_TIME.getBoolean() && data.get().getLong("end.millis") <= 0) {
            data.get().set("end.millis", System.currentTimeMillis());
            data.save();
            data.reload();
        }
        data.get().set("world.papi", System.currentTimeMillis());
        data.get().set("nether.papi", System.currentTimeMillis());
        data.get().set("end.papi", System.currentTimeMillis());
    }

    private void storeTimeLeft() {
        if (Config.Setting.WORLD_RESETS_ENABLED.getBoolean() && Config.Setting.WORLD_ENABLED.getBoolean() && Config.Setting.WORLD_STORE_TIME.getBoolean()) {
            data.get().set("world.timer", Config.Setting.WORLD_RESETS_INTERVAL.getInt() * 72000 - (System.currentTimeMillis() - data.get().getLong("world.millis")) / 1000D * 20D);
        }
        if (Config.Setting.NETHER_RESETS_ENABLED.getBoolean() && Config.Setting.NETHER_ENABLED.getBoolean() && Config.Setting.NETHER_STORE_TIME.getBoolean()) {
            data.get().set("nether.timer", Config.Setting.NETHER_RESETS_INTERVAL.getInt() * 72000 - (System.currentTimeMillis() - data.get().getLong("nether.millis")) / 1000D * 20D);
        }
        if (Config.Setting.END_RESETS_ENABLED.getBoolean() && Config.Setting.END_ENABLED.getBoolean() && Config.Setting.END_STORE_TIME.getBoolean()) {
            data.get().set("end.timer", Config.Setting.END_RESETS_INTERVAL.getInt() * 72000 - (System.currentTimeMillis() - data.get().getLong("end.millis")) / 1000D * 20D);
        }
        data.save();
    }

    private long worldTimer() {
        if (!Config.Setting.WORLD_STORE_TIME.getBoolean()) {
            return Config.Setting.WORLD_RESETS_INTERVAL.getInt() * 72000L;
        } else if (data.get().getLong("world.timer") <= 0) {
            return Config.Setting.WORLD_RESETS_INTERVAL.getInt() * 72000L;
        } else {
            return data.get().getLong("world.timer");
        }
    }

    private long netherTimer() {
        if (!Config.Setting.NETHER_STORE_TIME.getBoolean()) {
            return Config.Setting.NETHER_RESETS_INTERVAL.getInt() * 72000L;
        } else if (data.get().getLong("nether.timer") <= 0) {
            return Config.Setting.NETHER_RESETS_INTERVAL.getInt() * 72000L;
        } else {
            return data.get().getLong("nether.timer");
        }
    }

    private long endTimer() {
        if (!Config.Setting.END_STORE_TIME.getBoolean()) {
            return Config.Setting.END_RESETS_INTERVAL.getInt() * 72000L;
        } else if (data.get().getLong("end.timer") <= 0) {
            return Config.Setting.END_RESETS_INTERVAL.getInt() * 72000L;
        } else {
            return data.get().getLong("end.timer");
        }
    }

    private void loadFiles() {
        config.setup();

        lang.setup(this);
        lang.addDefaults();
        lang.get().options().copyDefaults(true);
        lang.save();

        data.setup(this);
        data.addDefaults();
        data.get().options().copyDefaults(true);
        data.save();
    }

    private void initializeListeners() {
        final PluginManager pm = this.getServer().getPluginManager();

        //Load listener modules
        this.modules.add(new LeaveWorld(this));
        this.modules.add(new Drowning(this));
        this.modules.add(new Suffocation(this));
        this.modules.add(new SuffocationEnd(this));
        this.modules.add(new SuffocationNether(this));
        this.modules.add(new PortalEnd(this));
        this.modules.add(new PortalNether(this));
        this.modules.add(new Explosion(this));
        this.modules.add(new ExplosionEnd(this));
        this.modules.add(new ExplosionNether(this));
        this.modules.add(new EntitySpawning(this));
        this.modules.add(new EntitySpawningEnd(this));
        this.modules.add(new EntitySpawningNether(this));
        this.modules.add(new CommandsEnd(this));
        this.modules.add(new CommandsNether(this));
        this.modules.add(new CommandsWorld(this));

        //Initialize them
        this.modules.forEach(ListenerModule::init);

        //Don't be an idiot Nik, Always register this Listener
        pm.registerEvents(new GuiListener(), this);
    }

    private void startIntervals() {
        if (Config.Setting.WORLD_ENABLED.getBoolean() && Config.Setting.WORLD_RESETS_ENABLED.getBoolean()) {
            int interval = Config.Setting.WORLD_RESETS_INTERVAL.getInt() * 72000;
            new ResetWorld(this).runTaskTimer(this, worldTimer(), interval);
        }
        if (Config.Setting.NETHER_ENABLED.getBoolean() && Config.Setting.NETHER_RESETS_ENABLED.getBoolean()) {
            int interval = Config.Setting.NETHER_RESETS_INTERVAL.getInt() * 72000;
            new ResetNetherWorld(this).runTaskTimer(this, netherTimer(), interval);
        }
        if (Config.Setting.END_ENABLED.getBoolean() && Config.Setting.END_RESETS_ENABLED.getBoolean()) {
            int interval = Config.Setting.END_RESETS_INTERVAL.getInt() * 72000;
            new ResetEndWorld(this).runTaskTimer(this, endTimer(), interval);
        }
    }
}