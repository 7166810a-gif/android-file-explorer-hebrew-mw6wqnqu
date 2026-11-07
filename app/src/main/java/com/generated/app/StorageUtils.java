package com.generated.app;

import android.os.Environment;
import android.os.StatFs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageUtils {

    public static class StorageVolumeInfo {
        public String name;
        public File path;
        public long totalBytes;
        public long freeBytes;

        public StorageVolumeInfo(String name, File path, long totalBytes, long freeBytes) {
            this.name = name;
            this.path = path;
            this.totalBytes = totalBytes;
            this.freeBytes = freeBytes;
        }

        public long getUsedBytes() {
            return totalBytes - freeBytes;
        }
    }

    public static List<StorageVolumeInfo> getStorageVolumes() {
        List<StorageVolumeInfo> volumes = new ArrayList<>();
        File internal = Environment.getExternalStorageDirectory();
        if (internal != null && internal.exists()) {
            StatFs stat = new StatFs(internal.getPath());
            long total = stat.getTotalBytes();
            long free = stat.getAvailableBytes();
            volumes.add(new StorageVolumeInfo("אחסון פנימי", internal, total, free));
        }

        File storageDir = new File("/storage");
        if (storageDir.exists() && storageDir.isDirectory()) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory() && !f.getName().equals("emulated") && !f.getName().equals("self")) {
                        try {
                            StatFs stat = new StatFs(f.getPath());
                            long total = stat.getTotalBytes();
                            long free = stat.getAvailableBytes();
                            if (total > 0) {
                                volumes.add(new StorageVolumeInfo("כרטיס זיכרון / USB (" + f.getName() + ")", f, total, free));
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
        return volumes;
    }

    public static void scanMediaFiles(File dir, FileItem.Category targetCat, List<File> resultList, int maxDepth) {
        if (maxDepth < 0 || dir == null || !dir.canRead()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory() && !f.getName().startsWith(".")) {
                    scanMediaFiles(f, targetCat, resultList, maxDepth - 1);
                } else if (f.isFile()) {
                    if (targetCat == null || FileUtils.getFileCategory(f) == targetCat) {
                        resultList.add(f);
                    }
                }
            }
        }
    }
}
