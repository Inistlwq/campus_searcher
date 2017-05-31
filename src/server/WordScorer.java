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
        try {
            for (int i = 0; i < fieldNum; ++i) {
                terms[i] = new Term(fields[i], word);
                this.termDocs[i] = reader.termDocs(terms[i]);
                idf[i] = similarity.idf(reader.docFreq(terms[i]),
                        reader.numDocs());
                valid[i] = termDocs[i].next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public float score() throws IOException {
        float res = 0;
        Document document = reader.document(doc);
        for (int i = 0; i < fieldNum; ++i) {
            if (termDocs[i].doc() == docID()
                    && document.getField(fields[i]) != null) {
                float len = document.getField(fields[i]).stringValue().length();
                float tf = termDocs[i].freq();
                double fscore = (K1 + 1) * tf;
                fscore /= (K1 * (1 - b + b * len / avgLength[i]) + tf);
                fscore *= idf[i];
                res += fscore * boosts[i];
            }
        }
        res *= 10;
        res += Float.valueOf(document.getField("pagerank").stringValue());
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

    @Override
    public int nextDoc() throws IOException {
        int nextDoc = Integer.MAX_VALUE;
        for (int i = 0; i < fieldNum; ++i) {
            if (valid[i]) {
                if (termDocs[i].doc() == this.doc) {
                    if (!termDocs[i].next()) {
                        valid[i] = false;
                        continue;
                    }
                }
                if (termDocs[i].doc() < nextDoc) {
                    nextDoc = termDocs[i].doc();
                }
            }
        }

        if (nextDoc == Integer.MAX_VALUE) {
            return doc = NO_MORE_DOCS;
        } else {
            return doc = nextDoc;
        }
    }

}
