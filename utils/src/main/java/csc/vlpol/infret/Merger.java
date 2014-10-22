package csc.vlpol.infret;

import java.util.ArrayList;

public abstract class Merger<T> {

    public abstract int value(T t);

    public abstract void operation(ArrayList<T> list);

    public void merge(ArrayList<T>[] lists) {
        int n = lists.length;
        int[] idx = new int[n];
        boolean[] doneFlag = new boolean[n];
        int done = 0;
        while (done < n) {
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < n; ++i) {
                if (!doneFlag[i] && idx[i] == lists[i].size()) {
                    doneFlag[i] = true;
                    ++done;
                }
                if (!doneFlag[i]) {
                    int val = value(lists[i].get(idx[i]));
                    if (val < min) {
                        min = val;
                    }
                }
            }
            ArrayList<T> forOperation = new ArrayList<>();
            for (int i = 0; i < n; ++i) {
                if (!doneFlag[i]) {
                    int val = value(lists[i].get(idx[i]));
                    if (val == min) {
                        forOperation.add(lists[i].get(idx[i]++));
                    }
                }
            }
            if (forOperation.isEmpty()) {
                break;
            }
            operation(forOperation);
        }
    }

}
