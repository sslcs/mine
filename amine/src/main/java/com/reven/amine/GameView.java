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
    private int iColCount, iRowCount, iTotalCount;
    private int iMineCount, iVirginRest;
    private ArrayList<Bitmap> aIcons = new ArrayList<>(15);
    private int iIconSize;
    private int iMineRest;
    private int iLevel, iFirstClickSetting;
    private int iOffsetX, iOffsetY;
    private ArrayList<Integer> aMineData;
    private boolean bMarking = false;
    private boolean bFinished = false;
    private boolean bStarted = false;
    private boolean bNeedInit = true;
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

    public void setMarking(boolean marking) {
        this.bMarking = marking;
        invalidate();
    }

    public void init(int level, int firstClickSetting) {
        iLevel = level;
        iFirstClickSetting = firstClickSetting;
    }

    public boolean hasStarted() {
        return bStarted;
    }

    public boolean hasFinished() {
        return bFinished;
    }

    public void resetData() {
        aMineData.clear();
        for (int i = 0; i < iTotalCount; i++) {
            aMineData.add(i < iMineCount ? 0x09 : 0x00);
        }
        Collections.shuffle(aMineData);

        invalidate();
        iMineRest = iMineCount;
        iVirginRest = iTotalCount;
        bFinished = false;
        if (mListener != null) {
            mListener.onMark();
        }
    }

    private void initGameData() {
        bNeedInit = false;

        int mPadding = 10;
        int width = getWidth() - mPadding * 2;
        iColCount = iLevel == 0 ? 8 : width / iIconSize;
        iOffsetX = (width - iColCount * iIconSize) / 2 + mPadding;
        int height = getHeight() - iOffsetX * 2;

        if (iLevel == 0) {
            iRowCount = 8;
        } else {
            iRowCount = height / iIconSize;
            if (iLevel == 1 && iRowCount * iColCount > 256) {
                for (int i = iRowCount - 1; i > 8; i--) {
                    if (i * iColCount < 256) {
                        iRowCount = i + 1;
                        break;
                    }
                }
            }
        }
        iOffsetY = (height - iRowCount * iIconSize) / 2 + iOffsetX;
        iTotalCount = iColCount * iRowCount;

        // 设置地雷数量
        if (iLevel == 0) {
            iMineCount = 10;
        } else if (iLevel == 1) {
            iMineCount = (int) (iTotalCount / 6.4);
        } else if (iLevel == 2) {
            iMineCount = (int) (iTotalCount / 4.8);
        }
        aMineData = new ArrayList<>(iTotalCount);
        mRandom = new Random(System.currentTimeMillis());
        resetData();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode() || iIconSize == 0) {
            return;
        }

        if (bNeedInit) {
            initGameData();
        }

        canvas.drawColor(bMarking ? 0XFFCCE9CF : Color.BLACK);
        for (int row = 0; row < iRowCount; row++) {
            for (int column = 0; column < iColCount; column++) {
                int indexData = row * iColCount + column;
                int status = getStatus(indexData);
                int indexIcon;
                if (status == 0x30) {
                    indexIcon = getValue(indexData);
                } else {
                    indexIcon = 0x0C + status / 0x10;
                }
                canvas.drawBitmap(aIcons.get(indexIcon), iOffsetX + column * iIconSize, iOffsetY + row * iIconSize,
                    null);
            }
        }
    }

    private boolean isMine(int index) {
        return getValue(index) == 0x09;
    }

    private int getValue(int index) {
        return aMineData.get(index) & 0x0F;
    }

    private int getStatus(int index) {
        return aMineData.get(index) & 0xF0;
    }

    private void onMine(int index) {
        for (int i = 0; i < iTotalCount; i++) {
            if (isMine(i)) {
                if (getStatus(i) != 0x10) {
                    aMineData.set(i, 0x39);
                }
            } else if (getStatus(i) == 0x10) {
                aMineData.set(i, 0x3B);
            }
        }
        aMineData.set(index, 0x3A);
        bFinished = true;
        bStarted = false;
        if (mListener != null) {
            mListener.onLose();
        }

        invalidate();
    }

    private void onSafe(int index) {
        setNumber(index);
        iVirginRest--;

        if (getValue(index) == 0x00) {
            onBlank(index);
        }

        if (isWin() && mListener != null) {
            mListener.onWin();
        }

        invalidate();
        if (mListener != null) mListener.onMark();
    }

    private void setNumber(int index) {
        int amount = 0;
        //Above
        if (index - iColCount > -1) {
            if (isMine(index - iColCount)) amount++;
            //Left
            if ((index - iColCount) % iColCount != 0 && isMine(index - iColCount - 1)) amount++;
            //Right
            if ((index - iColCount + 1) % iColCount != 0 && isMine(index - iColCount + 1)) amount++;
        }
        //Left
        if (index % iColCount != 0 && isMine(index - 1)) amount++;
        //Right
        if ((index + 1) % iColCount != 0 && isMine(index + 1)) amount++;
        //Below
        if (index + iColCount < iTotalCount) {
            if (isMine(index + iColCount)) amount++;
            //Left
            if ((index + iColCount) % iColCount != 0 && isMine(index + iColCount - 1)) amount++;
            //Right
            if ((index + iColCount + 1) % iColCount != 0 && isMine(index + iColCount + 1)) amount++;
        }
        aMineData.set(index, amount + 0x30);
    }

    private void mark(int index) {
        if (getStatus(index) == 0x30) {
            return;
        }

        if (getStatus(index) == 0x00) {
            aMineData.set(index, aMineData.get(index) + 0x10);
            iMineRest--;
            iVirginRest--;
        } else if (getStatus(index) == 0x10) {
            aMineData.set(index, aMineData.get(index) + 0x10);
            iMineRest++;
            iVirginRest++;
        } else {
            aMineData.set(index, aMineData.get(index) & 0x0F);
        }
        invalidate();

        if (mListener != null) {
            mListener.onMark();
        }
    }

    private void moveMine(int index, int center) {
        int i;
        while (true) {
            i = mRandom.nextInt(iTotalCount);
            if ((i > center - iColCount - 2 && i < center + iColCount + 2) || isMine(i)) {
                continue;
            }

            aMineData.set(i, 0x09);
            aMineData.set(index, 0x00);
            return;
        }
    }

    private void firstClick(int index) {
        bStarted = true;
        if (mListener != null) {
            mListener.onStart();
        }

        // 第一次点击可以有雷，不做移动直接返回
        if (iFirstClickSetting == 1) {
            click(index);
            return;
        }

        if (isMine(index)) {
            moveMine(index, index);
        }

        // 仅第一次点击不会有雷，移动完成返回
        if (iFirstClickSetting == 0) {
            onSafe(index);
            return;
        }

        // 移动9格内的所有雷
        //Above
        if (index - iColCount > -1) {
            if (isMine(index - iColCount)) {
                moveMine(index - iColCount, index);
            }
            //Left
            if ((index - iColCount) % iColCount != 0 && isMine(index - iColCount - 1)) {
                moveMine(index - iColCount - 1, index);
            }
            //Right
            if ((index - iColCount + 1) % iColCount != 0 && isMine(index - iColCount + 1)) {
                moveMine(index - iColCount + 1, index);
            }
        }
        //Left
        if (index % iColCount != 0 && isMine(index - 1)) {
            moveMine(index - 1, index);
        }
        //Right
        if ((index + 1) % iColCount != 0 && isMine(index + 1)) {
            moveMine(index + 1, index);
        }
        //Below
        if (index + iColCount < iTotalCount) {
            if (isMine(index + iColCount)) {
                moveMine(index + iColCount, index);
            }
            //Left
            if ((index + iColCount) % iColCount != 0 && isMine(index + iColCount - 1)) {
                moveMine(index + iColCount - 1, index);
            }
            //Right
            if ((index + iColCount + 1) % iColCount != 0 && isMine(index + iColCount + 1)) {
                moveMine(index + iColCount + 1, index);
            }
        }

        onSafe(index);
    }

    private boolean isTouchMine(int x, int y) {
        return x > iOffsetX && x < getWidth() - iOffsetX && y > iOffsetY && y < getHeight() - iOffsetY;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        super.onTouchEvent(event);
        if (bFinished) {
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            if (!isTouchMine(x, y)) return true;
            int selCol = (x - iOffsetX) / iIconSize;
            int selRow = (y - iOffsetY) / iIconSize;
            if (selRow < iRowCount && selCol < iColCount) {
                int index = selRow * iColCount + selCol;

                if (!bStarted) {
                    firstClick(index);
                    return true;
                }

                if (bMarking) {
                    mark(index);
                    return true;
                }

                if (getStatus(index) != 0x00) {
                    return true;
                }

                click(index);
            }
        }
        return true;
    }

    private void click(int index) {
        if (isMine(index)) {
            onMine(index);
        } else {
            onSafe(index);
        }
    }

    private boolean isOpen(int index) {
        return getStatus(index) == 0x30;
    }

    private void onBlank(int i) {
        //Above
        if (i - iColCount > -1) {
            if (!isOpen(i - iColCount)) {
                onSafe(i - iColCount);
            }
            //Left
            if ((i - iColCount) % iColCount != 0 && !isOpen(i - iColCount - 1)) {
                onSafe(i - iColCount - 1);
            }
            //Right
            if ((i - iColCount + 1) % iColCount != 0 && !isOpen(i - iColCount + 1)) {
                onSafe(i - iColCount + 1);
            }
        }
        //Left
        if (i % iColCount != 0 && !isOpen(i - 1)) {
            onSafe(i - 1);
        }
        //Right
        if ((i + 1) % iColCount != 0 && !isOpen(i + 1)) {
            onSafe(i + 1);
        }
        //Below
        if (i + iColCount < iTotalCount) {
            if (!isOpen(i + iColCount)) {
                onSafe(i + iColCount);
            }
            //Left
            if ((i + iColCount) % iColCount != 0 && !isOpen(i + iColCount - 1)) {
                onSafe(i + iColCount - 1);
            }
            //Right
            if ((i + iColCount + 1) % iColCount != 0 && !isOpen(i + iColCount + 1)) {
                onSafe(i + iColCount + 1);
            }
        }
    }

    private void initResource() {
        Resources r = getResources();
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_blank));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_01));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_02));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_03));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_04));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_05));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_06));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_07));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.n_08));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_mine_right));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_mine_explode));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_mine_wrong));

        Bitmap bDefault = BitmapFactory.decodeResource(r, R.drawable.i_default);
        iIconSize = bDefault.getWidth();
        aIcons.add(bDefault);
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_mark_flag));
        aIcons.add(BitmapFactory.decodeResource(r, R.drawable.i_mark_doubt));
    }

    public int getMineRest() {
        return iMineRest;
    }

    public int getVirginRest() {
        return iVirginRest;
    }

    public void setOnStatusChangeListener(OnStatusChangeListener listener) {
        mListener = listener;
    }

    public boolean isWin() {
        if (bFinished) {
            return false;
        }

        for (int i = 0; i < iTotalCount; i++) {
            if (getStatus(i) != 0x30 && !isMine(i)) {
                return false;
            }
        }
        bFinished = true;
        bStarted = false;
        return true;
    }

    public void onDestroy() {
        for (Bitmap bm : aIcons) {
            bm.recycle();
        }
        aIcons.clear();
        aMineData.clear();
        System.gc();
    }

    public interface OnStatusChangeListener {
        void onWin();

        void onLose();

        void onMark();

        void onStart();
    }
}
