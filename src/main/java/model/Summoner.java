package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Summoner {
    @JsonProperty("accountId")
    private int accountId;

    @JsonProperty("gameName")
    private String gameName;

    @JsonProperty("percentCompleteForNextLevel")
    private int percentCompleteForNextLevel;

    @JsonProperty("summonerId")
    private long summonerId;

    @JsonProperty("summonerLevel")
    private int summonerLevel;

    @JsonProperty("tagLine")
    private String tagLine;

    @JsonProperty("xpSinceLastLevel")
    private int xpSinceLastLevel;

    @JsonProperty("xpUntilNextLevel")
    private int xpUntilNextLevel;

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public int getPercentCompleteForNextLevel() {
        return percentCompleteForNextLevel;
    }

    public void setPercentCompleteForNextLevel(int percentCompleteForNextLevel) {
        this.percentCompleteForNextLevel = percentCompleteForNextLevel;
    }

    public long getSummonerId() {
        return summonerId;
    }

    public void setSummonerId(long summonerId) {
        this.summonerId = summonerId;
    }

    public int getSummonerLevel() {
        return summonerLevel;
    }

    public void setSummonerLevel(int summonerLevel) {
        this.summonerLevel = summonerLevel;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public int getXpSinceLastLevel() {
        return xpSinceLastLevel;
    }

    public void setXpSinceLastLevel(int xpSinceLastLevel) {
        this.xpSinceLastLevel = xpSinceLastLevel;
    }

    public int getXpUntilNextLevel() {
        return xpUntilNextLevel;
    }

    public void setXpUntilNextLevel(int xpUntilNextLevel) {
        this.xpUntilNextLevel = xpUntilNextLevel;
    }

    public Summoner() {}

    public Summoner(int accountId, String gameName, int percentCompleteForNextLevel, long summonerId, int summonerLevel, String tagLine, int xpSinceLastLevel, int xpUntilNextLevel) {
        this.accountId = accountId;
        this.gameName = gameName;
        this.percentCompleteForNextLevel = percentCompleteForNextLevel;
        this.summonerId = summonerId;
        this.summonerLevel = summonerLevel;
        this.tagLine = tagLine;
        this.xpSinceLastLevel = xpSinceLastLevel;
        this.xpUntilNextLevel = xpUntilNextLevel;
    }

    @JsonIgnoreProperties
    public static Summoner parseFromJson(String eventData) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(eventData, Summoner.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        return "{" +
                "accountId=" + accountId +
                ", gameName='" + gameName + '\'' +
                ", percentCompleteForNextLevel=" + percentCompleteForNextLevel +
                ", summonerId=" + summonerId +
                ", summonerLevel=" + summonerLevel +
                ", tagLine='" + tagLine + '\'' +
                ", xpSinceLastLevel=" + xpSinceLastLevel +
                ", xpUntilNextLevel=" + xpUntilNextLevel +
                '}';
    }
}
