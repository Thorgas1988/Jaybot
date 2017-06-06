package Jaybot.YOLOBOT.Util.RandomForest;

import java.util.Arrays;

/**
 * Created by Torsten on 25.05.17.
 */
public class TreePath {
    private boolean[] path;

    public TreePath(int pathSize) {
        path = new boolean[pathSize];
    }

    public void setPathNode(int nodeIndex, boolean result) {
        path[nodeIndex] = result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreePath treePath = (TreePath) o;

        return Arrays.equals(path, treePath.path);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(path);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();

        for (boolean b : path) {
            if (b)
                sb.append(1);
            else
                sb.append(0);
        }

        return sb.toString();
    }
}
