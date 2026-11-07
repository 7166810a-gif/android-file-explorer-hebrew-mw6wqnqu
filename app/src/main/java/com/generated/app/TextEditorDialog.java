package com.generated.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class TextEditorDialog extends Dialog {

    private final File textFile;
    private EditText etContent;

    public TextEditorDialog(Context context, File textFile) {
        super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        this.textFile = textFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText("עורך טקסט: " + textFile.getName());
        tvTitle.setTextSize(16);
        tvTitle.setPadding(0, 0, 0, 16);
        layout.addView(tvTitle);

        etContent = new EditText(getContext());
        etContent.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        etContent.setLayoutParams(params);
        layout.addView(etContent);

        android.widget.LinearLayout btnBar = new android.widget.LinearLayout(getContext());
        btnBar.setOrientation(android.widget.LinearLayout.HORIZONTAL);

        Button btnSave = new Button(getContext());
        btnSave.setText("שמור");
        btnSave.setOnClickListener(v -> saveFile());
        btnBar.addView(btnSave);

        Button btnClose = new Button(getContext());
        btnClose.setText("סגור");
        btnClose.setOnClickListener(v -> dismiss());
        btnBar.addView(btnClose);

        layout.addView(btnBar);
        setContentView(layout);

        loadFile();
    }

    private void loadFile() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            etContent.setText(sb.toString());
        } catch (Exception e) {
            Toast.makeText(getContext(), "שגיאה בקריאת הקובץ", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveFile() {
        try {
            FileOutputStream fos = new FileOutputStream(textFile);
            fos.write(etContent.getText().toString().getBytes(StandardCharsets.UTF_8));
            fos.close();
            Toast.makeText(getContext(), "הקובץ נשמר בהצלחה", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "שגיאה בשמירת הקובץ", Toast.LENGTH_SHORT).show();
        }
    }
}
