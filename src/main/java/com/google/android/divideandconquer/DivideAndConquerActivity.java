/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.divideandconquer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import java.util.Stack;

/**
 * The activity for the game.  Listens for callbacks from the game engine, and
 * response appropriately, such as bringing up a 'game over' dialog when a ball
 * hits a moving line and there is only one life left.
 */
public class DivideAndConquerActivity extends Activity
        implements DivideAndConquerView.BallEngineCallBack {

    private static final int NEW_GAME_NUM_BALLS = 1;
    private static final double LEVEL_UP_THRESHOLD = 0.8;
    private static final int COLLISION_VIBRATE_MILLIS = 50;

    private boolean mVibrateOn;
    
    private int mNumBalls = NEW_GAME_NUM_BALLS;
    
    private DivideAndConquerView mBallsView;

    private TextView mLivesLeft;
    private TextView mPercentContained;
    private int mNumLives;
    private Vibrator mVibrator;
    private TextView mLevelInfo;
    private int mNumLivesStart = 5;

    private Toast mCurrentToast;

    private GameState state = null;
    private boolean mNewGame = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Turn off the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);
        mBallsView = (DivideAndConquerView) findViewById(R.id.ballsView);
        mBallsView.setCallback(this);

        mPercentContained = (TextView) findViewById(R.id.percentContained);
        mLevelInfo = (TextView) findViewById(R.id.levelInfo);
        mLivesLeft = (TextView) findViewById(R.id.livesLeft);

        // we'll vibrate when the ball hits the moving line
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (savedInstanceState != null) {
            state = savedInstanceState.getParcelable("game_state");
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mNewGame) {
            // don't store state, we're going to get a new game anyway
            return;
        }

        GameState state = new GameState();
        mBallsView.saveState(state);
        state.numBalls = mNumBalls;
        state.numLives = mNumLives;
        state.numLivesStart = mNumLivesStart;
        outState.putParcelable("game_state", state);
    }

    /** {@inheritDoc} */
    public void onEngineReady(BallEngine ballEngine) {
        if (state == null) {
            mNumBalls = NEW_GAME_NUM_BALLS;
            mNumLives = mNumLivesStart;
            updatePercentDisplay(0);
            updateLivesDisplay(mNumLives);
            updateLevelDisplay(mNumBalls);
            mBallsView.getEngine().reset(SystemClock.elapsedRealtime(), mNumBalls);
            mBallsView.setMode(DivideAndConquerView.Mode.Bouncing);
        }
        else {
            mNumBalls = state.numBalls;
            mNumLives = state.numLives;
            mNumLivesStart = state.numLivesStart;
            updateLivesDisplay(mNumLives);
            updateLevelDisplay(mNumBalls);
            mBallsView.reset(SystemClock.elapsedRealtime(), state);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isChangingConfigurations()) {
            mBallsView.setMode(DivideAndConquerView.Mode.PausedByUser);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mVibrateOn = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Preferences.KEY_VIBRATE, true);

        mNumLivesStart = Preferences.getCurrentDifficulty(this).getLivesToStart();
    }

    /** {@inheritDoc} */
    public void onBallHitsMovingLine(final BallEngine ballEngine, final Ball ball, float x, float y) {
        if (--mNumLives == 0) {
            mBallsView.setMode(DivideAndConquerView.Mode.Paused);

            // vibrate three times
            if (mVibrateOn) {
                mVibrator.vibrate(
                    new long[]{0l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS,
                                   50l, COLLISION_VIBRATE_MILLIS},
                        -1);
            }
            startActivityForResult(new Intent(this, GameOverActivity.class), R.id.gameOverRequest);
        } else {
            if (mVibrateOn) {
                mVibrator.vibrate(COLLISION_VIBRATE_MILLIS);
            }
            updateLivesDisplay(mNumLives);
            if (mNumLives <= 1) {
                mBallsView.postDelayed(mOneLifeToastRunnable, 700);
            } else {
                mBallsView.postDelayed(mLivesBlinkRedRunnable, 700);
            }
        }
    }

    private Runnable mOneLifeToastRunnable = new Runnable() {
        public void run() {
            showToast("1 life left!");
        }
    };

    private Runnable mLivesBlinkRedRunnable = new Runnable() {
        public void run() {
            mLivesLeft.setTextColor(Color.RED);
            mLivesLeft.postDelayed(mLivesTextWhiteRunnable, 2000);
        }
    };

    /** {@inheritDoc} */
    public void onAreaChange(final BallEngine ballEngine) {
        final float percentageFilled = ballEngine.getPercentageFilled();
        updatePercentDisplay(percentageFilled);
        if (percentageFilled > LEVEL_UP_THRESHOLD) {
            levelUp(ballEngine);
        }
    }

    /**
     * Go to the next level
     * @param ballEngine The ball engine.
     */
    private void levelUp(final BallEngine ballEngine) {
        mNumBalls++;

        updatePercentDisplay(0);
        updateLevelDisplay(mNumBalls);
        ballEngine.reset(SystemClock.elapsedRealtime(), mNumBalls);
        mBallsView.setMode(DivideAndConquerView.Mode.Bouncing);
        if (mNumBalls % 4 == 0) {
            mNumLives++;
            updateLivesDisplay(mNumLives);
            showToast("bonus life!");
        }
        if (mNumBalls == 10) {
            showToast("Level 10? You ROCK!");
        } else if (mNumBalls == 15) {
            showToast("BALLS TO THE WALL!");
        }
    }

    private Runnable mLivesTextWhiteRunnable = new Runnable() {

        public void run() {
            mLivesLeft.setTextColor(Color.WHITE);
        }
    };

    private void showToast(String text) {
        cancelToasts();
        mCurrentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        mCurrentToast.show();
    }

    private void cancelToasts() {
        if (mCurrentToast != null) {
            mCurrentToast.cancel();
            mCurrentToast = null;
        }
    }

    /**
     * Update the header that displays how much of the space has been contained.
     * @param amountFilled The fraction, between 0 and 1, that is filled.
     */
    private void updatePercentDisplay(float amountFilled) {
        final int prettyPercent = (int) (amountFilled *100);
        mPercentContained.setText(
                getString(R.string.percent_contained, prettyPercent));
    }

    /**
     * Update the header displaying the current level
     */
    private void updateLevelDisplay(int numBalls) {
        mLevelInfo.setText(getString(R.string.level, numBalls));
    }

    /**
     * Update the display showing the number of lives left.
     * @param numLives The number of lives left.
     */
    void updateLivesDisplay(int numLives) {
        String text = (numLives == 1) ?
                getString(R.string.one_life_left) : getString(R.string.lives_left, numLives);
        mLivesLeft.setText(text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == R.id.gameOverRequest) {
            if (resultCode == RESULT_OK) {
                mNewGame = true;
                recreate();
            } else {
                finish();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
