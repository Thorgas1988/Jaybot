package Jaybot.YOLOBOT.Util.RandomForest;

import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Torsten on 18.05.2017.
 */
public class RandomForest {

    private RandomTree[] forest;
    private int treeSize;

    public RandomForest(int treeSize, int forestSize) {
        this.treeSize = treeSize;
        forest = new RandomTree[forestSize];

        for (int treeIndex = 0; treeIndex < forestSize; treeIndex++) {
            forest[treeIndex] = new RandomTree(treeSize);
        }
    }

    public void train(int playerIType, int otherIType, byte[][] inventory, YoloEvent[] events) {
        if (inventory == null || events == null || inventory.length != events.length)
            throw new IllegalArgumentException("The inventory and event arrays have to be the same length");

        for (int i = 0; i < inventory.length; i++) {
            train(playerIType, otherIType, inventory[i], events[i]);
        }
    }

    public void train(int playerIType, int otherIType, byte[] inventory, YoloEvent event) {
        for (RandomTree tree : forest) {
            tree.train(playerIType, otherIType, inventory, event);
        }
    }

    public YoloEvent getEvent(int playerIType, int otherIType, byte[] inventory) {
        Map<YoloEvent, Integer> eventFrequencies = new HashMap<>();
        int maxFrequency = -1;
        YoloEvent maxYoloEvent = new YoloEvent();

        for (RandomTree tree : forest) {
            YoloEvent event = tree.getEvent(playerIType, otherIType, inventory);

            if (event == null)
                continue;

            int frequency = 1;
            if (eventFrequencies.containsKey(event)) {
                frequency = eventFrequencies.get(event) + 1;
            }
            eventFrequencies.put(event, frequency);

            if (frequency > maxFrequency) {
                maxFrequency = frequency;
                maxYoloEvent = event;
            }
        }

        return maxYoloEvent;
    }

    public boolean hasEventForActors(int playerIType, int otherIType) {
        for (RandomTree tree : forest) {
            if (!tree.hasEventForActors(playerIType, otherIType))
                return false;
        }
        return true;
    }

    @Override
    public String toString() {
        boolean addComma = false;
        StringBuilder sb = new StringBuilder("{ \"forest\" : [\n");

        for (RandomTree tree : forest) {
            sb.append((addComma ? ", \n" : ""));
            sb.append(tree.toString());
            addComma = true;
        }

        sb.append("]}");
        return sb.toString();
    }

    public RandomTree[] getTrees() {
        return forest;
    }
}
