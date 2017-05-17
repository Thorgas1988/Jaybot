package Jaybot.YOLOBOT.Util.RandomForest;

import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Torsten on 17.05.17.
 */
public class RandomTree {

    private final RandomCondition[] conditions;
    Map<boolean[], YoloEvent> classes = new HashMap<>();

    public RandomTree(byte treeSize) {
        conditions = new RandomCondition[treeSize];

        for (int i=0; i<treeSize; i++) {
            conditions[i] = new RandomCondition();
        }
    }

    public YoloEvent getEvent(byte[] inventory) {
        boolean[] path = new boolean[conditions.length];

        for (int i = 0; i<inventory.length; i++) {
            path[i] = conditions[i].conditionIsTrue(inventory[i]);
        }

        return classes.get(path);
    }
}
