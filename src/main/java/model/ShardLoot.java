package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ShardLoot {
    @JsonProperty("disenchantLootName")
    private String disenchantLootName;

    @JsonProperty("count")
    private int count;

    @JsonProperty("isNew")
    private boolean isNew;

    @JsonProperty("itemStatus")
    private String itemStatus;

    @JsonProperty("lootId")
    private String lootId;

    @JsonProperty("storeItemId")
    private int storeItemId;

    @JsonProperty("disenchantValue")
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

    @JsonIgnoreProperties
    public static List<ShardLoot> parseFromJson(String eventData) {
        List<ShardLoot> lootList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            List<JsonNode> jsonNodes = objectMapper.readValue(eventData, new TypeReference<List<JsonNode>>() {});

            for (JsonNode node : jsonNodes) {
                String disenchantLootName = node.get("disenchantLootName").asText();
                int count = Parse.parseInt(node.get("count"));
                boolean isNew = Parse.parseBoolean(node.get("isNew"));
                String itemStatus = node.get("itemStatus").asText();
                String lootId = node.has("lootId") ? node.get("lootId").asText() : "";
                int storeItemId = Parse.parseInt(node.get("storeItemId"));
                int disenchantValue = Parse.parseInt(node.get("disenchantValue"));

                lootList.add(new ShardLoot(disenchantLootName, count, isNew, itemStatus, lootId, storeItemId, disenchantValue));
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
