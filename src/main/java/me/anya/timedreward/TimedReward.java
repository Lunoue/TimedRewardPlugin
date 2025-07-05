package me.anya.timedreward;

import org.bukkit.plugin.java.JavaPlugin;

public class TimedReward extends JavaPlugin {
    private static TimedReward instance;
    private PlayerDataManager dataManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        dataManager = new PlayerDataManager(this);
        getServer().getPluginManager().registerEvents(dataManager, this);
        getServer().getScheduler().runTaskTimer(this, new RewardTask(dataManager, this), 20L, 1200L); // раз в 60 сек
    }

    public static TimedReward getInstance() {
        return instance;
    }
}
