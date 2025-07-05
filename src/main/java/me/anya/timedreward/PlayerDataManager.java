package me.anya.timedreward;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.EventHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerDataManager implements Listener {
    private final File dataFile;
    private final YamlConfiguration dataConfig;
    private final HashMap<UUID, Long> joinTimestamps = new HashMap<>();

    public PlayerDataManager(TimedReward plugin) {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // При входе фиксируем текущее время
        joinTimestamps.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        long joined = joinTimestamps.getOrDefault(uuid, System.currentTimeMillis());
        long played = (System.currentTimeMillis() - joined) / 1000;
        long total = getPlayTime(uuid) + played;

        // Сохраняем наигранное время перед выходом
        setPlayTime(uuid, total);
        joinTimestamps.remove(uuid);
    }

    public void addPlayTime(UUID uuid, long seconds) {
        long current = getPlayTime(uuid);
        setPlayTime(uuid, current + seconds);
    }

    public long getPlayTime(UUID uuid) {
        return dataConfig.getLong(uuid.toString(), 0);
    }

    public void resetPlayTime(UUID uuid) {
        dataConfig.set(uuid.toString(), 0);
        saveData();
    }

    public void setPlayTime(UUID uuid, long seconds) {
        dataConfig.set(uuid.toString(), seconds);
        saveData();
    }

    public HashMap<UUID, Long> getJoinTimestamps() {
        return joinTimestamps;
    }

    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
