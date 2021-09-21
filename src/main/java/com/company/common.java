package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class common {
    public static String readPasswordFromFile(String path) throws FileNotFoundException {
        Scanner sc = new Scanner(new File(path));
        return sc.nextLine();
    }
}
