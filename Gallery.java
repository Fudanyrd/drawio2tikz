/* not a package */

import edu.fudan.drawio2tikz.Color;
import edu.fudan.drawio2tikz.Context;
import edu.fudan.drawio2tikz.Geometry;
import edu.fudan.drawio2tikz.TikzGen;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This script does the following:
 *
 * 1. find all drawio pictures from glob "resources/*.drawio" (one of them is resources/$filename.tex);
 * 2. foreach of the pictures, generate tikz code into resources/$filename.tex;
 * 3. generate a tex document at resources/gallery.tex, which
 *   includes all generated tikzpictures.
 * - When resources/$filename.png exists, include two subfigures
 *   , with $filename.png on the left and generated tikz on the right side.
 * - else, only insert generated tikz from resources/$filename.tex
 * 4. if pdflatex is detected, compile resources/gallery.tex into resources/gallery.pdf.
 *
 * <h2>Invocation</h2>
 * First build the package via `mvn package`, then run the script via
 * `java -classpath target/classes Gallery.java`.
 */
public class Gallery {

    private static List<Path> findDrawioFiles() {
        List<Path> drawioFiles = new ArrayList<>();
        try {
            Stream<Path> paths = Files.walk(FileSystems.getDefault().getPath("resources"));
            for (Path path : (Iterable<Path>)paths::iterator) {
                if (path.toString().endsWith(".drawio")) {
                    drawioFiles.add(path);
                }
            }
            paths.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return drawioFiles;
    }

    private static String generateGallery(List<Path> drawioFiles, Context context) {
        StringBuilder sb = new StringBuilder();

        for (Path drawioFile : drawioFiles) {
            String filename = drawioFile.getFileName().toString();
            String nameWithoutExt = filename.substring(0, filename.lastIndexOf('.'));
            String tikzFilename = "resources/" + nameWithoutExt + ".tex";
            String pngFilename = "resources/" + nameWithoutExt + ".png";

            /* generate resources/$filename.tex */
            try {
                TikzGen tikzGen = TikzGen.fromFile(drawioFile, context);
                String tikzCode = tikzGen.generateTikz();
                Files.write(FileSystems.getDefault().getPath(tikzFilename), tikzCode.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            sb.append("\\begin{figure}[ht]\n");
            if (java.nio.file.Files.exists(FileSystems.getDefault().getPath(pngFilename))) {
                sb.append("\\begin{subfigure}{0.5\\textwidth}\n")
                    .append("\\includegraphics[width=\\linewidth]{")
                    .append(pngFilename)
                    .append("}\n")
                    .append("\\caption{Original image}\n")
                    .append("\\end{subfigure}\n");
            }
            sb.append("\\begin{subfigure}{0.5\\textwidth}\n")
                .append("\\resizebox{\\textwidth}{!}{%\n")
                .append("\\input{")
                .append(tikzFilename)
                .append("}\n")
                .append("}%\n")
                .append("\\caption{Generated TikZ code}\n")
                .append("\\end{subfigure}\n");
            sb.append("\\caption{Comparison for ").append(nameWithoutExt).append("}\n");
            sb.append("\\end{figure}\n\n");
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        Context ctx = new Context();
        List<Path> drawioFiles = findDrawioFiles();
        String galleryCode = generateGallery(drawioFiles, ctx);

        sb.append("\\documentclass{article}\n");
        sb.append("\\usepackage{tikz}\n");
        sb.append("\\usepackage{graphicx}\n");
        sb.append("\\usepackage{subcaption}\n");
        sb.append("\\title{Drawio to TikZ Gallery}\n");
        sb.append("\\begin{document}\n");
        sb.append("\\maketitle\n");

        /* Load required tikz libraries. */
        for (String tikzLib : ctx.tikzLibraries) {
            sb.append("\\usetikzlibrary{").append(tikzLib).append("}\n");
        }

        /**
         * Generate "\\definecolor" commands for all colors used in generated tikz code.
         *
         * The colors are stored in the context object, and the generator will automatically add colors to the context
         * when processing geometries, so users only need to load the colors defined in the context.
         */
        for (Color color : ctx.colors) {
            sb.append("\\definecolor{")
                .append(color.uniqueName())
                .append("}{HTML}{")
                .append(color.toString())
                .append("}")
                .append("\n");
        }
        sb.append('\n');
        sb.append(galleryCode);
        sb.append("\\end{document}\n");

        try {
            Files.write(FileSystems.getDefault().getPath("resources/gallery.tex"), sb.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        /* compile to gallery.pdf */
        try {
            Process process =
                new ProcessBuilder("pdflatex", "-halt-on-error", "-output-directory=resources", "resources/gallery.tex")
                    .inheritIO()
                    .start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }
}
