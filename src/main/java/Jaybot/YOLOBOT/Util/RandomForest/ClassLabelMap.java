package Jaybot.YOLOBOT.Util.RandomForest;

import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Torsten on 05.06.17.
 */
public class ClassLabelMap {
    Map<TreePath, Map<YoloEvent, Integer>> classes = new HashMap<>();

    public void put(TreePath path, YoloEvent classEvent) {
        Map<YoloEvent, Integer> hit = classes.get(path);

        if (hit == null) {
            hit = new HashMap<>();
            hit.put(classEvent, 1);
        } else {
            hit.put(classEvent, hit.get(classEvent) + 1);
        }

        classes.put(path, hit);
    }

    public YoloEvent getEvent(TreePath path) {
        YoloEvent resultEvent = new YoloEvent();

        Map<YoloEvent, Integer> events = classes.get(path);
        if (events == null) {
            return resultEvent;
        }

        int max = Integer.MIN_VALUE;
        for (Map.Entry<YoloEvent, Integer> entry : events.entrySet()) {
            if (max < entry.getValue().intValue()) {
                max = entry.getValue().intValue();
                resultEvent = entry.getKey();
            }
        }

        return resultEvent;
    }
}
