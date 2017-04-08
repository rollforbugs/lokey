package com.rollforbugs.shittykeyboard;

import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by jonathan on 4/8/17.
 */

public class ShittyIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;

    private int shuffleAmount = 6;
    private Random rand = new Random();
    private boolean caps = false;
    private String keys = "abcdefghijklmnopqrstuvwxyz1234567890-=#@ :.,/";

    private void swapKeys(Keyboard.Key k1, Keyboard.Key k2) {
        int tmpX = k1.x;
        int tmpY = k1.y;
        int tmpWidth = k1.width;
        int tmpHeight = k1.height;
        int tmpGap = k1.gap;

        k1.x = k2.x;
        k1.y = k2.y;
        k1.width = k2.width;
        k1.height = k2.height;
        k1.gap = k2.gap;

        k2.x = tmpX;
        k2.y = tmpY;
        k2.width = tmpWidth;
        k2.height = tmpHeight;
        k2.gap = tmpGap;
    }

    private void shuffleKeyboard(Keyboard kbd) {
        List<Keyboard.Key> keys = kbd.getKeys();
        int nKeys = keys.size();
        for (int i = 0; i < shuffleAmount; i++) {
            swapKeys(keys.get(rand.nextInt(nKeys)), keys.get(rand.nextInt(nKeys)));
        }
    }

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        shuffleKeyboard(keyboard);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }

    private void playClick(int keyCode) {
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        playClick(primaryCode);
        switch (primaryCode) {
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1, 0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                caps = !caps;
                keyboard.setShifted(caps);
                kv.invalidateAllKeys();
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                char code = (char)primaryCode;
                if (Character.isLetter(code) && caps)
                        code = Character.toUpperCase(code);
                ic.commitText(String.valueOf(code), 1);
        }
        shuffleKeyboard(keyboard);
        kv.invalidateAllKeys();
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
