package com.cleanroommc.kirino.engine.render.task.adt;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.PriorityQueue;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;
import java.util.Comparator;
import java.util.function.BiFunction;

public class BoundedPriorityQueue<K> implements PriorityQueue<K>, Serializable {
    private final PriorityQueue<K> delegate;
    private final int maxSize;

    public BoundedPriorityQueue(BiFunction<Integer, Comparator<? super K>, PriorityQueue<K>> delegate, int maxSize, Comparator<? super K> comparator) {
        Preconditions.checkArgument(maxSize >= 0,"maxSize (%s) must >= 0", maxSize);

        this.delegate = delegate.apply(maxSize, comparator);
        this.maxSize = maxSize;
    }

    public int remainingCapacity() {
        return maxSize - size();
    }

    @Override
    public void enqueue(@NonNull K x) {
        Preconditions.checkNotNull(x);
        if (maxSize == 0) {
            return;
        }
        if (size() == maxSize) {
            if (delegate.comparator().compare(x, first()) > 0) {
                delegate.dequeue();
                delegate.enqueue(x);
            }
        }
        else {
            delegate.enqueue(x);
        }
    }

    @NonNull
    @Override
    public K dequeue() {
        return delegate.dequeue();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public K first() {
        return delegate.first();
    }

    @Override
    public Comparator<? super K> comparator() {
        return delegate.comparator();
    }
}
