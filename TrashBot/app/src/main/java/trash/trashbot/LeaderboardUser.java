package trash.trashbot;

public class LeaderboardUser {
    private String username;
    private String carbonPoints;
    private int icon;

    public LeaderboardUser(String name, String points, int iconId) {
        username = name;
        carbonPoints = points;
        icon = iconId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCarbonPoints() {
        return carbonPoints;
    }

    public void setCarbonPoints(String carbonPoints) {
        this.carbonPoints = carbonPoints;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
