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

    public MySearcher(String indexdir) {
        try {
            reader = IndexReader.open(FSDirectory.open(new File(Tool.GetDir()
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
    
    /*MultifieldQuery*/
    public TopDocs searchQuery(Query queryString,int maxnum){
		try {
			// Term term=new Term(field,queryString);
			// Query query=new SimpleQuery(term,avgLength);
			// query.setBoost(1.0f);
			//Weight w=searcher.createNormalizedWeight(query);
			// System.out.println("in "+ queryString);
			TopDocs results = searcher.search(queryString, maxnum);
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
