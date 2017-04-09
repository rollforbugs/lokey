package com.rollforbugs.goodluckkeyboard;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.util.List;
import java.util.Random;

/**
 * Created by jonathan on 4/8/17.
 */

public class IME extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private static MediaPlayer[] mediaEight = new MediaPlayer[7];

    private KeyboardView kv;
    private Keyboard qwertyKeyboard;
    private Keyboard symbolsKeyboard;
    private Keyboard symShiftKeyboard;
    private SharedPreferences prefs;

    private Random rand = new Random();
    private boolean shift = false;

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

    private void shuffleKeyboard(Keyboard keyboard) {
        List<Keyboard.Key> keys = keyboard.getKeys();
        int nKeys = keys.size();
        int swappiness = Integer.parseInt(prefs.getString("pref_swap_swappiness", "1"));
        for (int i = 0; i < swappiness; i++) {
            Keyboard.Key k1 = keys.get(rand.nextInt(nKeys));
            Keyboard.Key k2 = keys.get(rand.nextInt(nKeys));
            swapKeys(k1, k2);
        }
        kv.invalidateAllKeys();
    }

    private void initializeMedia() {
        mediaEight[0] = MediaPlayer.create(getApplicationContext(), R.raw.eight_1);
        mediaEight[1] = MediaPlayer.create(getApplicationContext(), R.raw.eight_2);
        mediaEight[2] = MediaPlayer.create(getApplicationContext(), R.raw.eight_3);
        mediaEight[3] = MediaPlayer.create(getApplicationContext(), R.raw.eight_4);
        mediaEight[4] = MediaPlayer.create(getApplicationContext(), R.raw.eight_5);
        mediaEight[5] = MediaPlayer.create(getApplicationContext(), R.raw.eight_7);
        mediaEight[6] = MediaPlayer.create(getApplicationContext(), R.raw.eight_8);

        mediaEight[0].setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaEight[1].setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaEight[2].setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaEight[3].setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaEight[4].setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaEight[5].setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaEight[6].setAudioStreamType(AudioManager.STREAM_MUSIC);
    }



    @Override
    public View onCreateInputView() {
        initializeMedia();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        qwertyKeyboard = new Keyboard(this, R.xml.qwerty);
        symbolsKeyboard = new Keyboard(this, R.xml.symbols);
        symShiftKeyboard = new Keyboard(this, R.xml.symbols_shift);

        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        kv.setKeyboard(qwertyKeyboard);
        kv.setOnKeyboardActionListener(this);
        return kv;
    }

    private void playClick(int keyCode) {
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch (keyCode) {
            case 0x20:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 0x0a:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            case 0x38:
                if (prefs.getBoolean("pref_eight_enable", false)) {
                    // Play random "eight" sound
                    MediaPlayer mPlayer = mediaEight[rand.nextInt(mediaEight.length)];
                    mPlayer.seekTo(0);
                    mPlayer.start();
                }
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
                shift = !shift;
                if (kv.getKeyboard() == qwertyKeyboard) {
                    kv.getKeyboard().setShifted(shift);
                } else {
                    if (shift) {
                        kv.setKeyboard(symShiftKeyboard);
                    } else {
                        kv.setKeyboard(symbolsKeyboard);
                    }
                }
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_MODE_CHANGE:
                if (kv.getKeyboard() == symbolsKeyboard || kv.getKeyboard() == symShiftKeyboard) {
                    kv.setKeyboard(qwertyKeyboard);
                } else {
                    kv.setKeyboard(symbolsKeyboard);
                    symbolsKeyboard.setShifted(false);
                }
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                char code = (char)primaryCode;
                if (Character.isLetter(code) && shift) {
                    code = Character.toUpperCase(code);
                }
                if (shift) {
                    shift = false;
                    kv.getKeyboard().setShifted(false);
                    kv.invalidateAllKeys();
                }
                ic.commitText(String.valueOf(code), 1);
        }

        if (prefs.getBoolean("pref_swap_enable", false)) {
            shuffleKeyboard(kv.getKeyboard());
        }
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
