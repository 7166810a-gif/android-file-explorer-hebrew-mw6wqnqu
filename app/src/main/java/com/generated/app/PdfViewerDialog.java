package com.generated.app;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class PdfViewerDialog extends Dialog {

    private final File pdfFile;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private ParcelFileDescriptor pfd;
    private int pageIndex = 0;

    private ImageView ivPdfPage;
    private TextView tvPageInfo;

    public PdfViewerDialog(Context context, File pdfFile) {
        super(context, android.R.style.Theme_Light_NoTitleBar_Fullscreen);
        this.pdfFile = pdfFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(getContext());
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFFF1F5F9);

        tvPageInfo = new TextView(getContext());
        tvPageInfo.setText(pdfFile.getName());
        tvPageInfo.setPadding(24, 24, 24, 24);
        tvPageInfo.setTextSize(16);
        tvPageInfo.setTextColor(0xFF1E293B);
        layout.addView(tvPageInfo);

        ivPdfPage = new ImageView(getContext());
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        ivPdfPage.setLayoutParams(params);
        layout.addView(ivPdfPage);

        android.widget.LinearLayout navBar = new android.widget.LinearLayout(getContext());
        navBar.setOrientation(android.widget.LinearLayout.HORIZONTAL);
        navBar.setGravity(android.view.Gravity.CENTER);

        Button btnPrev = new Button(getContext());
        btnPrev.setText("הקודם");
        btnPrev.setOnClickListener(v -> showPage(pageIndex - 1));
        navBar.addView(btnPrev);

        Button btnNext = new Button(getContext());
        btnNext.setText("הבא");
        btnNext.setOnClickListener(v -> showPage(pageIndex + 1));
        navBar.addView(btnNext);

        Button btnClose = new Button(getContext());
        btnClose.setText("סגור");
        btnClose.setOnClickListener(v -> dismiss());
        navBar.addView(btnClose);

        layout.addView(navBar);
        setContentView(layout);

        initPdfRenderer();
    }

    private void initPdfRenderer() {
        try {
            pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfRenderer = new PdfRenderer(pfd);
            showPage(0);
        } catch (Exception e) {
            Toast.makeText(getContext(), "שגיאה בטעינת קובץ PDF", Toast.LENGTH_SHORT).show();
            dismiss();
        }
    }

    private void showPage(int index) {
        if (pdfRenderer == null || index < 0 || index >= pdfRenderer.getPageCount()) return;

        if (currentPage != null) {
            currentPage.close();
        }

        pageIndex = index;
        currentPage = pdfRenderer.openPage(pageIndex);

        Bitmap bitmap = Bitmap.createBitmap(currentPage.getWidth(), currentPage.getHeight(), Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        ivPdfPage.setImageBitmap(bitmap);

        tvPageInfo.setText(pdfFile.getName() + " (" + (pageIndex + 1) + " / " + pdfRenderer.getPageCount() + ")");
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if (currentPage != null) currentPage.close();
            if (pdfRenderer != null) pdfRenderer.close();
            if (pfd != null) pfd.close();
        } catch (Exception ignored) {}
    }
}
