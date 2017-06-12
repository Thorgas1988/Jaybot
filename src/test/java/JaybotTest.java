import Jaybot.YOLOBOT.Agent;
import core.ArcadeMachine;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

/**
 * Created by Torsten on 21.04.17.
 */
public class JaybotTest {
    private static final String gamesPath = "examples/gridphysics/";
    private static final String generateLevelPath = "examples/gridphysics/";

    private static final String[] games = new String[]{
            "aliens", "bait", "blacksmoke", "boloadventures", "boulderchase", "boulderdash", "brainman", "butterflies",
            "cakybaky", "camelRace", "catapults", "chase", "chipschallenge", "chopper", "cookmepasta", "crossfire",
            "defem", "defender", "digdug", "eggomania", "enemycitadel", "escape", "factorymanager", "firecaster",
            "firestorms", "frogs", "gymkhana", "hungrybirds", "iceandfire", "infection", "intersection", "jaws",
            "labyrinth", "lasers", "lasers2", "lemmings", "missilecommand", "modality", "overload", "pacman",
            "painter", "plants", "plaqueattack", "portals", "raceBet2", "realportals", "realsokoban", "roguelike",
            "seaquest", "sheriff", "sokoban", "solarfox", "superman", "surround", "survivezombies", "tercio",
            "thecitadel", "waitforbreakfast", "watergame", "whackamole", "zelda", "zenpuzzle"
    };

    private static final int seed = (new Random()).nextInt();
    private static final byte gameIdx = 25;//18//28//29//31
    private static byte levelIdx = 1;
    private static String game = gamesPath + games[gameIdx] + ".txt";
    private static String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx + ".txt";
    private static final String recordLevelFile = generateLevelPath + games[gameIdx] + "_glvl.txt";
    private static final String recordActionsFileOnFS = "/tmp/JaybotTests.txt";
    private static final String recordActionsFile = null;


    @Test
    @Ignore("Activate to play the game by yourself")
    public void shouldPlayGame() {
        ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);
    }

    @Test
    //@Ignore
    public void shouldRunGame() {
        ArcadeMachine.runOneGame(game, level1, true, Agent.class.getCanonicalName(), recordActionsFile, seed, 0);
    }

    @Test
    @Ignore
    public void runGameInLoop() {
        int times = 5;
        int wins = 0;
        int disq = 0;
        int loose = 0;
        for (int j = 0; j < 1; j++)
        {
            for (int i = 0; i < times; i++) {
                double[] testresult = ArcadeMachine.runOneGame(game, level1, false, Agent.class.getCanonicalName(), null, seed, 0);
                System.out.println("Level:"+levelIdx+"+,testresult[0]:+"+testresult[0]);

                //TODO: Works only for jaws #31
                if (testresult[0] > 1000) {
                    wins++;
                }
                else if(testresult[0] == -1000) {
                    disq++;
                }
                else {
                    loose++;
                }
                //wrap around
                if (levelIdx == 4) {
                    levelIdx = 0;
                }
            }
            System.out.println("Level:"+levelIdx);
            ++levelIdx;
            System.out.println("Wins:"+wins);
            System.out.println("Disqualified:"+disq);
            System.out.println("Loose:"+loose);
            int all = wins+disq+loose;
            System.out.println("Win Rate:"+(double)wins/(double)all);
        }
    }

    @Test
    @Ignore
    public void runOneGameForXTimes(){
        int gameIndex = 26;
        int levelIndex = 2;
        int times = 5;
        System.out.println("# " + gameIndex + " " + games[gameIndex]);
        for(int i=0;i<times;i++){
            game = gamesPath + games[gameIndex] + ".txt";
            level1 = gamesPath + games[gameIndex] + "_lvl" + levelIndex + ".txt";
            System.out.print((i+1)+"-th time:");
            try{
                ArcadeMachine.runOneGame(game, level1, false, Agent.class.getCanonicalName(), (String) recordActionsFile, seed, 0);
            }catch(Exception e){
                System.out.print("Fail:");
                System.out.println(e.toString());
            }
        }
    }


    @Test
    @Ignore("Only activate if you want to test all games at once! Takes a while.")
    public void shouldRunAllGames() {
        for (int i = 16; i < games.length; i++) {
            game = gamesPath + games[i] + ".txt";
            for(int j=0;j<1;j++){
                level1 = gamesPath + games[i] + "_lvl" + j + ".txt";
                System.out.println("# " + i);
                System.out.println(games[i] + " Level " + j + ":");
                for(int t = 0;t<5;t++){
                    System.out.print((t+1)+"-th time:");
                    try{
                        ArcadeMachine.runOneGame(game, level1, false, Agent.class.getCanonicalName(), (String) recordActionsFile, seed, 0);
                    }catch(Exception e){
                        System.out.println("Fail!");
                        System.out.println(e.toString());
                    }
                }
                System.out.println();
            }

        }
    }

}
