package org.academic.cli;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class InputHandler {
    private InputHandler() {
    }

    // read csv file
    public static String[] readCsvFile(String filePath) {
        String[] lines = null;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            lines = br.lines().toArray(String[]::new);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }


}
