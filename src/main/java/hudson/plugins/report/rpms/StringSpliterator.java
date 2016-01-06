/*
 * The MIT License
 *
 * Copyright 2016 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.report.rpms;

import java.util.Spliterator;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class StringSpliterator implements Spliterator<String> {

    private final String input;
    private final String delim;
    private StringTokenizer tokenizer;

    public static Stream<String> splitString(String input, String delim) {
        return StreamSupport.stream(new StringSpliterator(input, delim), false);
    }

    public StringSpliterator(String input, String delim) {
        this.input = input;
        this.delim = delim;
    }

    @Override
    public boolean tryAdvance(Consumer<? super String> action) {
        if (tokenizer == null) {
            tokenizer = new StringTokenizer(input, delim);
        }
        if (tokenizer.hasMoreTokens()) {
            action.accept(tokenizer.nextToken());
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<String> trySplit() {
        // cannot split this:
        return null;
    }

    @Override
    public long estimateSize() {
        // as per javadoc if the lenght is too costly:
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE | ORDERED;
    }

}
