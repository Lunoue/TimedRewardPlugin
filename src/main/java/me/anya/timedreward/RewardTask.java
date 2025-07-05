package me.anya.timedreward;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class RewardTask implements Runnable {
    private final PlayerDataManager dataManager;
    private final TimedReward plugin;

    // Временная карта для хранения текущей сессии
    private final HashMap<UUID, Long> accumulatedSeconds = new HashMap<>();

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
            UUID uuid = player.getUniqueId();

            // Сколько секунд прошло с прошлого тика
            long secondsSinceLastTick = 60;

            // Добавим к уже накопленным в current-сессии
            long sessionSeconds = accumulatedSeconds.getOrDefault(uuid, 0L) + secondsSinceLastTick;

            // Добавим к уже сохранённым (предыдущим)
            long totalTime = dataManager.getPlayTime(uuid) + sessionSeconds;

            if (totalTime >= interval) {
                // Выдаём награду
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + player.getName() + " " + amount);
                player.sendMessage(message);

                // Обнуляем весь прогресс
                accumulatedSeconds.put(uuid, 0L);
                dataManager.resetPlayTime(uuid);
            } else {
                // Сохраняем в текущую сессию и в data.yml
                accumulatedSeconds.put(uuid, sessionSeconds);
                dataManager.setPlayTime(uuid, totalTime); // добавим метод ниже
            }
        }
    }
}
