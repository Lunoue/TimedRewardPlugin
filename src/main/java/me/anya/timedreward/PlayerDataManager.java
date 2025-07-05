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
        joinTimestamps.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        long joined = joinTimestamps.getOrDefault(uuid, System.currentTimeMillis());
        long played = (System.currentTimeMillis() - joined) / 1000;
        long total = dataConfig.getLong(uuid.toString(), 0) + played;
        dataConfig.set(uuid.toString(), total);
        saveData();
        joinTimestamps.remove(uuid);
    }

    public void addPlayTime(UUID uuid, long seconds) {
        long current = dataConfig.getLong(uuid.toString(), 0);
        dataConfig.set(uuid.toString(), current + seconds);
        saveData();
    }

    public long getPlayTime(UUID uuid) {
        return dataConfig.getLong(uuid.toString(), 0);
    }

    public void resetPlayTime(UUID uuid) {
        dataConfig.set(uuid.toString(), 0);
        saveData();
    }

    private void saveData() {
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<UUID, Long> getJoinTimestamps() {
        return joinTimestamps;
    }

    public void setPlayTime(UUID uuid, long seconds) {
        dataConfig.set(uuid.toString(), seconds);
        saveData();
    }
}
