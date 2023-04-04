package de.yannickm.steambot.dao.filesystemdao;

import java.io.IOException;
import java.util.List;

public interface FileSystem {
    void saveFile(String path, String content) throws IOException;

    List<String> getFileNames(String directory);

    void deleteFile(String path) throws IOException;

    String getContent(String path);
}
