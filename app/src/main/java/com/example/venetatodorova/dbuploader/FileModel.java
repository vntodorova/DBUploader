package com.example.venetatodorova.dbuploader;

import java.io.File;

class FileModel {
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

    boolean setChecked() {
        return isChecked;
    }

    public File getFile() {
        return file;
    }

    void setChecked(boolean checked) {
        this.isChecked = checked;
    }

}
