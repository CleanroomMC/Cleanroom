package com.cleanroommc.kirino.utils;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

public class QuantileUtils {

    public static int median(Integer @NonNull [] array) {
        Preconditions.checkNotNull(array);

        int len = array.length;
        if (len % 2 == 0) {
            return array[QuantileUtils.<Integer>select(array, 0, array.length - 1, array.length >> 1)];
        } else {
            return (array[QuantileUtils.<Integer>select(array, 0, array.length - 1, (array.length >> 1) - 1)] +
                    array[QuantileUtils.<Integer>select(array, 0, array.length - 1, array.length >> 1)])
                    / 2;
        }
    }

    public static <T extends Comparable<T>> T median(@NonNull T @NonNull [] array) {
        Preconditions.checkNotNull(array);

        return array[select(array, 0, array.length - 1, array.length >> 1)];
    }

    private static <T extends Comparable<T>> int select(@NonNull T @NonNull [] array, int left, int right, int n) {
        while (true) {
            if (left == right) {
                return left;
            }

            int pivotIdx = pivot(array, left, right);
            pivotIdx = partition(array, left, right, pivotIdx, n);
            if (pivotIdx == n) {
                return n;
            } else if (n < pivotIdx) {
                right = pivotIdx - 1;
            } else {
                left = pivotIdx + 1;
            }
        }
    }

    private static <T extends Comparable<T>> int pivot(@NonNull T @NonNull [] array,
                                                       int left, int right) {
        if (left - right < 5) {
            return partition5(array, left, right);
        }

        for (int i = left; i <= right; i += 5) {
            int subRight = i + 4;
            if (subRight > right) {
                subRight = right;
            }
            int median5 = partition5(array, left, subRight);
            swap(array, median5, left + Math.floorDiv(i - left, 5));
        }

        int mid = Math.floorDiv(right - left, 10) + left + 1;
        return select(array, left, left + Math.floorDiv(right - left, 5), mid);
    }

    private static <T extends Comparable<T>> int partition(@NonNull T @NonNull [] array,
                                                           int left, int right,
                                                           int pivotIdx, int n) {
        T pivotValue = array[pivotIdx];
        swap(array, pivotIdx, right);
        int storeIdx = left;

        for (int i = left; i < right; i++) {
            if (array[i].compareTo(pivotValue) < 0) {
                swap(array, storeIdx, i);
                storeIdx++;
            }
        }

        int storeIdxEq = storeIdx;

        for (int j = storeIdx; j < right; j++) {
            if (array[j].compareTo(pivotValue) == 0) {
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

    private static <T extends Comparable<T>> int partition5(@NonNull T @NonNull [] array,
                                                            int left, int right) {
        switch (right-left) {
            case 0:
                break;
            case 1:
                swap(array, left, right);
                break;
            case 2:
                if (array[left].compareTo(array[right-1]) <= 0) {
                    if (array[left+1].compareTo(array[right]) > 0) {
                        if (array[left].compareTo(array[right]) < 0) {
                            swap(array, left+1, right);
                        } else {
                            T tmp = array[left];
                            array[left] = array[right];
                            array[right] = array[left+1];
                            array[left+1] = tmp;
                        }
                    }
                } else {
                    if (array[left+1].compareTo(array[right]) < 0) {
                        if (array[left].compareTo(array[right]) < 0) {
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
            case 5:
                Arrays.sort(array, left, right+1);
                break;
        }

        return left + (right - left) >> 1;
    }

    public static <T extends Comparable<T>> void swap(@NonNull T @NonNull [] array, int left, int right) {
        T tmp = array[left];
        array[left] = array[right];
        array[right] = tmp;
    }
}
