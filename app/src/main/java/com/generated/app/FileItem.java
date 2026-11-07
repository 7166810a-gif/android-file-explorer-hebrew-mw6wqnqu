package com.generated.app;

import java.io.File;

public class FileItem {
    public enum Category {
        FOLDER, IMAGE, VIDEO, AUDIO, PDF, CODE, ARCHIVE, APK, DOCUMENT, OTHER
    }

    private final File file;
    private final String name;
    private final Category category;
    private boolean selected;

    public FileItem(File file) {
        this.file = file;
        this.name = file.getName();
        this.category = FileUtils.getFileCategory(file);
        this.selected = false;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long length() {
        return file.length();
    }

    public long lastModified() {
        return file.lastModified();
    }
}
