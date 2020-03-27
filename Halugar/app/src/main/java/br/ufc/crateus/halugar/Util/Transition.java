package br.ufc.crateus.halugar.Util;

import android.app.Activity;
import android.content.Context;

import br.ufc.crateus.halugar.R;

public class Transition {

    //Overrides the pending Activity transition by performing the "Enter" animation.
    public static void enterTransition(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    //Overrides the pending Activity transition by performing the "Enter" animation.
    public static void backTransition(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }
}