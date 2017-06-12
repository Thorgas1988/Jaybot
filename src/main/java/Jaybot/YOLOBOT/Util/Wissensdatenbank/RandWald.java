package Jaybot.YOLOBOT.Util.Wissensdatenbank;

/**
 * Created by Thomas on 08.06.17.
 */
import Jaybot.YOLOBOT.Util.RandomForest.InvolvedActors;
import java.lang.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RandWald {

    private static final int _VICTORY = 0;
    private static final int _DEFEAT = 1;
    private static final int _BLOCKED = 2;
    private static final int _ITYPE_CHANGE = 0;
    private static final int _SPAWNED = 1;
    private static final int _TELEPORT = 2;
    private static final int _ADD_INVENT = 3;
    private static final int _REMOVE_INVENT = 4;
    private static final int _SCORE_CHANGE = 5;
    private static final int _HP_CHANGE = 6;
    private static final int _PUSH = 7;

    private static final Random rand = new Random(System.currentTimeMillis());
    private int treeNum;
    private tree[] trees;
    private final boolean POISSON = false;

    RandWald(int treeNum, int treeSize){
        this.treeNum = treeNum;
        trees = new tree[this.treeNum];

        for(int i=0;i<this.treeNum;i++){
            trees[i] = new tree(treeSize);
            trees[i].buildTreeCondition(0,3);
        }
    }

    public void train(InvolvedActors actors, byte[] inventory,
                      boolean victory,boolean defeat, boolean blocked,
                      byte itypeChange, int scoreChange, int hpChange,
                      byte spawned, byte teleport, byte addInventory, byte removeInventory, byte pusher){
        for(int i=0;i<treeNum;i++){
            trees[i].train(actors,inventory,victory,defeat,blocked,itypeChange,scoreChange,hpChange,
                    spawned,teleport,addInventory,removeInventory,pusher,POISSON);
        }
    }

    public void trainCancel(InvolvedActors actors, byte[] inventory){
        for(int i=0;i<treeNum;i++){
            trees[i].trainCancel(actors,inventory,POISSON);
        }
    }

    public void trainVictory(InvolvedActors actors, byte[] inventory){
        for(int i=0;i<treeNum;i++){
            trees[i].trainVictory(actors,inventory,POISSON);
        }
    }

    public YoloEvent predict(InvolvedActors actors, byte[] inventory){
        leaf sum = new leaf();
        for(int i=0;i<treeNum;i++){
            leaf tmpLeaf = trees[i].predict(actors,inventory);
            if(tmpLeaf!=null){
                sum.sumLeaf(tmpLeaf);
            }
        }
        int[] res = sum.getMaxAttri();
        YoloEvent event = new YoloEvent();
        event.setVictory(res[_VICTORY]>0);
        event.setDefeat(res[_DEFEAT]>0);
        event.setBlocked(res[_BLOCKED]>0);
        event.setNewIType(res[3+_ITYPE_CHANGE]);
        event.setHpDelta(res[3+_HP_CHANGE]);
        event.setScoreDelta(res[3+_SCORE_CHANGE]);
        event.setTeleportTo(res[3+_TELEPORT]);
        event.setSpawns(res[3+_SPAWNED]);
        event.setRemoveInventorySlotItem(res[3+_REMOVE_INVENT]);
        event.setAddInventorySlotItem(res[3+_ADD_INVENT]);
        event.setOldIType(actors.getPlayerIType());
        return event;
    }


    public class tree{

        private int treeSize;
        private condition[] conditions;
        ArrayList<Integer> arr;
        HashMap<Byte,HashMap<InvolvedActors,leaf>> allLeaves;

        tree(int treeSize){
            this.treeSize = treeSize;
            int leafNum = (int) Math.pow(2,treeSize) - 1;
            conditions = new condition[leafNum];
            for(int i=0;i<leafNum;i++) conditions[i] = new condition();
            arr = new ArrayList<>();
            for(int i=0;i<treeSize;i++) arr.add(i);
            allLeaves = new HashMap<>();
        }

        public void train(InvolvedActors actors, byte[] inventory,
                          boolean victory,boolean defeat, boolean blocked,
                          byte itypeChange, int scoreChange, int hpChange,
                          byte spawned, byte teleport, byte addInventory, byte removeInventory, byte pusher,
                          boolean poisson){
            int step = poisson?getPoisson(1):1;
            byte path = getPathMask(inventory);
            if(!allLeaves.containsKey(path)){
                HashMap<InvolvedActors,leaf> tmp = new HashMap<>();
                tmp.put(actors,new leaf());
                allLeaves.put(path,tmp);
            }else if(!allLeaves.get(path).containsKey(actors)){
                allLeaves.get(path).put(actors,new leaf());
            }
            allLeaves.get(path).get(actors).updateLeaf(victory,defeat,blocked,itypeChange,scoreChange,hpChange,
                    spawned,teleport,addInventory,removeInventory,pusher,step);
        }

        public void trainCancel(InvolvedActors actors, byte[] inventory,boolean poisson){
            int step = poisson?getPoisson(1):1;
            byte path = getPathMask(inventory);
            if(!allLeaves.containsKey(path)){
                HashMap<InvolvedActors,leaf> tmp = new HashMap<>();
                tmp.put(actors,new leaf());
                allLeaves.put(path,tmp);
            }else if(!allLeaves.get(path).containsKey(actors)){
                allLeaves.get(path).put(actors,new leaf());
            }
            allLeaves.get(path).get(actors).updateCancel(step);
        }

        public void trainVictory(InvolvedActors actors, byte[] inventory,boolean poisson){
            int step = poisson?getPoisson(1):1;
            byte path = getPathMask(inventory);
            if(!allLeaves.containsKey(path)){
                HashMap<InvolvedActors,leaf> tmp = new HashMap<>();
                tmp.put(actors,new leaf());
                allLeaves.put(path,tmp);
            }else if(!allLeaves.get(path).containsKey(actors)){
                allLeaves.get(path).put(actors,new leaf());
            }
            allLeaves.get(path).get(actors).updateVictory(step);
        }

        public leaf predict(InvolvedActors actors,byte[] inventory){
            byte path = getPathMask(inventory);
            if(!allLeaves.containsKey(path)) return null;
            else if(!allLeaves.get(path).containsKey(actors)) return null;
            else return allLeaves.get(path).get(actors);
        }

        public int getPoisson(double lambda){
            double L = Math.exp(-lambda);
            double p = 1.0;
            int k = 0;
            do {
                k++;
                p *= Math.random();
            } while (p > L);
            return k - 1;
        }

        public void buildTreeCondition(int conditionIndex, int inventmax){
            int availableAttrNum = arr.size();
            if(availableAttrNum == 0) return;
            int arrIndex = (Math.abs(rand.nextInt()) % availableAttrNum);
            int attrindex = arr.get(arrIndex);
            byte key = (byte)(Math.abs(rand.nextInt()) % inventmax + 1);
            conditions[conditionIndex].setValue(attrindex,key);
            arr.remove(arrIndex);
            buildTreeCondition(2*conditionIndex+1, inventmax);
            buildTreeCondition(2*conditionIndex+2, inventmax);
            arr.add(attrindex);
        }

        public byte getPathMask(byte[] inventory){
            int p = 0, i=0;
            byte res = 0;
            while(i<treeSize){
                int currentAttri = conditions[p].attriNum;
                byte currentKey = conditions[p].key;
                if(currentAttri>=inventory.length || inventory[currentAttri]<=currentKey){
                    p = 2*p+1;
                }else{
                    p = 2*p+2;
                    res |= 1<<i;
                }
                i++;
            }
            return res;
        }

        public condition[] getConditions(){
            return this.conditions;
        }

    }

    public class leaf{

        private HashMap<Byte,Integer>[] byteMaps;
        private int[] boolCounter;

        leaf(){
            boolCounter = new int[6];
            byteMaps = new HashMap[8];
            for(int i=0;i<8;i++) byteMaps[i] = new HashMap<>();
        }

        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("Victory Count:" + boolCounter[_VICTORY]);
            sb.append("   Defeat Count:" + boolCounter[_DEFEAT]);
            sb.append("   Block Count:" + boolCounter[_BLOCKED] + "\n");
            sb.append("Map of itype change:" + byteMaps[_ITYPE_CHANGE].toString()+"\n");
            sb.append("Map of spawned itypes:" + byteMaps[_SPAWNED].toString()+"\n");
            sb.append("Map of teleport:" + byteMaps[_TELEPORT].toString()+"\n");
            sb.append("Map of add inventory:" + byteMaps[_ADD_INVENT].toString()+"\n");
            sb.append("Map of remove inventory:" + byteMaps[_REMOVE_INVENT].toString()+"\n");
            sb.append("Map of score change:" + byteMaps[_SCORE_CHANGE].toString()+"\n");
            sb.append("Map of hp change:" + byteMaps[_HP_CHANGE].toString()+"\n\n");
            return sb.toString();
        }

        private void updateLeaf(boolean victory,boolean defeat, boolean blocked,
                                byte itypeChange, int scoreChange, int hpChange,
                                byte spawned, byte teleport, byte addInventory, byte removeInventory, byte pusher,
                                int step){
            updateBoolCounter(boolCounter,_DEFEAT,defeat,step);
            updateBoolCounter(boolCounter,_VICTORY,victory,step);
            updateBoolCounter(boolCounter,_BLOCKED,blocked,step);

            updateHashMap(this.byteMaps[_SCORE_CHANGE],scoreChange>127?(byte)127:(byte)scoreChange);
            updateHashMap(this.byteMaps[_HP_CHANGE],hpChange>127?(byte)127:(byte)hpChange);
            updateHashMap(this.byteMaps[_ITYPE_CHANGE],itypeChange);
            updateHashMap(this.byteMaps[_SPAWNED],spawned);
            updateHashMap(this.byteMaps[_TELEPORT],teleport);
            updateHashMap(this.byteMaps[_ADD_INVENT],addInventory);
            updateHashMap(this.byteMaps[_REMOVE_INVENT],removeInventory);
            updateHashMap(this.byteMaps[_PUSH],pusher);
        }

        private void updateVictory(int step){
            boolCounter[_VICTORY] += step;
        }

        private void updateCancel(int step){
            boolCounter[_BLOCKED] += step;
        }

        private void updateBoolCounter(int[] a,int index, boolean test, int step){
            if(test) boolCounter[index] += step;
            else boolCounter[index+3] += step;
        }

        private void updateHashMap(HashMap<Byte,Integer> h, byte key){
            if(h.containsKey(key)) h.replace(key,h.get(key)+1);
            else h.put(key,1);
        }

        public int[] getBoolCounter(){ return this.boolCounter;}

        public HashMap<Byte,Integer>[] getByteMaps(){ return this.byteMaps; }

        public void sumLeaf(leaf b){
            HashMap<Byte,Integer>[] bmaps = b.getByteMaps();
            for(int i=0;i<8;i++) mergeByteMap(byteMaps[i],bmaps[i]);
            for(int i=0;i<6;i++) boolCounter[i] += b.getBoolCounter()[i];
        }

        public void mergeByteMap(HashMap<Byte,Integer> a, HashMap<Byte,Integer> b){
            for(Map.Entry<Byte,Integer> e:b.entrySet()){
                if(a.containsKey(e.getKey())) a.replace(e.getKey(),a.get(e.getKey()) + e.getValue());
                else a.put(e.getKey(),e.getValue());
            }
        }

        public int[] getMaxAttri(){
            int[] res = new int[11];
            res[_ADD_INVENT] = -1;
            res[_REMOVE_INVENT] = -1;
            res[_ITYPE_CHANGE] = -1;
            res[_SPAWNED] = -1;
            res[_TELEPORT] = -1;
            res[_PUSH] = -1;
//            if(boolCounter[_BLOCKED]>0 && boolCounter[_BLOCKED]>=boolCounter[_VICTORY]
//                    && boolCounter[_BLOCKED]>=boolCounter[_DEFEAT] ) res[_BLOCKED] = 1;
//            else if(boolCounter[_VICTORY]>boolCounter[_DEFEAT]) res[_VICTORY] = 1;
//            else{
//                res[_DEFEAT] = 1;
//                res[_BLOCKED] = 1;
//            }
            boolean blockGreater = boolCounter[_BLOCKED]>boolCounter[_BLOCKED+3];
            boolean defeatGreater = boolCounter[_DEFEAT]>boolCounter[_DEFEAT+3];
            boolean victoryGreater = boolCounter[_VICTORY]>boolCounter[_VICTORY+3];
            if(blockGreater) res[_BLOCKED] = 1;
            if(defeatGreater) res[_DEFEAT] = 1;
            if(victoryGreater) res[_VICTORY] = boolCounter[_VICTORY]>boolCounter[_BLOCKED]+boolCounter[_DEFEAT]?1:0;

            for(int i=0;i<8;i++){
                HashMap<Byte,Integer> tmp = byteMaps[i];
                int max = -1;
                int maxind = -1;
                for(Map.Entry<Byte,Integer> e:tmp.entrySet()){
                    if(e.getValue()>max){
                        max = e.getValue();
                        maxind = e.getKey();
                    }
                }
                res[3+i] = maxind;
            }
            return res;
        }

    };

    public class condition{
        int attriNum;
        byte key;
        void setValue(int attrinum, byte key){
            this.attriNum = attrinum;
            this.key = key;
        }
    };

}
