package Jaybot.YOLOBOT.Util.RandomForest;

import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;
import Jaybot.YOLOBOT.YoloState;

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

    public void train(byte[][] inventory, YoloEvent[] events) {
        if (inventory == null || events == null || inventory.length != events.length)
            throw new IllegalArgumentException("The inventory and event arrays have to be the same length");

        for (int i = 0; i < inventory.length; i++) {
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
        YoloEvent maxYoloEvent = new YoloEvent();

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


    /**
     * Returns a forest compatible (i.e. with the count of the conditions) inventory.
     * CAREFUL: Cuts off all inventoryItems with an index > the condition count.
     *
     * @param state The YoloState to retrieve the inventory from.
     * @return The inventory as byte array. if a iType was not available in the inventory it is stored with an amount of zero.
     */
    public byte[] getInventoryArray(YoloState state) {
        Map<Integer, Integer> inventory = state.getAvatarResources();
        byte[] inventoryArray = new byte[treeSize];

        for (int i = 0; i<inventoryArray.length; i++) {
            Integer itemAmount = inventory.get(i);
            if (itemAmount == null) {
                inventoryArray[i] = 0;
            } else {
                inventoryArray[i] = itemAmount.byteValue();
            }
        }

        return inventoryArray;
    }

    public RandomTree[] getTrees() {
        return forest;
    }
}
