package com.generated.app;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;

public class CryptoDialog {

    public interface OnCryptoCompleteListener {
        void onComplete();
    }

    public static void showEncrypt(Context context, File file, OnCryptoCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("הצפנת קובץ (AES-256)");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etPassword = new EditText(context);
        etPassword.setHint("הזן סיסמת הצפנה");
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPassword);

        builder.setView(layout);

        builder.setPositiveButton("הצפן", (dialog, which) -> {
            String pwd = etPassword.getText().toString();
            if (pwd.isEmpty()) {
                Toast.makeText(context, "חובה להזין סיסמה", Toast.LENGTH_SHORT).show();
                return;
            }
            File outFile = new File(file.getParentFile(), file.getName() + ".enc");
            boolean success = CryptoUtils.encryptFile(file, outFile, pwd);
            if (success) {
                Toast.makeText(context, "הקובץ הוצפן בהצלחה: " + outFile.getName(), Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onComplete();
            } else {
                Toast.makeText(context, "שגיאה בהצפנת הקובץ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    public static void showDecrypt(Context context, File file, OnCryptoCompleteListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("פענוח קובץ (AES-256)");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        final EditText etPassword = new EditText(context);
        etPassword.setHint("הזן סיסמת פענוח");
        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPassword);

        builder.setView(layout);

        builder.setPositiveButton("פענח", (dialog, which) -> {
            String pwd = etPassword.getText().toString();
            if (pwd.isEmpty()) {
                Toast.makeText(context, "חובה להזין סיסמה", Toast.LENGTH_SHORT).show();
                return;
            }
            String outName = file.getName().replace(".enc", "");
            if (outName.equals(file.getName())) outName = "decrypted_" + file.getName();
            File outFile = new File(file.getParentFile(), outName);
            boolean success = CryptoUtils.decryptFile(file, outFile, pwd);
            if (success) {
                Toast.makeText(context, "הקובץ פוענח בהצלחה", Toast.LENGTH_SHORT).show();
                if (listener != null) listener.onComplete();
            } else {
                Toast.makeText(context, "שגיאה בפענוח הקובץ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("ביטול", null);
        builder.show();
    }
}
