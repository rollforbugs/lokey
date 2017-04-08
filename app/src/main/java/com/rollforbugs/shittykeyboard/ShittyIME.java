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

import java.util.List;
import java.util.Random;

/**
 * Created by jonathan on 4/8/17.
 */

public class ShittyIME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private Keyboard keyboard;

    private int swappiness = 1;
    private Random rand = new Random();
    private boolean caps = false;
    private String keys = "abcdefghijklmnopqrstuvwxyz1234567890-=#@ :.,/";

    private void swapKeys(Keyboard.Key k1, Keyboard.Key k2) {
        int[] codes = k1.codes;
        Drawable icon = k1.icon;
        Drawable iconPreview = k1.iconPreview;
        CharSequence label = k1.label;
        boolean modifier = k1.modifier;
        boolean on = k1.on;
        CharSequence popupCharacters = k1.popupCharacters;
        int popupResId = k1.popupResId;
        boolean pressed = k1.pressed;
        boolean repeatable = k1.repeatable;
        boolean sticky = k1.sticky;
        CharSequence text = k1.text;

        k1.codes = k2.codes;
        k1.icon = k2.icon;
        k1.iconPreview = k2.iconPreview;
        k1.label = k2.label;
        k1.modifier = k2.modifier;
        k1.on = k2.on;
        k1.popupCharacters = k2.popupCharacters;
        k1.popupResId = k2.popupResId;
        k1.pressed = k2.pressed;
        k1.repeatable = k2.repeatable;
        k1.sticky = k2.sticky;
        k1.text = k2.text;

        k2.codes = codes;
        k2.icon = icon;
        k2.iconPreview = iconPreview;
        k2.label = label;
        k2.modifier = modifier;
        k2.on = on;
        k2.popupCharacters = popupCharacters;
        k2.popupResId = popupResId;
        k2.pressed = pressed;
        k2.repeatable = repeatable;
        k2.sticky = sticky;
        k2.text = text;
    }

    private void shuffleKeyboard() {
        List<Keyboard.Key> keys = keyboard.getKeys();
        int nKeys = keys.size();
        for (int i = 0; i < swappiness; i++) {
            Keyboard.Key k1 = keys.get(rand.nextInt(nKeys));
            Keyboard.Key k2 = keys.get(rand.nextInt(nKeys));
            swapKeys(k1, k2);
        }
        kv.invalidateAllKeys();
    }

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty);
        shuffleKeyboard();
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

    private String charToString(int c) {
        if (c >= 0x20 && c <= 0x7e) {
            return String.format("%c", c);
        }
        return String.format("%02x", c);
    }

    @Override
    public void onPress(int primaryCode) {
        Log.e("ShittyKeyboard", "KEY DOWN: " + charToString(primaryCode));
    }

    @Override
    public void onRelease(int primaryCode) {
        Log.e("ShittyKeyboard", "KEY UP: " + charToString(primaryCode));
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
        shuffleKeyboard();
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
