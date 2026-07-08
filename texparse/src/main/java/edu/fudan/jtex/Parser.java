package edu.fudan.jtex;

import java.util.ArrayDeque;
import java.util.ArrayList;

/**
 * A very basic parser that only groups LaTex command- and environment-
 * definitions to create an initial document tree.
 */
public final class Parser {
    /**
     * Result of parsing.
     */
    public NodeBase output;
    private SmallStack<NodeBase> stack;

    /**
     * Some point of parsing requires looking ahead
     * some tokens, causing some tokens not handled.
     * We therefore use a FIFO to track them.
     */
    private ArrayDeque<String> pendingTokens;
    private Tokenizer tokenizer;

    /**
     * Like C/C++'s unlikely macro to indicate
     * some branch is hardly true.
     */
    private static boolean unlikely(boolean cond) { return cond; }

    private static interface TokenMatcher {
        /**
         * A pure function that controls the termination
         * of parsing. It must be <b>pure</b> in that
         * it does not have side effects and returns
         * the same value given the same token.
         *
         * @return true if parsing process should be halted.
         */
        public boolean halt(String token);
    }
    private static final class AlwaysContinueMatcher implements TokenMatcher {
        public boolean halt(String token) { return false; }
    }
    private static final class BraceMatcher implements TokenMatcher {
        final String till;
        public BraceMatcher(String rightHalf) { till = rightHalf; }
        public boolean halt(String token) { return till.equals(token); }
    }
    private static final TokenMatcher ALWAYS_CONT = new AlwaysContinueMatcher();
    private static final TokenMatcher TEXT_BLOCK_END = new BraceMatcher("}");
    private static final TokenMatcher OPT_ARG_END = new BraceMatcher("]");
    private static final TokenMatcher INLINE_MATH_END = new BraceMatcher("$");
    private static final TokenMatcher DISPLAY_MATH_END = new BraceMatcher("\\]");
    private static final TokenMatcher ENVIRON_END = new BraceMatcher("\\end");

    /**
     * This class's initialization is parsing.
     * I.e. after creating a parser object,
     * the {@code Parser#output} is set to
     * the top level of document tree.
     */
    public Parser(TexSourceBase src) {
        output = new ContainerNode();
        tokenizer = new Tokenizer(src);
        stack = new SmallStack<NodeBase>(16);
        pendingTokens = new ArrayDeque<String>();
        stack.push(output);

        /* invoke parsing. */
        this.step(ALWAYS_CONT);
    }

    private void addUnusedToken(String token) { pendingTokens.addLast(token); }

    /**
     * A small routine that adds a child {@code newNode} to the current node and
     * simultaneously push it to the stack.
     *
     * <p><b>Note</b>: this should probably be followed by a
     * {@code stack#pop} operation.</p>
     */
    private void descend(NodeBase newNode) {
        NodeBase parent = stack.top();
        if (parent.children == null) {
            parent.children = new ArrayList<NodeBase>();
        }
        parent.children.add(newNode);
        this.stack.push(newNode);
    }

    private String next() {
        if (pendingTokens.size() > 0) {
            String front = pendingTokens.pollFirst();
            return front;
        }
        String ret = tokenizer.next();
        return ret;
    }

    private void fillEnviron() {
        ArgumentBase nameArg = nextArgument();
        if (nameArg == null) {
            throw new RuntimeException("\\begin without name.");
        }
        EnvironNode env = new EnvironNode(nameArg.inner.toString(), null);

        /* Get the rest of arguments. */
        ArrayList<ArgumentBase> args = new ArrayList<ArgumentBase>();
        ArgumentBase arg = nextArgument();
        while (arg != null) {
            args.add(arg);
            arg = nextArgument();
        }
        if (args.size() > 0) {
            env.restArguments = args;
        } else {
            env.restArguments = null;
        }

        /* Parse the body of the environ node. */
        descend(env); /* Required defer operation: stack.pop */
        step(ENVIRON_END);

        /* finish. */
        stack.pop();
        nextArgument(); /* the &quot;{envname}&quot; followed by \\end. */
    }

    private void fillCommand(String name) {
        /* contract of both this method and {@code CommandNode} constructor. */
        assert name.charAt(0) == '\\';

        CommandNode cmd = new CommandNode(name, null);

        /* arguments: */
        ArrayList<ArgumentBase> args = new ArrayList<ArgumentBase>();
        ArgumentBase arg = nextArgument();
        while (arg != null) {
            args.add(arg);
            arg = nextArgument();
        }
        cmd.arguments = args;

        /* add it as a child. */
        descend(cmd);
        stack.pop();
    }

    private void handleBackslash(String firstToken) {
        assert firstToken.charAt(0) == '\\'; /* contract of this method. */
        if (firstToken.length() == 1) {
            /* possibly wrong code. Should have used double backslashes in LaTex. */
            descend(new TextNode("\\"));
            stack.pop();
            return;
        }

        /* is this a math environ? */
        if (firstToken.equals("\\[")) {
            /* Rewrite: as a displaymath environment. */
            EnvironNode current = new EnvironNode("displaymath", null);
            descend(current);
            this.step(DISPLAY_MATH_END);
            stack.pop();
            return;
        }

        /* is this a command token? */
        if (ParserConfig.isIdentifier(firstToken.charAt(1))) {
            fillCommand(firstToken);
            return;
        }

        /* a normal text node which is quoting a punctuation. */
        descend(new TextNode(firstToken));
        stack.pop();
    }

    private ArgumentBase nextArgument() {
        /**
         * Skip whitespaces/comments/line breaks till a &quot;[&quot;
         * or &quot;{&quot;
         *
         * <p>Why would someone do this (putting blanks inside a
         * command/environ, e.g.)<code>\begin{figure} [ht]</code>?
         * These have to be discarded because they does not fit
         * in a {@code EnvironNode} or {@code CommandNode}, sry.
         * </p>
         */
        String token = next();
        ArrayList<String> temporary = new ArrayList<String>();
        while (token != null && unlikely(ParserConfig.isLineBreakToken(token) ||
                                         ParserConfig.isWhitespaceToken(token) || ParserConfig.isCommentToken(token))) {
            temporary.add(token);
            token = next();
        }
        if (token == null) {
            for (int i = temporary.size() - 1; i >= 0; --i) {
                pendingTokens.addFirst(temporary.get(i));
            }
            return null;
        }
        char first = token.charAt(0);
        if (first != '{' && first != '[') {
            /* No more argument for last environ/command. */
            /* To avoid losing current token and temporary list: */
            pendingTokens.addFirst(token);
            for (int i = temporary.size() - 1; i >= 0; --i) {
                pendingTokens.addFirst(temporary.get(i));
            }
            return null;
        }

        /* Parse till the end of argument. */
        boolean isOptional = first == '[';
        ContainerNode placeholder = new ContainerNode();
        stack.push(placeholder);
        step(isOptional ? OPT_ARG_END : TEXT_BLOCK_END);
        stack.pop();

        NodeBase inner = placeholder.prune();
        ArgumentBase ret = isOptional ? new OptionalArgument(inner) : new Argument(inner);
        return ret;
    }

    private void step(TokenMatcher matcher) {
        String token = next();
        while (token != null) {
            if (matcher.halt(token)) {
                break;
            }

            /**
             * This parser does not check matching of square braces and
             * parenthesis in the document (sry).
             */
            if (token.equals("\\begin")) {
                /* handle environment node. */
                fillEnviron();
            } else if (token.charAt(0) == '\\') {
                handleBackslash(token);
            } else if (token.equals("{")) {
                /* TextBlockNode: match till a right curly brace */
                TextBlockNode tb = new TextBlockNode(null);
                descend(tb);
                step(TEXT_BLOCK_END);
                stack.pop();
            } else if (token.equals("$")) {
                /* InlineMathNode: match till $ */
                InlineMathNode current = new InlineMathNode();
                descend(current);
                step(INLINE_MATH_END);
                stack.pop();
            } else {
                /* Treat as a single text node. */
                TextNode text = new TextNode(token);
                /* leverage this.descend, and then pop out current text. */
                descend(text);
                stack.pop();
            }
            token = next();
        }
    }
}
