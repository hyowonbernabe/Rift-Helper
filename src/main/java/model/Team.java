package model;

public class Team {
    private String assignedPosition;
    private int cellId;
    private int championId;
    private int championPickIntent;
    private String nameVisibilityType;
    private int selectedSkinId;
    private int spell1Id;
    private int spell2Id;
    private long summonerId;
    private int team;
    private int wardSkinId;

    public Team() {}

    public Team(String assignedPosition, int cellId, int championId, int championPickIntent, String nameVisibilityType, int selectedSkinId, int spell1Id, int spell2Id, long summonerId, int team, int wardSkinId) {
        this.assignedPosition = assignedPosition;
        this.cellId = cellId;
        this.championId = championId;
        this.championPickIntent = championPickIntent;
        this.nameVisibilityType = nameVisibilityType;
        this.selectedSkinId = selectedSkinId;
        this.spell1Id = spell1Id;
        this.spell2Id = spell2Id;
        this.summonerId = summonerId;
        this.team = team;
        this.wardSkinId = wardSkinId;
    }

    public String getAssignedPosition() {
        return assignedPosition;
    }

    public void setAssignedPosition(String assignedPosition) {
        this.assignedPosition = assignedPosition;
    }

    public int getCellId() {
        return cellId;
    }

    public void setCellId(int cellId) {
        this.cellId = cellId;
    }

    public int getChampionId() {
        return championId;
    }

    public void setChampionId(int championId) {
        this.championId = championId;
    }

    public int getChampionPickIntent() {
        return championPickIntent;
    }

    public void setChampionPickIntent(int championPickIntent) {
        this.championPickIntent = championPickIntent;
    }

    public String getNameVisibilityType() {
        return nameVisibilityType;
    }

    public void setNameVisibilityType(String nameVisibilityType) {
        this.nameVisibilityType = nameVisibilityType;
    }

    public int getSelectedSkinId() {
        return selectedSkinId;
    }

    public void setSelectedSkinId(int selectedSkinId) {
        this.selectedSkinId = selectedSkinId;
    }

    public int getSpell1Id() {
        return spell1Id;
    }

    public void setSpell1Id(int spell1Id) {
        this.spell1Id = spell1Id;
    }

    public int getSpell2Id() {
        return spell2Id;
    }

    public void setSpell2Id(int spell2Id) {
        this.spell2Id = spell2Id;
    }

    public long getSummonerId() {
        return summonerId;
    }

    public void setSummonerId(long summonerId) {
        this.summonerId = summonerId;
    }

    public int getTeam() {
        return team;
    }

    public void setTeam(int team) {
        this.team = team;
    }

    public int getWardSkinId() {
        return wardSkinId;
    }

    public void setWardSkinId(int wardSkinId) {
        this.wardSkinId = wardSkinId;
    }

    @Override
    public String toString() {
        return "{" +
                "assignedPosition='" + assignedPosition + '\'' +
                ", cellId=" + cellId +
                ", championId=" + championId +
                ", championPickIntent=" + championPickIntent +
                ", nameVisibilityType='" + nameVisibilityType + '\'' +
                ", selectedSkinId=" + selectedSkinId +
                ", spell1Id=" + spell1Id +
                ", spell2Id=" + spell2Id +
                ", summonerId=" + summonerId +
                ", team=" + team +
                ", wardSkinId=" + wardSkinId +
                '}';
    }
}
