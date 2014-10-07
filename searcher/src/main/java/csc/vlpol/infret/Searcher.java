package csc.vlpol.infret;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import static csc.vlpol.infret.Utils.*;

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

    private HashMap<String, IntArrayList> indexMap = new HashMap<>();
    private String[] documents;
    @SuppressWarnings("all")
    private boolean running = true;
    private BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    public Searcher(String indexFile) throws IOException {
        loadIndex(indexFile);
        while (running) {
            Query q = readQuery();
            if (q == null) {
                System.out.println("    incorrect query");
                continue;
            }
            exec(q);
        }
    }

    private void loadIndex(String indexFile) throws IOException {
        System.out.print("Loading index ... ");
        BufferedReader in = new BufferedReader(new FileReader(indexFile));
        documents = new String[Integer.parseInt(in.readLine())];
        for (int i = 0; i < documents.length; ++i) {
            documents[i] = in.readLine();
        }
        int sz = Integer.parseInt(in.readLine());
        for (int i = 0; i < sz; ++i) {
            StringTokenizer tok = new StringTokenizer(in.readLine());
            String word = tok.nextToken();
            IntArrayList value = new IntArrayList();
            while (tok.hasMoreTokens()) {
                value.add(Integer.parseInt(tok.nextToken()));
            }
            indexMap.put(word, value);
        }
        in.close();
        System.out.println("done");
    }

    private Query readQuery() throws IOException {
        StringTokenizer tok = new StringTokenizer(in.readLine());
        ArrayList<String> query = new ArrayList<>();
        if (!tok.hasMoreTokens()) {
            return null;
        }
        {
            String word = tok.nextToken();
            if (!check(word)) {
                return null;
            }
            query.add(word);
        }
        boolean and = false;
        boolean or = false;
        while (tok.hasMoreTokens()) {
            String con = tok.nextToken();
            if (con.equalsIgnoreCase("AND")) {
                if (or) {
                    return null;
                }
                and = true;
            } else if (con.equalsIgnoreCase("OR")) {
                if (and) {
                    return null;
                }
                or = true;
            } else {
                return null;
            }
            if (!tok.hasMoreTokens()) {
                return null;
            }
            String word = tok.nextToken();
            if (!check(word)) {
                return null;
            }
            query.add(word);
        }
        return new Query(query, and);
    }

    private void exec(Query q) {
        String NOTHING_MSG = "    no documents found";
        int[] results = new int[documents.length];
        for (String word : q.list) {
            if (!indexMap.containsKey(word)) {
                if (q.and) {
                    System.out.println(NOTHING_MSG);
                    return;
                }
            } else {
                IntArrayList value = indexMap.get(word);
                for (int i = 0; i < value.size(); ++i) {
                    ++results[value.get(i)];
                }
            }
        }
        ArrayList<String> found = new ArrayList<>();
        for (int i = 0; i < results.length; ++i) {
            if (q.and && results[i] == q.list.size() || !q.and && results[i] > 0) {
                found.add(documents[i]);
            }
        }
        if (found.size() == 0) {
            System.out.println(NOTHING_MSG);
        } else {
            System.out.print("    found " + found.get(0));
            if (found.size() > 1) {
                System.out.print(", " + found.get(1));
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

    private boolean check(String word) {
        boolean rus = false;
        boolean eng = false;
        for (int i = 0; i < word.length(); ++i) {
            if (isRussian(word.charAt(i))) {
                if (eng) {
                    return false;
                }
                rus = true;
            } else if (isEnglish(word.charAt(i))) {
                if (rus) {
                    return false;
                }
                eng = true;
            } else {
                return false;
            }
        }
        return true;
    }

    public class Query {
        public ArrayList<String> list = new ArrayList<>();
        public boolean and;

        public Query(ArrayList<String> list, boolean and) {
            this.list = list;
            this.and = and;
        }
    }
}
