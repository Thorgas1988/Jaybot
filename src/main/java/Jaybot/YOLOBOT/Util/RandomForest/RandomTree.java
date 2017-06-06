package Jaybot.YOLOBOT.Util.RandomForest;

import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Torsten on 17.05.17.
 */
public class RandomTree {

    private final RandomCondition[] conditions;
    private final Map<InvolvedActors, ClassLabelMap> classes = new HashMap<>();

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

    public YoloEvent getEvent(InvolvedActors actors, byte[] inventory) {
        ClassLabelMap classLabels = classes.get(actors);

        if (classLabels == null) {
            return new YoloEvent();
        }

        return classLabels.getEvent(getTreePath(inventory));
    }

    public void train(InvolvedActors actors, byte[] inventory, YoloEvent event) {
        ClassLabelMap classLabels = classes.get(actors);

        if (classLabels == null) {
            classLabels = new ClassLabelMap();
        }

        classLabels.put(getTreePath(inventory), event);
        classes.put(actors, classLabels);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<conditions.length; i++) {
            sb.append("x").append(i).append(conditions[i].toString()).append("\n");
        }

        return sb.toString();
    }

    public boolean hasEventForActors(InvolvedActors actors) {
        return classes.containsKey(actors);
    }

    public int classLabelCount() {
        int count = 0;
        for (ClassLabelMap classLabels : classes.values()) {
            count += classLabels.classLabelCount();
        }
        return count;
    }

    public int classLabelCount(YoloEvent event) {
        int count = 0;
        for (ClassLabelMap classLabels : classes.values()) {
            count += classLabels.classLabelCount(event);
        }
        return count;
    }
}
