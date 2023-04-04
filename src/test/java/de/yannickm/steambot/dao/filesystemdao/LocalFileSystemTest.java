package de.yannickm.steambot.dao.filesystemdao;

import junit.framework.TestCase;

import java.io.IOException;

public class LocalFileSystemTest extends TestCase {

    /**
     * saves a test file to the file system, gets a list of all files in given dir, reads in the content of the test
     * file and deletes it afterwards.
     */
    public void testSaveFile() throws IOException {
        String dir = "testClass";
        String path = dir+"/test.json";
        String content = "{\"test\":\"test\"}";

        FileSystem fileSystem = new LocalFileSystem();

        fileSystem.saveFile(path,content);
        assertEquals(1,fileSystem.getFileNames(dir).size());
        String receivedFileName = fileSystem.getFileNames(dir).get(0);
        assertEquals(path,receivedFileName);
        assertEquals(content,fileSystem.getContent(path));
        fileSystem.deleteFile(fileSystem.getFileNames(dir).get(0));
        assertEquals(0,fileSystem.getFileNames(dir).size());
    }
}