package csc.vlpol.infret;

import java.util.Iterator;

public class IntArrayList implements Iterable<Integer> {
    private int[] array;
    private int size = 0;

    public IntArrayList() {
        this(8);
    }

    public IntArrayList(int capacity) {
        array = new int[capacity];
    }

    public int size() {
        return size;
    }

    public void add(int x) {
        if (size >= array.length) {
            int[] arr2 = new int[array.length * 2];
            System.arraycopy(array, 0, arr2, 0, array.length);
            array = arr2;
        }
        array[size++] = x;
    }

    public int get(int i) {
        return array[i];
    }

    public int leftSearch(int x) {
        int l = -1;
        int r = size - 1;
        while (r - l > 1) {
            int m = (l + r) >> 1;
            if (array[m] < x) {
                l = m;
            } else {
                r = m;
            }
        }
        if (array[r] < x) {
            ++r;
        }
        return r;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int i = 0;
            @Override
            public boolean hasNext() {
                return i < size - 1;
            }

            @Override
            public Integer next() {
                return array[i++];
            }
        };
    }

}
