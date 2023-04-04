package de.yannickm.steambot.common;

import java.io.File;
import java.util.Scanner;

public class PasswordHelper {
    public static String readPasswordFromFile(String path) throws Exception {
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNext()) {
            String line = sc.next();
            if (line.indexOf("password") == 0) {
                return line.split("=")[1];
            }
        }
        throw new Exception("No password within file!");
    }
}
