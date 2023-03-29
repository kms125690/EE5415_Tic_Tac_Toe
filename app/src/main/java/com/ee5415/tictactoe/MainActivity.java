package com.ee5415.tictactoe;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ee5415.tictactoe.R;

import java.io.IOException;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    // Represents the internal state of the game
    private TicTacToeGame mGame;
    // Buttons making up the board
    private Button mBoardButtons[];
    // Various text displayed
    private TextView mInfoTextView;
    private TextView mUserScore, mAndroidScore, mTie;
    private LinearLayout mMainLayout;
    private ImageView imageView;
    // Game Over
    Boolean mGameOver;
    private RadioGroup radioGroup;
    private RadioButton radioHuman, radioAndroid;
    private MenuItem mMenuItemLevel1, mMenuItemLevel2, mMenuItemLevel3;
    private Animation scaleUp, scaleDown;
    private AnimationDrawable animationDrawable;
    private MediaPlayer clickMusic;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch mSoundSwitch;
    Boolean sound = true;
    boolean mBounded;
    private BackgroundSoundService mServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        mGame = new TicTacToeGame();

        loadPreferences();
    }

    @Override
    protected void onStart() {
        super.onStart();

        startNewGame();

        playBackgroundSound();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            backgroundMusicAction();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBounded) {
            unbindService(mConnection);
            mBounded = false;
        }

        savePreferences();
    }

    public void playBackgroundSound() {
        Intent mIntent = new Intent(MainActivity.this, BackgroundSoundService.class);
        bindService(mIntent, mConnection, BIND_AUTO_CREATE);
    }

    public void backgroundMusicAction() throws InterruptedException {
        if (mServer == null) {
            Thread callService = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (mServer == null) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if (mBounded) {
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
                    }
                }
            });
            callService.start();
        } else {
            if (mBounded) {
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
        }
    }

    public ServiceConnection mConnection = new ServiceConnection() {
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
        imageView = (ImageView) findViewById(R.id.imageView);

        mMainLayout.setBackgroundResource(R.drawable.gradient_list);
        animationDrawable = (AnimationDrawable) mMainLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
        radioHuman = (RadioButton) findViewById(R.id.radio_human);
        radioAndroid = (RadioButton) findViewById(R.id.radio_android);

        mMenuItemLevel1 = (MenuItem) findViewById(R.id.menu_level_1);
        mMenuItemLevel2 = (MenuItem) findViewById(R.id.menu_level_2);
        mMenuItemLevel3 = (MenuItem) findViewById(R.id.menu_level_3);

        mUserScore = (TextView) findViewById(R.id.tv_user_score);
        mAndroidScore = (TextView) findViewById(R.id.tv_android_score);
        mTie = (TextView) findViewById(R.id.tv_tie);

        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);

        clickMusic = MediaPlayer.create(this, R.raw.on_click);
        mSoundSwitch = (Switch) findViewById(R.id.switchForActionBar);
    }

    //--- OnClickListener for Restart a New Game Button
    public void newGame(View v) {
        startNewGame();
    }

    public void regretMove(View v) {
        userRegretMove();
    }

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
            if (!mGameOver) {
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
            drawVictoryLine(winStruct.startBox, winStruct.endBox);
        } else {
            mInfoTextView.setTextColor(Color.rgb(200, 0, 0));
            addScore(mAndroidScore);
            mInfoTextView.setText(R.string.android_win);
            mGameOver = true;
            drawVictoryLine(winStruct.startBox, winStruct.endBox);
        }
    }

    //--- Set up the game board.
    private void startNewGame() {
        mGameOver = false;
        mGame.clearBoard();
        clearVictoryLine();
        //---Reset all buttons
        for (int i = 0; i < mBoardButtons.length; i++) {
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }

        // who go first
        switch (mGame.getTurn()) {
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
        if (mGame != null && !mGameOver) {
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
            switch (mGame.getDifficulty()) {
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

    private void drawVictoryLine(int startBox, int endBox) {
        final int width = 300;
        final int height = 300;
        final int width_box = width / 3;
        final int height_box = height / 3;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT);
        Paint paint = new Paint();
        paint.setColor(Color.MAGENTA);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setAntiAlias(true);

        if (startBox == 0 && endBox == 2)
            canvas.drawLine(width_box / 2, height_box / 2, 2 * width_box + width_box / 2, height_box / 2, paint);
        if (startBox == 3 && endBox == 5)
            canvas.drawLine(width_box / 2, height_box + height_box / 2, 2 * width_box + width_box / 2, height_box + height_box / 2, paint);
        if (startBox == 6 && endBox == 8)
            canvas.drawLine(width_box / 2, 2 * height_box + height_box / 2, 2 * width_box + width_box / 2, 2 * height_box + height_box / 2, paint);

        if (startBox == 0 && endBox == 6)
            canvas.drawLine(width_box / 2, height_box / 2, width_box / 2, 2 * height_box + height_box / 2, paint);
        if (startBox == 1 && endBox == 7)
            canvas.drawLine(width_box + width_box / 2, height_box / 2, width_box + width_box / 2, 2 * height_box + height_box / 2, paint);
        if (startBox == 2 && endBox == 8)
            canvas.drawLine(2 * width_box + width_box / 2, height_box / 2, 2 * width_box + width_box / 2, 2 * height_box + height_box / 2, paint);

        if (startBox == 0 && endBox == 8)
            canvas.drawLine(width_box / 2, height_box / 2, 2 * width_box + width_box / 2, 2 * height_box + height_box / 2, paint);
        if (startBox == 2 && endBox == 6)
            canvas.drawLine(2 * width_box + width_box / 2, height_box / 2, width_box / 2, 2 * height_box + height_box / 2, paint);

        imageView.setImageBitmap(bitmap);
    }

    private void clearVictoryLine() {
        final int width = 300;
        final int height = 300;
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
        else
            savedInstanceState.putString("start", getResources().getString(R.string.android));

        savedInstanceState.putBoolean("sound", mSoundSwitch.isChecked());

        savedInstanceState.putString("curr_message", mInfoTextView.getText().toString());
        savedInstanceState.putString("user_score", mUserScore.getText().toString());
        savedInstanceState.putString("android_score", mAndroidScore.getText().toString());
        savedInstanceState.putString("tie", mTie.getText().toString());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mGame.clearBoard();
        for (int i = 0; i < mBoardButtons.length; i++) {
            mBoardButtons[i].setText("");
            mBoardButtons[i].setEnabled(true);
            mBoardButtons[i].setOnClickListener(new ButtonClickListener(i));
        }

        for (int i = 0; i < mBoardButtons.length; i++) {
            String str_i = savedInstanceState.getString("button_" + i);
            if (!str_i.isEmpty()) {
                char char_i = str_i.charAt(0);
                if (char_i == TicTacToeGame.HUMAN_PLAYER || char_i == TicTacToeGame.COMPUTER_PLAYER)
                    setMove(char_i, i);
            }
        }

        String str = savedInstanceState.getString("start");

        if (str.equals(getResources().getString(R.string.user)))
            mGame.setTurn(TicTacToeGame.HUMAN_PLAYER);
        else
            mGame.setTurn(TicTacToeGame.COMPUTER_PLAYER);

        checkWinner();

        sound = savedInstanceState.getBoolean("sound");

        mUserScore.setText(savedInstanceState.getString("user_score"));
        mAndroidScore.setText(savedInstanceState.getString("android_score"));
        mTie.setText(savedInstanceState.getString("tie"));
    }

    public void onRadioButtonClicked(@NonNull View view) {
        switch (view.getId()) {
            case R.id.radio_human:
                Toast.makeText(this, "HUMAN_PLAYER", Toast.LENGTH_SHORT).show();
                if (mGame != null) {
                    mGame.setTurn(TicTacToeGame.HUMAN_PLAYER);
                }
                break;
            case R.id.radio_android:
                Toast.makeText(this, "COMPUTER_PLAYER", Toast.LENGTH_SHORT).show();
                if (mGame != null) {
                    mGame.setTurn(TicTacToeGame.COMPUTER_PLAYER);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_options, menu);
        MenuItem item = menu.findItem(R.id.menu_sound_switch);
        item.setActionView(R.layout.switch_item);

        mSoundSwitch = item.getActionView().findViewById(R.id.switchForActionBar);
        mSoundSwitch.setChecked(sound);
        mSoundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sound = isChecked;
                try {
                    backgroundMusicAction();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        switch (mGame.getDifficulty()) {
            case level_1:
                menu.findItem(R.id.menu_level_1).setChecked(true);
                break;
            case level_2:
                menu.findItem(R.id.menu_level_2).setChecked(true);
                break;
            case level_3:
                menu.findItem(R.id.menu_level_3).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        boolean checked = ((RadioButton) item).isChecked();
        switch (item.getItemId()) {
            case R.id.menu_level_1:
                item.setChecked(!item.isChecked());
                Toast.makeText(this, getResources().getString(R.string.name_level_1), Toast.LENGTH_SHORT).show();
                if (mGame != null)
                    mGame.setDifficulty(Level.level_1);
                return true;
            case R.id.menu_level_2:
                item.setChecked(!item.isChecked());
                Toast.makeText(this, getResources().getString(R.string.name_level_2), Toast.LENGTH_SHORT).show();
                if (mGame != null)
                    mGame.setDifficulty(Level.level_2);
                return true;
            case R.id.menu_level_3:
                item.setChecked(!item.isChecked());
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

    public void savePreferences() {
        SharedPreferences pref = getSharedPreferences("TicTacToe", MODE_PRIVATE);
        pref.edit().putBoolean("sound", sound).commit();

        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = (RadioButton) findViewById(selectedId);
        String str = selectedRadioButton.getText().toString();
        if (str.equals(getResources().getString(R.string.user)))
            pref.edit().putString("start", getResources().getString(R.string.user)).commit();
        else
            pref.edit().putString("start", getResources().getString(R.string.android)).commit();

        switch (mGame.getDifficulty()) {
            case level_1:
                pref.edit().putInt("level", 1).commit();
                break;
            case level_2:
                pref.edit().putInt("level", 2).commit();
                break;
            case level_3:
                pref.edit().putInt("level", 3).commit();
                break;
        }
    }

    public void loadPreferences() {
        SharedPreferences pref = getSharedPreferences("TicTacToe", MODE_PRIVATE);
        sound = pref.getBoolean("sound", false);

        String str = pref.getString("start", "");
        if (str.equals(getResources().getString(R.string.user))) {
            radioHuman.setChecked(true);
            mGame.setTurn(TicTacToeGame.HUMAN_PLAYER);
        } else {
            radioAndroid.setChecked(true);
            mGame.setTurn(TicTacToeGame.COMPUTER_PLAYER);
        }

        int level = pref.getInt("level", 1);
        switch (level) {
            case 1:
                mGame.setDifficulty(Level.level_1);
                break;
            case 2:
                mGame.setDifficulty(Level.level_2);
                break;
            case 3:
                mGame.setDifficulty(Level.level_3);
                break;
        }

    }

}