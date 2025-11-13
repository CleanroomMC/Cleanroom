package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.Comparator;

public final class QuantileUtils {

    public static float median(Integer @NonNull [] array) {
        Preconditions.checkNotNull(array);
        Preconditions.checkState(array.length > 0);

        int len = array.length;
        if (len % 2 != 0) {
            return array[QuantileUtils.<Integer>select(array, 0, array.length - 1, array.length >> 1, Integer::compareTo)];
        } else {
            return (float) (array[QuantileUtils.<Integer>select(array, 0, array.length - 1, (array.length >> 1) - 1, Integer::compareTo)] +
                    array[QuantileUtils.<Integer>select(array, 0, array.length - 1, array.length >> 1, Integer::compareTo)])
                    / 2;
        }
    }

    public static <T extends Comparable<T>> T median(@NonNull T @NonNull [] array) {
        Preconditions.checkNotNull(array);
        Preconditions.checkState(array.length > 0);

        return array[select(array, 0, array.length - 1, array.length >> 1, T::compareTo)];
    }

    public static <T> T median(@NonNull T @NonNull [] array, @NonNull Comparator<T> comparator) {
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(comparator);
        Preconditions.checkState(array.length > 0);

        return array[select(array, 0, array.length - 1, array.length >> 1, comparator)];
    }

    private static <T> int select(@NonNull T @NonNull [] array, int left, int right, int n, Comparator<T> comparator) {
        Preconditions.checkState(right > left);
        Preconditions.checkPositionIndex(left, array.length);
        Preconditions.checkPositionIndex(right, array.length);
        Preconditions.checkPositionIndex(n, right);

        while (true) {
            if (left == right) {
                return left;
            }

            int pivotIdx = pivot(array, left, right, comparator);
            pivotIdx = partition(array, left, right, pivotIdx, n, comparator);
            if (pivotIdx == n) {
                return n;
            } else if (n < pivotIdx) {
                right = pivotIdx - 1;
            } else {
                left = pivotIdx + 1;
            }
        }
    }

    private static <T> int pivot(@NonNull T @NonNull [] array,
                                                       int left, int right, Comparator<T> comparator) {
        Preconditions.checkState(right > left);
        Preconditions.checkPositionIndex(left, array.length);
        Preconditions.checkPositionIndex(right, array.length);

        if (right - left < 5) {
            return partition5(array, left, right, comparator);
        }

        for (int i = left; i <= right; i += 5) {
            int subRight = i + 4;
            if (subRight > right) {
                subRight = right;
            }
            int median5 = partition5(array, i, subRight, comparator);
            swap(array, median5, left + Math.floorDiv(i - left, 5));
        }

        int mid = Math.floorDiv(right - left, 10) + left;
        return select(array, left, left + Math.floorDiv(right - left, 5), mid, comparator);
    }

    private static <T> int partition(@NonNull T @NonNull [] array,
                                                           int left, int right,
                                                           int pivotIdx, int n, Comparator<T> comparator) {
        Preconditions.checkState(right > left);
        Preconditions.checkPositionIndex(left, array.length);
        Preconditions.checkPositionIndex(right, array.length);

        T pivotValue = array[pivotIdx];
        swap(array, pivotIdx, right);
        int storeIdx = left;

        for (int i = left; i < right; i++) {
            if (comparator.compare(array[i], pivotValue) < 0) {
                swap(array, storeIdx, i);
                storeIdx++;
            }
        }

        int storeIdxEq = storeIdx;

        for (int j = storeIdx; j < right; j++) {
            if (comparator.compare(array[j], pivotValue) == 0) {
                swap(array, storeIdxEq, j);
                storeIdxEq++;
            }
        }

        swap(array, right, storeIdxEq);

        if (n < storeIdx) {
            return storeIdx;
        }
        if (n <= storeIdxEq) {
            return n;
        }
        return storeIdx;
    }

    private static <T> int partition5(@NonNull T @NonNull [] array,
                                                            int left, int right, Comparator<T> comparator) {
        Preconditions.checkState(right >= left, "%s is less than %s", right, left);
        Preconditions.checkPositionIndex(left, array.length);
        Preconditions.checkPositionIndex(right, array.length);


        switch (right-left) {
            case 0:
                break;
            case 1:
                if (comparator.compare(array[left],array[right]) > right) {
                    swap(array, left, right);
                }
                break;
            case 2:
                if (comparator.compare(array[left],array[right-1]) <= 0) {
                    if (comparator.compare(array[left+1],array[right]) > 0) {
                        if (comparator.compare(array[left],array[right]) < 0) {
                            swap(array, left+1, right);
                        } else {
                            T tmp = array[left];
                            array[left] = array[right];
                            array[right] = array[left+1];
                            array[left+1] = tmp;
                        }
                    }
                } else {
                    if (comparator.compare(array[left+1],array[right]) < 0) {
                        if (comparator.compare(array[left],array[right]) < 0) {
                            swap(array, left, right-1);
                        } else {
                            T tmp = array[left];
                            array[left] = array[left+1];
                            array[left+1] = array[right];
                            array[right] = tmp;
                        }
                    } else {
                        swap(array, left, right);
                    }
                }
                break;
            case 3:
            case 4:
                Arrays.sort(array, left, right+1, comparator);
                break;
        }

        return left + ((right - left) >>> 1);
    }

    public static <T> void swap(@NonNull T @NonNull [] array, int left, int right) {
        Preconditions.checkPositionIndex(left, array.length);
        Preconditions.checkPositionIndex(right, array.length);

        T tmp = array[left];
        array[left] = array[right];
        array[right] = tmp;
    }
}
