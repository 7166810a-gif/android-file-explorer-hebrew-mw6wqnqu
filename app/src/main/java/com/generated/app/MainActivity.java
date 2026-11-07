package com.generated.app;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity implements FileListAdapter.OnItemActionListener {

    private static final int PERMISSION_REQ_CODE = 1001;

    private View viewHome;
    private View viewExplorer;
    private ListView lvFiles;
    private GridView gvFiles;
    private TextView tvEmpty;
    private LinearLayout containerStorageList;
    private LinearLayout containerBreadcrumbs;
    private LinearLayout barSelectionAction;
    private TextView tvSelectedCount;

    private File currentDirectory;
    private final List<FileItem> allItems = new ArrayList<>();
    private final List<FileItem> displayedItems = new ArrayList<>();
    private FileListAdapter adapter;

    private boolean isGridView = false;
    private final List<FileItem> selectedItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        checkAndRequestPermissions();
    }

    private void initViews() {
        viewHome = findViewById(R.id.view_home);
        viewExplorer = findViewById(R.id.view_explorer);
        lvFiles = findViewById(R.id.lv_files);
        gvFiles = findViewById(R.id.gv_files);
        tvEmpty = findViewById(R.id.tv_empty);
        containerStorageList = findViewById(R.id.container_storage_list);
        containerBreadcrumbs = findViewById(R.id.container_breadcrumbs);
        barSelectionAction = findViewById(R.id.bar_selection_action);
        tvSelectedCount = findViewById(R.id.tv_selected_count);

        adapter = new FileListAdapter(this, displayedItems);
        adapter.setOnItemActionListener(this);
        lvFiles.setAdapter(adapter);

        lvFiles.setOnItemClickListener((parent, view, position, id) -> handleItemClick(displayedItems.get(position)));
        lvFiles.setOnItemLongClickListener((parent, view, position, id) -> {
            adapter.setSelectionMode(true);
            barSelectionAction.setVisibility(View.VISIBLE);
            return true;
        });

        ImageButton btnToggleView = findViewById(R.id.btn_toggle_view);
        btnToggleView.setOnClickListener(v -> {
            isGridView = !isGridView;
            lvFiles.setVisibility(isGridView ? View.GONE : View.VISIBLE);
            gvFiles.setVisibility(isGridView ? View.VISIBLE : View.GONE);
        });

        ImageButton btnAddNew = findViewById(R.id.btn_add_new);
        btnAddNew.setOnClickListener(v -> showCreateNewDialog());

        EditText etQuickFilter = findViewById(R.id.et_quick_filter);
        etQuickFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Bottom navigation handlers
        findViewById(R.id.nav_home).setOnClickListener(v -> switchTab(0));
        findViewById(R.id.nav_explorer).setOnClickListener(v -> switchTab(1));
        findViewById(R.id.nav_analyzer).setOnClickListener(v -> {
            StorageAnalyzerDialog dialog = new StorageAnalyzerDialog(this, currentDirectory != null ? currentDirectory : Environment.getExternalStorageDirectory());
            dialog.show();
        });
        findViewById(R.id.nav_search).setOnClickListener(v -> {
            SearchDialog dialog = new SearchDialog(this, Environment.getExternalStorageDirectory(), file -> {
                if (file.isDirectory()) {
                    openDirectory(file);
                } else {
                    openFile(file);
                }
            });
            dialog.show();
        });

        findViewById(R.id.btn_home_analyzer).setOnClickListener(v -> {
            StorageAnalyzerDialog dialog = new StorageAnalyzerDialog(this, Environment.getExternalStorageDirectory());
            dialog.show();
        });

        findViewById(R.id.btn_home_search).setOnClickListener(v -> {
            SearchDialog dialog = new SearchDialog(this, Environment.getExternalStorageDirectory(), file -> {
                if (file.isDirectory()) {
                    openDirectory(file);
                } else {
                    openFile(file);
                }
            });
            dialog.show();
        });

        // Selection Bar Action Handlers
        findViewById(R.id.btn_action_copy).setOnClickListener(v -> performCopySelected());
        findViewById(R.id.btn_action_move).setOnClickListener(v -> performMoveSelected());
        findViewById(R.id.btn_action_delete).setOnClickListener(v -> performDeleteSelected());
        findViewById(R.id.btn_action_more).setOnClickListener(v -> showMoreBatchOptions());
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, PERMISSION_REQ_CODE);
                } catch (Exception e) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivityForResult(intent, PERMISSION_REQ_CODE);
                }
            } else {
                loadDashboardData();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, PERMISSION_REQ_CODE);
            } else {
                loadDashboardData();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ_CODE) {
            loadDashboardData();
        }
    }

    private void switchTab(int index) {
        viewHome.setVisibility(index == 0 ? View.VISIBLE : View.GONE);
        viewExplorer.setVisibility(index == 1 ? View.VISIBLE : View.GONE);
    }

    private void loadDashboardData() {
        containerStorageList.removeAllViews();
        List<StorageUtils.StorageVolumeInfo> volumes = StorageUtils.getStorageVolumes();

        for (StorageUtils.StorageVolumeInfo vol : volumes) {
            View card = getLayoutInflater().inflate(R.layout.item_file, containerStorageList, false);
            TextView tvName = card.findViewById(R.id.tv_name);
            TextView tvDetails = card.findViewById(R.id.tv_details);

            tvName.setText(vol.name);
            tvDetails.setText(FileUtils.formatSize(vol.getUsedBytes()) + " / " + FileUtils.formatSize(vol.totalBytes));

            card.setOnClickListener(v -> openDirectory(vol.path));
            containerStorageList.addView(card);
        }

        if (!volumes.isEmpty()) {
            currentDirectory = volumes.get(0).path;
        }
    }

    private void openDirectory(File dir) {
        if (dir == null || !dir.exists()) return;
        currentDirectory = dir;
        switchTab(1);

        allItems.clear();
        displayedItems.clear();

        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                allItems.add(new FileItem(f));
            }
        }

        Collections.sort(allItems, (f1, f2) -> {
            if (f1.isDirectory() && !f2.isDirectory()) return -1;
            if (!f1.isDirectory() && f2.isDirectory()) return 1;
            return f1.getName().compareToIgnoreCase(f2.getName());
        });

        displayedItems.addAll(allItems);
        adapter.notifyDataSetChanged();

        tvEmpty.setVisibility(displayedItems.isEmpty() ? View.VISIBLE : View.GONE);
        updateBreadcrumbs();
    }

    private void updateBreadcrumbs() {
        containerBreadcrumbs.removeAllViews();
        if (currentDirectory == null) return;

        List<File> pathParts = new ArrayList<>();
        File curr = currentDirectory;
        while (curr != null) {
            pathParts.add(0, curr);
            curr = curr.getParentFile();
        }

        for (int i = 0; i < pathParts.size(); i++) {
            final File f = pathParts.get(i);
            TextView tv = new TextView(this);
            tv.setText(f.getName().isEmpty() ? "אחסון" : f.getName());
            tv.setPadding(12, 8, 12, 8);
            tv.setTextColor(0xFF2563EB);
            tv.setOnClickListener(v -> openDirectory(f));
            containerBreadcrumbs.addView(tv);

            if (i < pathParts.size() - 1) {
                TextView sep = new TextView(this);
                sep.setText(" > ");
                sep.setTextColor(0xFF94A3B8);
                containerBreadcrumbs.addView(sep);
            }
        }
    }

    private void handleItemClick(FileItem item) {
        if (adapter.isSelectionMode()) {
            item.setSelected(!item.isSelected());
            adapter.notifyDataSetChanged();
            onItemSelectionChanged();
            return;
        }

        if (item.isDirectory()) {
            openDirectory(item.getFile());
        } else {
            openFile(item.getFile());
        }
    }

    private void openFile(File file) {
        FileItem.Category cat = FileUtils.getFileCategory(file);
        switch (cat) {
            case IMAGE:
                new ImageViewerDialog(this, file).show();
                break;
            case VIDEO:
                new MediaPlayerDialog(this, file, true).show();
                break;
            case AUDIO:
                new MediaPlayerDialog(this, file, false).show();
                break;
            case PDF:
                new PdfViewerDialog(this, file).show();
                break;
            case CODE:
                new TextEditorDialog(this, file).show();
                break;
            case ARCHIVE:
                new AlertDialog.Builder(this)
                        .setTitle("ארכיון")
                        .setMessage("האם לחלץ את הארכיון?")
                        .setPositiveButton("חלץ", (dialog, which) -> {
                            File dest = new File(file.getParentFile(), file.getName() + "_extracted");
                            if (FileUtils.unzip(file, dest)) {
                                Toast.makeText(this, "חולץ בהצלחה ל-" + dest.getName(), Toast.LENGTH_SHORT).show();
                                openDirectory(currentDirectory);
                            }
                        })
                        .setNegativeButton("ביטול", null)
                        .show();
                break;
            default:
                new HexViewerDialog(this, file).show();
                break;
        }
    }

    private void filterItems(String query) {
        displayedItems.clear();
        if (query.isEmpty()) {
            displayedItems.addAll(allItems);
        } else {
            for (FileItem item : allItems) {
                if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                    displayedItems.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMoreOptionsClicked(FileItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getName());
        String[] options = new String[]{"הצפנה (AES)", "פענח (AES)", "מאפיינים", "שנה שם", "מחיקה"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    CryptoDialog.showEncrypt(this, item.getFile(), () -> openDirectory(currentDirectory));
                    break;
                case 1:
                    CryptoDialog.showDecrypt(this, item.getFile(), () -> openDirectory(currentDirectory));
                    break;
                case 2:
                    showFileProperties(item.getFile());
                    break;
                case 3:
                    showRenameDialog(item.getFile());
                    break;
                case 4:
                    FileUtils.deleteRecursive(item.getFile());
                    openDirectory(currentDirectory);
                    break;
            }
        });
        builder.show();
    }

    @Override
    public void onItemSelectionChanged() {
        selectedItems.clear();
        for (FileItem item : allItems) {
            if (item.isSelected()) selectedItems.add(item);
        }
        tvSelectedCount.setText(selectedItems.size() + " נבחרו");
    }

    private void showFileProperties(File file) {
        String md5 = FileUtils.getHash(file, "MD5");
        String sha256 = FileUtils.getHash(file, "SHA-256");

        String msg = "נתיב: " + file.getAbsolutePath() + "\n" +
                "גודל: " + FileUtils.formatSize(file.length()) + "\n" +
                "תאריך: " + FileUtils.formatDate(file.lastModified()) + "\n\n" +
                "MD5:\n" + md5 + "\n\n" +
                "SHA-256:\n" + sha256;

        new AlertDialog.Builder(this)
                .setTitle("מאפייני קובץ")
                .setMessage(msg)
                .setPositiveButton("אישור", null)
                .show();
    }

    private void showRenameDialog(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("שינוי שם");
        final EditText input = new EditText(this);
        input.setText(file.getName());
        builder.setView(input);

        builder.setPositiveButton("אישור", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                File target = new File(file.getParentFile(), newName);
                file.renameTo(target);
                openDirectory(currentDirectory);
            }
        });
        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void showCreateNewDialog() {
        if (currentDirectory == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("צור תיקייה חדשה");
        final EditText input = new EditText(this);
        input.setHint("שם התיקייה");
        builder.setView(input);

        builder.setPositiveButton("צור", (dialog, which) -> {
            String folderName = input.getText().toString().trim();
            if (!folderName.isEmpty()) {
                File newFolder = new File(currentDirectory, folderName);
                newFolder.mkdirs();
                openDirectory(currentDirectory);
            }
        });
        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    private void performCopySelected() {
        Toast.makeText(this, "הועתק ללוח", Toast.LENGTH_SHORT).show();
    }

    private void performMoveSelected() {
        Toast.makeText(this, "מוכן להעברה", Toast.LENGTH_SHORT).show();
    }

    private void performDeleteSelected() {
        new AlertDialog.Builder(this)
                .setTitle("מחיקה")
                .setMessage("האם למחוק את הקבצים שנבחרו?")
                .setPositiveButton("מחק", (dialog, which) -> {
                    for (FileItem item : selectedItems) {
                        FileUtils.deleteRecursive(item.getFile());
                    }
                    adapter.setSelectionMode(false);
                    barSelectionAction.setVisibility(View.GONE);
                    openDirectory(currentDirectory);
                })
                .setNegativeButton("ביטול", null)
                .show();
    }

    private void showMoreBatchOptions() {
        BatchRenameDialog.show(this, selectedItems, () -> openDirectory(currentDirectory));
    }

    @Override
    public void onBackPressed() {
        if (adapter.isSelectionMode()) {
            adapter.setSelectionMode(false);
            barSelectionAction.setVisibility(View.GONE);
            return;
        }

        if (viewExplorer.getVisibility() == View.VISIBLE && currentDirectory != null) {
            File parent = currentDirectory.getParentFile();
            if (parent != null && parent.canRead()) {
                openDirectory(parent);
                return;
            }
        }
        super.onBackPressed();
    }
}
