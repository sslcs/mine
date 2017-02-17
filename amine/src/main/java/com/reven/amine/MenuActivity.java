package com.reven.amine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;

import com.reven.amine.databinding.ActivityMenuBinding;

public class MenuActivity extends Activity {
    private int mScoreFirst, mScoreSecond, mScoreThird;
    private int mLevel, mFirstClickSetting;
    private SharedPreferences mPref;
    private String[] arrayLevel, arrayFirstClickSetting;
    private ActivityMenuBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_menu);

        arrayLevel = getResources().getStringArray(R.array.level);
        arrayFirstClickSetting = getResources().getStringArray(R.array.first_click_setting);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mLevel = mPref.getInt("level", 0);
        mFirstClickSetting = mPref.getInt("first_click_setting", 0);
        showLevel();
        showFirstClickSetting();
    }

    private void showLevel() {
        binding.btnLevel.setText(getString(R.string.level, arrayLevel[mLevel]));
    }

    private void showFirstClickSetting() {
        binding.btnFirstClickSetting.setText(getString(R.string.first_click,
            arrayFirstClickSetting[mFirstClickSetting]));
    }

    public void onClickRank(View view) {
        getScore();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("排行榜");
        builder.setMessage(getString(R.string.level, arrayLevel[mLevel]) + "\n\n第一名 ：" + mScoreFirst + "秒\n" + "第二名 ：" +
            mScoreSecond + "秒\n" + "第三名 ：" + mScoreThird + "秒\n");
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void onSelectLevel(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("难度");
        builder.setSingleChoiceItems(arrayLevel, mLevel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSelectLevel(which);
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    public void onClickFirstClickSetting(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("第一次点击设置");
        builder.setSingleChoiceItems(arrayFirstClickSetting, mFirstClickSetting, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onSelectFirstClickSetting(which);
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void getScore() {
        mScoreFirst = mPref.getInt("first_" + mLevel, 9999);
        mScoreSecond = mPref.getInt("second_" + mLevel, 9999);
        mScoreThird = mPref.getInt("third_" + mLevel, 9999);
    }

    private void onSelectLevel(int level) {
        if (level == mLevel) return;
        mLevel = level;
        showLevel();
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt("level", mLevel).apply();
    }

    private void onSelectFirstClickSetting(int position) {
        if (position == mFirstClickSetting) return;
        mFirstClickSetting = position;
        showFirstClickSetting();
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt("first_click_setting", mFirstClickSetting).apply();
    }

    public void onClickStart(View view) {
        startActivity(new Intent(this, GameActivity.class));
    }
}
