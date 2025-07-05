package me.anya.timedreward;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class RewardTask implements Runnable {
    private final PlayerDataManager dataManager;
    private final TimedReward plugin;

    public RewardTask(PlayerDataManager dataManager, TimedReward plugin) {
        this.dataManager = dataManager;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long interval = plugin.getConfig().getLong("reward.interval-seconds");
        int amount = plugin.getConfig().getInt("reward.amount");
        String message = plugin.getConfig().getString("reward.message");

        for (Player player : Bukkit.getOnlinePlayers()) {
            long joined = dataManager.getJoinTimestamps().getOrDefault(player.getUniqueId(), System.currentTimeMillis());
            long sessionTime = (System.currentTimeMillis() - joined) / 1000;
            dataManager.addPlayTime(player.getUniqueId(), sessionTime);
            dataManager.getJoinTimestamps().put(player.getUniqueId(), System.currentTimeMillis());

            long totalTime = dataManager.getPlayTime(player.getUniqueId());
            if (totalTime >= interval) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + amount);
                player.sendMessage(message);
                dataManager.resetPlayTime(player.getUniqueId());
            }
        }
    }
}
