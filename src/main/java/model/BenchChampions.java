package model;

public class BenchChampions {
    private int championId;
    private boolean isPriority;

    public BenchChampions() {}

    public BenchChampions(int championId, boolean isPriority) {
        this.championId = championId;
        this.isPriority = isPriority;
    }

    public int getChampionId() {
        return championId;
    }

    public void setChampionId(int championId) {
        this.championId = championId;
    }

    public boolean isPriority() {
        return isPriority;
    }

    public void setPriority(boolean priority) {
        isPriority = priority;
    }

    @Override
    public String toString() {
        return "{" +
                "championID=" + championId +
                ", isPriority=" + isPriority +
                '}';
    }
}
