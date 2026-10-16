package fr.onyx.shard;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class OnyxShardPlugin extends JavaPlugin {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private ShardItem shard;
    private DataManager data;
    private EffectManager effects;

    public static Component mm(String text) {
        return MM.deserialize(text);
    }

    public void send(CommandSender to, String text) {
        to.sendMessage(mm("<dark_gray>[<gradient:#b388ff:#6a1b9a>Onyx</gradient>]</dark_gray> " + text));
    }

    public ShardItem shard() { return shard; }
    public DataManager data() { return data; }
    public EffectManager effects() { return effects; }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        shard = new ShardItem(this);
        data = new DataManager(this);
        data.load();
        effects = new EffectManager(this);

        getServer().getPluginManager().registerEvents(new PluginListener(this), this);

        Commands commands = new Commands(this);
        for (String name : new String[]{"shop", "dropshard", "sold"}) {
            var cmd = getCommand(name);
            if (cmd != null) {
                cmd.setExecutor(commands);
                cmd.setTabCompleter(commands);
            }
        }

        // Réapplique les effets (milk, mort, totem...) toutes les 2 s.
        getServer().getScheduler().runTaskTimer(this, () -> {
            for (Player p : getServer().getOnlinePlayers()) effects.apply(p);
        }, 100L, 40L);

        // Sauvegarde automatique toutes les 30 s si besoin.
        getServer().getScheduler().runTaskTimer(this, () -> data.saveAsyncIfDirty(), 600L, 600L);
    }

    @Override
    public void onDisable() {
        if (data != null) data.saveNow();
    }
}
