package com.mfh.enjoycity.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.inputmethodservice.Keyboard;
import android.support.annotation.NonNull;

/**
 * Created by kun on 15/8/11.
 */
public class MfKeyboard extends Keyboard {
    private Key mEnterKey;

    public MfKeyboard(Context context, int xmlLayoutResId) {
        super(context, xmlLayoutResId);
    }

    public MfKeyboard(Context context, int xmlLayoutResId, int modeId, int width, int height) {
        super(context, xmlLayoutResId, modeId, width, height);
    }

    public MfKeyboard(Context context, int xmlLayoutResId, int modeId) {
        super(context, xmlLayoutResId, modeId);
    }

    public MfKeyboard(Context context, int layoutTemplateResId, CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
    }

    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y, XmlResourceParser parser) {
//        return super.createKeyFromXml(res, parent, x, y, parser);
        Key key = new Key(res, parent, x, y, parser);
        if (key.codes[0] == 10) {
            mEnterKey = key;
        }
        return key;
    }

}
