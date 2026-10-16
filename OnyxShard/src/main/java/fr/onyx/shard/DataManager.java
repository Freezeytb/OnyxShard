package fr.onyx.shard;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Sauvegarde des Sold et des achats, par joueur, dans plugins/OnyxShard/data.yml. */
public final class DataManager {

    private final OnyxShardPlugin plugin;
    private final File file;
    private final Map<UUID, PlayerData> data = new HashMap<>();
    private final Object writeLock = new Object();
    private boolean dirty;

    public DataManager(OnyxShardPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
    }

    public void load() {
        data.clear();
        if (!file.exists()) return;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection players = yaml.getConfigurationSection("players");
        if (players == null) return;
        for (String key : players.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            PlayerData pd = new PlayerData();
            pd.setSold(players.getInt(key + ".sold", 0));
            ConfigurationSection buys = players.getConfigurationSection(key + ".purchases");
            if (buys != null) {
                for (String id : buys.getKeys(false)) {
                    int c = buys.getInt(id, 0);
                    if (c > 0) pd.purchases().put(id, c);
                }
            }
            data.put(uuid, pd);
        }
    }

    public PlayerData get(UUID uuid) {
        return data.computeIfAbsent(uuid, k -> new PlayerData());
    }

    public void markDirty() {
        dirty = true;
    }

    private String serialize() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<UUID, PlayerData> e : data.entrySet()) {
            String base = "players." + e.getKey();
            yaml.set(base + ".sold", e.getValue().sold());
            for (Map.Entry<String, Integer> p : e.getValue().purchases().entrySet()) {
                yaml.set(base + ".purchases." + p.getKey(), p.getValue());
            }
        }
        return yaml.saveToString();
    }

    /** Sauvegarde asynchrone si des changements existent (appelée sur le thread principal). */
    public void saveAsyncIfDirty() {
        if (!dirty) return;
        dirty = false;
        String content = serialize();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> write(content));
    }

    /** Sauvegarde synchrone (arrêt du serveur). */
    public void saveNow() {
        dirty = false;
        write(serialize());
    }

    private void write(String content) {
        synchronized (writeLock) {
            try {
                Files.createDirectories(file.getParentFile().toPath());
                Path tmp = new File(file.getParentFile(), "data.yml.tmp").toPath();
                Files.writeString(tmp, content, StandardCharsets.UTF_8);
                try {
                    Files.move(tmp, file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (IOException atomicFail) {
                    Files.move(tmp, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                plugin.getLogger().severe("Impossible de sauvegarder data.yml : " + ex.getMessage());
            }
        }
    }
}
