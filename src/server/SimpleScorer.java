package server;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.*;

/**
 * Expert: A <code>Scorer</code> for documents matching a <code>Term</code>.
 */
final class SimpleScorer extends Scorer {
    private IndexReader reader;
    private Scorer[] scorers;
    private int doc;

    @SuppressWarnings("deprecation")
    protected SimpleScorer(IndexReader reader, Similarity similarity,
            Scorer[] scorers) {
        super(similarity);
        this.reader = reader;
        this.scorers = scorers;
        this.doc = -1;
    }

    @SuppressWarnings("deprecation")
    @Override
    public float score() throws IOException {
        float score = 0.0f;
        for (int i = 0; i < scorers.length; ++i) {
            if (scorers[i].docID() != docID()) {
                continue;
            }
            score += scorers[i].score();
        }
        Document document = reader.document(doc);
        score += Float.valueOf(document.getField("pagerank").stringValue());
        return score;
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
        for (int i = 0; i < scorers.length; ++i) {
            if (scorers[i].docID() != NO_MORE_DOCS) {
                if (scorers[i].docID() == this.doc) {
                    if (scorers[i].nextDoc() == NO_MORE_DOCS) {
                        continue;
                    }
                }
                if (scorers[i].docID() < nextDoc) {
                    nextDoc = scorers[i].docID();
                }
            }
        }

        if (nextDoc == Integer.MAX_VALUE) {
            return doc = NO_MORE_DOCS;
        } else {
            return doc = nextDoc;
        }
    }

    /** Returns a string representation of this <code>SimpleScorer</code>. */
    @Override
    public String toString() {
        return "scorer(" + weight + ")";
    }
}
