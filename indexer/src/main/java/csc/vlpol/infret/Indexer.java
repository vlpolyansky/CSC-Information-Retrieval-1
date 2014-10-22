package csc.vlpol.infret;

import org.apache.lucene.morphology.EnglishLuceneMorphology;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.*;
import java.util.ArrayList;

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
    private IndexMap indexMap = new IndexMap();
    private File[] documents;

    public Indexer() throws IOException {
        rusMorphology = new RussianLuceneMorphology();
        engMorphology = new EnglishLuceneMorphology();
    }

    public void build(String docsDir, String indexFile) throws IOException {
        documents = new File(docsDir).listFiles();
        assert documents != null;
        //documents = Arrays.copyOfRange(documents, 0, 100);
        for (int i = 0; i < documents.length; ++i) {
            System.out.print("Handling " + documents[i].getName() + "(" + (i + 1) + "/" + documents.length + ") ... ");
            handleFile(i);
            System.out.println("done");
        }
        saveIndex(indexFile);
    }

    private void handleFile(int doc) throws IOException {
        int position = 0;
        BufferedReader in = new BufferedReader(new FileReader(documents[doc]));
        String line;
        while ((line = in.readLine()) != null) {
            WordTokenizer tok = new WordTokenizer(line);
            String word;
            while ((word = tok.nextWord()) != null) {
                LuceneMorphology curMorphology = isRussian(word.charAt(0)) ? rusMorphology : engMorphology;
                for (String def : curMorphology.getNormalForms(word.toLowerCase())) {
                    addWord(def, doc, position);
                }
                ++position;
            }
        }
    }

    /**
     * Important: while adding, for each word document ids have to go in increasing order,
     * and inside each document word positions have to go in increasing order too.
     */
    private void addWord(String word, int docId, int position) {
        ArrayList<IndexMap.DocPositionList> list;
        if (!indexMap.containsKey(word)) {
            list = new ArrayList<>();
            indexMap.put(word, list);
        } else {
            list = indexMap.get(word);
        }
        IndexMap.DocPositionList docList = list.isEmpty() ? null : list.get(list.size() - 1);
        if (docList == null || docList.getDocId() != docId) {
            docList = new IndexMap.DocPositionList(docId);
            list.add(docList);
        }
        docList.add(position);
    }

    private void saveIndex(String indexFile) throws IOException {
        System.out.print("Writing index ... ");
        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(indexFile)));
        out.writeInt(documents.length);
        for (File doc : documents) {
            out.writeUTF(doc.getName());
        }
        indexMap.writeTo(out);
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
