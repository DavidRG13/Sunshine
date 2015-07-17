package com.android.sunshine.app.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;
import com.android.sunshine.app.R;

public class EditTextPreferencesWithAttributes extends EditTextPreference {

    private static final int DEFAULT_LOCATION_MIN_LENGTH = 3;
    private int minLength;

    public EditTextPreferencesWithAttributes(final Context context) {
        super(context);
    }

    public EditTextPreferencesWithAttributes(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public EditTextPreferencesWithAttributes(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(final AttributeSet attrs) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.LocationEditTextPreference, 0, 0);
        try {
            minLength = typedArray.getInteger(R.styleable.LocationEditTextPreference_minLength, DEFAULT_LOCATION_MIN_LENGTH);
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void showDialog(final Bundle state) {
        super.showDialog(state);

        EditText editText = getEditText();
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                Dialog dialog = getDialog();
                if (dialog instanceof AlertDialog) {
                    AlertDialog alertDialog = (AlertDialog) dialog;
                    Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    positiveButton.setEnabled(s.length() >= minLength);
                }
            }
        });
    }
}
