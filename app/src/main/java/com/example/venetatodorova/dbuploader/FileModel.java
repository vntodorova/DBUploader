package com.example.venetatodorova.dbuploader;

import java.io.File;

class FileModel {
    private String name;
    private File file;
    private boolean isChecked;

    FileModel(String name, File file, boolean isChecked) {
        this.name = name;
        this.file = file;
        this.isChecked = isChecked;
    }

    public String getName() {
        return name;
    }

    boolean getChecked() {
        return isChecked;
    }

    public File getFile() {
        return file;
    }

    void setChecked(boolean checked) {
        this.isChecked = checked;
    }

}
