package main.java.penny.util;

import java.io.PrintStream;
import java.util.List;

/**
 * ProgressBar represents a task-completeness visualizer that displays the amount of progress a given task
 * has made in a user-configured format in-place.  ProgressBars function similar to classic "loading bars" and recreates
 * this effect in a provided output.  Users can update the ProgressBar every time a step of progress has been made
 * and request that the current progress meter be displayed to system output at any time.  The choice and granularity
 * of a ProgressBar is completely configurable and user-definable.
 *
 * ProgressBars generally take the form of:
 * title: [#####     ] unique
 * where this represents an instance of progress bar with 10 tokens, start symbol: "[", end symbol "[", progress
 * token: "#", and gap token " ".  This progress bar example shows 50% completeness so far.  Note that the title
 * and unique tokens on either side of the progress meter are optional and configurable.
 */
public class ProgressBar {

    /** Default starting symbol for ProgressBars unless specified */
    public static String DEFAULT_START_SYMBOL = "[";

    /** Default progress token for ProgressBars unless specified */
    public static String DEFAULT_PROGRESS_TOKEN = "#";

    /** Default gap token for ProgressBars unless specified */
    public static String DEFAULT_GAP_TOKEN = " ";

    /** Default end symbol for ProgressBars unless specified */
    public static String DEFAULT_END_SYMBOL = "]";

    /** The current progress of this progress bar */
    private int progress;

    /** The total amount of progress that can be made before this progress meter is filled */
    private final int total;

    /** The granularity of displayed progress tokens */
    private final int numberOfBars;

    /** The prepended title to this progress bar */
    private final String title;

    /** The starting symbol of the progress meter */
    private final String startSymbol;

    /** The progress token conveying completeness */
    private final String progressToken;

    /** The gap token conveying incompleteness */
    private final String gapToken;

    /** The ending symbol of the progress meter */
    private final String endSymbol;

    /** The list of unique tokens corresponding to a 1:1 relationship with progress appended to the bar */
    private final List<String> uniqueTokens;

    /** The dedicated thread for displaying the progress bar to a provided output */
    private Thread displayThread;

    /**
     * Constructs a new progress bar with all the configurations provided.
     *
     * @param total The total amount of progress that can be made until this progress bar is complete
     * @param numberOfBars The granularity of displayed progress tokens
     * @param startSymbol The choice of starting symbol of the progress meter
     * @param progressToken The choice of progress token conveying completeness
     * @param gapToken The choice of gap token conveying incompleteness
     * @param endSymbol The choice of ending symbol of the progress meter
     * @param uniqueTokens The unique tokens with a 1:1 relationship with progress appended to the bar
     * @param title The choice of title prepended to the progress bar
     */
    private ProgressBar(int total, int numberOfBars, String startSymbol, String progressToken, String gapToken,
                        String endSymbol, List<String> uniqueTokens, String title) {
        this.progress = 0;

        this.total = total;
        this.numberOfBars = numberOfBars;

        this.title = title;
        this.startSymbol = startSymbol;
        this.progressToken = progressToken;
        this.gapToken = gapToken;
        this.endSymbol = endSymbol;
        this.uniqueTokens = uniqueTokens;
    }

    /**
     * Increments the progress of this progress bar.  Represents to a single step of progress that has been made
     * in the corresponding program execution.  Updates this progress bar to reflect the increased completeness
     * of the desired task.  This method is thread-safe.
     */
    public synchronized void increment() {
        this.progress++;
    }

    /**
     * Displays the current progress bar visualization to standard system output.
     */
    public synchronized void display() {
        display(System.out);
    }

    /**
     * Displays the current progress bar visualization to the provided output.
     * @param out The output to visualize this progress bar to
     */
    public synchronized void display(PrintStream out) {
        if (this.displayThread == null || !this.displayThread.isAlive()) {
            this.displayThread = new Thread(() -> out.print(this.toString() + "\r")); // out.print(this.toString() + "\r"));
            this.displayThread.start();
        }
    }

    /**
     * Constructs the current visual representation of the progress bar based on the amount of
     * progress that has been made.
     *
     * @return A String representing the current progress bar visualization
     */
    private String getProgressBar() {
        double percentComplete = (double) this.progress / this.total;

        int barCount = (int) Math.ceil(this.numberOfBars * percentComplete);

        StringBuilder builder = new StringBuilder();

        // Prepend the title to this progress bar if one exists, otherwise ignore
        if (this.title != null) {
            builder.append(this.title);
            builder.append(": ");
        }

        builder.append(this.startSymbol);

        // Include progress and gap tokens based on the percent progress that has been made
        for (int i = 0; i < Math.min(barCount, this.numberOfBars); i++) {
            builder.append(this.progressToken);
        }
        for (int i = barCount; i < this.numberOfBars; i++) {
            builder.append(this.gapToken);
        }
        builder.append(this.endSymbol);

        // If unique tokens were provided, append the next unique token based on the amount of progress
        // that has been made so far.
        builder.append(" ");
        if (this.uniqueTokens != null && this.progress < this.uniqueTokens.size()) {
            builder.append(this.uniqueTokens.get(this.progress));
        }

        return builder.toString();
    }

    /**
     * Resets this progress bar.  Specifically, resets the progress made by this progress bar to 0 or the
     * start of the original task.
     */
    public void reset() {
        this.progress = 0;
    }

    @Override
    public String toString() {
        return this.getProgressBar();
    }

    /**
     * ProgressBarBuilder represents a builder for configuring and creating a new instance of ProgressBar.
     */
    public static class ProgressBarBuilder {

        /** The current configured total amount of progress that can be made before the progress meter is filled */
        private int total;

        /** The current configured granularity of displayable progress tokens */
        private int numberOfBars;

        /** The current configured prepended title of the progress bar */
        private String title;

        /** The current configured starting symbol of the progress meter */
        private String startSymbol;

        /** The current configured progress token of the progress meter */
        private String progressToken;

        /** The current configured gap token of the progress meter */
        private String gapToken;

        /** The current configured end symbol of the progress meter */
        private String endSymbol;

        /** The current configured list of unique tokens of the progress bar */
        private List<String> uniqueTokens;

        /**
         * Creates a new ProgressBarBuilder with default initialized progress meter symbols.  Default
         * initialization sets the total amount of progress to 100 and number of bars to 10 for the progress bar.
         */
        public ProgressBarBuilder() {
            this.total = 100;
            this.numberOfBars = 10;
            this.startSymbol = DEFAULT_START_SYMBOL;
            this.progressToken = DEFAULT_PROGRESS_TOKEN;
            this.gapToken = DEFAULT_GAP_TOKEN;
            this.endSymbol = DEFAULT_END_SYMBOL;
        }

        /**
         * Sets the total progress of the to-be-built ProgressBar to the total specified.
         * Configures the current total to be the value provided to be used when building and
         * finalizing the new ProgressBar.
         *
         * @param total The total amount of progress that can be made before the progress bar is full
         * @return This ProgressBarBuilder for chaining
         */
        public ProgressBarBuilder withTotal(int total) {
            this.total = total;
            return this;
        }

        /**
         * Sets the number of bars granularity of the to-be-built ProgressBar to the amount specified.
         * Configures the current number of bars to be the value provided to be used when building and
         * finalizing the new ProgressBar.
         *
         * @param numberOfBars The numberOfBars that can be used to display the completeness of the progress meter
         * @return This ProgressBarBuilder for chaining
         */
        public ProgressBarBuilder withNumberOfBars(int numberOfBars) {
            this.numberOfBars = numberOfBars;
            return this;
        }

        /**
         * Sets the title of the to-be-built ProgressBar to the title specified.
         * Configures the current title to be the String provided to be used when building and
         * finalizing the new ProgressBar.
         *
         * @param title The title to prepend to the progress bar
         * @return This ProgressBarBuilder for chaining
         */
        public ProgressBarBuilder withTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Sets the start symbol of the to-be-built ProgressBar to the symbol specified.
         * Configures the current symbol to be the String provided to be used when building and
         * finalizing the new ProgressBar.
         *
         * @param startSymbol The symbol to use as the start of the progress meter
         * @return This ProgressBarBuilder for chaining
         */
        public ProgressBarBuilder withStartSymbol(String startSymbol) {
            this.startSymbol = startSymbol;
            return this;
        }

        /**
         * Sets the end symbol of the to-be-built ProgressBar to the symbol specified.
         * Configures the current symbol to be the String provided to be used when building and
         * finalizing the new ProgressBar.
         *
         * @param endSymbol The symbol to use as the end of the progress meter
         * @return This ProgressBarBuilder for chaining
         */
        public ProgressBarBuilder withEndSymbol(String endSymbol) {
            this.endSymbol = endSymbol;
            return this;
        }


        /**
         * Sets the progress token of the to-be-built ProgressBar to the token specified.
         * Configures the current token to be the String provided to be used when building and
         * finalizing the new ProgressBar.
         *
         * @param progressToken The token to use as the indicator of completeness in the progress meter
         * @return This ProgressBarBuilder for chaining
         */
        public ProgressBarBuilder withProgressToken(String progressToken) {
            this.progressToken = progressToken;
            return this;
        }

        /**
         * Sets the gap token of the to-be-built ProgressBar to the token specified.
         * Configures the current token to be the String provided to be used when building and
         * finalizing the new ProgressBar.
         *
         * @param gapToken The token to use as the indicator of incompleteness in the progress meter
         * @return This ProgressBarBuilder for chaining
         */
        public ProgressBarBuilder withGapToken(String gapToken) {
            this.gapToken = gapToken;
            return this;
        }

        /**
         * Sets the unique tokens of the to-be-built ProgressBar to the tokens provided.
         * Configures the unique tokens to be the list of Strings provided to be used when building and
         * finalizing the new ProgressBar.  Assumes there are the same number of tokens as total progress
         * possible in this progress bar.
         *
         * @param uniqueTokens The list of String tokens with a 1:1 relationship with progress in this progress bar
         * @return This ProgressBarBuilder for chaining
         */
        public ProgressBarBuilder withUniqueTokens(List<String> uniqueTokens) {
            this.uniqueTokens = uniqueTokens;
            return this;
        }

        /**
         * Builds a new instance of ProgressBar based on the configured values previously defined in this builder.
         * If the configured parameters are not compatible with one another for a valid functioning progress bar,
         * throws an IllegalStateException instead.
         *
         * @return A ProgressBar with the specified and configured values from this builder
         * @throws IllegalStateException If the configured values are not compatible with one another i.e.
         *                               unique tokens size is not the same as the total progress
         *                               The number of bars exceeds the total amount of possible progress
         */
        public ProgressBar build() {
            if (this.uniqueTokens != null && this.uniqueTokens.size() != this.total) {
                throw new IllegalStateException("Total must be equal to the number of unique tokens when specified." +
                        "  Total: " + this.total + " and number unique tokens: " + this.uniqueTokens.size());
            }

            if (this.numberOfBars > this.total) {
                throw new IllegalStateException("Number of bars must be less than the total.  Bars: " +
                        this.numberOfBars + " and total: " + this.total);
            }

            return new ProgressBar(this.total, this.numberOfBars, this.startSymbol, this.progressToken, this.gapToken,
                    this.endSymbol, this.uniqueTokens, this.title);
        }
    }
}
