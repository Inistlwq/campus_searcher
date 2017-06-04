package index;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.wltea.analyzer.lucene.IKAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;//.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;


@SuppressWarnings("deprecation")
public class Indexer {
    private Analyzer analyzer;
    private IndexWriter indexWriter;
    private static String page_root = "C:/newwork/Heritrix/jobs/No_dd.yyyy-20170524164321461/mirror/news.tsinghua.edu.cn";

    private float anchorInAvgLength = 1.0f;
    private float anchorOutAvgLength = 1.0f;
    private float titleAvgLength = 1.0f;
    private float contentAvgLength = 1.0f;
    private float hAvgLength = 1.0f;
    private int hNum = 0;
    private int anchorOutNum = 0;

    public Indexer(String indexDir) {
        analyzer = new IKAnalyzer();
        try {
            IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40,
                    analyzer);
            Directory dir = FSDirectory.open(new File(indexDir));
            indexWriter = new IndexWriter(dir, iwc);
            // indexWriter.setSimilarity(new BM25Similarity());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveGlobals(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new File(filename));
            pw.println(titleAvgLength);
            pw.println(hAvgLength);
            pw.println(anchorInAvgLength);
            pw.println(contentAvgLength);
            pw.println(anchorOutAvgLength);
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    float pagerank;
    
    public void indexFiles(String filename, String rootDir) {
        try {
            int num = 0;

            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(format.format(new Date()) + " : Start");
            while ((line = reader.readLine()) != null) {
                if (line.equals("")) {
                    break;
                }
                String[] items = line.split("\t", 4);
                String loc = items[0];
                //String pagerank = items[1];
                pagerank = Float.parseFloat(items[1]);
                pagerank = (float) (16 + Math.log(pagerank));
                String anchorIn;
                if (items[2].equals("null")) {
                    if (items[3].equals("null")) {
                        anchorIn = "";
                    } else {
                        anchorIn = items[3];
                    }
                } else {
                    anchorIn = items[2];
                }

                File res;
                try {
                    res = new File(rootDir + loc);
                } catch (Exception e) {
                    continue;
                }

                Document document = new Document();
                Field pathField = new Field("path", loc, Field.Store.YES,
                        Field.Index.NO);
                document.add(pathField);
                //Field pagerankField = new Field("pagerank", pagerank,
                //        Field.Store.YES, Field.Index.NO);
                //document.add(pagerankField);
                Field anchorInField = new Field("anchorIn", anchorIn,
                        Field.Store.YES, Field.Index.ANALYZED);
                anchorInField.setBoost(pagerank);
                document.add(anchorInField);
                anchorInAvgLength += anchorIn.length();
                // document.setBoost(Float.parseFloat(pagerank));

                boolean success = false;
                if (loc.toLowerCase().endsWith(".html")
                        || loc.toLowerCase().endsWith(".htm")) {
                    success = parseHtml(res, document);
                } else if (loc.toLowerCase().endsWith(".doc")
                        || loc.toLowerCase().endsWith(".docx")) {
                    success = parseDoc(res, document);
                } else if (loc.toLowerCase().endsWith(".pdf")) {
                    success = parsePdf(res, document);
                }
                if (success) {
                    indexWriter.addDocument(document);
                    if (++num % 1000 == 0) {
                        System.out.println(format.format(new Date())
                                + " : process " + num);
                    }
                }
            }
            if ((num = indexWriter.numDocs()) > 0) {
                contentAvgLength /= indexWriter.numDocs();
                titleAvgLength /= indexWriter.numDocs();
                anchorInAvgLength /= indexWriter.numDocs();
            }
            if (hNum > 0) {
                hAvgLength /= hNum;
            }
            if (anchorOutNum > 0) {
                anchorOutAvgLength /= anchorOutNum;
            }

            System.out.println("contentAvgLength = " + contentAvgLength);
            System.out.println("titleAvgLength = " + titleAvgLength);
            System.out.println("hAvgLength = " + hAvgLength);
            System.out.println("anchorInAvgLength = " + anchorInAvgLength);
            System.out.println("anchorOutAvgLength = " + anchorOutAvgLength);
            System.out.println("total " + indexWriter.numDocs() + " documents");
            indexWriter.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*private boolean parseHtml(File in, Document doc) {
        try {
            org.jsoup.nodes.Document html = Jsoup.parse(in, "utf-8");

            // title
            String title = "";
            org.jsoup.select.Elements titleEles = html
                    .getElementsByTag("title");
            if (titleEles.size() > 0) {
                title = titleEles.get(0).text();
            }
            Field titleField = new Field("title", title, Field.Store.YES,
                    Field.Index.ANALYZED);
            titleField.setBoost(pagerank);
            doc.add(titleField);
            titleAvgLength += title.length();

            // content
            String content = "";
            for (org.jsoup.nodes.Element e : html.select("p,span,td,div,li,a")) {
                content += ' ' + e.ownText();
            }
            Field contentField = new Field("content", content, Field.Store.YES,
                    Field.Index.ANALYZED);
            contentField.setBoost(pagerank);
            doc.add(contentField);
            contentAvgLength += content.length();

            String anchorOut = "";
            for (org.jsoup.nodes.Element e : html.getElementsByTag("a")) {
                anchorOut += ' ' + e.text();
            }
            Field anchorOutField = new Field("anchorOut", anchorOut,
                    Field.Store.YES, Field.Index.ANALYZED);
            anchorOutField.setBoost(pagerank);
            doc.add(anchorOutField);
            anchorOutAvgLength += anchorOut.length();
            if (anchorOut.length() > 0) {
                ++anchorOutNum;
            }

            String hStr = "";
            for (org.jsoup.nodes.Element e : html
                    .getElementsByTag("h1,h2,h3,h4,h5,h6")) {
                hStr += e.ownText();
            }
            Field hField = new Field("h", hStr, Field.Store.YES,
                    Field.Index.ANALYZED);
            hField.setBoost(pagerank);
            doc.add(hField);
            hAvgLength += hStr.length();
            if (hStr.length() > 0) {
                ++hNum;
            }

            doc.add(new Field("type", "htm", Field.Store.YES,
                    Field.Index.NOT_ANALYZED));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }*/
    
    private boolean parseHtml(File in, Document doc) {
        try {
            org.jsoup.nodes.Document html = Jsoup.parse(in, "utf-8");

            // title
            String title = "";
            org.jsoup.select.Elements titleEles = html
                    .getElementsByTag("title");
            if (titleEles.size() > 0) {
                title = titleEles.get(0).text();
            }
            Field titleField = new Field("title", title, Field.Store.YES,
                    Field.Index.ANALYZED);
            titleField.setBoost(pagerank);

            doc.add(titleField);
            titleAvgLength += title.length();

            String content = "";
            String anchorOut = "";
            String hStr = "";
            Elements elements = html.getElementsByClass("article");
            if (elements.size() > 0) {
                for (org.jsoup.nodes.Element e : elements.get(0).select(
                        "p,span,td,div,li,a")) {
                    content += ' ' + e.ownText();
                }
                for (org.jsoup.nodes.Element e : elements.get(0)
                        .getElementsByTag("a")) {
                    anchorOut += ' ' + e.text();
                }
                for (org.jsoup.nodes.Element e : elements.get(0)
                        .getElementsByTag("h1,h2,h3,h4,h5,h6")) {
                    hStr += e.ownText();
                }
            } else {
                for (org.jsoup.nodes.Element e : html
                        .select("p,span,td,div,li,a")) {
                    content += ' ' + e.ownText();
                }
                for (org.jsoup.nodes.Element e : html.getElementsByTag("a")) {
                    anchorOut += ' ' + e.text();
                }
                for (org.jsoup.nodes.Element e : html
                        .getElementsByTag("h1,h2,h3,h4,h5,h6")) {
                    hStr += e.ownText();
                }
            }
            Field contentField = new Field("content", content, Field.Store.YES,
                    Field.Index.ANALYZED);
            doc.add(contentField);
            contentField.setBoost(pagerank);

            contentAvgLength += content.length();

            Field anchorOutField = new Field("anchorOut", anchorOut,
                    Field.Store.YES, Field.Index.ANALYZED);
            doc.add(anchorOutField);
            anchorOutField.setBoost(pagerank);

            anchorOutAvgLength += anchorOut.length();
            if (anchorOut.length() > 0) {
                ++anchorOutNum;
            }

            Field hField = new Field("h", hStr, Field.Store.YES,
                    Field.Index.ANALYZED);
            doc.add(hField);
            hField.setBoost(pagerank);
            hAvgLength += hStr.length();
            if (hStr.length() > 0) {
                ++hNum;
            }

            doc.add(new Field("type", "htm", Field.Store.YES,
                    Field.Index.NOT_ANALYZED));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean parsePdf(File in, Document doc) {
        try {
            PDFParser p = new PDFParser(new FileInputStream(in));
            p.parse();
            PDDocument pd = p.getPDDocument();
            PDFTextStripper ts = new PDFTextStripper();
            String content = ts.getText(pd);
            pd.close();
            contentAvgLength += content.length();

            Field contentField = new Field("content", content, Field.Store.YES,
                    Field.Index.ANALYZED);
            contentField.setBoost(pagerank);
            doc.add(contentField);

            String title = in.getName();
            Field titleField = new Field("title", title, Field.Store.YES,
                    Field.Index.NOT_ANALYZED);
            titleField.setBoost(pagerank);
            doc.add(titleField);

            doc.add(new Field("type", "pdf", Field.Store.YES,
                    Field.Index.NOT_ANALYZED));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getDocContent(File in) {
        try {
            WordExtractor doc = new WordExtractor(new FileInputStream(in));
            String content = doc.getText();
            doc.close();
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getDocxContent(File in) {
        try {
            XWPFWordExtractor docx = new XWPFWordExtractor(
                    POIXMLDocument.openPackage(in.getAbsolutePath()));
            String content = docx.getText();
            docx.close();
            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private boolean parseDoc(File in, Document doc) {
        try {
            String content;
            String name = in.getName().toLowerCase();
            if (name.endsWith("doc")) {
                content = getDocContent(in);
            } else if (name.endsWith("docx")) {
                content = getDocxContent(in);
            } else {
                return false;
            }
            contentAvgLength += content.length();
            Field contentField = new Field("content", content, Field.Store.YES,
                    Field.Index.ANALYZED);
            contentField.setBoost(pagerank);
            doc.add(contentField);

            String title = in.getName();
            Field titleField = new Field("title", title, Field.Store.YES,
                    Field.Index.NOT_ANALYZED);
            titleField.setBoost(pagerank);
            doc.add(titleField);

            doc.add(new Field("type", "doc", Field.Store.YES,
                    Field.Index.NOT_ANALYZED));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        Indexer indexer = new Indexer("WebRoot/forIndex/index");
        indexer.indexFiles("pagerank.txt", page_root);
        indexer.saveGlobals("WebRoot/forIndex/global.txt");
    }

    public static List<String> tokenize(String queryStr) {
        List<String> tokens = new ArrayList<String>();
        IKAnalyzer analyzer = new IKAnalyzer(true);
        for (String part : queryStr.split(" ")) {
            if (part.length() == 0) {
                continue;
            }
            int colonIndex = part.indexOf(':');
            if (colonIndex >= 0) {
                tokens.add(part);
            } else {
                TokenStream ts;
				try {
					ts = analyzer.tokenStream("content",
					        new StringReader(part));
				} catch (IOException e) {
					e.printStackTrace();
					return tokens;
				}
                try {
                    while (ts.incrementToken()) {
                        tokens.add(ts.getAttribute(CharTermAttribute.class).toString());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        analyzer.close();
        return tokens;
    }

    @SuppressWarnings("serial")
    public static final Set<Character> stopChar = new HashSet<Character>() {
        {
            add('.');
            add(',');
            add('!');
            add('?');
            add('。');
            add('，');
            add('！');
            add('？');
        }
    };

    public static String genAbstract(List<String> tokens, String content) {
        int maxLength = 300;
        int range = 30;
        String result = "";
        content = content.trim();
        List<Integer> startPositions = new ArrayList<Integer>();
        List<Integer> endPositions = new ArrayList<Integer>();
        for (String t : tokens) {
            String token = new String(t);
            int colonIndex = token.indexOf(':');
            if (colonIndex >= 0) {
                token = token.split(":")[1];
            }
            int pos = 0;
            Pattern pattern = Pattern.compile(token, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(content);

            int num = 0;
            while (matcher.find(pos) && ++num < maxLength / range) {
                pos = matcher.start();
                startPositions.add(pos);
                endPositions.add(pos + token.length());
                ++pos;
            }
        }
        Collections.sort(startPositions);
        Collections.sort(endPositions);
        int i = 0;
        int size = startPositions.size();
        while (i < size) {
            int pos = startPositions.get(i);
            int end = endPositions.get(i);
            int ptr;
            for (ptr = pos; ptr >= pos - range; --ptr) {
                if (ptr < 0 || stopChar.contains(content.charAt(ptr))) {
                    ++ptr;
                    break;
                }
            }
            result += content.subSequence(ptr, pos);
            result += "<em>";
            result += content.subSequence(pos, end);
            result += "</em>";
            ++i;
            while (i < size) {
                pos = startPositions.get(i);
                if (end > pos) {
                    result += "<em>";
                    result += content.subSequence(end, endPositions.get(i));
                    result += "</em>";
                    pos = end;
                    end = endPositions.get(i);
                    ++i;
                } else {
                    if (pos == end) {
                        result += content.subSequence(end, pos);
                        end = endPositions.get(i);
                        result += "<em>";
                        result += content.subSequence(pos, end);
                        result += "</em>";
                        ++i;
                    } else if (pos - end < range) {
                        result += content.subSequence(end, pos);
                        end = endPositions.get(i);
                        result += "<em>";
                        result += content.subSequence(pos, end);
                        result += "</em>";
                        ++i;
                        if (result.length() > maxLength - range) {
                            break;
                        }
                    } else {
                        break;
                    }
                }
            }
            for (ptr = end; ptr < end + range; ++ptr) {
                if (ptr >= content.length()
                        || stopChar.contains(content.charAt(ptr))) {
                    break;
                }
            }
            result += content.subSequence(end, ptr) + "... ";

            if (result.length() > maxLength) {
                break;
            }
        }
        return result;
    }
}
