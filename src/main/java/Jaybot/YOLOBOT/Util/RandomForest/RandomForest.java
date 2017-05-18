package Jaybot.YOLOBOT.Util.RandomForest;

import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Torsten on 18.05.2017.
 */
public class RandomForest {

    private RandomTree[] forest;

    public RandomForest(byte treeSize, int forestSize) {
        forest = new RandomTree[forestSize];

        for (int treeIndex = 0; treeIndex < forestSize; treeIndex++) {
            forest[treeIndex] = new RandomTree(treeSize);
        }
    }

    public void train(byte[][] inventory, YoloEvent[] events) {
        if (inventory == null || events == null || inventory.length != events.length)
            throw new IllegalArgumentException("The inventory and event arrays have to be the same length");

        for (int i=0; i < inventory.length; i++){
            train(inventory[i], events[i]);
        }
    }

    public void train(byte[] inventory, YoloEvent event) {
        for (RandomTree tree : forest) {
            tree.train(inventory, event);
        }
    }

    public YoloEvent getEvent(byte[] inventory) {
        Map<YoloEvent, Integer> eventFrequencies = new HashMap<>();
        int maxFrequency = -1;
        YoloEvent maxYoloEvent = null;

        for (RandomTree tree : forest) {
            YoloEvent event = tree.getEvent(inventory);

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
}
