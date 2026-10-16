package fr.onyx.shard;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Catalogue des pouvoirs (identique au tableau du cahier des charges). */
public final class Powers {

    public static final List<Power> ALL = new ArrayList<>();
    private static final Map<String, Power> BY_ID = new HashMap<>();

    private Powers() {}

    public static Power get(String id) {
        return BY_ID.get(id);
    }

    private static void add(Power p) {
        ALL.add(p);
        BY_ID.put(p.id(), p);
    }

    /** amp = amplificateur Minecraft (0 = niveau I). */
    private static void eff(String id, String name, Material icon, int price, int limit,
                            PotionEffectType type, int amp) {
        add(new Power(id, name, icon, price, limit, List.of(new Power.Eff(type, amp)), 0, 0));
    }

    static {
        eff("fire_res",   "Résistance au feu I",      Material.MAGMA_CREAM,     5, -1, PotionEffectType.FIRE_RESISTANCE, 0);
        eff("water_br",   "Respiration aquatique I",  Material.PUFFERFISH,      5, -1, PotionEffectType.WATER_BREATHING, 0);
        eff("dolphin",    "Grâce du dauphin I",       Material.PRISMARINE_SHARD, 5, 3, PotionEffectType.DOLPHINS_GRACE, 0);
        eff("jump",       "Saut amélioré I",          Material.RABBIT_FOOT,     5, -1, PotionEffectType.JUMP_BOOST, 0);

        eff("haste1",     "Hâte I",                   Material.GOLDEN_PICKAXE,  5, 3, PotionEffectType.HASTE, 0);
        eff("haste2",     "Hâte II",                  Material.IRON_PICKAXE,   10, 3, PotionEffectType.HASTE, 1);
        eff("haste3",     "Hâte III",                 Material.DIAMOND_PICKAXE, 15, 2, PotionEffectType.HASTE, 2);
        eff("haste4",     "Hâte IV",                  Material.NETHERITE_PICKAXE, 25, 1, PotionEffectType.HASTE, 3);
        eff("haste5",     "Hâte V",                   Material.BEACON,         40, 1, PotionEffectType.HASTE, 4);

        eff("speed1",     "Vitesse I",                Material.SUGAR,          10, 3, PotionEffectType.SPEED, 0);
        eff("speed2",     "Vitesse II",               Material.FEATHER,        20, 1, PotionEffectType.SPEED, 1);
        eff("speed3",     "Vitesse III",              Material.GOLDEN_BOOTS,   40, 1, PotionEffectType.SPEED, 2);

        eff("strength1",  "Force I",                  Material.IRON_SWORD,     10, 3, PotionEffectType.STRENGTH, 0);
        eff("strength2",  "Force II",                 Material.DIAMOND_SWORD,  25, 1, PotionEffectType.STRENGTH, 1);
        eff("strength3",  "Force III",                Material.NETHERITE_SWORD, 60, 1, PotionEffectType.STRENGTH, 2);

        eff("regen1",     "Régénération I",           Material.GHAST_TEAR,     20, 1, PotionEffectType.REGENERATION, 0);
        eff("regen2",     "Régénération II",          Material.GLISTERING_MELON_SLICE, 40, 1, PotionEffectType.REGENERATION, 1);
        eff("resist1",    "Résistance I",             Material.IRON_CHESTPLATE, 30, 1, PotionEffectType.RESISTANCE, 0);
        eff("absorb1",    "Absorption I",             Material.GOLDEN_APPLE,   20, 2, PotionEffectType.ABSORPTION, 0);

        add(new Power("hearts2", "Cœurs supplémentaires (+2)", Material.APPLE, 30, 1, List.of(), 2, 0));
        add(new Power("hearts4", "Cœurs supplémentaires (+4)", Material.ENCHANTED_GOLDEN_APPLE, 60, 1, List.of(), 4, 0));
        add(new Power("knockback1", "Résistance au recul I", Material.SHIELD, 25, 1, List.of(), 0, 0.5));

        add(new Power("combo_sf", "Combo Vitesse I + Force I", Material.BLAZE_POWDER, 35, 1,
                List.of(new Power.Eff(PotionEffectType.SPEED, 0), new Power.Eff(PotionEffectType.STRENGTH, 0)), 0, 0));
        add(new Power("combo_hs", "Combo Hâte II + Vitesse I", Material.CLOCK, 25, 1,
                List.of(new Power.Eff(PotionEffectType.HASTE, 1), new Power.Eff(PotionEffectType.SPEED, 0)), 0, 0));
    }
}
