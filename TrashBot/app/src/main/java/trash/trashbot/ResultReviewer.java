package trash.trashbot;


import java.util.Arrays;
import java.util.List;

public class ResultReviewer {

    public static String TRASH = "Trash";
    public static String COMPOST = "Compost";
    public static String RECYCLABLE = "Recyclable";
    public static String SPECIAL = "E-Waste";

    public static String[] recyclableList = {"file", "steel drum", "letter opener", "snorkel",
            "sunglasses", "computer keyboard", "typewriter keyboard", "sunglass",
            "pick", "coffeepot", "shower curtain", "carton", "plate", "tray", "whiskey jug",
            "beer bottle", "bottlecap", "Petri dish", "pop bottle", "crossword puzzle", "menu",
            "sunscreen", "beer glass", "water jug", "bucket", "soup bowl", "wine bottle", "binder",
            "birdhouse", "ping-pong ball", "pencil box", "cup", "milk can", "paper towel",
            "envelope", "toilet tissue", "lotion", "hair spray", "crate", "pill bottle", "barrel",
            "rain barrel", "comic book", "measuring cup", "water bottle", "plastic bag", "pitcher",
            "beaker", "coffee mug"
    };

    public static String[] compostList = {"strawberry", "Granny Smith", "orange", "lemon", "fig",
            "pineapple", "banana", "jackfruit", "custard apple", "pomegranate", "acorn",
            "rapeseed", "corn", "daisy", "yellow lady's slipper", "cock", "hen", "mashed potato",
            "bell pepper", "head cabbage", "broccoli", "cauliflower", "zucchini",
            "spaghetti squash", "acorn squash", "butternut squash", "cucumber", "artichoke",
            "cardoon", "mushroom", "bagel", "meat loaf", "guacamole", "wool", "hay", "potpie",
            "hotdog", "burrito", "espresso", "pizza", "wooden spoon", "saltshaker", "ice cream",
            "pretzel", "cheeseburger"};

    public static String[] trashList = {"umbrella", "soccer ball", "accordion", "balloon",
            "paintbrush", "lighter", "candle", "handkerchief", "sandal", "running shoe",
            "feather boa", "baseball", "swab", "hamper", "apron", "punching bag", "backpack",
            "broom", "mosquito net", "rugby ball", "miniskirt", "cowboy hat", "basketball",
            "wallet", "diaper", "Band Aid", "tennis ball", "doormat", "matchstick", "bikini",
            "sock", "rubber eraser", "stole", "carbonara"};

    public static String[] specialList = {"notebook","laptop", "mouse", "desktop computer",
            "hand-held computer", "remote control", "modem", "Polaroid camera", "monitor",
            "television", "iPod", "CD player"};


    private int counter = 0;
    private Classifier.Recognition recognition = null;

    public static ResultReviewer newInstance() {
        return new ResultReviewer();
    }

    private ResultReviewer() {

    }

    public void resultCheckin(List<Classifier.Recognition> itemList) {
        if (itemList.size() > 0) {
            if (recognition == null) {
                recognition = itemList.get(0);
                counter = 1;
            } else {
                if (recognition.equals(itemList.get(0)) && counter < 4) {
                    counter += 1;
                } else {
                    counter -= 1;
                    if (counter == 0)
                        recognition = null;
                }
            }
        } else if (counter > 0) {
            counter -= 1;
            if (counter == 0)
                recognition = null;

        }
    }

    public DisplayInfo decideInfo() {
        if (counter >= 2) {
            String name = recognition.getTitle();
            DisplayInfo info = null;
            if(Arrays.asList(recyclableList).contains(name)) {
                info = new DisplayInfo(name, RECYCLABLE);
            } else if (Arrays.asList(specialList).contains(name)) {
                if(name.equals("notebook")) {
                    name = "laptop";
                }
                info = new DisplayInfo(name, SPECIAL);
            } else if(Arrays.asList(compostList).contains(name)) {
                info = new DisplayInfo(name, COMPOST);
            } else if(Arrays.asList(trashList).contains(name)) {
                info = new DisplayInfo(name, TRASH);
            }
            return info;
        }
        return null;
    }
}
