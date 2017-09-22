package tcd.training.com.calendar.ReminderTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import tcd.training.com.calendar.MainApplication;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Settings.LocaleHelper;
import tcd.training.com.calendar.Utils.PreferenceUtils;

/**
 * Created by cpu10661-local on 9/15/17.
 */

public class EventPopup extends Activity {

    public static final String ARG_EVENT_TITLE = "eventTitle";
    public static final String ARG_EVENT_MESSAGE = "eventMessage";

    private String mCurLanguage = PreferenceUtils.getLanguage(MainApplication.getContext());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String title = getIntent().getStringExtra(ARG_EVENT_TITLE);
        String message = getIntent().getStringExtra(ARG_EVENT_MESSAGE);

        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setIcon(R.mipmap.ic_launcher);
        alertDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        alertDialog.show();
    }

    @Override
    public Resources getResources() {
        Context context = LocaleHelper.getContext(this, mCurLanguage);
        return context == null ? super.getResources() : context.getResources();
    }
}
