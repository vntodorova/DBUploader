package com.example.venetatodorova.dbuploader;

import java.io.File;

public class FileModel {
    private String name;
    private File file;
    private boolean isChecked;

    FileModel(String name, File file, boolean value) {
        this.name = name;
        this.file = file;
        this.isChecked = value;
    }

    public String getName() {
        return name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public File getFile() {
        return file;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void isChecked(boolean checked) {
        this.isChecked = checked;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
