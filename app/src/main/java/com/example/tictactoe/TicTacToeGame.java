package com.example.tictactoe;

import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class TicTacToeGame {
    // Characters used to represent the human, computer, and open spots
    public static final char HUMAN_PLAYER = 'X';
    public static final char COMPUTER_PLAYER = 'O';
    public static final char OPEN_SPOT = ' ';
    public static final int BOARD_SIZE = 9;
    private char turn = HUMAN_PLAYER;
    private char mBoard[] = { '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    private Random mRand;

    public TicTacToeGame() {
        // Seed the random number generator
        mRand = new Random();

        int win = 0; // Set to 1, 2, or 3 when game is over
    }

    /** Clear the board of all X's and O's. */
    public void clearBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            mBoard[i] = OPEN_SPOT;
        }
    }

    /** Set the given player at the given location on the game board * */
    public void setMove(char player, int location) {
        mBoard[location] = player;
    }

    // Check for a winner.  Return
    //  0 if no winner or tie yet
    //  1 if it's a tie
    //  2 if X won
    //  3 if O won
    public int checkForWinner() {
        // Check horizontal wins
        for (int i = 0; i <= 6; i += 3)	{
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i+1] == HUMAN_PLAYER &&
                    mBoard[i+2]== HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i+1]== COMPUTER_PLAYER &&
                    mBoard[i+2] == COMPUTER_PLAYER)
                return 3;
        }

        // Check vertical wins
        for (int i = 0; i <= 2; i++) {
            if (mBoard[i] == HUMAN_PLAYER &&
                    mBoard[i+3] == HUMAN_PLAYER &&
                    mBoard[i+6]== HUMAN_PLAYER)
                return 2;
            if (mBoard[i] == COMPUTER_PLAYER &&
                    mBoard[i+3] == COMPUTER_PLAYER &&
                    mBoard[i+6]== COMPUTER_PLAYER)
                return 3;
        }

        // Check for diagonal wins
        if ((mBoard[0] == HUMAN_PLAYER &&
                mBoard[4] == HUMAN_PLAYER &&
                mBoard[8] == HUMAN_PLAYER) ||
                (mBoard[2] == HUMAN_PLAYER &&
                        mBoard[4] == HUMAN_PLAYER &&
                        mBoard[6] == HUMAN_PLAYER))
            return 2;
        if ((mBoard[0] == COMPUTER_PLAYER &&
                mBoard[4] == COMPUTER_PLAYER &&
                mBoard[8] == COMPUTER_PLAYER) ||
                (mBoard[2] == COMPUTER_PLAYER &&
                        mBoard[4] == COMPUTER_PLAYER &&
                        mBoard[6] == COMPUTER_PLAYER))
            return 3;

        // Check for tie
        for (int i = 0; i < BOARD_SIZE; i++) {
            // If we find a number, then no one has won yet
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER)
                return 0;
        }

        // If we make it through the previous loop, all places are taken, so it's a tie
        return 1;
    }

    public void getUserMove()
    {
        // Eclipse throws a NullPointerException with Console.readLine
        // Known bug: https://bugs.eclipse.org/bugs/show_bug.cgi?id=122429
        //Console console = System.console();

        Scanner s = new Scanner(System.in);

        int move = -1;

        while (move == -1) {
            try {
                System.out.print("Enter your move: ");
                move = s.nextInt();

                while (move < 1 || move > BOARD_SIZE ||
                        mBoard[move-1] == HUMAN_PLAYER || mBoard[move-1] == COMPUTER_PLAYER) {

                    if (move < 1 || move > BOARD_SIZE)
                        System.out.println("Please enter a move between 1 and " + BOARD_SIZE + ".");
                    else
                        System.out.println("That space is occupied.  Please choose another space.");

                    System.out.print("Enter your move: ");
                    move = s.nextInt();
                }
            }
            catch (InputMismatchException ex) {
                System.out.println("Please enter a number between 1 and " + BOARD_SIZE + ".");
                s.next();  // Get next line so we start fresh
                move = -1;
            }
        }

        mBoard[move-1] = HUMAN_PLAYER;
    }

    public int getComputerMove()
    {
        int move;
        // First see if there's a move O can make to win
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
                char curr = mBoard[i];
                mBoard[i] = COMPUTER_PLAYER;
                if (checkForWinner() == 3) {
                    System.out.println("Computer is moving to " + (i + 1));
                    return i;
                }
                else
                    mBoard[i] = curr;
            }
        }

        // See if there's a move O can make to block X from winning
        for (int i = 0; i < BOARD_SIZE; i++) {
            if (mBoard[i] != HUMAN_PLAYER && mBoard[i] != COMPUTER_PLAYER) {
                char curr = mBoard[i]; // Save the current number
                mBoard[i] = HUMAN_PLAYER;
                if (checkForWinner() == 2) {
                    mBoard[i] = COMPUTER_PLAYER;
                    System.out.println("Computer is moving to " + (i + 1));
                    return i;
                }
                else
                    mBoard[i] = curr;
            }
        }
        // Generate random move
        do
        {
            move = mRand.nextInt(BOARD_SIZE);
        } while (mBoard[move] == HUMAN_PLAYER || mBoard[move] == COMPUTER_PLAYER);
        mBoard[move] = COMPUTER_PLAYER;
        return move;
    }

    // This function returns true if there are moves remaining on the board. It returns false if there are no moves left to play.
    static Boolean isMovesLeft(char board[])
    {
        for (int i = 0; i < BOARD_SIZE; i++)
            if (board[i] == OPEN_SPOT)
                return true;
        return false;
    }

    static int evaluate(char b[])
    {
        // Checking for Rows for X or O victory.
        for (int i = 0; i < 3; i++)
        {
            if (b[i] == b[i + 3] && b[i + 3] == b[i + 6])
            {
                if (b[i] == HUMAN_PLAYER)
                    return +10;
                else if (b[i] == COMPUTER_PLAYER)
                    return -10;
            }
        }

        // Checking for Columns for X or O victory.
        for (int i = 0; i <= 6; i += 3)
        {
            if (b[i] == b[i + 1] && b[i + 1] == b[i + 2])
            {
                if (b[i] == HUMAN_PLAYER)
                    return +10;
                else if (b[i] == COMPUTER_PLAYER)
                    return -10;
            }
        }

        // Checking for Diagonals for X or O victory.
        if (b[0] == b[4] && b[4] == b[8])
        {
            if (b[0] == HUMAN_PLAYER)
                return +10;
            else if (b[0] == COMPUTER_PLAYER)
                return -10;
        }

        if (b[2] == b[4] && b[4] == b[6])
        {
            if (b[2] == HUMAN_PLAYER)
                return +10;
            else if (b[2] == COMPUTER_PLAYER)
                return -10;
        }

        // Else if none of them have won then return 0
        return 0;
    }

    // This is the minimax function. It considers all the possible ways the game can go and returns the value of the board
    static int minimax(char board[], int depth, Boolean isMax)
    {
        int score = evaluate(board);

        // If Maximizer has won the game return his/her evaluated score
        if (score == 10)
            return score;

        // If Minimizer has won the game return his/her evaluated score
        if (score == -10)
            return score;

        // If there are no more moves and no winner then it is a tie
        if (isMovesLeft(board) == false)
            return 0;

        // If this maximizer's move
        if (isMax)
        {
            int best = -1000;

            // Traverse all cells
            for (int i = 0; i < BOARD_SIZE; i++)
            {
                // Check if cell is empty
                if (board[i] == OPEN_SPOT)
                {
                    // Make the move
                    board[i] = HUMAN_PLAYER;
                    // Call minimax recursively and choose the maximum value
                    best = Math.max(best, minimax(board, depth + 1, !isMax));
                    // Undo the move
                    board[i] = OPEN_SPOT;
                }
            }
            return best;
        }

        // If this minimizer's move
        else
        {
            int best = 1000;
            // Traverse all cells
            for (int i = 0; i < BOARD_SIZE; i++)
            {
                // Check if cell is empty
                if (board[i] == OPEN_SPOT)
                {
                    // Make the move
                    board[i] = COMPUTER_PLAYER;
                    // Call minimax recursively and choose the minimum value
                    best = Math.min(best, minimax(board, depth + 1, !isMax));
                    // Undo the move
                    board[i] = OPEN_SPOT;
                }
            }
            return best;
        }
    }

    // This will return the best possible move for the player
    static int findBestMove(char board[])
    {
        int bestVal = -1000;
        int bestMove = -1;

        // Traverse all cells, evaluate minimax function for all empty cells. And return the cell with optimal value.
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            // Check if cell is empty
            if (board[i] == OPEN_SPOT)
            {
                // Make the move
                board[i] = HUMAN_PLAYER;
                // compute evaluation function for this move.
                int moveVal = minimax(board, 0, false);
                // Undo the move
                board[i] = OPEN_SPOT;
                // If the value of the current move is more than the best value, then update best
                if (moveVal > bestVal)
                {
                    bestMove = i;
                    bestVal = moveVal;
                }
            }

        }
        System.out.printf("The value of the best Move " +  "is : %d\n\n", bestVal);
        return bestMove;
    }

    public char getTurn() {
        return turn;
    }

    public void setTurn(char turn) {
        this.turn = turn;
    }
}
