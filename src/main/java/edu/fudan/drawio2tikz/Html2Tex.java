package edu.fudan.drawio2tikz;

import java.util.Stack;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

public class Html2Tex {
    public static class StyleSheet {
        public Color textColor;
        public enum TextAlign { LEFT, CENTER, RIGHT }
        ;

        public TextAlign textAlign;
        public int fontSize;

        public String texEnvironBegin() {
            String ret = "";
            if (textAlign == TextAlign.CENTER) {
                ret += "\\begin{center}\n";
            } else if (textAlign == TextAlign.RIGHT) {
                ret += "\\begin{flushright}\n";
            } else {
                ret += "";
            }

            Color black = new Color("000000");
            if (!black.equals(textColor)) {
                ret += String.format("\\textcolor[HTML]{%s}{", textColor.toString());
            }

            if (fontSize != 12) {
                ret += String.format("\\fontsize{%d}{%d}\\selectfont ", fontSize, fontSize + 2);
            }
            return ret;
        }

        public String texEnvironAfter() {
            String ret = "";
            Color black = new Color("000000");
            if (!black.equals(textColor)) {
                ret += "}";
            }

            if (textAlign == TextAlign.CENTER) {
                ret += "\n\\end{center}\n";
            } else if (textAlign == TextAlign.RIGHT) {
                ret += "\n\\end{flushright}\n";
            } else {
                ret += "";
            }

            return ret;
        }

        public StyleSheet(String style) {
            textColor = new Color("000000");
            textAlign = TextAlign.LEFT;
            fontSize = 12;

            if (style == null || style.isEmpty()) {
                /* use default values. */
                return;
            }

            String[] styleItems = style.split(";");
            for (String item : styleItems) {
                String[] keyValue = item.split(":");
                /* strip leading and trailing whitespaces. */
                if (keyValue.length == 2) {
                    keyValue[0] = keyValue[0].trim();
                    keyValue[1] = keyValue[1].trim();
                    switch (keyValue[0]) {
                    case "color": {
                        textColor = Color.fromHTMLStyle(keyValue[1]);
                        break;
                    }
                    case "text-align": {
                        String alignStr = keyValue[1].toLowerCase();
                        switch (alignStr) {
                        case "left":
                            textAlign = TextAlign.LEFT;
                            break;
                        case "center":
                            textAlign = TextAlign.CENTER;
                            break;
                        case "right":
                            textAlign = TextAlign.RIGHT;
                            break;
                        default:
                            /* ignore invalid textAlign. */
                            break;
                        }
                        break;
                    }
                    case "font-size": {
                        String sizeStr = keyValue[1];
                        if (sizeStr.endsWith("px")) {
                            sizeStr = sizeStr.substring(0, sizeStr.length() - 2);
                        }
                        try {
                            fontSize = Integer.parseInt(sizeStr);
                        } catch (NumberFormatException e) {
                            /* ignore invalid fontSize. */
                        }
                        break;
                    }
                    default:
                        /* ignore unknown style item. */
                        break;
                    }
                } else {
                    System.err.println(String.format("Invalid style item: %s", item));
                }
            }
        }
    };

    private static class ToTexParserCallback extends HTMLEditorKit.ParserCallback {
        private StringBuilder sb;
        private Stack<StyleSheet> styleStack = new Stack<>();

        public ToTexParserCallback(StringBuilder sb) { this.sb = sb; }

        @Override
        public void handleText(char[] data, int pos) {
            if (data.length == 1 && (data[0] == ' ' || (int)data[0] == 0xa0)) {
                data[0] = '~'; /* non-breaking space in LaTeX. */
            }
            sb.append(data);
        }

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
            StyleSheet ss = new StyleSheet((String)attributes.getAttribute(HTML.Attribute.STYLE));
            styleStack.push(ss);
            sb.append(ss.texEnvironBegin());

            if (tag == HTML.Tag.B) {
                sb.append("\\textbf{");
            } else if (tag == HTML.Tag.I) {
                sb.append("\\textit{");
            }
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int pos) {
            if (tag == HTML.Tag.P || tag == HTML.Tag.SPAN) {
                sb.append("\\\\\n");
            }
            if (tag == HTML.Tag.B || tag == HTML.Tag.I) {
                sb.append("}");
            }

            if (!styleStack.isEmpty()) {
                StyleSheet ss = styleStack.peek();
                sb.append(ss.texEnvironAfter());
                styleStack.pop();
            } else {
                throw new IllegalStateException("Style stack underflow: unmatched end tag " + tag);
            }
        }

        @Override
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int pos) {
            if (tag == HTML.Tag.BR) {
                sb.append("\\\\\n");
            }
        }
    };

    public static String convert(String html) throws Exception {
        StringBuilder sb = new StringBuilder();
        ParserDelegator parserDelegator = new ParserDelegator();
        ToTexParserCallback callback = new ToTexParserCallback(sb);
        parserDelegator.parse(new java.io.StringReader(html), callback, false);
        return sb.toString();
    }
}
