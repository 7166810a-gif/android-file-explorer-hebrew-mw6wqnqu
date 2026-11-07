package com.generated.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SearchDialog extends Dialog {

    public interface OnFileSelectedListener {
        void onFileSelected(File file);
    }

    private final File rootDir;
    private final OnFileSelectedListener listener;
    private EditText etSearchQuery;
    private ListView lvResults;
    private final List<File> resultFiles = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final List<String> resultNames = new ArrayList<>();

    public SearchDialog(Context context, File rootDir, OnFileSelectedListener listener) {
        super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        this.rootDir = rootDir;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(24, 24, 24, 24);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText("חיפוש מתקדם");
        tvTitle.setTextSize(20);
        tvTitle.setTextStyle(android.graphics.Typeface.BOLD);
        layout.addView(tvTitle);

        etSearchQuery = new EditText(getContext());
        etSearchQuery.setHint("הקלד שם קובץ לחיפוש...");
        layout.addView(etSearchQuery);

        Button btnSearch = new Button(getContext());
        btnSearch.setText("חפש");
        btnSearch.setOnClickListener(v -> performSearch());
        layout.addView(btnSearch);

        lvResults = new ListView(getContext());
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        lvResults.setLayoutParams(params);
        layout.addView(lvResults);

        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, resultNames);
        lvResults.setAdapter(adapter);

        lvResults.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < resultFiles.size()) {
                if (listener != null) listener.onFileSelected(resultFiles.get(position));
                dismiss();
            }
        });

        Button btnClose = new Button(getContext());
        btnClose.setText("סגור");
        btnClose.setOnClickListener(v -> dismiss());
        layout.addView(btnClose);

        setContentView(layout);
    }

    private void performSearch() {
        String query = etSearchQuery.getText().toString().trim().toLowerCase();
        if (query.isEmpty()) return;

        resultFiles.clear();
        resultNames.clear();

        new Thread(() -> {
            List<File> allFiles = new ArrayList<>();
            StorageUtils.scanMediaFiles(rootDir, null, allFiles, 5);

            for (File f : allFiles) {
                if (f.getName().toLowerCase().contains(query)) {
                    resultFiles.add(f);
                    resultNames.add(f.getName() + "\n" + f.getAbsolutePath());
                }
            }

            lvResults.post(() -> adapter.notifyDataSetChanged());
        }).start();
    }
}
