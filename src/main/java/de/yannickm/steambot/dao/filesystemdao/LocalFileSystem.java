package de.yannickm.steambot.dao.filesystemdao;

import org.assertj.core.util.Files;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalFileSystem implements FileSystem {

    private final String root = "C:/steambot/filesystem/";

    public LocalFileSystem() {

    }

    @Override
    public void saveFile(String path, String content) throws IOException {

        File dir = new File(root + path);
        if (!dir.exists()) {
            dir.getParentFile().mkdirs();
        }
        FileWriter myWriter = new FileWriter(root + path);
        myWriter.write(content);
        myWriter.close();
        System.out.println("Successfully wrote to the file.");

    }

    @Override
    public List<String> getFileNames(String directory) {
        File folder = new File(root + directory);
        File[] listOfFiles = folder.listFiles();

        List<String> fileNames = new ArrayList<>();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("File " + listOfFiles[i].getName());
                fileNames.add(directory +"/" +listOfFiles[i].getName());
            }
        }

        return fileNames;
    }

    @Override
    public void deleteFile(String path) throws IOException {
        File myObj = new File(root + path);
        if (myObj.delete()) {
            System.out.println("Deleted the file: " + myObj.getName());
        } else {
            throw new IOException("Failed to delete the file: " + root + path);
        }
    }

    @Override
    public String getContent(String path) {
        return Files.contentOf(new File(root + path), "utf-8");
    }
}
