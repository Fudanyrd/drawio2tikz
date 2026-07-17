package edu.fudan.jtex;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class ExpandInclude extends DocRewriter {
    private ArrayList<String> includePaths = new ArrayList<String>();

    @Override
    public void parseArgs(String[] args) throws Exception {
        for (String arg : args) {
            if (arg.startsWith("--add-include=")) {
                String path = arg.substring("--add-include=".length());
                includePaths.add(path);
            }
        }
    }

    private static boolean isFile(String path) {
        boolean exists = false;
        try {
            (new FileReader(path)).close();
            exists = true;
        } catch (FileNotFoundException _ex) {
            exists = false;
        } catch (IOException _ex) {
            exists = false;
        }
        return exists;
    }

    private String searchFile(String file) {
        for (String path : includePaths) {
            String fullPath = path + "/" + file;
            if (isFile(fullPath)) {
                return fullPath;
            }
        }

        if (isFile(file)) {
            return file;
        }
        return null;
    }

    private void expand(String file, ArrayList<NodeBase> afterExpansion) {
        if (!file.endsWith(".tex")) {
            file += ".tex";
        }
        file = searchFile(file);
        if (file == null) {
            throw new RuntimeException("Cannot find file: " + file);
        }

        TexFile texFile = new TexFile(file);
        NodeBase document = (new Parser(texFile)).output;
        (new ExpandInclude()).recurse(document);
        assert document instanceof ContainerNode;
        for (NodeBase child : document.children) {
            afterExpansion.add(child);
        }
    }

    @Override
    public boolean rewrite(SmallStack<NodeBase> context) {
        boolean overwrite = false;
        NodeBase top = context.top();
        if (top.children == null) {
            return false;
        }
        ArrayList<NodeBase> afterExpansion = new ArrayList<>();

        for (NodeBase child : top.children) {
            if (!(child instanceof CommandNode)) {
                afterExpansion.add(child);
                continue;
            }
            CommandNode cmd = (CommandNode)child;
            if (!cmd.name.equals("\\include") && !cmd.name.equals("\\input")) {
                afterExpansion.add(child);
                continue;
            }

            if (cmd.arguments == null || cmd.arguments.size() != 1) {
                throw new RuntimeException("Should have only one argument: " + cmd);
            }

            overwrite = true;
            String file = cmd.arguments.get(0).inner.toString().trim();
            expand(file, afterExpansion);
        }

        if (overwrite) {
            top.children = afterExpansion;
        }

        return true;
    }
}
