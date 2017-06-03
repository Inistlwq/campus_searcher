package server;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;

public class WordScorer extends Scorer {
    private IndexReader reader;
    private int fieldNum;
    private String[] fields;
    private float[] boosts;
    private Term[] terms;
    private TermDocs termDocs[];
    private boolean valid[];
    private float avgLength[];
    private float[] idf;
    private int doc;

    private static final int BUFFER_SIZE = 1024;
    private final int[][] docs; // buffered doc numbers
    private final int[][] freqs; // buffered term frequents
    private int[] pointer;
    private int[] pointerMax;

    private float K1 = 2.0f;
    private float b = 0.75f;

    @SuppressWarnings("deprecation")
    protected WordScorer(IndexReader reader, String word, String[] fields,
            float[] boosts, float[] avgLength, Similarity similarity) {
        super(similarity);
        fieldNum = fields.length;
        this.reader = reader;
        this.fields = fields;
        this.boosts = boosts;
        this.terms = new Term[fieldNum];
        this.termDocs = new TermDocs[fieldNum];
        this.valid = new boolean[fieldNum];
        this.avgLength = avgLength;
        this.idf = new float[fieldNum];
        this.doc = -1;
        this.pointer = new int[fieldNum];
        this.pointerMax = new int[fieldNum];
        this.docs = new int[fieldNum][];
        this.freqs = new int[fieldNum][];
        try {
            for (int i = 0; i < fieldNum; ++i) {
                terms[i] = new Term(fields[i], word);
                this.termDocs[i] = reader.termDocs(terms[i]);
                idf[i] = similarity.idf(reader.docFreq(terms[i]),
                        reader.numDocs());
                this.pointer[i] = -1;
                this.pointerMax[i] = -1;
                this.docs[i] = new int[BUFFER_SIZE];
                this.freqs[i] = new int[BUFFER_SIZE];
                valid[i] = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public float score() throws IOException {
        float res = 0.0f;
        Document document = reader.document(doc);
        for (int i = 0; i < fieldNum; ++i) {
            if (valid[i] && docs[i][pointer[i]] == docID()
                    && document.getField(fields[i]) != null) {
                float len = document.getField(fields[i]).stringValue().length();
                float tf = freqs[i][pointer[i]];
                double fscore = (K1 + 1) * tf
                        / (K1 * (1 - b + b * len / avgLength[i]) + tf) * idf[i];
                res += fscore * boosts[i];
            }
        }
        return res;
    }

    @Override
    public int advance(int target) throws IOException {
        while (docID() < target) {
            if (nextDoc() == NO_MORE_DOCS) {
                return NO_MORE_DOCS;
            }
        }
        return docID();
    }

    @Override
    public int docID() {
        return this.doc;
    }

    private boolean refill(int i) throws IOException {
        pointerMax[i] = termDocs[i].read(docs[i], freqs[i]);
        if (pointerMax[i] != 0) {
            pointer[i] = 0;
            return true;
        } else {
            termDocs[i].close();
            valid[i] = false;
            return false;
        }
    }

    @Override
    public int nextDoc() throws IOException {
        int nextDoc = Integer.MAX_VALUE;
        for (int i = 0; i < fieldNum; ++i) {
            if (valid[i]) {
                if (pointer[i] < 0) {
                    if (!refill(i)) {
                        continue;
                    }
                } else {
                    if (docs[i][pointer[i]] == doc) {
                        ++pointer[i];
                        if (pointer[i] >= pointerMax[i]) {
                            if (!refill(i)) {
                                continue;
                            }
                        }
                    }
                }
                if (docs[i][pointer[i]] < nextDoc) {
                    nextDoc = docs[i][pointer[i]];
                }
            } else {
                continue;
            }
        }

        if (nextDoc == Integer.MAX_VALUE) {
            return doc = NO_MORE_DOCS;
        } else {
            return doc = nextDoc;
        }
    }

}
