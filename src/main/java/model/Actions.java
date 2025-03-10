package model;

public class Actions {
    private int actorCellId;
    private int championId;
    private boolean completed;
    private int id;
    private boolean isAllyAction;
    private boolean isInProgress;
    private int pickTurn;
    private String type;

    public Actions() {}

    public Actions(int actorCellId, int championId, boolean completed, int id, boolean isAllyAction, boolean isInProgress, int pickTurn, String type) {
        this.actorCellId = actorCellId;
        this.championId = championId;
        this.completed = completed;
        this.id = id;
        this.isAllyAction = isAllyAction;
        this.isInProgress = isInProgress;
        this.pickTurn = pickTurn;
        this.type = type;
    }

    public int getActorCellId() {
        return actorCellId;
    }

    public void setActorCellId(int actorCellId) {
        this.actorCellId = actorCellId;
    }

    public int getChampionId() {
        return championId;
    }

    public void setChampionId(int championId) {
        this.championId = championId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isAllyAction() {
        return isAllyAction;
    }

    public void setAllyAction(boolean allyAction) {
        isAllyAction = allyAction;
    }

    public boolean isInProgress() {
        return isInProgress;
    }

    public void setInProgress(boolean inProgress) {
        isInProgress = inProgress;
    }

    public int getPickTurn() {
        return pickTurn;
    }

    public void setPickTurn(int pickTurn) {
        this.pickTurn = pickTurn;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "{" +
                "actorCellId=" + actorCellId +
                ", championId=" + championId +
                ", completed=" + completed +
                ", id=" + id +
                ", isAllyAction=" + isAllyAction +
                ", isInProgress=" + isInProgress +
                ", pickTurn=" + pickTurn +
                ", type='" + type + '\'' +
                '}';
    }
}
