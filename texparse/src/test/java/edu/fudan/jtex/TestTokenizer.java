package edu.fudan.jtex;

import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;

public class TestTokenizer {
    private static ArrayList<String> tokenize(String[] lines) {
        InMemoryTexSource source = new InMemoryTexSource(lines);
        Tokenizer tokenizer = new Tokenizer(source);
        ArrayList<String> tokens = new ArrayList<String>();
        String token;
        while ((token = tokenizer.next()) != null) {
            tokens.add(token);
        }
        return tokens;
    }

    private static void check(ArrayList<String> actualTokens, String[] expectedTokens) {
        int size = actualTokens.size();
        Assert.assertEquals(expectedTokens.length, size);
        for (int i = 0; i < size; i++) {
            Assert.assertEquals(expectedTokens[i], actualTokens.get(i));
        }
    }

    /**
     * Check splitting by whitespaces.
     */
    @Test
    public void testSepSpaces() {
        ArrayList<String> result = tokenize(new String[] {"\tHello \n"});
        check(result, new String[] {"\t", "Hello", " ", "\n"});
    }

    /**
     * Check splitting by braces (not including &quot;()&quot;)
     */
    @Test
    public void testBraces() {
        ArrayList<String> result = tokenize(new String[] {"[1{2}[3]]\n"});
        String[] expected = {"[", "1", "{", "2", "}", "[", "3", "]", "]", "\n"};
        check(result, expected);
    }

    @Test
    public void testLineBreak() {
        ArrayList<String> result = tokenize(new String[] {"{1\n", "2}\n"});
        String[] expected = {"{", "1", "\n", "2", "}", "\n"};
        check(result, expected);
    }

    /**
     * Again, the format of a comment token is &quot;%.*\n&quot;.
     */
    @Test
    public void testComment() {
        ArrayList<String> result = tokenize(new String[] {"Hello% world\n"});
        check(result, new String[] {"Hello", "% world\n"});
    }

    @Test
    public void testBackslash() {
        /* backslash: distinguish quoting & command token. */
        ArrayList<String> result = tokenize(new String[] {"\\[a\\b\\c d\\]\n"});
        String[] expected = {"\\[", "a", "\\b", "\\c", " ", "d", "\\]", "\n"};
        check(result, expected);
    }

    @Test(timeout = 1000)
    public void testRealWorld1() {
        /* a commonly seen paragraph in LaTex. */
        String[] doc = {"\\par This is a \\LaTex \\citep{texbook} paragraph.\n"};
        ArrayList<String> result = tokenize(doc);
        /**
         * The word &quot;paragraph&quot; and the following dot is treated
         * as one token. However, this is not a bug (I did not tokenize
         * finely enough, sry).
         */
        String[] expected = {"\\par", " ",       "This", " ",       "is", " ", "a",          " ", "\\LaTex",
                             " ",     "\\citep", "{",    "texbook", "}",  " ", "paragraph.", "\n"};
        check(result, expected);
    }

    @Test(timeout = 1000)
    public void testRealWorld2() {
        /* a not-so-trivial math environ. */
        String[] doc = {"\\[E_v=\\frac{1}{2}mv^2\\]\n"};
        String[] expected = {"\\[", "E_v=", "\\frac", "{", "1", "}", "{", "2", "}", "mv^2", "\\]", "\n"};
        ArrayList<String> result = tokenize(doc);
        check(result, expected);
    }
}
