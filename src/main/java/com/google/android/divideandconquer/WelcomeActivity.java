package com.google.android.divideandconquer;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;

public class WelcomeActivity extends Activity implements NewGameCallback, DivideAndConquerView.BallEngineCallBack, DialogInterface.OnCancelListener {
    private static final int WELCOME_DIALOG = 20;
    private DivideAndConquerView mBallsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mBallsView = (DivideAndConquerView) findViewById(R.id.ballsView);
        mBallsView.setCallback(this);

        if (savedInstanceState == null) {
            showDialog(WELCOME_DIALOG);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == WELCOME_DIALOG) {
            Dialog welcomeDialog = new WelcomeDialog(this, this);
            welcomeDialog.setOnCancelListener(this);
            return welcomeDialog;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onNewGame() {
        startActivity(new Intent(this, DivideAndConquerActivity.class));
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }

    @Override
    public void onEngineReady(BallEngine ballEngine) {
        // display 10 balls bouncing around for visual effect
        ballEngine.reset(SystemClock.elapsedRealtime(), Constants.MAIN_MENU_BALLS_NUM);
        mBallsView.setMode(DivideAndConquerView.Mode.Bouncing);
    }

    @Override
    public void onBallHitsMovingLine(BallEngine ballEngine, Ball ball, float x, float y) {
        /* ignore */
    }

    @Override
    public void onAreaChange(BallEngine ballEngine) {
        /* ignore */
    }
}
