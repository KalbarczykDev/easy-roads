package dev.kalbarczyk.easyroads;

import java.util.Objects;

import dev.kalbarczyk.easyroads.commands.EasyRoadsCommand;
import dev.kalbarczyk.easyroads.config.ConfigState;
import dev.kalbarczyk.easyroads.tasks.EasyRoadsTask;
import dev.kalbarczyk.easyroads.config.ConfigHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class EasyRoads extends JavaPlugin {

    private ConfigState config;

    @Override
    public void onEnable() {
        //initialize metrics
        new Metrics(this, 23160);

        //save default config if not present
        if (!getDataFolder().exists()) {
            saveDefaultConfig();
        }

        loadConfiguration();

        //register command executor
        Objects.requireNonNull(getCommand("easyroads")).setExecutor(new EasyRoadsCommand(this));

        //start the repeating task
        new EasyRoadsTask(this).runTaskTimer(this, 1L, 1L);

        getLogger().info("EasyRoads Enabled");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        getLogger().warning("EasyRoads Disabled");
    }

    public void reloadConfiguration() {
        reloadConfig();
        loadConfiguration();
    }

    private void loadConfiguration() {
        var configHandler = new ConfigHandler(getConfig(), getLogger());
        config = configHandler.load();
    }

    public ConfigState getConfigState() {
        return config;
    }


}
