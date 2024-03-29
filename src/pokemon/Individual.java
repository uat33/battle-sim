package pokemon;

import moves.Attack;
import moves.Move;
import moves.StatChange;
import type.Type;
import setup.Setup;

import java.util.ArrayList;
import java.util.Arrays;


/*
 * subclass of pokemon.
 * this is where the actual pokemon is created
 */

public class Individual extends Species{




    private final int[] evs;
//    private double accuracy = 100, evasion = 100;

    // stats can be increased or decreased throughout a battle
    // the default multiplier is one, we can alter it here.
    // we need an array for this because we need to keep track of alterations
    // because alterations disappear if you switch out your pokemon

    private final int[] stats; // the actual stats of the pokemon, including changes


    private final int[] startingStats; // the stats when the pokemon first switches in. keeping this separate makes recalculation easier


    private final int maxHP; // the maximum health of this pokemon



    // natures. there are 25 of them. each one raises a stat and decreases one of the five stats (hp is excluded)
    // this change is 10 percent.
    private final String nature;


    private final int teamNum; // keep track of which team this pokemon belongs to. 1 for player 1, 2 for player 2

    public final Move[] moves; // the actual moves the pokemon will have.


    // the stat changes
    // the default multiplier is one, and we want this to be the index of the array in PokemonInterface so 6's across the board for now
    private final int[] statCodes = {0, 0, 0, 0, 0, 0, 0};
    // the first 5 are attack, def, spattack, spdef, speed. 6 and 7 are accuracy and evasion, which for the purposes of alteration work the same as the other stats.


    // the nature multipliers
    // default is 1
    private final double[] natureCodes = {1, 1, 1, 1, 1};

    // struggle: a special move that can only be used when a pokemon is out of pp
    // as every pokemon can use it, and it never runs out of pp, we only need one struggle object
    public static final Attack struggle = new Attack("Struggle", Setup.getTypeFromName("Normal"), "Physical", 1, "50", "—");

    // initialize the stat change codes
    // we only want this to run once
    // so use a static initializer
    // this will allow us to keep track of stat changes
    static {
        double start1 = 4.0;
        double start2 = 9.0;
        for (int i = -6; i < 0; i++){
            statChanges.put(i, 1/start1);
            statChangesAccEvasion.put(i, 3/start2);
            start1 -= 0.5;
            start2 -= 1.0;
        }
        start1 = 1.0;
        start2 = 3.0;
        for (int i = 0; i <= 6; i++){
            statChanges.put(i, start1);
            statChangesAccEvasion.put(i, start2/3.0);
            start1 += 0.5;
            start2 += 1.0;
        }
    }


    // take constructor from super class.
    public Individual(String name, ArrayList<Type> types, int[] baseStats, String nature, Move[] moves, int[] evs, int teamNum) {

        super(name, types, baseStats);
        this.nature = nature;
        this.moves = moves;
        this.evs = evs;
        this.teamNum = teamNum;
        // except for stats because we can't calculate that until here

        // this part requires some explanation.
        // natures do not affect hp, only the other five stats.
        // the default multiplier would be 1.
        // a nature that increases a stat results in a multiplier of 1.1, a decrease results in .9.
        // a fifth of natures increase and decrease the same stat and thus do nothing.
        // natures are always given as a word that corresponds to the changes.
        // adamant increases attack, lowers special attack etc.

        // use the method natures while throwing in the array we just made and the nature to make the necessary changes to the array
        // the appropriate multiplier will now be used.
        natures(natureCodes, nature);

        this.stats = statCalcs();
        this.startingStats = this.stats.clone(); // keep it separate so we can change one without changing the other
        this.maxHP = stats[0]; // keep track of max hp so we can tell players what it is.

    }


    /*
     * This method gets the health percentage by dividing current health by max health, muliplying by 100 and then rounding.

     * */
    public int getPercentHealth(){
        return (int) Math.round((double) stats[0] / maxHP * 100);
    }


    /*
     * This method calculates this pokemon's stats using the level, ivs, evs, basestats and nature.

     * */
    public int[] statCalcs() {
        int[] stats = new int[6]; // array to hold the 6 stats
        int[] baseStats = getBaseStats(); // get the base stats here so we don't have to keep doing it in the loop

        for(int i = 0; i < 6; i++) {

            // common among all stats
            int stat = ((2 * baseStats[i] + ivs[i] + evs[i] / 4) * level) / 100;

            // hp is a bit different
            if (i == 0) {
                stats[0] = stat + level + 10;
            }
            else{
                stats[i] = (int) ((stat + 5) * natureCodes[i - 1]);
            }

        }

        return stats; // return it
    }

    /*
     * This method returns whether this pokemon has any moves left. if the pp of all moves is 0, there are no moves left.
     * */
    public boolean hasPP(){
        // open a stream to check if any of the moves' pp is > 0
        return Arrays.stream(moves).anyMatch(x -> x.getPp() > 0);
    }

    // getters

    public int getTeamNum() {
        return teamNum;
    }

    public int[] getStartingStats() {
        return startingStats;
    }

    public int[] getStats() {
        return stats;
    }

    public int getMaxHP(){
        return maxHP;
    }



    public int[] getStatCodes() {
        return statCodes;
    }



    /*
     * This method recalculates stats afters stat changes have happened
     * */
    public void recalculate() { // just setting the statcodes does nothing

        // we also need to recalculate stats when this happens
        // start at 1 because hp never needs recalculation
        for (int i = 1; i < startingStats.length; i++){
            stats[i] = (int) (startingStats[i] * statChanges.get(statCodes[i - 1]));
        }

    }


    /*
     * This method displays the user's stats in a readable manner.

     * */
    private String displayStats(){
        String[] changes = new String[5]; // we want to show the stat changes if there are any
        for (int i = 0; i < 5; i++){ // 5 stats that can be changed
            int statChange = statCodes[i]; // create a variable so we don't keep accessing
            if (statChange != 0){ // if there is a change, do the appropriate formatting
                // if the stat change is more than 0, throw in a + sign
                // otherwise, it will be negative so no sign needed
                changes[i] = statChange > 0 ? "(+%d)".formatted(statChange) : "(%d)".formatted(statChange);
            }
            else{ // empty string if there is no change
                changes[i] = "";
            }
        }
        // return the string with some formatting
        return String.format("""

                Health: %d / %d
                Attack: %d %s
                Defense: %d %s
                Special Attack: %d %s
                Special Defense: %d %s
                Speed: %d %s""", stats[0], maxHP, stats[1], changes[0],
                stats[2], changes[1], stats[3], changes[2], stats[4], changes[3], stats[5], changes[4]);


    }

    /*
     * This method is the toString. When it is called, we want all this pokemon's information displayed in a readable manner.
     * */
    public String toString() {
        return super.toString() + "\nNature: " + nature + displayStats();
    }

    /*
     * This method displays this pokemon's name and health remaining.
     * */
    public void pokemonStatus() {
        System.out.println(); // skip a line
        System.out.printf("%s, Health remaining: %d%%\n", getName(), getPercentHealth());
    }



    // we're going to make a custom get moves method
    // the default one can't be understood due to containing all the type matchups
    // we can't just use moves.toString() cause there is more information we want to show which is in the getMoves method
    // we want this method to give extra information on the moves.
    public StringBuilder getMoves() {

        StringBuilder moveString = new StringBuilder();
        for (Move move : moves) {
            moveString.append(move.getMove()).append("\n");
        }
        return moveString; // return the string

    }


    /*
     * This method is run when a pokemon switches out. It resets stats and informs players.
     * */
    public void switchPokemonOut() {

        Arrays.fill(statCodes, 0); // reset stat changes
        recalculate(); // recalculate so stats go back to their base stats

        System.out.println(getName() + " is switched out."); // let the players know what's happening
    }

    /*
     * This method is run when a pokemon switches in. It informs players.
     * */
    public void switchPokemonIn() { // this needs to run when the pokemon is sent in.
        System.out.println("Player " + teamNum + " sends out " + getName() + '.'); // let the players know what's happening
    }

    /*
     * This method checks whether a pokemon is alive by checking if its hp is more than 0.
     * */
    public boolean isAlive() {
        return stats[0] > 0;

    }
    /*
     * This method sets the natureCodes for a pokemon.
     * Parameters:
     *  - natureCodes - the current nature codes array
     *  - nature - the nature this pokemon has
     * */
    public static void natures(double[] natureCodes, String nature) {
        // here's the method to change the multiplier based on the nature
        // we're comparing one string with 20 so switch statements are easier.

        // if it matches one of these cases, change the increased multiplier to 1.1 and the decreased to .9
        // we don't even need a default case because the default case is that there are no matches to these 20 strings
        // which would mean it is one of the 5 natures that don't change anything
        // natures that do nothing are never seen competitively, but they exist so have to take them into account
        // the default case is to do nothing

        // only attack, defense, special attack, special defense and speed can be affected by natures
        // the indices for this are the same as the ones in statchange
        // so make those public and use them so it is more clear what each nature does

        final double BOOST = 1.1; // when it increases a stat, it becomes 110 percent of what it was
        final double LOWER = 0.9; // when a nature lowers a stat it becomes 90 percent of what it was

        switch (nature) {
            case "Adamant" -> {
                natureCodes[StatChange.ATTACK] = BOOST;
                natureCodes[StatChange.SP_ATTACK] = LOWER;
            }
            case "Modest" -> {
                natureCodes[StatChange.SP_ATTACK] = BOOST;
                natureCodes[StatChange.ATTACK] = LOWER;
            }
            case "Jolly" -> {
                natureCodes[StatChange.SPEED] = BOOST;
                natureCodes[StatChange.SP_ATTACK] = LOWER;
            }
            case "Naive" -> {
                natureCodes[StatChange.SPEED] = BOOST;
                natureCodes[StatChange.SP_DEFENSE] = LOWER;
            }
            case "Lonely" -> {
                natureCodes[StatChange.ATTACK] = BOOST;
                natureCodes[StatChange.DEFENSE] = LOWER;
            }
            case "Timid" -> {
                natureCodes[StatChange.SPEED] = BOOST;
                natureCodes[StatChange.ATTACK] = LOWER;
            }
            case "Hasty" -> {
                natureCodes[StatChange.SPEED] = BOOST;
                natureCodes[StatChange.DEFENSE] = LOWER;
            }
            case "Naughty" -> {
                natureCodes[StatChange.ATTACK] = BOOST;
                natureCodes[StatChange.SP_DEFENSE] = LOWER;
            }
            case "Brave" -> {
                natureCodes[StatChange.ATTACK] = BOOST;
                natureCodes[StatChange.SPEED] = LOWER;
            }
            case "Bold" -> {
                natureCodes[StatChange.DEFENSE] = BOOST;
                natureCodes[StatChange.ATTACK] = LOWER;
            }
            case "Impish" -> {
                natureCodes[StatChange.DEFENSE] = BOOST;
                natureCodes[StatChange.SP_ATTACK] = LOWER;
            }
            case "Lax" -> {
                natureCodes[StatChange.DEFENSE] = BOOST;
                natureCodes[StatChange.SP_DEFENSE] = LOWER;
            }
            case "Relaxed" -> {
                natureCodes[StatChange.DEFENSE] = BOOST;
                natureCodes[StatChange.SPEED] = LOWER;
            }
            case "Rash" -> {
                natureCodes[StatChange.SP_ATTACK] = BOOST;
                natureCodes[StatChange.SP_DEFENSE] = LOWER;
            }
            case "Calm" -> {
                natureCodes[StatChange.SP_DEFENSE] = BOOST;
                natureCodes[StatChange.ATTACK] = LOWER;
            }
            case "Gentle" -> {
                natureCodes[StatChange.SP_DEFENSE] = BOOST;
                natureCodes[StatChange.DEFENSE] = LOWER;
            }
            case "Sassy" -> {
                natureCodes[StatChange.SP_DEFENSE] = BOOST;
                natureCodes[StatChange.SPEED] = LOWER;
            }
            case "Careful" -> {
                natureCodes[StatChange.SP_DEFENSE] = BOOST;
                natureCodes[StatChange.SP_ATTACK] = LOWER;
            }
            case "Mild" -> {
                natureCodes[StatChange.SP_ATTACK] = BOOST;
                natureCodes[StatChange.DEFENSE] = LOWER;
            }
            case "Quiet" -> {
                natureCodes[StatChange.SP_ATTACK] = BOOST;
                natureCodes[StatChange.SPEED] = LOWER;
            }
        }
    }

}
