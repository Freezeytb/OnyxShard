package fr.onyx.shard;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public final class PluginListener implements Listener {

    private final OnyxShardPlugin plugin;

    public PluginListener(OnyxShardPlugin plugin) {
        this.plugin = plugin;
    }

    /* ---------- Récompense de kill ---------- */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim)) return;

        ItemStack reward = plugin.shard().create(1);
        Map<Integer, ItemStack> leftover = killer.getInventory().addItem(reward);
        for (ItemStack rest : leftover.values()) {
            killer.getWorld().dropItem(killer.getLocation(), rest);
        }
        plugin.send(killer, "<light_purple>Vous avez reçu une <rainbow>ONYX SHARD</rainbow><light_purple> !");
        killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
    }

    /* ---------- Conversion en Sold (clic droit dans l'air) ---------- */

    @EventHandler(ignoreCancelled = false)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR) return;
        EquipmentSlot hand = e.getHand();
        if (hand == null) return;

        Player player = e.getPlayer();
        ItemStack stack = player.getInventory().getItem(hand);
        if (!plugin.shard().isShard(stack)) return;

        e.setCancelled(true);

        // On consomme réellement 1 shard AVANT de créditer : impossible de convertir deux fois la même.
        ItemStack rest = stack.clone();
        rest.setAmount(stack.getAmount() - 1);
        player.getInventory().setItem(hand, rest.getAmount() <= 0 ? null : rest);

        PlayerData data = plugin.data().get(player.getUniqueId());
        data.setSold(data.sold() + 1);
        plugin.data().markDirty();

        plugin.send(player, "<green>+1 Sold ! Solde : <gold>" + data.sold() + " Sold");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
    }

    /* ---------- Une ONYX SHARD ne sert jamais de vraie Amethyst Shard (craft) ---------- */

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent e) {
        for (ItemStack ingredient : e.getInventory().getMatrix()) {
            if (plugin.shard().isShard(ingredient)) {
                e.getInventory().setResult(null);
                return;
            }
        }
    }

    /* ---------- Boutique ---------- */

    @EventHandler
    public void onShopClick(InventoryClickEvent e) {
        if (!(e.getView().getTopInventory().getHolder() instanceof ShopMenu menu)) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;

        Power power = menu.powerAt(e.getRawSlot());
        if (power != null) menu.buy(power);
    }

    @EventHandler
    public void onShopDrag(InventoryDragEvent e) {
        if (e.getView().getTopInventory().getHolder() instanceof ShopMenu) {
            e.setCancelled(true);
        }
    }

    /* ---------- Effets permanents ---------- */

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.effects().apply(p), 5L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.effects().apply(p), 2L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.data().saveAsyncIfDirty();
    }
}
