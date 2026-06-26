package edu.fudan.jtex;

/**
 * Note that the ending '\n' should be treated (by the parser)
 * as part of the comment.
 */
public class CommentNode extends NodeBase {
    public String comment;

    CommentNode(String comment) {
        this.comment = comment;
        children = null;

        int len = comment.length();
        if (len == 0 || comment.charAt(0) != '%') {
            throw new IllegalArgumentException("Comment must start with '%'");
        }

        if (comment.charAt(len - 1) != '\n') {
            comment += '\n'; /* ensure that the comment ends with a linebreak */
        }
    }

    @Override
    public boolean allowAutoBreak() {
        return true;
    }

    @Override
    public void appendTo(FormatterInterface formatter) {
        formatter.append(comment);
    }
}
