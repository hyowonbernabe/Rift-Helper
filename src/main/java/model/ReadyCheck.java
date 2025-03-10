package model;

public class ReadyCheck {
    private String dodgeWarning;
    private String playerResponse;
    private String state;
    private boolean suppressUx;
    private double timer;

    public String getDodgeWarning() {
        return dodgeWarning;
    }

    public void setDodgeWarning(String dodgeWarning) {
        this.dodgeWarning = dodgeWarning;
    }

    public String getPlayerResponse() {
        return playerResponse;
    }

    public void setPlayerResponse(String playerResponse) {
        this.playerResponse = playerResponse;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public boolean isSuppressUx() {
        return suppressUx;
    }

    public void setSuppressUx(boolean suppressUx) {
        this.suppressUx = suppressUx;
    }

    public double getTimer() {
        return timer;
    }

    public void setTimer(double timer) {
        this.timer = timer;
    }

    public ReadyCheck() {}

    public ReadyCheck(String dodgeWarning, String playerResponse, String state, boolean suppressUx, double timer) {
        this.dodgeWarning = dodgeWarning;
        this.playerResponse = playerResponse;
        this.state = state;
        this.suppressUx = suppressUx;
        this.timer = timer;
    }

    @Override
    public String toString() {
        return "{" +
                "dodgeWarning='" + dodgeWarning + '\'' +
                ", playerResponse='" + playerResponse + '\'' +
                ", state='" + state + '\'' +
                ", suppressUx=" + suppressUx +
                ", timer=" + timer +
                '}';
    }
}
