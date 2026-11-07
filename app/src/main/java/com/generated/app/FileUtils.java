package com.generated.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    public static FileItem.Category getFileCategory(File file) {
        if (file.isDirectory()) {
            return FileItem.Category.FOLDER;
        }
        String ext = getExtension(file.getName()).toLowerCase(Locale.ROOT);
        switch (ext) {
            case "jpg": case "jpeg": case "png": case "gif": case "bmp": case "webp": case "svg":
                return FileItem.Category.IMAGE;
            case "mp4": case "mkv": case "avi": case "mov": case "wmv": case "flv": case "3gp":
                return FileItem.Category.VIDEO;
            case "mp3": case "wav": case "ogg": case "flac": case "m4a": case "aac":
                return FileItem.Category.AUDIO;
            case "pdf":
                return FileItem.Category.PDF;
            case "txt": case "java": case "kt": case "xml": case "json": case "html": case "css":
            case "js": case "py": case "c": case "cpp": case "h": case "md": case "sh":
                return FileItem.Category.CODE;
            case "zip": case "rar": case "7z": case "tar": case "gz":
                return FileItem.Category.ARCHIVE;
            case "apk":
                return FileItem.Category.APK;
            case "doc": case "docx": case "xls": case "xlsx": case "ppt": case "pptx":
                return FileItem.Category.DOCUMENT;
            default:
                return FileItem.Category.OTHER;
        }
    }

    public static String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        if (idx > 0 && idx < fileName.length() - 1) {
            return fileName.substring(idx + 1);
        }
        return "";
    }

    public static String formatSize(long sizeBytes) {
        if (sizeBytes <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(sizeBytes) / Math.log10(1024));
        if (digitGroups >= units.length) digitGroups = units.length - 1;
        return String.format(Locale.getDefault(), "%.1f %s", sizeBytes / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    public static String formatDate(long timeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    public static boolean copyFile(File src, File dst) {
        try {
            if (src.isDirectory()) {
                if (!dst.exists()) dst.mkdirs();
                File[] files = src.listFiles();
                if (files != null) {
                    for (File f : files) {
                        copyFile(f, new File(dst, f.getName()));
                    }
                }
                return true;
            } else {
                InputStream in = new FileInputStream(src);
                OutputStream out = new FileOutputStream(dst);
                byte[] buf = new byte[8192];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteRecursive(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            File[] children = fileOrDir.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return fileOrDir.delete();
    }

    public static boolean zipFiles(List<File> files, File zipFile) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
            for (File file : files) {
                zipRecursive(file, file.getName(), zos);
            }
            zos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void zipRecursive(File srcFile, String parentPath, ZipOutputStream zos) throws Exception {
        if (srcFile.isDirectory()) {
            File[] children = srcFile.listFiles();
            if (children != null) {
                for (File child : children) {
                    zipRecursive(child, parentPath + "/" + child.getName(), zos);
                }
            }
        } else {
            byte[] buf = new byte[8192];
            FileInputStream fis = new FileInputStream(srcFile);
            zos.putNextEntry(new ZipEntry(parentPath));
            int len;
            while ((len = fis.read(buf)) > 0) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
            fis.close();
        }
    }

    public static boolean unzip(File zipFile, File destDir) {
        try {
            if (!destDir.exists()) destDir.mkdirs();
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            ZipEntry ze;
            byte[] buffer = new byte[8192];
            while ((ze = zis.readNextEntry()) != null) {
                File file = new File(destDir, ze.getName());
                // Prevent Zip Slip vulnerability
                if (!file.getCanonicalPath().startsWith(destDir.getCanonicalPath())) {
                    zis.closeEntry();
                    continue;
                }
                if (ze.isDirectory()) {
                    file.mkdirs();
                } else {
                    File parent = file.getParentFile();
                    if (parent != null && !parent.exists()) parent.mkdirs();
                    FileOutputStream fos = new FileOutputStream(file);
                    int count;
                    while ((count = zis.read(buffer)) != -1) {
                        fos.write(buffer, 0, count);
                    }
                    fos.close();
                }
                zis.closeEntry();
            }
            zis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String getHash(File file, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            FileInputStream fis = new FileInputStream(file);
            byte[] byteArray = new byte[8192];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
            fis.close();
            byte[] bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "שגיאה בחישוב";
        }
    }

    public static Bitmap getImageThumbnail(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        } catch (Exception e) {
            return null;
        }
    }

    public static Bitmap getVideoFrame(File file) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(file.getAbsolutePath());
            Bitmap bmp = retriever.getFrameAtTime(1000000);
            retriever.release();
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }
}
