package model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class ShardLoot {
    private String disenchantLootName;
    private int count;
    private boolean isNew;
    private String itemStatus;
    private String lootId;
    private int storeItemId;
    private int disenchantValue;

    public ShardLoot() {}

    public ShardLoot(String disenchantLootName, int count, boolean isNew, String itemStatus, String lootId, int storeItemId, int disenchantValue) {
        this.disenchantLootName = disenchantLootName;
        this.count = count;
        this.isNew = isNew;
        this.itemStatus = itemStatus;
        this.lootId = lootId;
        this.storeItemId = storeItemId;
        this.disenchantValue = disenchantValue;
    }

    public String getDisenchantLootName() {
        return disenchantLootName;
    }

    public void setDisenchantLootName(String disenchantLootName) {
        this.disenchantLootName = disenchantLootName;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean getNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getLootId() {
        return lootId;
    }

    public void setLootId(String lootId) {
        this.lootId = lootId;
    }

    public int getStoreItemId() {
        return storeItemId;
    }

    public void setStoreItemId(int storeItemId) {
        this.storeItemId = storeItemId;
    }

    public int getDisenchantValue() {
        return disenchantValue;
    }

    public void setDisenchantValue(int disenchantValue) {
        this.disenchantValue = disenchantValue;
    }

    public static List<ShardLoot> parseFromJson(String eventData) {
        List<ShardLoot> lootList = new ArrayList<>();
        Gson gson = new Gson();

        try {
            JsonArray jsonArray = JsonParser.parseString(eventData).getAsJsonArray();
            for (JsonElement element : jsonArray) {
                ShardLoot loot = gson.fromJson(element, ShardLoot.class);
                lootList.add(loot);
            }
        } catch (Exception e) {
            System.out.println("Error parsing Shards Loot JSON: " + e.getMessage());
        }

        return lootList;
    }

    @Override
    public String toString() {
        return "{" +
                "disenchantLootName=" + disenchantLootName +
                ", count=" + count +
                ", isNew=" + isNew +
                ", itemStatus=" + itemStatus +
                ", lootId='" + lootId + '\'' +
                ", storeItemId=" + storeItemId +
                ", disenchantValue=" + disenchantValue +
                '}';
    }
}
