package fr.onyx.shard;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import java.util.List;

public final class Commands implements CommandExecutor, TabCompleter {

    private final OnyxShardPlugin plugin;

    public Commands(OnyxShardPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Commande réservée aux joueurs.");
            return true;
        }
        switch (cmd.getName().toLowerCase()) {
            case "shop" -> ShopMenu.open(plugin, player);
            case "sold" -> plugin.send(player, "<gray>Votre solde : <gold>"
                    + plugin.data().get(player.getUniqueId()).sold() + " Sold");
            case "dropshard" -> dropShard(player, args);
            default -> { return false; }
        }
        return true;
    }

    private void dropShard(Player player, String[] args) {
        if (args.length != 1) {
            plugin.send(player, "<red>Usage : /dropshard <nombre>");
            return;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            plugin.send(player, "<red>Le nombre doit être un entier strictement positif.");
            return;
        }
        if (amount <= 0) {
            plugin.send(player, "<red>Le nombre doit être un entier strictement positif.");
            return;
        }

        PlayerInventory inv = player.getInventory();

        // 1) compter
        int owned = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack it = inv.getItem(i);
            if (plugin.shard().isShard(it)) owned += it.getAmount();
        }
        ItemStack off = inv.getItemInOffHand();
        if (plugin.shard().isShard(off)) owned += off.getAmount();

        if (owned < amount) {
            plugin.send(player, "<red>Vous n'avez que <white>" + owned + "<red> ONYX SHARD (demandé : " + amount + "). Rien n'a été retiré.");
            return;
        }

        // 2) retirer réellement de l'inventaire
        int toRemove = amount;
        for (int i = 0; i < 36 && toRemove > 0; i++) {
            ItemStack it = inv.getItem(i);
            if (!plugin.shard().isShard(it)) continue;
            int take = Math.min(toRemove, it.getAmount());
            toRemove -= take;
            if (take == it.getAmount()) {
                inv.setItem(i, null);
            } else {
                ItemStack rest = it.clone();
                rest.setAmount(it.getAmount() - take);
                inv.setItem(i, rest);
            }
        }
        if (toRemove > 0 && plugin.shard().isShard(off)) {
            int take = Math.min(toRemove, off.getAmount());
            toRemove -= take;
            if (take == off.getAmount()) {
                inv.setItemInOffHand(null);
            } else {
                ItemStack rest = off.clone();
                rest.setAmount(off.getAmount() - take);
                inv.setItemInOffHand(rest);
            }
        }

        // 3) déposer au sol (seulement ce qui a vraiment été retiré)
        int removed = amount - toRemove;
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection().multiply(0.3);
        int remaining = removed;
        while (remaining > 0) {
            int stackSize = Math.min(64, remaining);
            remaining -= stackSize;
            player.getWorld().dropItem(eye, plugin.shard().create(stackSize), item -> {
                item.setVelocity(dir);
                item.setPickupDelay(40);
                item.setThrower(player.getUniqueId());
            });
        }
        plugin.send(player, "<green>Vous avez déposé <white>" + removed + "<green> ONYX SHARD au sol.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("dropshard") && args.length == 1) {
            return List.of("1", "16", "64");
        }
        return List.of();
    }
}
