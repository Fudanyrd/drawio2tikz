package edu.fudan.jtex;

import java.util.HashSet;

public class ParserConfig {
    public static final char LINE_BREAK = '\n';
    public static final char COMMENT_START = '%';

    /**
     * The tokenizer checks this set when determining
     * the start of a new token.
     *
     * <p>However, the tokenizer do has to treat &quot;\&quot;
     * specially, for we do not add this to the set (why I did this?
     * I could not remember, sry)</p>
     */
    public static final HashSet<Character> TOKEN_START = new HashSet<Character>();
    static {
        TOKEN_START.add('$');
        TOKEN_START.add('[');
        TOKEN_START.add(']');
        TOKEN_START.add('{');
        TOKEN_START.add('}');
        TOKEN_START.add(COMMENT_START);
        TOKEN_START.add(LINE_BREAK);
    }

    public static final HashSet<Character> WHITESPACES = new HashSet<Character>();
    static {
        WHITESPACES.add(' ');
        WHITESPACES.add('\t');
        WHITESPACES.add((char)0xa0); /* rare, but possible */
    }

    public static boolean isWhitespace(char c) { return WHITESPACES.contains(c); }

    public static boolean isIdentifier(char c) {
        return c == '@' || c == '_' || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') /* isalpha */
            || (c >= '0' && c <= '9');                                                  /* isdigit */
    }

    public static boolean isCommentToken(String token) { return token.charAt(0) == COMMENT_START; }
    public static boolean isWhitespaceToken(String token) { return isWhitespace(token.charAt(0)); }
    public static boolean isLineBreakToken(String token) { return token.equals(String.valueOf(LINE_BREAK)); }
}
