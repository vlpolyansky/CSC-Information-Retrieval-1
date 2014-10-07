package csc.vlpol.infret;

public class IntArrayList {
    private int[] array = new int[8];
    private int size = 0;

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

    public int last() {
        return array[size - 1];
    }

}
