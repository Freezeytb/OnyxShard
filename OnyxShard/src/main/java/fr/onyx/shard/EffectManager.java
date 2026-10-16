package fr.onyx.shard;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

/** Applique en permanence les pouvoirs achetés (effets infinis + attributs). */
public final class EffectManager {

    private final OnyxShardPlugin plugin;

    public EffectManager(OnyxShardPlugin plugin) {
        this.plugin = plugin;
    }

    public void apply(Player player) {
        if (!player.isOnline() || player.isDead()) return;
        PlayerData data = plugin.data().get(player.getUniqueId());
        boolean stack = plugin.getConfig().getBoolean("stack-repeat-purchases", true);

        Map<PotionEffectType, Integer> wanted = new HashMap<>();
        double extraHealth = 0;
        double knockback = 0;

        for (Power power : Powers.ALL) {
            int count = data.count(power.id());
            if (count <= 0) continue;
            int bonus = (stack && power.limit() > 1) ? Math.min(count, power.limit()) - 1 : 0;
            for (Power.Eff e : power.effects()) {
                wanted.merge(e.type(), e.amp() + bonus, Math::max);
            }
            extraHealth = Math.max(extraHealth, power.extraHealth());
            knockback = Math.max(knockback, power.knockback());
        }

        for (Map.Entry<PotionEffectType, Integer> e : wanted.entrySet()) {
            PotionEffectType type = e.getKey();
            int amp = e.getValue();
            PotionEffect current = player.getPotionEffect(type);
            boolean needs = current == null
                    || amp > current.getAmplifier()
                    || (amp == current.getAmplifier() && !current.isInfinite());
            if (needs) {
                player.addPotionEffect(new PotionEffect(type, PotionEffect.INFINITE_DURATION, amp, false, false, true));
            }
        }

        setModifier(player, Attribute.MAX_HEALTH, "bonus_health", extraHealth);
        setModifier(player, Attribute.KNOCKBACK_RESISTANCE, "bonus_knockback", knockback);
    }

    private void setModifier(Player player, Attribute attribute, String name, double amount) {
        AttributeInstance inst = player.getAttribute(attribute);
        if (inst == null) return;
        NamespacedKey key = new NamespacedKey(plugin, name);
        AttributeModifier current = inst.getModifier(key);
        if (amount <= 0) {
            if (current != null) inst.removeModifier(key);
            return;
        }
        if (current != null && current.getAmount() == amount) return;
        if (current != null) inst.removeModifier(key);
        inst.addModifier(new AttributeModifier(key, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
    }
}
