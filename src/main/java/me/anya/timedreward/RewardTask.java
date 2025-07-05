package me.anya.timedreward;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class RewardTask implements Runnable {
    private final PlayerDataManager dataManager;
    private final TimedReward plugin;

    // Сохраняем время последней проверки для каждого игрока (в миллисекундах)
    private final HashMap<UUID, Long> lastCheckTimestamps = new HashMap<>();

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

            // Время последней проверки, если не было — считаем что только что
            long lastTime = lastCheckTimestamps.getOrDefault(uuid, currentTime);

            // Сколько миллисекунд прошло с прошлого тика
            long elapsedMillis = currentTime - lastTime;
            long elapsedSeconds = elapsedMillis / 1000;

            if (elapsedSeconds <= 0) continue;

            // Обновляем "время последней проверки"
            lastCheckTimestamps.put(uuid, currentTime);

            // Прибавим к накопленному
            long totalPlayTime = dataManager.getPlayTime(uuid) + elapsedSeconds;

            if (totalPlayTime >= intervalSeconds) {
                // Выдаём награду
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + amount);
                player.sendMessage(message);

                // Обнуляем
                dataManager.resetPlayTime(uuid);
            } else {
                dataManager.setPlayTime(uuid, totalPlayTime);
            }
        }
    }
}
