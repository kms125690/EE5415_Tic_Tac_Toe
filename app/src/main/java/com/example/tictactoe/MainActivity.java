package com.example.tictactoe;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TableLayout;
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
    private TableLayout mticTacToe;
    // Game Over
    Boolean mGameOver;
    RadioGroup radioGroup;
    Animation scaleUp, scaleDown;
    MediaPlayer mp;
    Switch mSoundSwitch;
    Boolean sound = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        mGame = new TicTacToeGame();
        startNewGame();

//        drawConnectedLine();
    }

    private void init() {
        mBoardButtons = new Button[TicTacToeGame.BOARD_SIZE];
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

        mticTacToe = (TableLayout) findViewById(R.id.table_tic_tac_toe);

        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        mUserScore = (TextView) findViewById(R.id.tv_user_score);
        mAndroidScore = (TextView) findViewById(R.id.tv_android_score);
        mTie = (TextView) findViewById(R.id.tv_tie);

        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        mp = MediaPlayer.create(this, R.raw.on_click);
    }

    //--- OnClickListener for Restart a New Game Button
    public void newGame(View v) {
        startNewGame();
    }

    public void regretMove(View v) { userRegretMove(); }

    private void setMove(char player, int location) {
        mGame.setMove(player, location);
        mBoardButtons[location].setEnabled(false);
        mBoardButtons[location].setText(String.valueOf(player));
        if (player == TicTacToeGame.HUMAN_PLAYER)
            mBoardButtons[location].setTextColor(Color.rgb(0, 200, 0));
        else if (player == TicTacToeGame.COMPUTER_PLAYER)
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
                    if (sound)
                        mp.start();

                    setMove(TicTacToeGame.HUMAN_PLAYER, location);

                    mBoardButtons[location].startAnimation(scaleUp);
                    mBoardButtons[location].startAnimation(scaleDown);

                    //--- If no winner yet, let the computer make a move
                    androidMove();

                    checkWinner();
//                    int winner = mGame.checkForWinner();
//                    if (winner == 0) {
//                        mInfoTextView.setTextColor(Color.rgb(0, 0, 0));
//                        mInfoTextView.setText(R.string.user_turn);
//                    } else if (winner == 1) {
//                        mInfoTextView.setTextColor(Color.rgb(0, 0, 200));
//                        addScore(mTie);
//                        mInfoTextView.setText(R.string.tie);
//                        mGameOver = true;
//                    } else if (winner == 2) {
//                        mInfoTextView.setTextColor(Color.rgb(0, 200, 0));
//                        addScore(mUserScore);
//                        mInfoTextView.setText(R.string.user_win);
//                        mGameOver = true;
//                    } else {
//                        mInfoTextView.setTextColor(Color.rgb(200, 0, 0));
//                        addScore(mAndroidScore);
//                        mInfoTextView.setText(R.string.android_win);
//                        mGameOver = true;
//                    }
                }
            }
        }
    }

    public void checkWinner() {
        int winner = mGame.checkForWinner();
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

        // who go first
        switch (mGame.getTurn()){
            case TicTacToeGame.HUMAN_PLAYER:
                mInfoTextView.setText(R.string.user_start);
                break;
            case TicTacToeGame.COMPUTER_PLAYER:
                androidMove();
                checkWinner();
                break;
        }
    }

    private void userRegretMove() {
        if (mGame != null && mGameOver == false) {
            int regretMoves[] = mGame.regretMove();
            if (regretMoves[0] == -1 || regretMoves[1] == -1)
                return;

            for (int i = 0; i < regretMoves.length; i++) {
                mBoardButtons[regretMoves[i]].setText("");
                mBoardButtons[regretMoves[i]].setEnabled(true);
                mBoardButtons[regretMoves[i]].setOnClickListener(new ButtonClickListener(regretMoves[i]));
            }

            mInfoTextView.setTextColor(Color.rgb(0, 0, 0));
            mInfoTextView.setText(R.string.user_turn);
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

    private void androidMove() {
        int winner = mGame.checkForWinner();
        if (winner == 0) {
            mInfoTextView.setText(R.string.android_turn);
            int move = -1;
            switch (mGame.getDifficulty()){
                case level_1:
                    move = mGame.getComputerMoveLevel1();
                    break;
                case level_2:
                    move = mGame.getComputerMoveLevel2();
                    break;
                case level_3:
                    move = mGame.getComputerMoveLevel3();
                    break;
            }
            setMove(TicTacToeGame.COMPUTER_PLAYER, move);
            mBoardButtons[move].startAnimation(scaleUp);
            mBoardButtons[move].startAnimation(scaleDown);
        }
    }

    private void drawConnectedLine(View view) {
        Point pt1 = getPointOfView(mBoardButtons[0]);
        Log.d("drawConnectedLine", "view point x,y (" + pt1.x + ", " + pt1.y + ")");

        Point pt2 = getPointOfView(mBoardButtons[2]);
        Log.d("drawConnectedLine", "view point x,y (" + pt2.x + ", " + pt2.y + ")");

        Bitmap bitmap = Bitmap.createBitmap(mticTacToe.getWidth(), mticTacToe.getHeight(), Bitmap.Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);

        Canvas canvas = new Canvas(bitmap);

        canvas.drawLine(0,0,100,100, paint);
    }

    private Point getPointOfView(View view) {
        int[] location = new int[2];
        view.getLocationInWindow(location);
        return new Point(location[0], location[1]);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        for (int i = 0; i < mBoardButtons.length; i++) {
            savedInstanceState.putString("button_" + i, mBoardButtons[i].getText().toString());
            Log.i("DEBUG", i + ": " + mBoardButtons[i].getText().toString());
        }

        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = (RadioButton) findViewById(selectedId);
        if (selectedRadioButton.getText().toString().equals(getResources().getString(R.string.user)))
            savedInstanceState.putString("start", getResources().getString(R.string.user));
        else if (selectedRadioButton.getText().toString().equals(getResources().getString(R.string.android)))
            savedInstanceState.putString("start", getResources().getString(R.string.android));

        savedInstanceState.putString("curr_message", mInfoTextView.getText().toString());
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
            mGame.setTurn(TicTacToeGame.HUMAN_PLAYER);
        else if (str.equals(getResources().getString(R.string.android)))
            mGame.setTurn(TicTacToeGame.COMPUTER_PLAYER);

        checkWinner();

        mUserScore.setText(savedInstanceState.getString("user_score"));
        mAndroidScore.setText(savedInstanceState.getString("android_score"));
        mTie.setText(savedInstanceState.getString("tie"));
    }

    public void onRadioButtonClicked(@NonNull View view) {
        switch(view.getId()) {
            case R.id.radio_human:
                Toast.makeText(this, "HUMAN_PLAYER", Toast.LENGTH_SHORT).show();
                if (mGame != null)
                    mGame.setTurn(TicTacToeGame.HUMAN_PLAYER);
                break;
            case R.id.radio_android:
                Toast.makeText(this, "COMPUTER_PLAYER", Toast.LENGTH_SHORT).show();
                if (mGame != null)
                    mGame.setTurn(TicTacToeGame.COMPUTER_PLAYER);
                break;
        }
        startNewGame();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        MenuItem item = menu.findItem(R.id.menu_sound_switch);
        item.setActionView(R.layout.switch_item);

        mSoundSwitch = item.getActionView().findViewById(R.id.switchForActionBar);
        mSoundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sound = isChecked;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        boolean checked = ((RadioButton) item).isChecked();
        switch (item.getItemId()) {
            case R.id.menu_level_1:
                item.setChecked(item.isChecked() ? false : true);
                Toast.makeText(this, getResources().getString(R.string.name_level_1), Toast.LENGTH_SHORT).show();
                if (mGame != null)
                    mGame.setDifficulty(level.level_1);
                return true;
            case R.id.menu_level_2:
                item.setChecked(item.isChecked() ? false : true);
                Toast.makeText(this, getResources().getString(R.string.name_level_2), Toast.LENGTH_SHORT).show();
                if (mGame != null)
                    mGame.setDifficulty(level.level_2);
                return true;
            case R.id.menu_level_3:
                item.setChecked(item.isChecked() ? false : true);
                Toast.makeText(this, getResources().getString(R.string.name_level_3), Toast.LENGTH_SHORT).show();
                if (mGame != null)
                    mGame.setDifficulty(level.level_3);
                return true;
            case R.id.menu_exit:
                finish();
                return true;
        }
        return false;
    }
}