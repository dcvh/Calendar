package tcd.training.com.calendar.ReminderTask;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 9/15/17.
 */

public class EventPopup extends Activity {

    public static final String ARG_EVENT_TITLE = "eventTitle";
    public static final String ARG_EVENT_MESSAGE = "eventMessage";

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
}
