package fr.onyx.shard;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ShopMenu implements InventoryHolder {

    private static final int SIZE = 54;
    private static final int BALANCE_SLOT = 49;

    private final OnyxShardPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final Map<Integer, Power> slots = new HashMap<>();

    private ShopMenu(OnyxShardPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.inventory = Bukkit.createInventory(this, SIZE,
                OnyxShardPlugin.mm("<gradient:#b388ff:#6a1b9a><bold>Boutique ONYX</bold></gradient>"));
        for (int i = 0; i < Powers.ALL.size(); i++) {
            int slot = (i / 7 + 1) * 9 + (i % 7 + 1);
            slots.put(slot, Powers.ALL.get(i));
        }
        render();
    }

    public static void open(OnyxShardPlugin plugin, Player player) {
        player.openInventory(new ShopMenu(plugin, player).inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Power powerAt(int slot) {
        return slots.get(slot);
    }

    public void render() {
        PlayerData data = plugin.data().get(player.getUniqueId());

        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta paneMeta = pane.getItemMeta();
        paneMeta.displayName(Component.text(" "));
        pane.setItemMeta(paneMeta);
        for (int i = 0; i < SIZE; i++) inventory.setItem(i, pane);

        for (Map.Entry<Integer, Power> e : slots.entrySet()) {
            inventory.setItem(e.getKey(), powerItem(e.getValue(), data));
        }

        ItemStack balance = new ItemStack(Material.AMETHYST_SHARD);
        ItemMeta bm = balance.getItemMeta();
        bm.displayName(OnyxShardPlugin.mm("<!italic><light_purple><bold>Votre solde"));
        bm.lore(List.of(OnyxShardPlugin.mm("<!italic><gold>" + data.sold() + " Sold")));
        bm.setEnchantmentGlintOverride(true);
        balance.setItemMeta(bm);
        inventory.setItem(BALANCE_SLOT, balance);
    }

    private ItemStack powerItem(Power power, PlayerData data) {
        int owned = data.count(power.id());
        boolean maxed = !power.unlimited() && owned >= power.limit();
        boolean afford = data.sold() >= power.price();

        ItemStack item = new ItemStack(power.icon());
        ItemMeta meta = item.getItemMeta();
        meta.displayName(OnyxShardPlugin.mm("<!italic><yellow><bold>" + power.name()));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        List<Component> lore = new ArrayList<>();
        lore.add(OnyxShardPlugin.mm("<!italic><gray>Effet permanent"));
        lore.add(OnyxShardPlugin.mm("<!italic><gray>Prix : <gold>" + power.price() + " Sold"));
        lore.add(OnyxShardPlugin.mm("<!italic><gray>Achats : <white>" + owned + "/" + (power.unlimited() ? "∞" : power.limit())));
        lore.add(Component.empty());
        if (maxed) {
            lore.add(OnyxShardPlugin.mm("<!italic><red>Limite d'achat atteinte"));
        } else if (!afford) {
            lore.add(OnyxShardPlugin.mm("<!italic><red>Pas assez de Sold"));
        } else {
            lore.add(OnyxShardPlugin.mm("<!italic><green>Cliquez pour acheter"));
        }
        meta.lore(lore);
        if (owned > 0) meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        return item;
    }

    /** Tente un achat. Tout est exécuté sur le thread principal : pas de course possible. */
    public void buy(Power power) {
        PlayerData data = plugin.data().get(player.getUniqueId());
        int owned = data.count(power.id());

        if (!power.unlimited() && owned >= power.limit()) {
            plugin.send(player, "<red>Vous avez atteint la limite d'achat pour <white>" + power.name() + "<red>.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }
        if (data.sold() < power.price()) {
            plugin.send(player, "<red>Il vous faut <gold>" + power.price() + " Sold<red> (vous en avez <gold>" + data.sold() + "<red>).");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        data.setSold(data.sold() - power.price());
        data.addPurchase(power.id());
        plugin.data().markDirty();
        plugin.data().saveAsyncIfDirty();

        plugin.effects().apply(player);
        if (power.extraHealth() > 0) {
            AttributeInstance max = player.getAttribute(Attribute.MAX_HEALTH);
            if (max != null) player.setHealth(max.getValue());
        }

        plugin.send(player, "<green>Achat réussi : <white>" + power.name() + "<green> ! Solde : <gold>" + data.sold() + " Sold");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
        render();
    }
}
