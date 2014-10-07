package csc.vlpol.infret;

import org.apache.lucene.morphology.EnglishLuceneMorphology;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static csc.vlpol.infret.Utils.*;

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

    private LuceneMorphology rusMorphology, engMorphology;
    private HashMap<String, IntArrayList> indexMap = new HashMap<>();
    private File[] documents;

    public Indexer() throws IOException {
        rusMorphology = new RussianLuceneMorphology();
        engMorphology = new EnglishLuceneMorphology();
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
                LuceneMorphology curMorphology = isRussian(word.charAt(0)) ? rusMorphology : engMorphology;
                for (String def : curMorphology.getNormalForms(word.toLowerCase())) {
                    if (!indexMap.containsKey(def)) {
                        IntArrayList value = new IntArrayList();
                        value.add(doc);
                        indexMap.put(def, value);
                    } else {
                        IntArrayList list = indexMap.get(def);
                        if (list.last() != doc) {
                            list.add(doc);
                        }
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
        for (Map.Entry<String, IntArrayList> entry : indexMap.entrySet()) {
            out.print(entry.getKey() + " ");
            IntArrayList value = entry.getValue();
            for (int i = 0; i < value.size(); ++i) {
                out.print(value.get(i) + " ");
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
            while (i < line.length() && !isRussian(line.charAt(i)) && !isEnglish(line.charAt(i))) {
                ++i;
            }
            if (i == line.length()) {
                return null;
            }
            int start = i;
            boolean rus = isRussian(line.charAt(i));
            while (i < line.length() && (rus ? isRussian(line.charAt(i)) : isEnglish(line.charAt(i)))) {
                ++i;
            }
            return line.substring(start, i);

        }
    }
}
