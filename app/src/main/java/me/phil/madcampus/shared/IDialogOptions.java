package me.phil.madcampus.shared;

import android.content.DialogInterface;

/** Interface for confirmDialog from MainActivity Utility method **/
public interface IDialogOptions {
    void yesOption(DialogInterface dialog, int which);
    default void noOption(DialogInterface dialog, int which){
        dialog.cancel();
    }
}

