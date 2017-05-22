package Jaybot.YOLOBOT.Util.Wissensdatenbank;

/**
 * Created by Thomas on 17.05.17.
 */
public class DecisionNode {
    public int attributeID;
    public int key;
    public int classCounter[];
    public DecisionNode leftChild, rightChild;

    public DecisionNode(){
        leftChild = null;
        rightChild = null;
    }

    public DecisionNode(int classNum){
        classCounter = new int[classNum];
        leftChild = null;
        rightChild = null;
    }

    public boolean isLeaf(){
        return this.leftChild==null && this.rightChild==null;
    }
}
