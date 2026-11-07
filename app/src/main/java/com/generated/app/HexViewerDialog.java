package com.generated.app;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

public class HexViewerDialog extends Dialog {

    private final File file;

    public HexViewerDialog(Context context, File file) {
        super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        this.file = file;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText("מציג HEX: " + file.getName());
        tvTitle.setTextSize(16);
        tvTitle.setPadding(0, 0, 0, 16);
        layout.addView(tvTitle);

        ScrollView scrollView = new ScrollView(getContext());
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        scrollView.setLayoutParams(params);

        TextView tvHex = new TextView(getContext());
        tvHex.setTypeface(android.graphics.Typeface.MONOSPACE);
        tvHex.setTextSize(12);
        scrollView.addView(tvHex);

        layout.addView(scrollView);

        Button btnClose = new Button(getContext());
        btnClose.setText("סגור");
        btnClose.setOnClickListener(v -> dismiss());
        layout.addView(btnClose);

        setContentView(layout);

        loadHexData(tvHex);
    }

    private void loadHexData(TextView tvHex) {
        new Thread(() -> {
            StringBuilder sb = new StringBuilder();
            try {
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[16];
                int bytesRead;
                int offset = 0;
                int maxBytes = 4096; // limit view to first 4KB for performance
                int totalRead = 0;

                while ((bytesRead = fis.read(buffer)) != -1 && totalRead < maxBytes) {
                    sb.append(String.format(Locale.US, "%08X: ", offset));
                    for (int i = 0; i < 16; i++) {
                        if (i < bytesRead) {
                            sb.append(String.format(Locale.US, "%02X ", buffer[i]));
                        } else {
                            sb.append("   ");
                        }
                    }
                    sb.append(" |");
                    for (int i = 0; i < bytesRead; i++) {
                        char c = (char) buffer[i];
                        if (c >= 32 && c <= 126) {
                            sb.append(c);
                        } else {
                            sb.append('.');
                        }
                    }
                    sb.append("|\n");
                    offset += bytesRead;
                    totalRead += bytesRead;
                }
                fis.close();
            } catch (Exception e) {
                sb.append("שגיאה בטעינת HEX");
            }

            final String result = sb.toString();
            tvHex.post(() -> tvHex.setText(result));
        }).start();
    }
}
