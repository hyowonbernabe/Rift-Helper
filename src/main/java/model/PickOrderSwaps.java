package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PickOrderSwaps {
    @JsonProperty("cellId")
    private int cellId;

    @JsonProperty("id")
    private int id;

    @JsonProperty("state")
    private String state;

    public PickOrderSwaps() {}

    public PickOrderSwaps(int cellId, int id, String state) {
        this.cellId = cellId;
        this.id = id;
        this.state = state;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "{" +
                "cellId=" + cellId +
                ", id=" + id +
                ", state='" + state + '\'' +
                '}';
    }
}
