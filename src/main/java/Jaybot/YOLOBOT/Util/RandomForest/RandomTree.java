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
    private final Map<TreePath, Map<YoloEvent, Integer>> classes = new HashMap<>();

    public RandomTree(int treeSize) {
        conditions = new RandomCondition[treeSize];

        for (int i=0; i<treeSize; i++) {
            // we assume the count of inventory items can only be positive.
            conditions[i] = new RandomCondition(true);
        }
    }

    private TreePath getTreePath(byte[] inventory) {
        int limit = Math.min(inventory.length, conditions.length);
        TreePath path = new TreePath(limit);

        for (int i = 0; i<limit; i++) {
            path.setPathNode(i, conditions[i].conditionIsTrue(inventory[i]));
        }

        return path;
    }

    public YoloEvent getEvent(byte[] inventory) {
        Map<YoloEvent, Integer> events = classes.get(getTreePath(inventory));

        if (events == null) {
            return null;
        }

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
        TreePath path = getTreePath(inventory);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<conditions.length; i++) {
            sb.append("x").append(i).append(conditions[i].toString()).append("\n");
        }

        return sb.toString();
    }

    public String printTrainedTree() {
        StringBuilder sb = new StringBuilder();

        for (Entry<TreePath, Map<YoloEvent, Integer>> entry : classes.entrySet()) {
            sb.append(entry.getKey().toString());
            sb.append("\n");
            for (Entry<YoloEvent, Integer> events : entry.getValue().entrySet()) {
                sb.append(events.getValue()).append("x ").append(events.getKey().toString()).append("\n");
            }
        }

        return sb.toString();
    }

}
