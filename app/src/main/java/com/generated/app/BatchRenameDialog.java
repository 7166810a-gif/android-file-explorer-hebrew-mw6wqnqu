package com.generated.app;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.List;

public class BatchRenameDialog {

    public interface OnRenameCompleteListener {
        void onRenameComplete();
    }

    public static void show(Context context, List<FileItem> items, OnRenameCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("שינוי שם מרובה");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etPrefix = new EditText(context);
        etPrefix.setHint("קידומת (למשל: תמונה_)");
        layout.addView(etPrefix);

        builder.setView(layout);

        builder.setPositiveButton("אישור", (dialog, which) -> {
            String prefix = etPrefix.getText().toString().trim();
            if (prefix.isEmpty()) prefix = "file_";

            int count = 1;
            for (FileItem item : items) {
                File orig = item.getFile();
                String ext = FileUtils.getExtension(orig.getName());
                String newName = prefix + count + (ext.isEmpty() ? "" : "." + ext);
                File target = new File(orig.getParentFile(), newName);
                orig.renameTo(target);
                count++;
            }
            Toast.makeText(context, "בוצע שינוי שם ל-" + items.size() + " קבצים", Toast.LENGTH_SHORT).show();
            if (listener != null) listener.onRenameComplete();
        });

        builder.setNegativeButton("ביטול", null);
        builder.show();
    }
}
