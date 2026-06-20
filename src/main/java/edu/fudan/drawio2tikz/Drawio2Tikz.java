package edu.fudan.drawio2tikz;

import java.nio.file.Files;
import java.nio.file.Paths;

class Drawio2Tikz {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -jar Drawio2Tikz.jar <input_file> <output_file>");
            return;
        }

        String inputFile = args[0];
        String outputFile = args[1];

        try {
            TikzGen tikzGen = TikzGen.fromFile(Paths.get(inputFile), null);
            String tikzCode = tikzGen.generateDoc();
            Files.write(Paths.get(outputFile), tikzCode.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }
}
