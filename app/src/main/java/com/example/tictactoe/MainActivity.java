package com.example.tictactoe;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    // Represents the internal state of the game
    private TicTacToeGame mGame;

    // Buttons making up the board
    private Button mBoardButtons[];
    // Various text displayed
    private TextView mInfoTextView;
    // Restart Button
    private Button startButton;
    private TextView mUserScore, mAndroidScore, mTie;
    private char turn = TicTacToeGame.HUMAN_PLAYER;
    // Game Over
    Boolean mGameOver;
    RadioGroup radioGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGame = new TicTacToeGame();
        init();
        mGame.setTurn(turn);

        startNewGame();
    }

    private void init() {
        mBoardButtons = new Button[mGame.BOARD_SIZE];
        mBoardButtons[0] = (Button) findViewById(R.id.button0);
        mBoardButtons[1] = (Button) findViewById(R.id.button1);
        mBoardButtons[2] = (Button) findViewById(R.id.button2);
        mBoardButtons[3] = (Button) findViewById(R.id.button3);
        mBoardButtons[4] = (Button) findViewById(R.id.button4);
        mBoardButtons[5] = (Button) findViewById(R.id.button5);
        mBoardButtons[6] = (Button) findViewById(R.id.button6);
        mBoardButtons[7] = (Button) findViewById(R.id.button7);
        mBoardButtons[8] = (Button) findViewById(R.id.button8);
        mInfoTextView = (TextView) findViewById(R.id.information);

        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        mUserScore = (TextView) findViewById(R.id.tv_user_score);
        mAndroidScore = (TextView) findViewById(R.id.tv_android_score);
        mTie = (TextView) findViewById(R.id.tv_tie);
    }

    //--- OnClickListener for Restart a New Game Button
    public void newGame(View v) {
        startNewGame();
    }

    private void setMove(char player, int location) {
        mGame.setMove(player, location);
        mBoardButtons[location].setEnabled(false);
        mBoardButtons[location].setText(String.valueOf(player));
        if (player == TicTacToeGame.HUMAN_PLAYER)
            mBoardButtons[location].setTextColor(Color.rgb(0, 200, 0));
        else
            mBoardButtons[location].setTextColor(Color.rgb(200, 0, 0));
    }

    //---Handles clicks on the game board buttons
    private class ButtonClickListener implements View.OnClickListener {
        int location;
        public ButtonClickListener(int location) {
            this.location = location;
        }
        @Override
        public void onClick(View v) {
            if (mGameOver == false) {
                if (mBoardButtons[location].isEnabled()) {
                    setMove(TicTacToeGame.HUMAN_PLAYER, location);
                    //--- If no winner yet, let the computer make a move
                    int winner = mGame.checkForWinner();
                    if (winner == 0) {
                        mInfoTextView.setText(R.string.android_turn);
                        int move = mGame.getComputerMove();
                        setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                        winner = mGame.checkForWinner();
                    }
                    if (winner == 0) {
                        mInfoTextView.setTextColor(Color.rgb(0, 0, 0));
                        mInfoTextView.setText(R.string.user_turn);
                    } else if (winner == 1) {
                        mInfoTextView.setTextColor(Color.rgb(0, 0, 200));
                        addScore(mTie);
                        mInfoTextView.setText(R.string.tie);
                        mGameOver = true;
                    } else if (winner == 2) {
                        mInfoTextView.setTextColor(Color.rgb(0, 200, 0));
                        addScore(mUserScore);
                        mInfoTextView.setText(R.string.user_win);
                        mGameOver = true;
                    } else {
                        mInfoTextView.setTextColor(Color.rgb(200, 0, 0));
                        addScore(mAndroidScore);
                        mInfoTextView.setText(R.string.android_win);
                        mGameOver = true;
                    }
                }
            }
        }
    }

    //--- Set up the game board.
    private void startNewGame() {
        mGameOver = false;
        mGame.clearBoard();
        //---Reset all buttons
        for (int i = 0; i < mBoardButtons.length; i++) {
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }

        //---Human goes first
        switch (mGame.getTurn()){
            case TicTacToeGame.HUMAN_PLAYER:
                mInfoTextView.setText(R.string.user_start);
                break;
            case TicTacToeGame.COMPUTER_PLAYER:
                int winner = mGame.checkForWinner();
                if (winner == 0) {
                    mInfoTextView.setText(R.string.android_turn);
                    int move = mGame.getComputerMove();
                    setMove(TicTacToeGame.COMPUTER_PLAYER, move);
                }
                break;
        }
    }

    private boolean addScore(@NonNull TextView tv) {
        if (tv.getText().toString() == null)
            return false;
        int score;
        try {
            score = Integer.parseInt(tv.getText().toString());
        } catch (NumberFormatException nfe) {
            return false;
        }
        tv.setText(String.valueOf(++score));
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        for (int i = 0; i < mBoardButtons.length; i++) {
            savedInstanceState.putString("button_" + i, mBoardButtons[i].getText().toString());
            Log.i("DEBUG", i + ": " + mBoardButtons[i].getText().toString());
        }

        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = (RadioButton) findViewById(selectedId);
        if (selectedRadioButton.getText().toString().equals(R.string.user))
            savedInstanceState.putString("start", getResources().getString(R.string.user));
        else if (selectedRadioButton.getText().toString().equals(R.string.android))
            savedInstanceState.putString("start", getResources().getString(R.string.android));

        savedInstanceState.putString("user_score", mUserScore.getText().toString());
        savedInstanceState.putString("android_score", mAndroidScore.getText().toString());
        savedInstanceState.putString("tie", mTie.getText().toString());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        for (int i = 0; i < mBoardButtons.length; i++) {
            String str_i = savedInstanceState.getString("button_" + i);
            if (!str_i.isEmpty() && str_i != null) {
                char char_i = str_i.charAt(0);
                if (char_i == TicTacToeGame.HUMAN_PLAYER || char_i == TicTacToeGame.COMPUTER_PLAYER)
                    setMove(char_i, i);
            }
        }

        String str = savedInstanceState.getString("start");
        if (str.equals(getResources().getString(R.string.user)))
            turn = TicTacToeGame.HUMAN_PLAYER;
        else if (str.equals(getResources().getString(R.string.android)))
            turn = TicTacToeGame.COMPUTER_PLAYER;

        mUserScore.setText(savedInstanceState.getString("user_score"));
        mAndroidScore.setText(savedInstanceState.getString("android_score"));
        mTie.setText(savedInstanceState.getString("tie"));
    }

    public void onRadioButtonClicked(@NonNull View view) {
        // Is the button now checked?
//        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
//            Log.i("DEBUG", "Radio = " + mGame.getTurn());
            case R.id.radio_human:
                Toast.makeText(this, "HUMAN_PLAYER", Toast.LENGTH_SHORT).show();
                mGame.setTurn(TicTacToeGame.HUMAN_PLAYER);
                break;
            case R.id.radio_android:
                Toast.makeText(this, "COMPUTER_PLAYER", Toast.LENGTH_SHORT).show();
                mGame.setTurn(TicTacToeGame.COMPUTER_PLAYER);
                break;
        }
        startNewGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_exit:
                finish();
                return true;
        }
        return false;
    }
}