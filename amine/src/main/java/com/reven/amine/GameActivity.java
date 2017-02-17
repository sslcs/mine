package com.reven.amine;

import android.app.Activity;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.reven.amine.databinding.ActivityGameBinding;

public class GameActivity extends Activity {
    private Handler mHandler;
    private int mSpendTime = 0;
    private int mFirst, mSecond, mThird;
    private int mLevel;
    private SharedPreferences mPref;
    private ActivityGameBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_game);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mLevel = mPref.getInt("level", 0);
        int mFirstClickSetting = mPref.getInt("first_click_setting", 0);
        binding.gameView.init(mLevel,mFirstClickSetting);
        getScore();

        binding.ivStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.gameView.hasFinished()) {
                    return;
                }

                binding.gameView.resetData();
                binding.ivStatus.getDrawable().setLevel(0);
                binding.tvTime.setText(getString(R.string.time, 0));
                mSpendTime = 0;
                binding.btnMark.setSelected(false);
                binding.gameView.setMarking(false);
                stopTimer();
            }
        });
        binding.btnMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!binding.gameView.hasStarted() || binding.gameView.hasFinished()) {
                    return;
                }

                binding.btnMark.setSelected(!binding.btnMark.isSelected());
                binding.gameView.setMarking(binding.btnMark.isSelected());
            }
        });

        binding.tvRest.setText(getString(R.string.rest, binding.gameView.getMineRest(), binding.gameView
            .getVirginRest()));
        binding.tvTime.setText(getString(R.string.time, 0));
        binding.gameView.setOnStatusChangeListener(new GameView.OnStatusChangeListener() {
            @Override
            public void onWin() {
                stopTimer();
                String tip = "完成！";
                if (mSpendTime < mThird) {
                    saveScore();
                    tip = "新纪录！";
                }

                Toast toast = Toast.makeText(GameActivity.this, tip, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onLose() {
                binding.ivStatus.getDrawable().setLevel(1);
                stopTimer();
            }

            @Override
            public void onMark() {
                binding.tvRest.setText(getString(R.string.rest, binding.gameView.getMineRest(), binding.gameView
                    .getVirginRest()));
            }

            @Override
            public void onStart() {
                startTimer();
            }
        });

        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                binding.tvTime.setText(getString(R.string.time, ++mSpendTime));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding.gameView.onDestroy();
        stopTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding.gameView.hasStarted()) {
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
