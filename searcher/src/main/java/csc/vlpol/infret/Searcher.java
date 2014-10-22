package csc.vlpol.infret;

import java.io.*;
import java.util.ArrayList;

public class Searcher {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Wrong number of arguments");
            return;
        }
        try {
            new Searcher(args[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private IndexMap indexMap;
    private String[] documents;
    @SuppressWarnings("all")
    private boolean running = true;

    public Searcher(String indexFile) throws IOException {
        loadIndex(indexFile);
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (running) {
            try {
                FullQuery q = new Parser(in.readLine()).parseFulQuery();
                if (q != null) {
                    ArrayList<Integer> results = exec(q);
                    outputResult(results);
                }
            } catch (QueryException ex) {
                System.out.println("    incorrect query (" + ex.getMessage() + ")");
            }
        }
    }

    private void loadIndex(String indexFile) throws IOException {
        System.out.print("Loading index ... ");
        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(indexFile)));
        documents = new String[in.readInt()];
        for (int i = 0; i < documents.length; ++i) {
            documents[i] = in.readUTF();
        }
        indexMap = IndexMap.readFrom(in);
        in.close();
        System.out.println("done");
    }

    private ArrayList<Integer> exec(FullQuery q) {
        @SuppressWarnings("all")
        final ArrayList<Integer>[] subResults = new ArrayList[q.list.size()];
        for (int i = 0; i < subResults.length; ++i) {
            subResults[i] = exec(q.list.get(i));
        }
        final ArrayList<Integer> result = new ArrayList<>();
        if (q.and) {
            new Merger<Integer>() {
                @Override
                public int value(Integer v) {
                    return v;
                }

                @Override
                public void operation(ArrayList<Integer> list) {
                    if (list.size() == subResults.length) {
                        result.add(list.get(0));
                    }
                }
            }.merge(subResults);
        } else {
            new Merger<Integer>() {
                @Override
                public int value(Integer v) {
                    return v;
                }

                @Override
                public void operation(ArrayList<Integer> list) {
                    if (list.size() > 0) {
                        result.add(list.get(0));
                    }
                }
            }.merge(subResults);
        }
        return result;
    }

    private ArrayList<Integer> exec(final SubQuery q) {
        @SuppressWarnings("all")
        ArrayList<IndexMap.DocPositionList>[] lists = new ArrayList[q.words.size()];
        for (int i = 0; i < q.words.size(); ++i) {
            lists[i] = indexMap.get(q.words.get(i));
            if (lists[i] == null) {
                lists[i] = new ArrayList<>();
            }
        }
        final ArrayList<Integer> result = new ArrayList<>();
        new Merger<IndexMap.DocPositionList>() {
            @Override
            public int value(IndexMap.DocPositionList list) {
                return list.getDocId();
            }
            @Override
            public void operation(ArrayList<IndexMap.DocPositionList> list) {
                if (list.size() == q.words.size()) {
                    if (find(q, list)) {
                        result.add(list.get(0).getDocId());
                    }
                }
            }
        }.merge(lists);
        return result;
    }

    private boolean find(SubQuery q, ArrayList<? extends IntArrayList> list) {
        for (int i = 0; i < list.get(0).size(); ++i) {
            if (recFind(q, list, list.get(0).get(i), 1)) {
                return true;
            }
        }
        return false;
    }

    private boolean recFind(SubQuery q, ArrayList<? extends IntArrayList> list, int lastPos, int curWord) {
        if (curWord == q.words.size()) {
            return true;
        }
        QueryOperator op = q.infix.get(curWord - 1);
        int l = lastPos - (op.direction <= 0 ? op.number : 0);
        int r = lastPos + (op.direction >= 0 ? op.number : 0);
        IntArrayList posList = list.get(curWord);
        for (int i = posList.leftSearch(l); i < posList.size() && posList.get(i) <= r; ++i) {
            if (recFind(q, list, posList.get(i), curWord + 1)) {
                return true;
            }
        }
        return false;
    }



    private void outputResult(ArrayList<Integer> found) {
        String NOTHING_MSG = "    no documents found";
        if (found.size() == 0) {
            System.out.println(NOTHING_MSG);
        } else {
            System.out.print("    found " + documents[found.get(0)]);
            if (found.size() > 1) {
                System.out.print(", " + documents[found.get(1)]);
                if (found.size() > 2) {
                    System.out.println(" and " + (found.size() - 2) + " more");
                } else {
                    System.out.println();
                }
            } else {
                System.out.println();
            }
        }
    }
}
