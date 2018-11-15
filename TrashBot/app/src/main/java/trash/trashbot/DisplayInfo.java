package trash.trashbot;

public class DisplayInfo {

    private String type;
    private String name;

    public DisplayInfo(String n, String t) {
        name = n;
        type = t;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
