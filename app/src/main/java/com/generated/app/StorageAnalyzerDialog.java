package com.generated.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorageAnalyzerDialog extends Dialog {

    private final File rootDir;
    private TextView tvStatus;
    private ProgressBar pbScan;
    private ListView lvLargestFiles;

    public StorageAnalyzerDialog(Context context, File rootDir) {
        super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        this.rootDir = rootDir;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText("ניתוח שטח אחסון");
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(null, Typeface.BOLD);
        layout.addView(tvTitle);

        tvStatus = new TextView(getContext());
        tvStatus.setText("סורק קבצים...");
        tvStatus.setPadding(0, 16, 0, 16);
        layout.addView(tvStatus);

        pbScan = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleHorizontal);
        pbScan.setIndeterminate(true);
        layout.addView(pbScan);

        TextView tvSubtitle = new TextView(getContext());
        tvSubtitle.setText("הקבצים הגדולים ביותר:");
        tvSubtitle.setPadding(0, 16, 0, 8);
        tvSubtitle.setTypeface(null, Typeface.BOLD);
        layout.addView(tvSubtitle);

        lvLargestFiles = new ListView(getContext());
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        lvLargestFiles.setLayoutParams(params);
        layout.addView(lvLargestFiles);

        Button btnClose = new Button(getContext());
        btnClose.setText("סגור");
        btnClose.setOnClickListener(v -> dismiss());
        layout.addView(btnClose);

        setContentView(layout);

        analyzeStorage();
    }

    private void analyzeStorage() {
        new Thread(() -> {
            List<File> allFiles = new ArrayList<>();
            StorageUtils.scanMediaFiles(rootDir, null, allFiles, 4);

            Collections.sort(allFiles, (f1, f2) -> Long.compare(f2.length(), f1.length()));

            List<String> displayList = new ArrayList<>();
            int limit = Math.min(allFiles.size(), 20);
            for (int i = 0; i < limit; i++) {
                File f = allFiles.get(i);
                displayList.add((i + 1) + ". " + f.getName() + " (" + FileUtils.formatSize(f.length()) + ")");
            }

            lvLargestFiles.post(() -> {
                pbScan.setVisibility(android.view.View.GONE);
                tvStatus.setText("נמצאו " + allFiles.size() + " קבצים בסריקה.");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, displayList);
                lvLargestFiles.setAdapter(adapter);
            });
        }).start();
    }
}
