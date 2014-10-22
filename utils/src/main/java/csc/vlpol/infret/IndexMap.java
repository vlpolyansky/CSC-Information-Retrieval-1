package csc.vlpol.infret;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class IndexMap extends HashMap<String, ArrayList<IndexMap.DocPositionList>> {

    public static class DocPositionList extends IntArrayList {
        private int docId;

        public DocPositionList(int docId) {
            this.docId = docId;
        }

        public DocPositionList(int docId, int capacity) {
            super(capacity);
            this.docId = docId;
        }

        public int getDocId() {
            return docId;
        }

        private void writeTo(DataOutputStream out) throws IOException {
            out.writeInt(docId);
            int sz = size();
            out.writeInt(sz);
            for (int i = 0; i < sz; ++i) {
                out.writeInt(get(i));
            }
        }

        private static DocPositionList readFrom(DataInputStream in) throws IOException {
            int docId = in.readInt();
            int sz = in.readInt();
            DocPositionList list = new DocPositionList(docId, sz);
            for (int i = 0; i < sz; ++i) {
                list.add(in.readInt());
            }
            return list;
        }
    }

    public void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(size());
        for (Map.Entry<String, ArrayList<IndexMap.DocPositionList>> entry : entrySet()) {
            ArrayList<IndexMap.DocPositionList> value = entry.getValue();
            out.writeUTF(entry.getKey());
            out.writeInt(value.size());
            for (DocPositionList list : value) {
                list.writeTo(out);
            }
            out.flush();
        }
    }

    public static IndexMap readFrom(DataInputStream in) throws IOException {
        int n = in.readInt();
        IndexMap map = new IndexMap();
        for (int i = 0; i < n; ++i) {
            String word = in.readUTF();
            int size = in.readInt();
            ArrayList<IndexMap.DocPositionList> value = new ArrayList<>(size);
            for (int j = 0; j < size; ++j) {
                value.add(DocPositionList.readFrom(in));
            }
            map.put(word, value);
        }
        return map;
    }
}