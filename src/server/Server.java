package server;
import index.Indexer;

import java.io.IOException;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;



import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.util.Version;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.util.*;
import java.net.*;

public class Server extends HttpServlet {
    private static final long serialVersionUID = -5321074657250744186L;

    public static final int PAGE_RESULT = 10;
    public static final String indexDir = "forIndex";
    private MySearcher search = null;
    private SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter("<font color='red'>", "</font>");
	private Highlighter highlighter = null;
	private Analyzer analyzer;
    

    public Server() {
        super();
    	analyzer = new IKAnalyzer(true);
        search = new MySearcher(new String(indexDir + "/index"));
    }

    public ScoreDoc[] showList(ScoreDoc[] results, int page) {
        if (results == null || results.length < (page - 1) * PAGE_RESULT) {
            return null;
        }
        int start = Math.max((page - 1) * PAGE_RESULT, 0);
        int docnum = Math.min(results.length - start, PAGE_RESULT);
        ScoreDoc[] ret = new ScoreDoc[docnum];
        for (int i = 0; i < docnum; i++) {
            ret[i] = results[start + i];
        }
        return ret;
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println(System.getProperty("user.dir"));
        response.setContentType("text/html;charset=utf-8");
        request.setCharacterEncoding("utf-8");
        String queryString = request.getParameter("query");
        String pageString = request.getParameter("page");
        int page = 1;
        if (pageString != null) {
            page = Integer.parseInt(pageString);
        }
        if (queryString == null) {
            System.out.println("null query");
        } else {
            System.out.println(queryString);
            System.out.println(URLDecoder.decode(queryString, "utf-8"));
            System.out.println(URLDecoder.decode(queryString, "gb2312"));

            List<String> tokens = Indexer.tokenize(queryString);
            System.out.println(tokens.toString());
            String[] titles = null;
            String[] absts = null;
            String[] paths = null;
            String[] types = null;
            
            Query query = null;

        	
 			String [] fields = Tool.fields;
 			Map<String, Float> boosts = new HashMap<String, Float>();
 			boosts.put("title", 100.0f);
 			boosts.put("h", 25.0f);
 			boosts.put("anchorIn", 35.0f);    
 			boosts.put("content", 1.0f);
 			boosts.put("anchorOut", 0.0f);
            MultiFieldQueryParser parser = null;
 			parser = new MultiFieldQueryParser(Version.LUCENE_40, fields, analyzer, boosts);
 			parser.setDefaultOperator(QueryParser.AND_OPERATOR);
 			try {
				query = parser.parse(queryString);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			highlighter = new Highlighter(htmlFormatter, new QueryScorer(query)); // highlighter will be used as text filter in function(getDecoratedTitle and Description)
 			System.out.println(query.toString());
            TopDocs results = search.searchQuery(query, 100);
            
            
            TokenStream ts = null;
			
            
            if (results != null) {
                ScoreDoc[] hits = showList(results.scoreDocs, page);
                if (hits != null) {
                    titles = new String[hits.length];
                    absts = new String[hits.length];
                    paths = new String[hits.length];
                    types = new String[hits.length];
                    for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
                        Document doc = search.getDoc(hits[i].doc);
                        try {
            				ts = analyzer.tokenStream("content", new StringReader(doc.get("content")));
            			} catch (IOException e) {
            				e.printStackTrace();
            			}
                        System.out.println("doc=" + hits[i].doc + " score="
                                + hits[i].score + " title= " + doc.get("title")
                                + " path= " + doc.get("path"));

                        titles[i] = doc.get("title");
                        paths[i] = doc.get("path");
                        types[i] = doc.get("type");
                        /*absts[i] = Indexer.genAbstract(tokens,
                                doc.get("content"))
                                + " " + doc.get("anchorIn");
                        */

                        try {
							absts[i] = highlighter.getBestFragments(ts, doc.get("content"), 3, "...");

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							try {
								absts[i] = Indexer.genAbstract(tokens,
		                                doc.get("content"))
		                                + " " + doc.get("anchorIn");
							} catch (Exception ee) {
		                        System.out.println("error");
								ee.printStackTrace();
								absts[i] = "null";
							}
							
						}
                    }

                } else {
                    System.out.println("page null");
                }
            } else {
                System.out.println("result null");
            }
            request.setAttribute("currentQuery", queryString);
            request.setAttribute("currentPage", page);
            request.setAttribute("titles", titles);
            request.setAttribute("paths", paths);
            request.setAttribute("types", types);
            request.setAttribute("absts", absts);
            request.getRequestDispatcher("/show.jsp")
                    .forward(request, response);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.doGet(request, response);
    }
}
