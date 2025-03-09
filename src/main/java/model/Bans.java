package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bans {
    @JsonProperty("myTeamBans")
    private int[] myTeamBans;

    @JsonProperty("numBans")
    private int numBans;

    @JsonProperty("theirTeamBans")
    private int[] theirTeamBans;

    public Bans() {}

    public Bans(int[] myTeamBans, int numBans, int[] theirTeamBans) {
        this.myTeamBans = myTeamBans;
        this.numBans = numBans;
        this.theirTeamBans = theirTeamBans;
    }

    public int[] getMyTeamBans() {
        return myTeamBans;
    }

    public void setMyTeamBans(int[] myTeamBans) {
        this.myTeamBans = myTeamBans;
    }

    public int getNumBans() {
        return numBans;
    }

    public void setNumBans(int numBans) {
        this.numBans = numBans;
    }

    public int[] getTheirTeamBans() {
        return theirTeamBans;
    }

    public void setTheirTeamBans(int[] theirTeamBans) {
        this.theirTeamBans = theirTeamBans;
    }

    @Override
    public String toString() {
        return "{" +
                "myTeamBans=" + Arrays.toString(myTeamBans) +
                ", numBans=" + numBans +
                ", theirTeamBans=" + Arrays.toString(theirTeamBans) +
                '}';
    }
}
