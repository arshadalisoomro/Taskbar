package com.farmerbb.taskbar.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.farmerbb.taskbar.BuildConfig;
import com.farmerbb.taskbar.util.AppEntry;
import com.farmerbb.taskbar.util.Blacklist;
import com.farmerbb.taskbar.util.BlacklistEntry;
import com.farmerbb.taskbar.util.PinnedBlockedApps;
import com.farmerbb.taskbar.util.SavedWindowSizes;
import com.farmerbb.taskbar.util.SavedWindowSizesEntry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class SendSettingsReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Ignore this broadcast if this is the paid version
        if(BuildConfig.APPLICATION_ID.equals(BuildConfig.BASE_APPLICATION_ID)) {
            Intent sendSettingsIntent = new Intent("com.farmerbb.taskbar.SEND_SETTINGS");

            // Get pinned and blocked apps
            PinnedBlockedApps pba = PinnedBlockedApps.getInstance(context);
            List<AppEntry> pinnedAppsList = pba.getPinnedApps();

            String[] pinnedAppsPackageNames = new String[pinnedAppsList.size()];
            String[] pinnedAppsComponentNames = new String[pinnedAppsList.size()];
            String[] pinnedAppsLabels = new String[pinnedAppsList.size()];

            for(int i = 0; i < pinnedAppsList.size(); i++) {
                AppEntry entry = pinnedAppsList.get(i);
                pinnedAppsPackageNames[i] = entry.getPackageName();
                pinnedAppsComponentNames[i] = entry.getComponentName();
                pinnedAppsLabels[i] = entry.getLabel();
            }

            sendSettingsIntent.putExtra("pinned_apps_package_names", pinnedAppsPackageNames);
            sendSettingsIntent.putExtra("pinned_apps_component_names", pinnedAppsComponentNames);
            sendSettingsIntent.putExtra("pinned_apps_labels", pinnedAppsLabels);

            List<AppEntry> blockedAppsList = pba.getBlockedApps();

            String[] blockedAppsPackageNames = new String[blockedAppsList.size()];
            String[] blockedAppsComponentNames = new String[blockedAppsList.size()];
            String[] blockedAppsLabels = new String[blockedAppsList.size()];

            for(int i = 0; i < blockedAppsList.size(); i++) {
                AppEntry entry = blockedAppsList.get(i);
                blockedAppsPackageNames[i] = entry.getPackageName();
                blockedAppsComponentNames[i] = entry.getComponentName();
                blockedAppsLabels[i] = entry.getLabel();
            }

            sendSettingsIntent.putExtra("blocked_apps_package_names", blockedAppsPackageNames);
            sendSettingsIntent.putExtra("blocked_apps_component_names", blockedAppsComponentNames);
            sendSettingsIntent.putExtra("blocked_apps_labels", blockedAppsLabels);

            // Get blacklist
            Blacklist blacklist = Blacklist.getInstance(context);
            List<BlacklistEntry> blacklistList = blacklist.getBlockedApps();

            String[] blacklistPackageNames = new String[blacklistList.size()];
            String[] blacklistLabels = new String[blacklistList.size()];

            for(int i = 0; i < blacklistList.size(); i++) {
                BlacklistEntry entry = blacklistList.get(i);
                blacklistPackageNames[i] = entry.getPackageName();
                blacklistLabels[i] = entry.getLabel();
            }

            sendSettingsIntent.putExtra("blacklist_package_names", blacklistPackageNames);
            sendSettingsIntent.putExtra("blacklist_labels", blacklistLabels);

            // Get saved window sizes
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                SavedWindowSizes savedWindowSizes = SavedWindowSizes.getInstance(context);
                List<SavedWindowSizesEntry> savedWindowSizesList = savedWindowSizes.getSavedWindowSizes();

                String[] savedWindowSizesComponentNames = new String[savedWindowSizesList.size()];
                String[] savedWindowSizesWindowSizes = new String[savedWindowSizesList.size()];

                for(int i = 0; i < savedWindowSizesList.size(); i++) {
                    SavedWindowSizesEntry entry = savedWindowSizesList.get(i);
                    savedWindowSizesComponentNames[i] = entry.getComponentName();
                    savedWindowSizesWindowSizes[i] = entry.getWindowSize();
                }

                sendSettingsIntent.putExtra("saved_window_sizes_component_names", savedWindowSizesComponentNames);
                sendSettingsIntent.putExtra("saved_window_sizes_window_sizes", savedWindowSizesWindowSizes);
            }

            // Get shared preferences
            StringBuilder preferences = new StringBuilder("");

            try {
                File file = new File(context.getFilesDir().getParent() + "/shared_prefs/" + BuildConfig.APPLICATION_ID + "_preferences.xml");
                FileInputStream input = new FileInputStream(file);
                InputStreamReader reader = new InputStreamReader(input);
                BufferedReader buffer = new BufferedReader(reader);

                String line = buffer.readLine();
                while(line != null) {
                    preferences.append(line);
                    line = buffer.readLine();
                    if(line != null)
                        preferences.append("\n");
                }

                reader.close();
            } catch (IOException e) { /* Gracefully fail */ }

            sendSettingsIntent.putExtra("preferences", preferences.toString());

            // Finally, send the broadcast
            context.sendBroadcast(sendSettingsIntent);
        }
    }
}
