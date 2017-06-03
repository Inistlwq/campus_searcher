package server;

import index.Indexer;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.util.*;

/**
 * A Query that matches documents containing a term. This may be combined with
 * other terms with a {@link BooleanQuery}.
 */
public class SimpleQuery extends Query {
    private static final long serialVersionUID = 706817812132425810L;
    private String[] tokens;

    private class SimpleWeight extends Weight {

        private static final long serialVersionUID = 3139902822756136865L;

        @Override
        public Explanation explain(IndexReader reader, int doc)
                throws IOException {
            return null;
        }

        @Override
        public Query getQuery() {
            return SimpleQuery.this;
        }

        @Override
        public float getValue() {
            return 0;
        }

        @Override
        public void normalize(float queryNorm) {
        }

        @Override
        public Scorer scorer(IndexReader reader, boolean scoreDocsInOrder,
                boolean topScorer) throws IOException {
            WordScorer[] scorers = new WordScorer[tokens.length];
            for (int i = 0; i < tokens.length; ++i) {
                String[] fields;
                float[] boosts;
                float[] avgLength;
                String word = "";
                if (tokens[i].indexOf(':') >= 0) {
                    String[] keyValue = tokens[i].split(":");
                    String field = keyValue[0];
                    word = keyValue[1];
                    fields = new String[] { field };
                    if (field.equals("type")) {
                        boosts = new float[] { 1 };
                        avgLength = new float[] { 3 };
                    } else {
                        boosts = new float[] { 1 };
                        avgLength = new float[] { Tool.avgLengthMap.get(field) };
                    }
                } else {
                    word = tokens[i];
                    fields = Tool.fields;
                    boosts = Tool.weight;
                    avgLength = Tool.avgLength;
                }
                scorers[i] = new WordScorer(reader, word, fields, boosts,
                        avgLength, new SimpleSimilarity());
            }
            return new SimpleScorer(reader, new SimpleSimilarity(), scorers);
        }

        @Override
        public float sumOfSquaredWeights() throws IOException {
            return 0;
        }

        @Override
        public String toString() {
            return "weight(" + SimpleQuery.this + ")";
        }
    }

    public SimpleQuery(String queryStr) {
        List<String> tokens = Indexer.tokenize(queryStr);
        this.tokens = new String[tokens.size()];
        this.tokens = tokens.toArray(this.tokens);
    }

    @SuppressWarnings("deprecation")
    public Weight createWeight(Searcher searcher) {
        return new SimpleWeight();
    }

    @Override
    public String toString(String field) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("SimpleQuery");
        buffer.append(ToStringUtils.boost(getBoost()));
        return buffer.toString();
    }
}
