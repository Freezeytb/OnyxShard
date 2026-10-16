package fr.onyx.shard;

import java.util.HashMap;
import java.util.Map;

public final class PlayerData {

    private int sold;
    private final Map<String, Integer> purchases = new HashMap<>();

    public int sold() { return sold; }
    public void setSold(int sold) { this.sold = Math.max(0, sold); }
    public Map<String, Integer> purchases() { return purchases; }
    public int count(String powerId) { return purchases.getOrDefault(powerId, 0); }
    public void addPurchase(String powerId) { purchases.merge(powerId, 1, Integer::sum); }
}
