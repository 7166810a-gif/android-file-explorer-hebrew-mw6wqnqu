package com.generated.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class ImageViewerDialog extends Dialog {

    private final File imageFile;
    private ImageView ivPhoto;
    private TextView tvTitle;
    private float currentRotation = 0f;

    public ImageViewerDialog(Context context, File imageFile) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        this.imageFile = imageFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF000000);

        tvTitle = new TextView(getContext());
        tvTitle.setText(imageFile.getName());
        tvTitle.setTextColor(0xFFFFFFFF);
        tvTitle.setTextSize(16);
        tvTitle.setPadding(24, 24, 24, 24);
        layout.addView(tvTitle);

        ivPhoto = new ImageView(getContext());
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        ivPhoto.setLayoutParams(params);
        ivPhoto.setScaleType(ImageView.ScaleType.FIT_CENTER);
        layout.addView(ivPhoto);

        android.widget.LinearLayout btnBar = new android.widget.LinearLayout(getContext());
        btnBar.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        btnBar.setPadding(16, 16, 16, 16);

        Button btnRotate = new Button(getContext());
        btnRotate.setText("סובב");
        btnRotate.setOnClickListener(v -> {
            currentRotation += 90f;
            ivPhoto.setRotation(currentRotation);
        });
        btnBar.addView(btnRotate);

        Button btnClose = new Button(getContext());
        btnClose.setText("סגור");
        btnClose.setOnClickListener(v -> dismiss());
        btnBar.addView(btnClose);

        layout.addView(btnBar);
        setContentView(layout);

        loadImage();
    }

    private void loadImage() {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            if (bitmap != null) {
                ivPhoto.setImageBitmap(bitmap);
            } else {
                Toast.makeText(getContext(), "שגיאה שטעינת התמונה", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "שגיאה שטעינת התמונה", Toast.LENGTH_SHORT).show();
        }
    }
}
