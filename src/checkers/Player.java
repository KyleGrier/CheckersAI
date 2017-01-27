package checkers;
import java.util.*;


public class Player {
    /**
     * Performs a move
     *
     * @param pState
     *            the current state of the board
     * @param pDue
     *            time before which we must have returned
     * @return the next state the board is in after our move
     */
    public GameState play(final GameState pState, final Deadline pDue) {

        Vector<GameState> lNextStates = new Vector<GameState>();
        pState.findPossibleMoves(lNextStates);

        if (lNextStates.size() == 0) {
            // Must play "pass" move if there are no other moves possible.
            return new GameState(pState, new Move());
        }

        /**
         * Here you should write your algorithms to get the best next move, i.e.
         * the best next state. This skeleton returns a random move instead.
         */
        /*Random random = new Random();
        return lNextStates.elementAt(random.nextInt(lNextStates.size()));
        */
        
        int index = alphabeta(pState, 9, 9, pState.getNextPlayer(), Integer.MIN_VALUE, Integer.MAX_VALUE);
        return lNextStates.elementAt(index);
    }
    
    public int alphabeta(GameState nextState, int origd, int depth, int origp, int alpha, int beta){
        if(nextState.isEOG() || depth == 0){
            int heurnum = heuristic(nextState, origp);
            return heurnum;
        }
        Vector<GameState> newStates = new Vector<GameState>();
        nextState.findPossibleMoves(newStates);
        int bestpossible;
        if(nextState.getNextPlayer() == origp){
            bestpossible = Integer.MIN_VALUE;
            int besti = 0;
            for(int i = 0; i < newStates.size(); i++){
                GameState cState = newStates.get(i);
                int challenger = alphabeta(cState, origd, (depth-1), origp, alpha, beta);
                if(depth == origd){
                    Move checkmove = cState.getMove();
                    
                }
                if(challenger > bestpossible){
                    bestpossible = challenger;
                }
                if(bestpossible  > alpha){
                    alpha = bestpossible;
                    besti = i;
                }
                if(beta <= alpha){
                    break;
                }
            }
            if(depth == origd){
                return besti;
            }
            return bestpossible;
        }else{
            bestpossible = Integer.MAX_VALUE;
            for(int i = 0; i < newStates.size(); i++){
                GameState cState = newStates.get(i);
                int challenger = alphabeta(cState, origd, (depth-1), origp, alpha, beta);
                if(challenger < bestpossible){
                    bestpossible = challenger;
                }
                if(bestpossible < beta){
                    beta = bestpossible;
                }
                if(beta <= alpha){
                    break;
                }
            }
            return bestpossible;
        }
    }
    
    
    private static int heuristic(GameState pState, int player) {
        int red_pieces = 0;
        int white_pieces = 0;
        int red_kings = 0;
        int white_kings = 0;
        int score = 0;

        // Count pieces
        for (int i = 0; i < pState.NUMBER_OF_SQUARES; i++) {
            if (0 != (pState.get(i) & Constants.CELL_RED)) {
                ++red_pieces;

                if (0 != (pState.get(i) & Constants.CELL_KING)){
                    ++red_kings;
                }

            } else if (0 != (pState.get(i) & Constants.CELL_WHITE)) {
                ++white_pieces;

                if (0 != (pState.get(i) & Constants.CELL_KING)) {
                    ++white_kings;
                }
            }
        }

        // Apply scorings

        score = piecesLeader(score, player, red_pieces, white_pieces);
        score = kingsLeader(score, player, red_kings, white_kings);
        score = kingsPenalty(score, player, red_kings, white_kings);
        score = endGame(score, player, pState);
        score = offense(score, player, pState);
        score = defenseOnSides(score, player, pState);
        score = defense(score, player, pState);
        
        return score;

    }

    /*
          heuristics
     */
    private static int piecesLeader(int score, int player, int red_pieces, int white_pieces){

        //if the player is red
        if (player == Constants.CELL_RED) {
            score += (red_pieces - white_pieces);
        }
        else {
            score += (white_pieces - red_pieces);
        }
        return score;
    }

    private static int kingsPenalty(int score, int player, int red_kings, int white_kings){

        //if the player is red
        if (player == Constants.CELL_RED) {
                //penalty if opponent gets a king
                score -= white_kings * 100;
        }
        else {
                score -= red_kings * 100;
        }
        return score;
    }

    private static int endGame(int score, int player, GameState pState){

        if (((pState.isRedWin()) && (player == Constants.CELL_RED)) || ((pState.isWhiteWin()) && (player == Constants.CELL_WHITE))){
            score += 2000;
        }
        else if (((pState.isWhiteWin()) && (player == Constants.CELL_RED) )|| ((pState.isRedWin() && player == Constants.CELL_WHITE)) ){
            score -= 2000;
        }
        else if (pState.isDraw()) {
            score -= 1000;
        }
        return score;
    }

    private static int kingsLeader(int score, int player, int red_kings, int white_kings){

        //if the player is red
        if (player == Constants.CELL_RED) {
            score += (red_kings - white_kings) * 2;
        }
        else {
            score += (white_kings - red_kings) * 2;
        }
        return score;

    }

    private static int offense(int score, int player, GameState pState){

        int advanced_red_pieces = 0;
        int advanced_white_pieces = 0;

        // count advanced pieces
        for (int i = pState.NUMBER_OF_SQUARES -1; i > 19 ; i--) {
            if (0 != (pState.get(i) & Constants.CELL_RED)) {
                ++advanced_red_pieces;
            }
        }
        for (int i = 0; i < 12; i++) {
            if (0 != (pState.get(i) & Constants.CELL_WHITE)) {
                ++advanced_white_pieces;
            }
        }

        //if the player is red
        if (player == Constants.CELL_RED) {
            score += advanced_red_pieces * 2;
        }
        else {
            score += advanced_white_pieces * 2;
        }
        return score;
    }

    private static int defenseOnSides(int score, int player, GameState pState){

        int red_pieces_on_side = 0;
        int white_pieces_on_side = 0;

        // count pieces on sides
        int l = 0;
        int r = 3;
        for (int i = 0; i < 5; i++) {

            // left side
            if ( 0 != (pState.get(l) & Constants.CELL_RED) ) {
                ++red_pieces_on_side;

            }else if( 0 != (pState.get(l) & Constants.CELL_WHITE) ){
                ++white_pieces_on_side;
            }

            // right side
            if ( 0 != (pState.get(r) & Constants.CELL_RED) ) {
                ++red_pieces_on_side;

            }else if( 0 != (pState.get(r) & Constants.CELL_WHITE) ){
                ++white_pieces_on_side;
            }

            l += 8;
            if( l == 8){ l = 4; }
            // catch special cases from upper left und lower right corner...
            r += 8;
            if( r > 31){ r = 31; }
        }


        //if the player is red
        if (player == Constants.CELL_RED) {
            score += (red_pieces_on_side - white_pieces_on_side);
        }
        else {
            score += (white_pieces_on_side - red_pieces_on_side);
        }

        return score;
    }

    private static int defense(int score, int player, GameState pState){

        int unprotected_red_pieces = 0;
        int unprotected_white_pieces = 0;

        // fields in rows without edge fields
        int [] leftShiftedRows =  {5,6,7,13,14,15,21,22,23};  // calc: (field +/- fieldsInRow) - 1
        int [] rightShiftedRows = {8,9,10,16,17,18,24,25,26}; // calc: (field +/- fieldsInRow) + 1
        int fieldsInRow = 4;

        /*
            Count unprotected pieces
         */
        for( int i : leftShiftedRows){ // calc: (field +/- fieldsInRow) - 1

            if ( 0 != (pState.get(i) & Constants.CELL_RED) ) // calc with - 4 ( fieldsInRow ) if RED
            {
                if( !(0 != (pState.get(i - fieldsInRow) & Constants.CELL_RED)) || !(0 != (pState.get(i - fieldsInRow - 1 ) & Constants.CELL_RED)) ){
                    ++unprotected_red_pieces;
                }
            }
            if ( 0 != (pState.get(i) & Constants.CELL_WHITE) ) // calc with + 4 ( fieldsInRow ) if WHITE
            {
                if( !(0 != (pState.get(i + fieldsInRow) & Constants.CELL_WHITE)) || !(0 != (pState.get(i + fieldsInRow - 1 ) & Constants.CELL_WHITE)) ){
                    ++unprotected_white_pieces;
                }
            }
        }

        for( int i : rightShiftedRows){ // calc: (field +/- fieldsInRow) + 1

            if ( 0 != (pState.get(i) & Constants.CELL_RED) )
            {
                if( !(0 != (pState.get(i - fieldsInRow) & Constants.CELL_RED)) || !(0 != (pState.get(i - fieldsInRow  + 1 ) & Constants.CELL_RED)) ){
                    ++unprotected_red_pieces;
                }
            }
            if ( 0 != (pState.get(i) & Constants.CELL_WHITE) )
            {
                if( !(0 != (pState.get(i + fieldsInRow) & Constants.CELL_WHITE)) || !(0 != (pState.get(i + fieldsInRow  + 1 ) & Constants.CELL_WHITE)) ){
                    ++unprotected_white_pieces;
                }
            }
        }

//        System.err.println("unpro-RED: " + unprotected_red_pieces + "  unpro-WHITE: " + unprotected_white_pieces +" ");

        /*
            score
         */

        //if the player is red
        if (player == Constants.CELL_RED) {
            score += (unprotected_white_pieces - unprotected_red_pieces);
        }
        else {
            score += (unprotected_red_pieces - unprotected_white_pieces);
        }

        return score;


    }
}
