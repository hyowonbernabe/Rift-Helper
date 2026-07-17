package model;

/**
 * One entry of the champ-select {@code trades} array (ARAM teammate champion trading).
 * The LCU entry carries {@code id}, {@code cellId} (the other player's cell), and {@code state}
 * (AVAILABLE / BUSY / SENT / RECEIVED / DECLINED / CANCELLED / INVALID). It does NOT carry a
 * champion id, so the controller resolves the champ a trade would give you by joining
 * {@code cellId} against {@code myTeam[].cellId -> championId}. See {@link TradeDecider}.
 */
public class Trades {
    private int id;
    private int cellId;
    private String state;

    public Trades() {}

    public Trades(int id, int cellId, String state) {
        this.id = id;
        this.cellId = cellId;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
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
                "id=" + id +
                ", cellId=" + cellId +
                ", state='" + state + '\'' +
                '}';
    }
}
