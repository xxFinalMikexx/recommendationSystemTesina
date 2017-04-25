package com.example.xxfin.recommendationsystemtesina.objects;

/**
 * Created by xxfin on 20/04/2017.
 */

public class Places_Object {
    private String pathToFile;
    private String nameFile;
    private String type;

    public Places_Object() {

    }

    public Places_Object(String pathToFile, String nameFile, String type) {
        this.pathToFile = pathToFile;
        this.nameFile = nameFile;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNameFile() {

        return nameFile;
    }

    public void setNameFile(String nameFile) {
        this.nameFile = nameFile;
    }

    public String getPathToFile() {

        return pathToFile;
    }

    public void setPathToFile(String pathToFile) {
        this.pathToFile = pathToFile;
    }
}
