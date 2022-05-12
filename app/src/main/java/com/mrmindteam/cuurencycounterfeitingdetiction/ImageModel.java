package com.mrmindteam.cuurencycounterfeitingdetiction;

public class ImageModel {
    
    int id;
    String filePath;

    public ImageModel(int id, String filePath) {
        this.id = id;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String toString() {
        return "ImageModel{" +
                "id=" + id +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
