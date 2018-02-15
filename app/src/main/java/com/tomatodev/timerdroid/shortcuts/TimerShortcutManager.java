package com.tomatodev.timerdroid.shortcuts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.tomatodev.timerdroid.R;
import com.tomatodev.timerdroid.Utilities;
import com.tomatodev.timerdroid.activities.TimerActivity;

import java.util.Arrays;

public class TimerShortcutManager {
    public static void storeAppShortcut(Context context, int id, String name, long length){
        if (Build.VERSION.SDK_INT >= 25){
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

            Intent intent = new Intent(Intent.ACTION_VIEW, null, context, TimerActivity.class);
            intent.putExtra("timerId", id);

            ShortcutInfo shortcut = new ShortcutInfo.Builder(context, Integer.toString(id))
                    .setShortLabel(name)
                    .setLongLabel(name + " (" + Utilities.formatTimeNoBlanksNoLeadingZeros(length) + ")")
                    .setIcon(Icon.createWithResource(context, R.drawable.ic_shortcut_history))
                    .setIntent(intent)
                    .setRank(1)
                    .build();

            shortcutManager.removeAllDynamicShortcuts();
            shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));
        }
    }
}
