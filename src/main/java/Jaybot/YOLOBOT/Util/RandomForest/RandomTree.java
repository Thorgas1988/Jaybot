package Jaybot.YOLOBOT.Util.RandomForest;

import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by Torsten on 17.05.17.
 */
public class RandomTree {

    private final RandomCondition[] conditions;
    private final Map<boolean[], Map<YoloEvent, Integer>> classes = new HashMap<>();

    public RandomTree(byte treeSize) {
        conditions = new RandomCondition[treeSize];

        for (int i=0; i<treeSize; i++) {
            conditions[i] = new RandomCondition();
        }
    }

    private boolean[] getTreePath(byte[] inventory) {
        boolean[] path = new boolean[conditions.length];

        for (int i = 0; i<inventory.length; i++) {
            path[i] = conditions[i].conditionIsTrue(inventory[i]);
        }

        return path;
    }

    public YoloEvent getEvent(byte[] inventory) {
        Map<YoloEvent, Integer> events = classes.get(getTreePath(inventory));

        int max = Integer.MIN_VALUE;
        YoloEvent resultEvent = null;

        for (Entry<YoloEvent, Integer> entry : events.entrySet()) {
            if (max < entry.getValue().intValue()) {
                max = entry.getValue().intValue();
                resultEvent = entry.getKey();
            }
        }

        return resultEvent;
    }

    public void train(byte[] inventory, YoloEvent event) {
        boolean[] path = getTreePath(inventory);
        Map<YoloEvent, Integer> events = classes.get(path);

        if (events == null) {
            events = new HashMap<>();
            events.put(event, 1);
            classes.put(path, events);
        } else {
            int frequency = 0;
            if (events.containsKey(event)) {
                frequency = events.get(event);
            }
            events.put(event, frequency + 1);
        }
    }
}
