package Jaybot.YOLOBOT.Util.Wissensdatenbank;

import java.util.*;

/**
 * Created by Thomas on 18.05.17.
 */
public class DecisionForest {
    private int attributeNum;
    private int classNum;
    private int treeNum;
    private ArrayList<DecisionTree> trees;
    private Random rdm;

    DecisionForest(){
        rdm = new Random(System.currentTimeMillis());
        trees = new ArrayList<>();
    }

    public void buildForest(int[] attrMax, int[] attrMin){
        while(trees.size()<treeNum){
            int treeDepth = Math.abs(rdm.nextInt() % attributeNum + 1);
            DecisionTree decisionTree = new DecisionTree();
            decisionTree.setTreeSize(treeDepth,classNum,attributeNum);
            decisionTree.buildTree(attrMax,attrMin);
            trees.add(decisionTree);
        }
    }

    public void learnSample(int[] X, int y, boolean poisson){
        Iterator<DecisionTree> iteTree = trees.iterator();
        while(iteTree.hasNext()){
            DecisionTree currentTree = iteTree.next();
            currentTree.learnSample(X,y,poisson);
        }
    }

    public int predict(int[] X, int searchDepth){
        int[] classCount = new int[classNum];
        Iterator<DecisionTree> iteTree = trees.iterator();
        while(iteTree.hasNext()){
            DecisionTree currentTree = iteTree.next();
            int[] currentClassCounter = currentTree.singlePredict(X,searchDepth);
            for(int i=0;i<classNum;i++) classCount[i] += currentClassCounter[i];
        }
        int maxCount = 0;
        int res = 0;
        for(int i=0;i<classNum;i++){
            if(classCount[i]>maxCount){
                maxCount = classCount[i];
                res = i;
            }
        }
        return res;
    }


    public void setForestSize(int attributeNum,int classNum,int treeNum){
        this.attributeNum = attributeNum;
        this.classNum = classNum;
        this.treeNum = treeNum;
    }
}
