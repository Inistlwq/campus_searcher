package server;

import java.io.IOException;

import org.apache.lucene.search.*;

/**
 * Expert: A <code>Scorer</code> for documents matching a <code>Term</code>.
 */
final class SimpleScorer extends Scorer {
    private Scorer[] scorers;
    private int doc;

    @SuppressWarnings("deprecation")
    protected SimpleScorer(Similarity similarity, Scorer[] scorers) {
        super(similarity);
        this.scorers = scorers;
        this.doc = 0;
    }

    @Override
    public float score() throws IOException {
        float score = 1.0f;
        for (int i = 0; i < scorers.length; ++i) {
            score *= scorers[i].score();
        }
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
        int maxDoc = doc + 1;
        int eqnCnt = 0;
        if (this.doc == NO_MORE_DOCS) {
            return NO_MORE_DOCS;
        }
        while (true) {
            for (int i = 0; i < scorers.length; ++i) {
                int nextDoc;
                while (scorers[i].docID() < maxDoc) {
                    nextDoc = scorers[i].nextDoc();
                    if (nextDoc == NO_MORE_DOCS) {
                        doc = NO_MORE_DOCS;
                        return NO_MORE_DOCS;
                    }
                }

                if (scorers[i].docID() > maxDoc) {
                    maxDoc = scorers[i].docID();
                    eqnCnt = 1;
                } else {
                    eqnCnt++;
                }
            }
            if (eqnCnt >= scorers.length) {
                doc = maxDoc;
                return maxDoc;
            }
        }
    }

    /** Returns a string representation of this <code>SimpleScorer</code>. */
    @Override
    public String toString() {
        return "scorer(" + weight + ")";
    }
}
