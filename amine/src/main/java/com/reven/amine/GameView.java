package com.reven.amine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by LiuCongshan on 2015/11/10.
 * GameView
 */
public class GameView extends View implements View.OnTouchListener {
    private int mColCount, mRowCount, mTotalCount;
    private int mMineCount;
    private ArrayList<Bitmap> mIcons = new ArrayList<>(15);
    private int mIconSide;
    private int mMineRest;
    private int mLevel;
    private int mOffsetX, mOffsetY;
    private ArrayList<Integer> mMineData;
    private boolean isMarking = false;
    private boolean isFinish = false;
    private boolean isStart = false;
    private OnStatusChangeListener mListener;
    private Random mRandom;

    public GameView(Context context) {
        this(context, null);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initResource();
        setOnTouchListener(this);
    }

    public void setIsMarking(boolean isMarking) {
        this.isMarking = isMarking;
        invalidate();
    }

    public void setLevel(int level) {
        mLevel = level;
        if (mMineData == null) {
            return;
        }

        if (level == 0) {
            mMineCount = mTotalCount / 10;
        } else if (level == 1) {
            mMineCount = mTotalCount / 7;
        } else if (level == 2) {
            mMineCount = mTotalCount / 5;
        } else {
            mMineCount = mTotalCount / 4;
        }
        resetData();
    }

    public boolean isStart() {
        return isStart;
    }

    public boolean isFinish() {
        return isFinish;
    }

    public void resetData() {
        mMineData.clear();
        for (int i = 0; i < mTotalCount; i++) {
            mMineData.add(i < mMineCount ? 0x09 : 0x00);
        }
        Collections.shuffle(mMineData);

        invalidate();
        mMineRest = mMineCount;
        isFinish = false;
        if (mListener != null) {
            mListener.onMark();
        }
    }

    private void initGameData() {
        int mPadding = 10;
        int width = getWidth() - mPadding * 2;
        mColCount = width / mIconSide;
        mOffsetX = (width - mColCount * mIconSide) / 2 + mPadding;
        int height = getHeight() - mOffsetX * 2;
        mRowCount = height / mIconSide;
        mOffsetY = (height - mRowCount * mIconSide) / 2 + mOffsetX;
        mTotalCount = mColCount * mRowCount;
        mRandom = new Random(System.currentTimeMillis());
        mMineData = new ArrayList<>(mTotalCount);
        setLevel(mLevel);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode() || mIconSide == 0) {
            return;
        }
        canvas.drawColor(isMarking ? 0XFFCCE9CF : Color.BLACK);

        if (mColCount == 0) {
            initGameData();
        }

        for (int i = 0; i < mRowCount; i++) {
            for (int j = 0; j < mColCount; j++) {
                int indexData = i * mColCount + j;
                int status = getStatus(indexData);
                int indexIcon;
                if (status == 0x30) {
                    indexIcon = getValue(indexData);
                } else {
                    indexIcon = 0x0C + status / 0x10;
                }
                canvas.drawBitmap(mIcons.get(indexIcon), mOffsetX + j * mIconSide, mOffsetY + i * mIconSide, null);
            }
        }
    }

    private boolean isMine(int index) {
        return getValue(index) == 0x09;
    }

    private int getValue(int index) {
        return mMineData.get(index) & 0x0F;
    }

    private int getStatus(int index) {
        return mMineData.get(index) & 0xF0;
    }

    private void onMine(int index) {
        for (int i = 0; i < mTotalCount; i++) {
            if (isMine(i)) {
                if (getStatus(i) != 0x10) {
                    mMineData.set(i, 0x39);
                }
            } else if (getStatus(i) == 0x10) {
                mMineData.set(i, 0x3B);
            }
        }
        mMineData.set(index, 0x3A);
        isFinish = true;
        isStart = false;
        if (mListener != null) {
            mListener.onLose();
        }

        invalidate();
    }

    private void open(int index) {
        setNumber(index);

        if (getValue(index) == 0x00) {
            onBlank(index);
        }

        if (isWin() && mListener != null) {
            mListener.onWin();
        }

        invalidate();
    }

    private void setNumber(int index) {
        int amount = 0;
        //Above
        if (index - mColCount > -1) {
            if (isMine(index - mColCount)) amount++;
            //Left
            if ((index - mColCount) % mColCount != 0 && isMine(index - mColCount - 1)) amount++;
            //Right
            if ((index - mColCount + 1) % mColCount != 0 && isMine(index - mColCount + 1)) amount++;
        }
        //Left
        if (index % mColCount != 0 && isMine(index - 1)) amount++;
        //Right
        if ((index + 1) % mColCount != 0 && isMine(index + 1)) amount++;
        //Below
        if (index + mColCount < mTotalCount) {
            if (isMine(index + mColCount)) amount++;
            //Left
            if ((index + mColCount) % mColCount != 0 && isMine(index + mColCount - 1)) amount++;
            //Right
            if ((index + mColCount + 1) % mColCount != 0 && isMine(index + mColCount + 1)) amount++;
        }
        mMineData.set(index, amount + 0x30);
    }

    private void mark(int index) {
        if (getStatus(index) == 0x30) {
            return;
        }

        if (getStatus(index) == 0x00) {
            mMineData.set(index, mMineData.get(index) + 0x10);
            mMineRest--;
        } else if (getStatus(index) == 0x10) {
            mMineData.set(index, mMineData.get(index) + 0x10);
            mMineRest++;
        } else {
            mMineData.set(index, mMineData.get(index) & 0x0F);
        }
        invalidate();

        if (mListener != null) {
            mListener.onMark();
        }
    }

    private void moveMine(int index, int center) {
        int i;
        while (true) {
            i = mRandom.nextInt(mTotalCount);
            if ((i > center - mColCount - 2 && i < center + mColCount + 2) || isMine(i)) {
                continue;
            }

            mMineData.set(i, 0x09);
            mMineData.set(index, 0x00);
            return;
        }
    }

    private void firstClick(int index) {
        isStart = true;
        if (mListener != null) {
            mListener.onStart();
        }

        if (isMine(index)) {
            moveMine(index, index);
        }

        //Above
        if (index - mColCount > -1) {
            if (isMine(index - mColCount)) {
                moveMine(index - mColCount, index);
            }
            //Left
            if ((index - mColCount) % mColCount != 0 && isMine(index - mColCount - 1)) {
                moveMine(index - mColCount - 1, index);
            }
            //Right
            if ((index - mColCount + 1) % mColCount != 0 && isMine(index - mColCount + 1)) {
                moveMine(index - mColCount + 1, index);
            }
        }
        //Left
        if (index % mColCount != 0 && isMine(index - 1)) {
            moveMine(index - 1, index);
        }
        //Right
        if ((index + 1) % mColCount != 0 && isMine(index + 1)) {
            moveMine(index + 1, index);
        }
        //Below
        if (index + mColCount < mTotalCount) {
            if (isMine(index + mColCount)) {
                moveMine(index + mColCount, index);
            }
            //Left
            if ((index + mColCount) % mColCount != 0 && isMine(index + mColCount - 1)) {
                moveMine(index + mColCount - 1, index);
            }
            //Right
            if ((index + mColCount + 1) % mColCount != 0 && isMine(index + mColCount + 1)) {
                moveMine(index + mColCount + 1, index);
            }
        }

        open(index);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        super.onTouchEvent(event);
        if (isFinish) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            int selCol = (x - mOffsetX) / mIconSide;
            int selRow = (y - mOffsetY) / mIconSide;
            if (selRow < mRowCount && selCol < mColCount) {
                int index = selRow * mColCount + selCol;

                if (!isStart) {
                    firstClick(index);
                    return true;
                }

                if (isMarking) {
                    mark(index);
                    return true;
                }

                if (getStatus(index) != 0x00) {
                    return true;
                }

                if (isMine(index)) {
                    onMine(index);
                } else {
                    open(index);
                }
            }
        }
        return true;
    }

    private void checkPosition(int i) {
        open(i);
        if (getValue(i) == 0x00) {
            onBlank(i);
        }
    }

    private boolean isOpen(int index) {
        return getStatus(index) == 0x30;
    }

    private void onBlank(int i) {
        //Above
        if (i - mColCount > -1) {
            if (!isOpen(i - mColCount)) {
                checkPosition(i - mColCount);
            }
            //Left
            if ((i - mColCount) % mColCount != 0 && !isOpen(i - mColCount - 1)) {
                checkPosition(i - mColCount - 1);
            }
            //Right
            if ((i - mColCount + 1) % mColCount != 0 && !isOpen(i - mColCount + 1)) {
                checkPosition(i - mColCount + 1);
            }
        }
        //Left
        if (i % mColCount != 0 && !isOpen(i - 1)) {
            checkPosition(i - 1);
        }
        //Right
        if ((i + 1) % mColCount != 0 && !isOpen(i + 1)) {
            checkPosition(i + 1);
        }
        //Below
        if (i + mColCount < mTotalCount) {
            if (!isOpen(i + mColCount)) {
                checkPosition(i + mColCount);
            }
            //Left
            if ((i + mColCount) % mColCount != 0 && !isOpen(i + mColCount - 1)) {
                checkPosition(i + mColCount - 1);
            }
            //Right
            if ((i + mColCount + 1) % mColCount != 0 && !isOpen(i + mColCount + 1)) {
                checkPosition(i + mColCount + 1);
            }
        }
    }

    private void initResource() {
        Resources r = getResources();
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_blank));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_01));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_02));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_03));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_04));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_05));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_06));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_07));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_08));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_mine_right));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_mine_explode));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_mine_wrong));

        Bitmap bDefault = BitmapFactory.decodeResource(r, R.drawable.i_default);
        mIconSide = bDefault.getWidth();
        mIcons.add(bDefault);
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_mark_flag));
        mIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_mark_doubt));
    }

    public int getMineRest() {
        return mMineRest;
    }

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        mListener = listener;
    }

    public boolean isWin() {
        for (int i = 0; i < mTotalCount; i++) {
            if (getStatus(i) != 0x30 && !isMine(i)) {
                return false;
            }
        }
        isFinish = true;
        isStart = false;
        return true;
    }

    public void onDestroy() {
        for (Bitmap bm : mIcons) {
            bm.recycle();
        }
        mIcons.clear();
        mMineData.clear();
        System.gc();
    }

    public interface OnStatusChangeListener {
        void onWin();

        void onLose();

        void onMark();

        void onStart();
    }
}
