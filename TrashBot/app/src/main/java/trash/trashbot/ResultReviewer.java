package trash.trashbot;


import java.util.List;

public class ResultReviewer {

    public static String TRASH = "Trash";
    public static String COMPOST = "Compost";
    public static String RECYCLABLE = "Recyclable";

    public static String[] recyclableList = {"plastic bottle", "water bottle"
            //...
    };

    private int counter = 0;

    private Classifier.Recognition recognition;

    public static ResultReviewer newInstance() {
        return new ResultReviewer();
    }

    private ResultReviewer() {

    }

    public void resultCheckin(List<Classifier.Recognition> itemList) {
        if(itemList.size() > 0) {
            recognition = itemList.get(0);
        }
    }

    public DisplayInfo decideInfo() {
        counter += 1;
        if (counter >= 5) {
            if(counter > 10) {
                counter = 0;
            }
            DisplayInfo info = new DisplayInfo("1234", COMPOST);
            return info;
        }
        return null;
    }
}
