package dev.smarttv.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Minimal ad-free TV home: grid of launchable apps. Long-press OK = app info/uninstall. */
public class HomeActivity extends Activity {

    private static class Entry {
        String label;
        String pkg;
        Intent intent;
        android.graphics.drawable.Drawable icon;
    }

    private final List<Entry> entries = new ArrayList<>();
    private GridAdapter adapter;

    private final BroadcastReceiver pkgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            load();
        }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_home);
        GridView grid = findViewById(R.id.grid);
        adapter = new GridAdapter();
        grid.setAdapter(adapter);
        grid.setOnItemClickListener((p, v, pos, id) -> launch(entries.get(pos)));
        grid.setOnItemLongClickListener((p, v, pos, id) -> {
            Entry e = entries.get(pos);
            if (e.pkg != null) {
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + e.pkg)));
            }
            return true;
        });

        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_PACKAGE_ADDED);
        f.addAction(Intent.ACTION_PACKAGE_REMOVED);
        f.addAction(Intent.ACTION_PACKAGE_CHANGED);
        f.addDataScheme("package");
        registerReceiver(pkgReceiver, f);
        load();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(pkgReceiver);
        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        GridView grid = findViewById(R.id.grid);
        grid.setSelection(0);
    }

    @Override
    public void onBackPressed() {
        // Home screen: swallow back.
    }

    private void launch(Entry e) {
        try {
            startActivity(e.intent);
        } catch (Exception ex) {
            Toast.makeText(this, "Cannot open " + e.label, Toast.LENGTH_SHORT).show();
        }
    }

    private void load() {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> list = new ArrayList<>(query(pm, Intent.CATEGORY_LEANBACK_LAUNCHER));
        // Include phone-style apps (sideloaded) that lack a leanback entry.
        for (ResolveInfo r : query(pm, Intent.CATEGORY_LAUNCHER)) {
            boolean dup = false;
            for (ResolveInfo x : list) {
                if (x.activityInfo.packageName.equals(r.activityInfo.packageName)) {
                    dup = true;
                    break;
                }
            }
            if (!dup) list.add(r);
        }
        entries.clear();
        for (ResolveInfo r : list) {
            String pkg = r.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            Entry e = new Entry();
            e.pkg = pkg;
            e.label = String.valueOf(r.loadLabel(pm));
            e.icon = r.loadIcon(pm);
            Intent in = pm.getLeanbackLaunchIntentForPackage(pkg);
            if (in == null) in = pm.getLaunchIntentForPackage(pkg);
            if (in == null) continue;
            e.intent = in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            entries.add(e);
        }
        Collections.sort(entries, (a, b) -> a.label.compareToIgnoreCase(b.label));

        Entry s = new Entry();
        s.label = "Settings";
        s.intent = new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        s.icon = getDrawable(android.R.drawable.ic_menu_preferences);
        entries.add(s);
        adapter.notifyDataSetChanged();
    }

    private static List<ResolveInfo> query(PackageManager pm, String category) {
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(category);
        return pm.queryIntentActivities(i, 0);
    }

    private class GridAdapter extends BaseAdapter {
        @Override public int getCount() { return entries.size(); }
        @Override public Object getItem(int p) { return entries.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int pos, View v, ViewGroup parent) {
            if (v == null) {
                v = LayoutInflater.from(HomeActivity.this).inflate(R.layout.item_app, parent, false);
                v.setBackgroundResource(R.drawable.item_bg);
            }
            Entry e = entries.get(pos);
            ((ImageView) v.findViewById(R.id.icon)).setImageDrawable(e.icon);
            ((TextView) v.findViewById(R.id.label)).setText(e.label);
            return v;
        }
    }
}
