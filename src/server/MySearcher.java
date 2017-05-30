package server;

import java.io.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class MySearcher {
    private IndexReader reader;
    private IndexSearcher searcher;

    public String GetDir() {
        return new String(System.getProperty("user.dir") + "/webapps/searcher/");
    }

    public MySearcher(String indexdir) {
        try {
            reader = IndexReader.open(FSDirectory.open(new File(GetDir()
                    + indexdir)));
            searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new SimpleSimilarity());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TopDocs searchQuery(String queryString, int maxnum) {
        try {
            Query query = new SimpleQuery(queryString);
            query.setBoost(1.0f);
            TopDocs results = searcher.search(query, maxnum);
            System.out.println(results);
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Document getDoc(int docID) {
        try {
            return searcher.doc(docID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        MySearcher search = new MySearcher("forIndex/index");

        TopDocs results = search.searchQuery("test", 100);
        ScoreDoc[] hits = results.scoreDocs;
        for (int i = 0; i < hits.length; i++) { // output raw format
            Document doc = search.getDoc(hits[i].doc);
            System.out.println("doc=" + hits[i].doc + " score=" + hits[i].score
                    + " title= " + doc.get("title") + " path= "
                    + doc.get("path"));
        }
    }
}
