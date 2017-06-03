package server;
import index.Indexer;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;


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

    public Server() {
        super();
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
            System.out.println(tokens);
            String[] titles = null;
            String[] absts = null;
            String[] paths = null;
            String[] types = null;
            // TopDocs results = search.searchQuery(queryString, 100);
            
            //TopDocs results = search.searchQuery(queryString, 100);
            //待查找字符串对应的字段
            /*
			Analyzer analyzer = new IKAnalyzer(true);//true智能切分
			String [] to_query = {queryString, queryString,queryString,queryString, queryString};
			// {"content"};
            //Occur.MUST表示对应字段必须有查询值， Occur.MUST_NOT 表示对应字段必须没有查询值， Occur.SHOULD(结果“或”)
            */
            Occur[] occ={Occur.SHOULD, Occur.SHOULD, Occur.SHOULD, Occur.SHOULD, Occur.SHOULD};
			String [] fields = Tool.fields;
            Query query = null;
            MultiFieldQueryParser parser = null;
			try {
				parser = new MultiFieldQueryParser(Version.LUCENE_35, fields, new IKAnalyzer(true));
				query = parser.parse(queryString);
				// query = MultiFieldQueryParser.parse(Version.LUCENE_35, to_query,fields,occ,analyzer);
				System.out.println(query.toString());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            TopDocs results = search.searchQuery(query, 100);
            
            if (results != null) {
                ScoreDoc[] hits = showList(results.scoreDocs, page);
                if (hits != null) {
                    titles = new String[hits.length];
                    absts = new String[hits.length];
                    paths = new String[hits.length];
                    types = new String[hits.length];
                    for (int i = 0; i < hits.length && i < PAGE_RESULT; i++) {
                        Document doc = search.getDoc(hits[i].doc);
                        System.out.println("doc=" + hits[i].doc + " score="
                                + hits[i].score + " title= " + doc.get("title")
                                + " path= " + doc.get("path"));
                        titles[i] = doc.get("title");
                        paths[i] = doc.get("path");
                        types[i] = doc.get("type");
                        absts[i] = Indexer.genAbstract(tokens,
                                doc.get("content"))
                                + " " + doc.get("anchorIn");
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
