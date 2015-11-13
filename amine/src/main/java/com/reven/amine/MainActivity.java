package com.reven.amine;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private Handler mHandler;
    private int mSpendTime = 0;
    private GameView mGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mGameView = (GameView) findViewById(R.id.game_view);
        final ImageView ivStatus = (ImageView) findViewById(R.id.btn_start);
        final TextView tvRest = (TextView) findViewById(R.id.tv_rest);
        final TextView tvTime = (TextView) findViewById(R.id.tv_time);
        final View btnMark = findViewById(R.id.btn_mark);

        ivStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGameView.start();
                ivStatus.getDrawable().setLevel(0);
                tvTime.setText(getString(R.string.time, 0));
                mSpendTime = 0;
                stopTimer();
                startTimer();
            }
        });
        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnMark.setSelected(!btnMark.isSelected());
                mGameView.setIsMarking(btnMark.isSelected());
            }
        });

        tvRest.setText(getString(R.string.rest, mGameView.getMineRest()));
        tvTime.setText(getString(R.string.time, 0));
        mGameView.setOnStatusChangeListener(new GameView.OnStatusChangeListener() {
            @Override
            public void onWin() {
                stopTimer();
            }

            @Override
            public void onLose() {
                ivStatus.getDrawable().setLevel(1);
                stopTimer();
            }

            @Override
            public void onMark() {
                tvRest.setText(getString(R.string.rest, mGameView.getMineRest()));
            }
        });

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                tvTime.setText(getString(R.string.time, mSpendTime++));
                startTimer();
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mGameView.isFinish()) {
            startTimer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopTimer();
    }

    private void startTimer() {
        mHandler.sendEmptyMessageDelayed(0, 1000);
    }

    private void stopTimer() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
