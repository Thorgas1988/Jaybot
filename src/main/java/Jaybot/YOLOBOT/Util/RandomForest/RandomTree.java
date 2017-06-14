package Jaybot.YOLOBOT.Util.RandomForest;

import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloKnowledge;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Torsten on 17.05.17.
 */
public class RandomTree {

    private final RandomCondition[] conditions;
    private final ClassLabelMap[][] classes = new ClassLabelMap[YoloKnowledge.INDEX_MAX][YoloKnowledge.INDEX_MAX];
    private final int[] indices;

    public RandomTree(int treeSize) {
        conditions = new RandomCondition[treeSize];
        indices = new int[treeSize];

        List<Integer> possibleIndices = new LinkedList<>();
        for (int i=0; i<YoloKnowledge.INDEX_MAX; i++) {
            possibleIndices.add(i);
        }
        Collections.shuffle(possibleIndices);

        for (int i=0; i<treeSize; i++) {
            // we assume the count of inventory items can only be positive.
            conditions[i] = new RandomCondition(true);
            indices[i] = possibleIndices.get(i).intValue();
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

    public YoloEvent getEvent(int playerIType, int otherIType, byte[] inventory) {
        int playerIndex = YoloKnowledge.instance.itypeToIndex(playerIType);
        int otherIndex = YoloKnowledge.instance.itypeToIndex(otherIType);
        ClassLabelMap classLabels = classes[playerIndex][otherIndex];

        if (classLabels == null) {
            return new YoloEvent();
        }

        return classLabels.getEvent(getTreePath(inventory));
    }

    public void train(int playerIType, int otherIType, byte[] inventory, YoloEvent event) {
        int playerIndex = YoloKnowledge.instance.itypeToIndex(playerIType);
        int otherIndex = YoloKnowledge.instance.itypeToIndex(otherIType);
        ClassLabelMap classLabels = classes[playerIndex][otherIndex];

        if (classLabels == null) {
            classLabels = new ClassLabelMap();
        }

        classLabels.put(getTreePath(inventory), event);
        classes[playerIndex][otherIndex] = classLabels;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<conditions.length; i++) {
            sb.append("x").append(i).append(conditions[i].toString()).append("\n");
        }

        return sb.toString();
    }

    public boolean hasEventForActors(int playerIType, int otherIType) {
        int playerIndex = YoloKnowledge.instance.itypeToIndex(playerIType);
        int otherIndex = YoloKnowledge.instance.itypeToIndex(otherIType);
        return classes[playerIndex][otherIndex] != null;
    }
}
