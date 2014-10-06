package csc.vlpol.infret;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Indexer {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Wrong number of arguments");
            return;
        }
        String docsDir = args[0];
        String indexFile = args[1];
        try {
            new Indexer().build(docsDir, indexFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private LuceneMorphology morphology;
    private HashMap<String, ArrayList<Integer>> indexMap = new HashMap<>();
    private File[] documents;

    public Indexer() throws IOException {
        morphology = new RussianLuceneMorphology();
    }

    public void build(String docsDir, String indexFile) throws IOException {
//        documents = Arrays.copyOfRange(new File(docsDir).listFiles(), 0, 100);
        documents = new File(docsDir).listFiles();
        assert documents != null;
        for (int i = 0; i < documents.length; ++i) {
            System.out.print("Handling " + documents[i].getName() + "(" + (i + 1) + "/" + documents.length + ") ... ");
            handleFile(i);
            System.out.println("done");
        }
        saveIndex(indexFile);
    }

    private void handleFile(int doc) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(documents[doc]));
        String line;
        while ((line = in.readLine()) != null) {
            WordTokenizer tok = new WordTokenizer(line);
            String word;
            while ((word = tok.nextWord()) != null) {
//                System.err.println(word);
                for (String def : morphology.getNormalForms(word.toLowerCase())) {
//                    System.err.println("    " + def);
                    if (!indexMap.containsKey(def)) {
                        ArrayList<Integer> value = new ArrayList<>();
                        value.add(doc);
                        indexMap.put(def, value);
                    } else {
                        indexMap.get(def).add(doc);
                    }
                }
            }
        }
    }

    private void saveIndex(String indexFile) throws FileNotFoundException {
        System.out.print("Writing index ... ");
        PrintWriter out = new PrintWriter(indexFile);
        out.println(documents.length);
        for (File doc : documents) {
            out.println(doc.getName());
        }
        out.println(indexMap.size());
        for (Map.Entry<String, ArrayList<Integer>> entry : indexMap.entrySet()) {
            out.print(entry.getKey() + " ");
            for (Integer value : entry.getValue()) {
                out.print(value + " ");
            }
            out.println();
        }
        out.close();
        System.out.println("done");
    }

    public static class WordTokenizer {
        private String line;
        private int i = 0;

        public WordTokenizer(String line) {
            this.line = line;
        }

        public String nextWord() {
            while (i < line.length() && !isRussian(line.charAt(i))) {
                ++i;
            }
            if (i == line.length()) {
                return null;
            }
            int start = i;
            while (i < line.length() && isRussian(line.charAt(i))) {
                ++i;
            }
            return line.substring(start, i);

        }

        private boolean isRussian(char c) {
            return c >= 'а' && c <= 'я' || c >= 'А' && c <= 'Я' || c == 'ё' || c == 'Ё';
        }
    }
}
