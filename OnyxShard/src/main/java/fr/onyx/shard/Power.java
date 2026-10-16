package fr.onyx.shard;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/** Un pouvoir achetable. limit &lt; 0 = achats illimités. */
public record Power(String id, String name, Material icon, int price, int limit,
                    List<Eff> effects, double extraHealth, double knockback) {

    public record Eff(PotionEffectType type, int amp) {}

    public boolean unlimited() {
        return limit < 0;
    }
}
