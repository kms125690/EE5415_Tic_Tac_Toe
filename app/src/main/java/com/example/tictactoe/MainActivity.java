package com.example.tictactoe;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;


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
    private LinearLayout mMainLayout;
//    private TableLayout mticTacToe;
    private ImageView imageView;
    // Game Over
    Boolean mGameOver;
    RadioGroup radioGroup;
    Animation scaleUp, scaleDown;
    AnimationDrawable animationDrawable;
    MediaPlayer clickMusic;
    Switch mSoundSwitch;
    Boolean sound = true;
    boolean mBounded;
    BackgroundSoundService mServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        mGame = new TicTacToeGame();
        startNewGame();

        PlayBackgroundSound();
//        drawConnectedLine();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }
    };

    public void PlayBackgroundSound() {
        Intent mIntent = new Intent(MainActivity.this, BackgroundSoundService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBounded = false;
            mServer = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBounded = true;
            BackgroundSoundService.LocalBinder mLocalBinder = (BackgroundSoundService.LocalBinder) service;
            mServer = mLocalBinder.getServerInstance();
        }
    };

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

        mMainLayout = (LinearLayout) findViewById(R.id.main_layout);
//        mticTacToe = (TableLayout) findViewById(R.id.table_tic_tac_toe);
        imageView = (ImageView) findViewById(R.id.imageView);

        mMainLayout.setBackgroundResource(R.drawable.gradient_list);
        animationDrawable = (AnimationDrawable) mMainLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        radioGroup = (RadioGroup) findViewById(R.id.radio_group);

        mUserScore = (TextView) findViewById(R.id.tv_user_score);
        mAndroidScore = (TextView) findViewById(R.id.tv_android_score);
        mTie = (TextView) findViewById(R.id.tv_tie);

        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        clickMusic = MediaPlayer.create(this, R.raw.on_click);


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
                        clickMusic.start();

                    setMove(TicTacToeGame.HUMAN_PLAYER, location);

                    mBoardButtons[location].startAnimation(scaleUp);
                    mBoardButtons[location].startAnimation(scaleDown);

                    //--- If no winner yet, let the computer make a move
                    androidMove();
                    checkWinner();
                }
            }
        }
    }

    public void checkWinner() {
//        int winner = mGame.checkForWinner();
        WinStruct winStruct = mGame.checkForWinnerStruct();
        if (winStruct.winner == 0) {
            mInfoTextView.setTextColor(Color.rgb(0, 0, 0));
            mInfoTextView.setText(R.string.user_turn);
        } else if (winStruct.winner == 1) {
            mInfoTextView.setTextColor(Color.rgb(0, 0, 200));
            addScore(mTie);
            mInfoTextView.setText(R.string.tie);
            mGameOver = true;
        } else if (winStruct.winner == 2) {
            mInfoTextView.setTextColor(Color.rgb(0, 200, 0));
            addScore(mUserScore);
            mInfoTextView.setText(R.string.user_win);
            mGameOver = true;
            drawConnectedLine(winStruct.startBox, winStruct.endBox);
        } else {
            mInfoTextView.setTextColor(Color.rgb(200, 0, 0));
            addScore(mAndroidScore);
            mInfoTextView.setText(R.string.android_win);
            mGameOver = true;
            drawConnectedLine(winStruct.startBox, winStruct.endBox);
        }
    }

    //--- Set up the game board.
    private void startNewGame() {
        mGameOver = false;
        mGame.clearBoard();
        clearConnectedLine();
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
        WinStruct winStruct = mGame.checkForWinnerStruct();
        if (winStruct.winner == 0) {
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

    private void drawConnectedLine(int startBox, int endBox) {
        // FIXME: dimension
        final int width = 512;
        final int height = 512;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint();
        paint.setColor(Color.MAGENTA);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(40);
        paint.setAntiAlias(true);

        if (startBox == 0 && endBox == 2)
            canvas.drawLine(0, 0, width, 0, paint);
        if (startBox == 3 && endBox == 5)
            canvas.drawLine(0, height/2, width, height/2, paint);
        if (startBox == 6 && endBox == 8)
            canvas.drawLine(0, height, width, height, paint);

        if (startBox == 0 && endBox == 6)
            canvas.drawLine(0, 0, 0, height, paint);
        if (startBox == 1 && endBox == 7)
            canvas.drawLine(width/2, height/2, width/2, height/2, paint);
        if (startBox == 2 && endBox == 8)
            canvas.drawLine(width, 0, width, height, paint);

        if (startBox == 0 && endBox == 8)
            canvas.drawLine(0, 0, width, height, paint);
        if (startBox == 2 && endBox == 6)
            canvas.drawLine(width, 0, 0, height, paint);

        imageView.setImageBitmap(bitmap);
    }

    private void clearConnectedLine() {
        final int width = 512;
        final int height = 512;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint();
        paint.setColor(Color.TRANSPARENT);
        paint.setStrokeWidth(0);
        paint.setAntiAlias(true);
        canvas.drawLine(0, 0, 0, 0, paint);
        imageView.setImageBitmap(bitmap);
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
                if (sound) {
                    try {
                        mServer.startMusic();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    mServer.stopMusic();
                }
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
                    mGame.setDifficulty(Level.level_1);
                return true;
            case R.id.menu_level_2:
                item.setChecked(item.isChecked() ? false : true);
                Toast.makeText(this, getResources().getString(R.string.name_level_2), Toast.LENGTH_SHORT).show();
                if (mGame != null)
                    mGame.setDifficulty(Level.level_2);
                return true;
            case R.id.menu_level_3:
                item.setChecked(item.isChecked() ? false : true);
                Toast.makeText(this, getResources().getString(R.string.name_level_3), Toast.LENGTH_SHORT).show();
                if (mGame != null)
                    mGame.setDifficulty(Level.level_3);
                return true;
            case R.id.menu_exit:
                finish();
                return true;
        }
        return false;
    }
}