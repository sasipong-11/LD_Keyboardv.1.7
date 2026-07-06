/*
 * Copyright (C) 2008-2009 The Android Open Source Project
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

package th.or.nectec.twskeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.InputMethodSubtype;

import java.util.List;

public class LatinKeyboardView extends KeyboardView {

    static final int KEYCODE_OPTIONS = -100;
    // TODO: Move this into android.inputmethodservice.Keyboard
    static final int KEYCODE_LANGUAGE_SWITCH = -101;

    static final int KEYCODE_THAI_SHIFT = -102;
    static final int KEYCODE_THAI_UNSHIFT = -103;
    static final int KEYCODE_ENG_SHIFT = -104;
    static final int KEYCODE_ENG = -105;
    SharedPreferences sharedPreferences;

    public LatinKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedPreferences = context.getSharedPreferences("th.or.nectec.twskeyboard", Context.MODE_PRIVATE);
    }

    public LatinKeyboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        sharedPreferences = context.getSharedPreferences("th.or.nectec.twskeyboard", Context.MODE_PRIVATE);
    }

    @Override
    protected boolean onLongPress(Key key) {
        if (key.codes[0] == Keyboard.KEYCODE_CANCEL) {
            getOnKeyboardActionListener().onKey(KEYCODE_OPTIONS, null);
            return true;
        /*} else if (key.codes[0] == 113) {

            return true; */
        } else {
            //Log.d("LatinKeyboardView", "KEY: " + key.codes[0]);
            return super.onLongPress(key);
        }
    }

    void setSubtypeOnSpaceKey(final InputMethodSubtype subtype) {
        final LatinKeyboard keyboard = (LatinKeyboard) getKeyboard();
        //keyboard.setSpaceIcon(getResources().getDrawable(subtype.getIconResId()));
        invalidateAllKeys();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int keyTextSize = sharedPreferences.getInt("keyTextSize", 50);

        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(keyTextSize);
        paint.setColor(Color.BLACK);

        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/CSChatThaiUI.ttf");
        paint.setTypeface(Typeface.create(font, Typeface.BOLD));

        List<Key> keys = getKeyboard().getKeys();

       /* if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE){
            Log.i("LARGE","LARGE");
        }else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL){
            Log.i("NORMAL","NORMAL");
        }else if((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE){
            Log.i("XLARGE","XLARGE");
        }
        else{
            Log.i("no size","no size");
        }*/


        for (Key key : keys) {

            if (key.label != null) {

                if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {

                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    {
                        if (key.label.equals("ุ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 20), key.y + 50, paint);
                        } else if (key.label.equals("ู")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 18), key.y + 50, paint);
                        } else if (key.label.equals("ฺ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 20), key.y + 57, paint);
                        } else if (key.label.equals("ิ")) { //อิ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 13), key.y + 80, paint);
                        } else if (key.label.equals("ี")) { //อี
                            canvas.drawText(key.label.toString(), key.x + (key.width - 13), key.y + 80, paint);
                        } else if (key.label.equals("ึ")) { //อึ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 13), key.y + 80, paint);
                        } else if (key.label.equals("ื")) { //อือ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 13), key.y + 80, paint);
                        } else if (key.label.equals("ั")) { //อั
                            canvas.drawText(key.label.toString(), key.x + (key.width - 20), key.y + 80, paint);
                        } else if (key.label.equals("็")) { //อ็
                            canvas.drawText(key.label.toString(), key.x + (key.width - 14), key.y + 80, paint);
                        } else if (key.label.equals("ํ")) { //อํ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 20), key.y + 80, paint);
                        } else if (key.label.equals("่")) { //อ่
                            canvas.drawText(key.label.toString(), key.x + (key.width - 20), key.y + 87, paint);
                        } else if (key.label.equals("้")) { //อ้
                            canvas.drawText(key.label.toString(), key.x + (key.width - 18), key.y + 87, paint);
                        } else if (key.label.equals("๊")) { //อ๊
                            canvas.drawText(key.label.toString(), key.x + (key.width - 18), key.y + 87, paint);
                        } else if (key.label.equals("๋")) { //อ๋
                            canvas.drawText(key.label.toString(), key.x + (key.width - 20), key.y + 87, paint);
                        } else if (key.label.equals("์")) { //อ์
                            canvas.drawText(key.label.toString(), key.x + (key.width - 18), key.y + 87, paint);
                        } else {
                            canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                                    key.y + 10 + (2 * key.height / 3), paint);
                        }
                    } else{
                        if (key.label.equals("ุ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 40, paint);
                        } else if (key.label.equals("ู")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 38), key.y + 40, paint);
                        } else if (key.label.equals("ฺ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 47, paint);
                        } else if (key.label.equals("ิ")) { //อิ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 33), key.y + 70, paint);
                        } else if (key.label.equals("ี")) { //อี
                            canvas.drawText(key.label.toString(), key.x + (key.width - 33), key.y + 70, paint);
                        } else if (key.label.equals("ึ")) { //อึ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 33), key.y + 70, paint);
                        } else if (key.label.equals("ื")) { //อือ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 33), key.y + 70, paint);
                        } else if (key.label.equals("ั")) { //อั
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 70, paint);
                        } else if (key.label.equals("็")) { //อ็
                            canvas.drawText(key.label.toString(), key.x + (key.width - 34), key.y + 70, paint);
                        } else if (key.label.equals("ํ")) { //อํ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 70, paint);
                        } else if (key.label.equals("่")) { //อ่
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 77, paint);
                        } else if (key.label.equals("้")) { //อ้
                            canvas.drawText(key.label.toString(), key.x + (key.width - 38), key.y + 77, paint);
                        } else if (key.label.equals("๊")) { //อ๊
                            canvas.drawText(key.label.toString(), key.x + (key.width - 38), key.y + 77, paint);
                        } else if (key.label.equals("๋")) { //อ๋
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 77, paint);
                        } else if (key.label.equals("์")) { //อ์
                            canvas.drawText(key.label.toString(), key.x + (key.width - 38), key.y + 77, paint);
                        } else {
                            canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                                    key.y + 10 + (2 * key.height / 3), paint);
                        }

                    }


                }
                else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {

                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    {
                        if (key.label.equals("ุ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 23), key.y + 45, paint);
                        } else if (key.label.equals("ู")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 18), key.y + 45, paint);
                        } else if (key.label.equals("ฺ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 22), key.y + 49, paint);
                        } else if (key.label.equals("ิ")) { //อิ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 15), key.y + 70, paint);
                        } else if (key.label.equals("ี")) { //อี
                            canvas.drawText(key.label.toString(), key.x + (key.width - 15), key.y + 70, paint);
                        } else if (key.label.equals("ึ")) { //อึ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 15), key.y + 70, paint);
                        } else if (key.label.equals("ื")) { //อือ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 15), key.y + 70, paint);
                        } else if (key.label.equals("ั")) { //อั
                            canvas.drawText(key.label.toString(), key.x + (key.width - 23), key.y + 70, paint);
                        } else if (key.label.equals("็")) { //อ็
                            canvas.drawText(key.label.toString(), key.x + (key.width - 15), key.y + 70, paint);
                        } else if (key.label.equals("ํ")) { //อํ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 22), key.y + 70, paint);
                        } else if (key.label.equals("่")) { //อ่
                            canvas.drawText(key.label.toString(), key.x + (key.width - 22), key.y + 77, paint);
                        } else if (key.label.equals("้")) { //อ้
                            canvas.drawText(key.label.toString(), key.x + (key.width - 21), key.y + 77, paint);
                        } else if (key.label.equals("๊")) { //อ๊
                            canvas.drawText(key.label.toString(), key.x + (key.width - 22), key.y + 77, paint);
                        } else if (key.label.equals("๋")) { //อ๋
                            canvas.drawText(key.label.toString(), key.x + (key.width - 22), key.y + 77, paint);
                        } else if (key.label.equals("์")) { //อ์
                            canvas.drawText(key.label.toString(), key.x + (key.width - 20), key.y + 77, paint);
                        } else {
                            canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                                    key.y + 10 + (2 * key.height / 3), paint);
                        }
                    } else{
                        if (key.label.equals("ุ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 41), key.y + 37, paint);
                        } else if (key.label.equals("ู")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 36), key.y + 37, paint);
                        } else if (key.label.equals("ฺ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 41, paint);
                        } else if (key.label.equals("ิ")) { //อิ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 33), key.y + 62, paint);
                        } else if (key.label.equals("ี")) { //อี
                            canvas.drawText(key.label.toString(), key.x + (key.width - 33), key.y + 62, paint);
                        } else if (key.label.equals("ึ")) { //อึ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 33), key.y + 62, paint);
                        } else if (key.label.equals("ื")) { //อือ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 33), key.y + 62, paint);
                        } else if (key.label.equals("ั")) { //อั
                            canvas.drawText(key.label.toString(), key.x + (key.width - 41), key.y + 62, paint);
                        } else if (key.label.equals("็")) { //อ็
                            canvas.drawText(key.label.toString(), key.x + (key.width - 33), key.y + 62, paint);
                        } else if (key.label.equals("ํ")) { //อํ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 62, paint);
                        } else if (key.label.equals("่")) { //อ่
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 70, paint);
                        } else if (key.label.equals("้")) { //อ้
                            canvas.drawText(key.label.toString(), key.x + (key.width - 39), key.y + 70, paint);
                        } else if (key.label.equals("๊")) { //อ๊
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 70, paint);
                        } else if (key.label.equals("๋")) { //อ๋
                            canvas.drawText(key.label.toString(), key.x + (key.width - 40), key.y + 70, paint);
                        } else if (key.label.equals("์")) { //อ์
                            canvas.drawText(key.label.toString(), key.x + (key.width - 38), key.y + 70, paint);
                        } else {
                            canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                                    key.y + 10 + (2 * key.height / 3), paint);
                        }

                    }


                }

                else if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {

                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                    {
                        if (key.label.equals("ุ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 85, paint);
                        } else if (key.label.equals("ู")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 85, paint);
                        } else if (key.label.equals("ฺ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 91, paint);
                        } else if (key.label.equals("ิ")) { //อิ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("ี")) { //อี
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("ึ")) { //อึ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("ื")) { //อือ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("ั")) { //อั
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("็")) { //อ็
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("ํ")) { //อํ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("่")) { //อ่
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("้")) { //อ้
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("๊")) { //อ๊
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("๋")) { //อ๋
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else if (key.label.equals("์")) { //อ์
                            canvas.drawText(key.label.toString(), key.x + (key.width - 50), key.y + 113, paint);
                        } else {
                            canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                                    key.y + 10 + (2 * key.height / 3), paint);
                        }
                    } else{
                        if (key.label.equals("ุ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 78, paint);
                        } else if (key.label.equals("ู")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 78, paint);
                        } else if (key.label.equals("ฺ")) {
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 84, paint);
                        } else if (key.label.equals("ิ")) { //อิ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 105, paint);
                        } else if (key.label.equals("ี")) { //อี
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 105, paint);
                        } else if (key.label.equals("ึ")) { //อึ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 105, paint);
                        } else if (key.label.equals("ื")) { //อือ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 105, paint);
                        } else if (key.label.equals("ั")) { //อั
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 105, paint);
                        } else if (key.label.equals("็")) { //อ็
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 105, paint);
                        } else if (key.label.equals("ํ")) { //อํ
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 105, paint);
                        } else if (key.label.equals("่")) { //อ่
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 108, paint);
                        } else if (key.label.equals("้")) { //อ้
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 108, paint);
                        } else if (key.label.equals("๊")) { //อ๊
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 108, paint);
                        } else if (key.label.equals("๋")) { //อ๋
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 108, paint);
                        } else if (key.label.equals("์")) { //อ์
                            canvas.drawText(key.label.toString(), key.x + (key.width - 80), key.y + 108, paint);
                        } else {
                            canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                                    key.y + 10 + (2 * key.height / 3), paint);
                        }

                    }


                }

                else {
                    canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                            key.y + 10 + (2 * key.height / 3), paint);
                }
            }
        }
    }

}
