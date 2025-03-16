/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.wakamiti.api.cli;


import java.io.*;
import java.util.*;

import static es.wakamiti.api.cli.Util.*;


/**
 * A formatter of help messages for command line options.
 * <p>
 * Example:
 * </p>
 * <pre>{@code
 * Options options = new Options();
 * options.addOption("f", "file", true, "The file to be processed");
 * options.addOption("v", "version", false, "Print the version of the application");
 * options.addOption("h", "help", false, "Show this help screen");
 *
 * HelpFormatter formatter = new HelpFormatter();
 * formatter.printHelp("myapp [options]", options);
 * }</pre>
 * <p>
 * This produces the following output:
 * </p>
 * <pre>{@code
 * Usage: myapp [options]
 * Do something useful with an input file
 *
 *  -f,--file <FILE>   The file to be processed
 *  -h,--help          Show this help screen
 *  -v,--version       Print the version of the application
 * }</pre>
 */
public class HelpFormatter {

    private static final int DEFAULT_WIDTH = 74;
    private static final int DEFAULT_LEFT_PAD = 1;
    private static final int DEFAULT_DESC_PAD = 3;
    private static final String DEFAULT_SYNTAX_PREFIX = "Usage: ";
    private static final String DEFAULT_OPT_PREFIX = "-";
    private static final String DEFAULT_LONG_OPT_PREFIX = "--";
    private static final String DEFAULT_ARG_NAME = "ARG";

    private final PrintWriter printWriter;

    protected Comparator<Option> optionComparator = new OptionComparator();

    public HelpFormatter() {
        this(createDefaultPrintWriter());
    }

    public HelpFormatter(final PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    private static PrintWriter createDefaultPrintWriter() {
        return new PrintWriter(System.out);
    }

    /**
     * Appends the usage clause for an Option to a StringBuffer.
     *
     * @param buff   the StringBuffer to append to
     * @param option the Option to append
     */
    private void appendOption(final StringBuilder buff, final Option option) {
        buff.append("[");
        if (option.getOpt() != null) {
            buff.append(DEFAULT_OPT_PREFIX).append(option.getOpt());
        } else {
            buff.append(DEFAULT_LONG_OPT_PREFIX).append(option.getLongOpt());
        }
        // if the Option has a value and a non blank argname
        if (option.hasArg() && (option.getArgName() == null || !option.getArgName().isEmpty())) {
            buff.append(SP);
            buff.append("<").append(option.getArgName() != null ? option.getArgName() : getArgName()).append(">");
        }
        buff.append("]");
    }

    /**
     * Renders the specified Options and return the rendered Options in a
     * StringBuffer.
     *
     * @param sb      The StringBuffer to place the rendered Options into.
     * @param width   The number of characters to display per line
     * @param options The command line Options
     * @param leftPad the number of characters of padding to be prefixed to each
     *                line
     * @param descPad the number of characters of padding to be prefixed to each
     *                description line
     *
     * @return the StringBuffer with the rendered Options contents.
     *
     * @throws IOException if an I/O error occurs.
     */
    <A extends Appendable> A appendOptions(
            final A sb,
            final int width,
            final Options options,
            final int leftPad,
            final int descPad
    ) throws IOException {
        final String lpad = createPadding(leftPad);
        final String dpad = createPadding(descPad);
        // first create list containing only <lpad>-a,--aaa where
        // -a is opt and --aaa is long opt; in parallel look for
        // the longest opt string this list will be then used to
        // sort options ascending
        int max = 0;
        final List<StringBuilder> prefixList = new ArrayList<>();
        final List<Option> optList = options.helpOptions();
        if (getOptionComparator() != null) {
            optList.sort(getOptionComparator());
        }
        for (final Option option : optList) {
            final StringBuilder optBuf = new StringBuilder();
            if (option.getOpt() == null) {
                optBuf.append(lpad).append("   ").append(getLongOptPrefix()).append(option.getLongOpt());
            } else {
                optBuf.append(lpad).append(getOptPrefix()).append(option.getOpt());
                if (option.hasLongOpt()) {
                    optBuf.append(", ").append(getLongOptPrefix()).append(option.getLongOpt());
                }
            }
            if (option.hasArg()) {
                final String argName = option.getArgName();
                optBuf.append(SP);
                if (argName == null || !argName.isEmpty()) {
                    optBuf.append("<").append(argName != null ? option.getArgName() : getArgName()).append(">");
                }
            }

            prefixList.add(optBuf);
            max = Math.max(optBuf.length(), max);
        }
        final int nextLineTabStop = max + descPad;

        int x = 0;
        for (final Iterator<Option> it = optList.iterator(); it.hasNext(); ) {
            final Option option = it.next();
            final StringBuilder optBuf = new StringBuilder(prefixList.get(x++).toString());
            if (optBuf.length() < max) {
                optBuf.append(createPadding(max - optBuf.length()));
                optBuf.append(createPadding(max - optBuf.length()));
            }
            optBuf.append(dpad);

            if (option.getDescription() != null) {
                optBuf.append(option.getDescription());
            }
            appendWrappedText(sb, width, nextLineTabStop, optBuf.toString());
            if (it.hasNext()) {
                sb.append(getNewLine());
            }
        }
        return sb;
    }

    /**
     * Renders the specified text and return the rendered Options in a
     * StringBuffer.
     *
     * @param <A>             The Appendable implementation.
     * @param appendable      The StringBuffer to place the rendered text into.
     * @param width           The number of characters to display per line
     * @param nextLineTabStop The position on the next line for the first tab.
     * @param text            The text to be rendered.
     *
     * @throws IOException if an I/O error occurs.
     */
    <A extends Appendable> void appendWrappedText(
            final A appendable,
            final int width,
            final int nextLineTabStop,
            final String text
    ) throws IOException {
        String render = text;
        int nextLineTabStopPos = nextLineTabStop;
        int pos = findWrapPos(render, width, 0);
        if (pos == -1) {
            appendable.append(rtrim(render));
        }
        appendable.append(rtrim(render.substring(0, pos))).append(getNewLine());
        if (nextLineTabStopPos >= width) {
            // stops infinite loop happening
            nextLineTabStopPos = 1;
        }
        // all following lines must be padded with nextLineTabStop space characters
        final String padding = createPadding(nextLineTabStopPos);
        while (true) {
            render = padding + render.substring(pos).trim();
            pos = findWrapPos(render, width, 0);
            if (pos == -1) {
                appendable.append(render);
                return;
            }
            if (render.length() > width && pos == nextLineTabStopPos - 1) {
                pos = width;
            }
            appendable.append(rtrim(render.substring(0, pos))).append(getNewLine());
        }
    }

    /**
     * Creates a String of padding of length {@code len}.
     *
     * @param len The length of the String of padding to create.
     *
     * @return The String of padding
     */
    protected String createPadding(
            final int len
    ) {
        return String.valueOf(SP).repeat(len);
    }

    /**
     * Finds the next text wrap position after {@code startPos} for the text in
     * {@code text} with the column width {@code width}. The wrap point is the
     * last position before startPos+width having a whitespace character (space,
     * \n, \r). If there is no whitespace character before startPos+width, it
     * will return startPos+width.
     *
     * @param text     The text being searched for the wrap position
     * @param width    width of the wrapped text
     * @param startPos position from which to start the lookup whitespace
     *                 character
     *
     * @return position on which the text must be wrapped or -1 if the wrap
     * position is at the end of the text
     */
    protected int findWrapPos(
            final String text,
            final int width,
            final int startPos
    ) {
        // the line ends before the max wrap pos or a new line char found
        int pos = text.indexOf(LF, startPos);
        if (pos != -1 && pos <= width) {
            return pos + 1;
        }
        pos = text.indexOf(TAB, startPos);
        if (pos != -1 && pos <= width) {
            return pos + 1;
        }
        if (startPos + width >= text.length()) {
            return -1;
        }
        // look for the last whitespace character before startPos+width
        for (pos = startPos + width; pos >= startPos; --pos) {
            final char c = text.charAt(pos);
            if (c == SP || c == LF || c == CR) {
                break;
            }
        }
        // if we found it - just return
        if (pos > startPos) {
            return pos;
        }
        // if we didn't find one, simply chop at startPos+width
        pos = startPos + width;
        return pos == text.length() ? -1 : pos;
    }

    /**
     * Gets the 'argName'.
     *
     * @return the 'argName'
     */
    public String getArgName() {
        return DEFAULT_ARG_NAME;
    }

    /**
     * Gets the 'descPadding'.
     *
     * @return the 'descPadding'
     */
    public int getDescPadding() {
        return DEFAULT_DESC_PAD;
    }

    /**
     * Gets the 'leftPadding'.
     *
     * @return the 'leftPadding'
     */
    public int getLeftPadding() {
        return DEFAULT_LEFT_PAD;
    }

    /**
     * Gets the 'longOptPrefix'.
     *
     * @return the 'longOptPrefix'
     */
    public String getLongOptPrefix() {
        return DEFAULT_LONG_OPT_PREFIX;
    }

    /**
     * Gets the 'newLine'.
     *
     * @return the 'newLine'
     */
    public String getNewLine() {
        return System.lineSeparator();
    }

    /**
     * Comparator used to sort the options when they output in help text.
     * Defaults to case-insensitive alphabetical sorting by option key.
     *
     * @return the {@link Comparator} currently in use to sort the options
     */
    public Comparator<Option> getOptionComparator() {
        return optionComparator;
    }

    /**
     * Gets the 'optPrefix'.
     *
     * @return the 'optPrefix'
     */
    public String getOptPrefix() {
        return DEFAULT_OPT_PREFIX;
    }

    /**
     * Gets the 'syntaxPrefix'.
     *
     * @return the 'syntaxPrefix'
     */
    public String getSyntaxPrefix() {
        return DEFAULT_SYNTAX_PREFIX;
    }

    /**
     * Gets the 'width'.
     *
     * @return the 'width'
     */
    public int getWidth() {
        return DEFAULT_WIDTH;
    }

    /**
     * Prints the help for {@code options} with the specified command line
     * syntax. This method prints help information to {@link System#out} by default.
     *
     * @param width         the number of characters to be displayed on each
     *                      line
     * @param cmdLineSyntax the syntax for this application
     * @param header        the banner to display at the beginning of the help
     * @param options       the Options instance
     * @param footer        the banner to display at the end of the help
     * @param autoUsage     whether to print an automatically generated usage
     *                      statement
     */
    public void printHelp(
            final int width,
            final String cmdLineSyntax,
            final String header,
            final Options options,
            final String footer,
            final boolean autoUsage
    ) {
        final PrintWriter pw = new PrintWriter(printWriter);
        printHelp(pw, width, cmdLineSyntax, header, options, getLeftPadding(), getDescPadding(), footer, autoUsage);
        pw.flush();
    }

    /**
     * Prints the help for {@code options} with the specified command line
     * syntax.
     *
     * @param pw            the writer to which the help will be written
     * @param width         the number of characters to be displayed on each
     *                      line
     * @param cmdLineSyntax the syntax for this application
     * @param header        the banner to display at the beginning of the help
     * @param options       the Options instance
     * @param leftPad       the number of characters of padding to be prefixed
     *                      to each line
     * @param descPad       the number of characters of padding to be prefixed
     *                      to each description line
     * @param footer        the banner to display at the end of the help
     * @param autoUsage     whether to print an automatically generated usage
     *                      statement
     *
     * @throws IllegalStateException if there is no room to print a line
     */
    public void printHelp(
            final PrintWriter pw,
            final int width,
            final String cmdLineSyntax,
            final String header,
            final Options options,
            final int leftPad,
            final int descPad,
            final String footer,
            final boolean autoUsage
    ) {
        if (cmdLineSyntax == null || cmdLineSyntax.isEmpty()) {
            throw new IllegalArgumentException("cmdLineSyntax not provided");
        }
        if (autoUsage) {
            printUsage(pw, width, cmdLineSyntax, options);
        } else {
            printUsage(pw, width, cmdLineSyntax);
        }
        if (header != null && !header.isEmpty()) {
            printWrapped(pw, width, header);
        }
        printOptions(pw, width, options, leftPad, descPad);
        if (footer != null && !footer.isEmpty()) {
            printWrapped(pw, width, footer);
        }
    }

    /**
     * Prints the help for {@code options} with the specified command line
     * syntax. This method prints help information to {@link System#out} by
     * default.
     *
     * @param cmdLineSyntax the syntax for this application
     * @param options       the Options instance
     */
    public void printHelp(
            final String cmdLineSyntax,
            final Options options
    ) {
        printHelp(getWidth(), cmdLineSyntax, null, options, null, false);
    }

    /**
     * Prints the help for the specified Options to the specified writer, using
     * the specified width, left padding, and description padding.
     *
     * @param pw      The printWriter to write the help to
     * @param width   The number of characters to display per line
     * @param options The command line Options
     * @param leftPad the number of characters of padding to be prefixed to each
     *                line
     * @param descPad the number of characters of padding to be prefixed to each
     *                description line
     */
    public void printOptions(
            final PrintWriter pw,
            final int width,
            final Options options,
            final int leftPad,
            final int descPad
    ) {
        try {
            pw.println(appendOptions(new StringBuilder(), width, options, leftPad, descPad));
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Prints the cmdLineSyntax to the specified writer, using the specified
     * width.
     *
     * @param pw            The printWriter to write the help to
     * @param width         The number of characters per line for the usage
     *                      statement.
     * @param cmdLineSyntax The usage statement.
     */
    public void printUsage(
            final PrintWriter pw,
            final int width,
            final String cmdLineSyntax
    ) {
        final int argPos = cmdLineSyntax.indexOf(SP) + 1;
        printWrapped(pw, width, getSyntaxPrefix().length() + argPos, getSyntaxPrefix() + cmdLineSyntax);
    }

    /**
     * Prints the usage statement for the specified application.
     *
     * @param pw      The PrintWriter to print the usage statement
     * @param width   The number of characters to display per line
     * @param app     The application name
     * @param options The command line Options
     */
    public void printUsage(
            final PrintWriter pw,
            final int width,
            final String app,
            final Options options
    ) {
        // initialize the string buffer
        final StringBuilder buff = new StringBuilder(getSyntaxPrefix()).append(app).append(SP);

        final List<Option> optList = new ArrayList<>(options.getOptions());
        if (getOptionComparator() != null) {
            optList.sort(getOptionComparator());
        }
        // iterate over the options
        for (final Iterator<Option> it = optList.iterator(); it.hasNext(); ) {
            // get the next Option
            final Option option = it.next();
            appendOption(buff, option);

            if (it.hasNext()) {
                buff.append(SP);
            }
        }

        // call printWrapped
        printWrapped(pw, width, buff.toString().indexOf(SP) + 1, buff.toString());
    }

    /**
     * Prints the specified text to the specified PrintWriter.
     *
     * @param pw              The printWriter to write the help to
     * @param width           The number of characters to display per line
     * @param nextLineTabStop The position on the next line for the first tab.
     * @param text            The text to be written to the PrintWriter
     */
    public void printWrapped(
            final PrintWriter pw,
            final int width,
            final int nextLineTabStop,
            final String text
    ) {
        pw.println(renderWrappedTextBlock(new StringBuilder(text.length()), width, nextLineTabStop, text));
    }

    /**
     * Prints the specified text to the specified PrintWriter.
     *
     * @param pw    The printWriter to write the help to
     * @param width The number of characters to display per line
     * @param text  The text to be written to the PrintWriter
     */
    public void printWrapped(
            final PrintWriter pw,
            final int width,
            final String text
    ) {
        printWrapped(pw, width, 0, text);
    }

    /**
     * Renders the specified text width a maximum width. This method doesn't
     * remove leading spaces after a new line.
     *
     * @param appendable      The StringBuffer to place the rendered text into.
     * @param width           The number of characters to display per line
     * @param nextLineTabStop The position on the next line for the first tab.
     * @param text            The text to be rendered.
     */
    private <A extends Appendable> A renderWrappedTextBlock(
            final A appendable,
            final int width,
            final int nextLineTabStop,
            final String text
    ) {
        try (final BufferedReader in = new BufferedReader(new StringReader(text))){
            String line;
            boolean firstLine = true;
            while ((line = in.readLine()) != null) {
                if (!firstLine) {
                    appendable.append(getNewLine());
                } else {
                    firstLine = false;
                }
                appendWrappedText(appendable, width, nextLineTabStop, line);
            }
        } catch (final IOException ignore) { // NOPMD
            // cannot happen
        }
        return appendable;
    }

    /**
     * Removes the trailing whitespace from the specified String.
     *
     * @param s The String to remove the trailing padding from.
     *
     * @return The String of without the trailing padding
     */
    protected String rtrim(
            final String s
    ) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        int pos = s.length();
        while (pos > 0 && Character.isWhitespace(s.charAt(pos - 1))) {
            --pos;
        }
        return s.substring(0, pos);
    }

    /**
     * This class implements the {@code Comparator} interface for comparing Options.
     */
    private static final class OptionComparator implements Comparator<Option>, Serializable {

        @Serial
        private static final long serialVersionUID = 5305467873966684014L;

        /**
         * Compares its two arguments for order. Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.
         *
         * @param opt1 The first Option to be compared.
         * @param opt2 The second Option to be compared.
         *
         * @return a negative integer, zero, or a positive integer as the first
         * argument is less than, equal to, or greater than the second.
         */
        @Override
        public int compare(
                final Option opt1,
                final Option opt2
        ) {
            return opt1.getKey().compareToIgnoreCase(opt2.getKey());
        }
    }

}
