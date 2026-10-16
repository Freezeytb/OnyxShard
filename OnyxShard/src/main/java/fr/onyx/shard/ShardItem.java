package fr.onyx.shard;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

/** Création et identification fiable (via PersistentDataContainer) des ONYX SHARD. */
public final class ShardItem {

    private final NamespacedKey key;

    public ShardItem(OnyxShardPlugin plugin) {
        this.key = new NamespacedKey(plugin, "onyx_shard");
    }

    public ItemStack create(int amount) {
        ItemStack item = new ItemStack(Material.AMETHYST_SHARD, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(OnyxShardPlugin.mm("<!italic><bold><rainbow>ONYX SHARD</rainbow></bold>"));
        meta.lore(List.of(
                OnyxShardPlugin.mm("<!italic><gray>Obtenue en tuant un joueur."),
                OnyxShardPlugin.mm("<!italic><gray>Clic droit : <gold>+1 Sold</gold>")));
        meta.setEnchantmentGlintOverride(true);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isShard(ItemStack item) {
        if (item == null || item.getType() != Material.AMETHYST_SHARD || !item.hasItemMeta()) return false;
        Byte b = item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.BYTE);
        return b != null && b == 1;
    }
}
