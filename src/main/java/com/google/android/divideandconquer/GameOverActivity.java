package com.google.android.divideandconquer;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
public class GameOverActivity extends Activity implements NewGameCallback, DialogInterface.OnCancelListener {

    private static final int GAME_OVER_DIALOG = 21;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            showDialog(GAME_OVER_DIALOG);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == GAME_OVER_DIALOG) {
            Dialog dialog = new GameOverDialog(this, this);
            dialog.setOnCancelListener(this);
            return dialog;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public void onNewGame() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
