package me.anya.timedreward;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RewardTask implements Runnable {
    private final PlayerDataManager dataManager;
    private final TimedReward plugin;

    public RewardTask(PlayerDataManager dataManager, TimedReward plugin) {
        this.dataManager = dataManager;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        long intervalSeconds = plugin.getConfig().getLong("reward.interval-seconds");
        int amount = plugin.getConfig().getInt("reward.amount");
        String message = plugin.getConfig().getString("reward.message");

        long currentTime = System.currentTimeMillis();

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            // Получаем время последней проверки (или время входа)
            long lastCheck = dataManager.getJoinTimestamps().getOrDefault(uuid, currentTime);

            long elapsedMillis = currentTime - lastCheck;
            long elapsedSeconds = elapsedMillis / 1000;

            if (elapsedSeconds <= 0) continue;

            // Обновляем время последней проверки для игрока
            dataManager.getJoinTimestamps().put(uuid, currentTime);

            // Получаем уже накопленное время
            long totalPlayTime = dataManager.getPlayTime(uuid) + elapsedSeconds;

            if (totalPlayTime >= intervalSeconds) {
                // Выдаём награду
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + amount);
                player.sendMessage(message);

                // Сбрасываем накопленное время
                dataManager.resetPlayTime(uuid);
            } else {
                // Сохраняем накопленное время
                dataManager.setPlayTime(uuid, totalPlayTime);
            }
        }
    }
}
