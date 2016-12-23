package com.reven.amine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private Handler mHandler;
    private int mSpendTime = 0;
    private GameView mGameView;
    private int mFirst, mSecond, mThird;
    private int mLevel;
    private SharedPreferences mPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mGameView = (GameView) findViewById(R.id.game_view);
        final ImageView ivStatus = (ImageView) findViewById(R.id.btn_start);
        final TextView tvRest = (TextView) findViewById(R.id.tv_rest);
        final TextView tvTime = (TextView) findViewById(R.id.tv_time);
        final View btnMark = findViewById(R.id.btn_mark);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mLevel = mPref.getInt("level", 0);
        mGameView.setLevel(mLevel);
        getScore();

        findViewById(R.id.btn_rank).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("排行榜");
                builder.setMessage("第一名 ：" + mFirst + "秒\n" + "第二名 ：" + mSecond + "秒\n" + "第三名 ：" + mThird + "秒\n");
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });

        final String[] arrayLevel = new String[]{"简单", "普通", "困难", "地狱(请慎重!)"};
        findViewById(R.id.btn_level).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("难度");
                builder.setSingleChoiceItems(arrayLevel, mLevel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which != mLevel) {
                            mLevel = which;
                            onLevel();
                        }
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });
        ivStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGameView.isFinish()) {
                    return;
                }

                mGameView.resetData();
                ivStatus.getDrawable().setLevel(0);
                tvTime.setText(getString(R.string.time, 0));
                mSpendTime = 0;
                btnMark.setSelected(false);
                mGameView.setIsMarking(false);
                stopTimer();
            }
        });
        btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGameView.isStart() || mGameView.isFinish()) {
                    return;
                }

                btnMark.setSelected(!btnMark.isSelected());
                mGameView.setIsMarking(btnMark.isSelected());
            }
        });

        tvRest.setText(getString(R.string.rest, mGameView.getMineRest(), mGameView.getVirginRest()));
        tvTime.setText(getString(R.string.time, 0));
        mGameView.setOnStatusChangeListener(new GameView.OnStatusChangeListener() {
            @Override
            public void onWin() {
                stopTimer();
                String tip = "完成！";
                if (mSpendTime < mThird) {
                    saveScore();
                    tip = "新纪录！";
                }

                Toast toast = Toast.makeText(MainActivity.this, tip, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onLose() {
                ivStatus.getDrawable().setLevel(1);
                stopTimer();
            }

            @Override
            public void onMark() {
                tvRest.setText(getString(R.string.rest, mGameView.getMineRest(), mGameView.getVirginRest()));
            }

            @Override
            public void onStart() {
                startTimer();
            }
        });

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                tvTime.setText(getString(R.string.time, ++mSpendTime));
                startTimer();
                return true;
            }
        });
    }

    private void saveScore() {
        SharedPreferences.Editor editor = mPref.edit();
        if (mSpendTime < mFirst) {
            mThird = mSecond;
            mSecond = mFirst;
            mFirst = mSpendTime;
            editor.putInt("first_" + mLevel, mFirst);
            editor.putInt("second_" + mLevel, mSecond);
            editor.putInt("third_" + mLevel, mThird);
        } else if (mSpendTime < mSecond) {
            mThird = mSecond;
            mSecond = mSpendTime;
            editor.putInt("second_" + mLevel, mSecond);
            editor.putInt("third_" + mLevel, mThird);
        } else {
            mThird = mSpendTime;
            editor.putInt("third_" + mLevel, mThird);
        }
        editor.apply();
    }

    private void getScore() {
        mFirst = mPref.getInt("first_" + mLevel, 9999);
        mSecond = mPref.getInt("second_" + mLevel, 9999);
        mThird = mPref.getInt("third_" + mLevel, 9999);
    }

    private void onLevel() {
        mGameView.setLevel(mLevel);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt("level", mLevel).apply();
        getScore();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGameView.onDestroy();
        stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGameView.isStart()) {
            startTimer();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
