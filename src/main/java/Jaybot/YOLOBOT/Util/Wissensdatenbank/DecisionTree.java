package Jaybot.YOLOBOT.Util.Wissensdatenbank;


import java.util.*;

/**
 * Created by Thomas on 17.05.17.
 */
public class DecisionTree {
    private int attributeNum;
    private int classNum;
    private int depth;
    private ArrayList<Integer> attributes;
    private DecisionNode root;
    private int[] attrMax;
    private int[] attrMin;
    private Random rdm;

    DecisionTree(){
        attributes = new ArrayList<>();
        for(int i=0;i<attributeNum;i++) attributes.add(i);
        root = new DecisionNode(classNum);
        rdm = new Random(System.currentTimeMillis());
    }

    public void buildTree(int[] attrMax, int[] attrMin){
        this.attrMax = attrMax;
        this.attrMin = attrMin;
        root = new DecisionNode(classNum);
        buildTree(root);
    }

    private void buildTree(DecisionNode p){
        if((attributeNum-attributes.size())>=depth) return;
        int attrIndex = Math.abs(rdm.nextInt() % attributes.size());
        int attrID = attributes.get(attrIndex);
        p.attributeID = attrID;
        p.key = Math.abs(rdm.nextInt() % (attrMax[attrID]-attrMin[attrID]+1)) + attrMin[attrID];
        attributes.remove(attrIndex);
        p.leftChild = new DecisionNode(classNum);
        p.rightChild = new DecisionNode(classNum);
        buildTree(p.leftChild);
        buildTree(p.rightChild);
        attributes.add(attrID);
    }

    public void learnSample(int[] X, int y,boolean poisson){
        DecisionNode current = root;
        while(current.leftChild!=null && current.rightChild!=null){
            if(X[current.attributeID]<=current.key || current.attributeID>=X.length) current = current.leftChild;
            else current = current.rightChild;
        }
        current.classCounter[y] += poisson?getPoisson(1):1;
    }

    public int[] singlePredict(int[] X, int searchDepth){
        DecisionNode current = root;
        int currentDepth=0;
        while(current!=null && !current.isLeaf() && currentDepth<searchDepth){
            if(X[current.attributeID]<=current.key || current.attributeID>=X.length) current = current.leftChild;
            else current = current.rightChild;
            currentDepth++;
        }
        return current.classCounter;
    }

    public static int getPoisson(double lambda){
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;
        do {
            k++;
            p *= Math.random();
        } while (p > L);
        return k - 1;
    }

    public void setTreeSize(int depth, int classNum, int attributeNum){
        this.depth = depth;
        this.classNum = classNum;
        this.attributeNum = attributeNum;
    }

}
