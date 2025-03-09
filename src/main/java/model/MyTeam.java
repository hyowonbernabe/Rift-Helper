package model;

public class MyTeam extends Team {
    public MyTeam() {
        super();
    }

    public MyTeam(String assignedPosition, int cellId, int championId, int championPickIntent, String nameVisibilityType,
                  int selectedSkinId, int spell1Id, int spell2Id, long summonerId, int team, int wardSkinId) {
        super(assignedPosition, cellId, championId, championPickIntent, nameVisibilityType,
                selectedSkinId, spell1Id, spell2Id, summonerId, team, wardSkinId);
    }
}
