package com.cleanroommc.catalogue.client.screen.layout;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntIterator;

import java.util.NoSuchElementException;

public class Divisor implements IntIterator {
    private final int denominator;
    private final int quotient;
    private final int mod;
    private int returnedParts;
    private int remainder;

    public Divisor(int numerator, int denominator) {
        this.denominator = denominator;
        if (denominator > 0) {
            this.quotient = numerator / denominator;
            this.mod = numerator % denominator;
        } else {
            this.quotient = 0;
            this.mod = 0;
        }

    }

    public boolean hasNext() {
        return this.returnedParts < this.denominator;
    }

    public int nextInt() {
        if (!this.hasNext()) {
            throw new NoSuchElementException();
        } else {
            int i = this.quotient;
            this.remainder += this.mod;
            if (this.remainder >= this.denominator) {
                this.remainder -= this.denominator;
                ++i;
            }

            ++this.returnedParts;
            return i;
        }
    }

    @VisibleForTesting
    public static Iterable<Integer> asIterable(int numerator, int denominator) {
        return () -> new Divisor(numerator, denominator);
    }
}
