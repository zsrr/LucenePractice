package com.stephen.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LuceneDemo {

    private void writeToDirectory() throws Exception {
        Directory directory = FSDirectory.open(new File("/Users/zhangshirui/lucene-index/").toPath());
        Analyzer analyzer = new IKAnalyzer(true);

        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        IndexWriter iw = new IndexWriter(directory, iwc);

        Article article = new Article(1L, "这是中文标题 By Stephen", "我也不知道说什么 标题 Stephen");
        Document document = new Document();
        document.add(new LongField("id", article.getId(), Field.Store.YES));
        document.add(new TextField("title", article.getTitle(), Field.Store.YES));
        document.add(new TextField("content", article.getContent(), Field.Store.NO));

        iw.addDocument(document);
        iw.close();
        analyzer.close();
        directory.close();
    }

    private void searchByKeywords(String... fields) throws Exception {
        Directory directory = FSDirectory.open(new File("/Users/zhangshirui/lucene-index/").toPath());
        String queryStr = "标题";

        Analyzer analyzer = new IKAnalyzer(true);

        QueryParser parser = new MultiFieldQueryParser(fields, analyzer);
        Query query = parser.parse(queryStr);

        IndexReader ir = DirectoryReader.open(directory);
        IndexSearcher is = new IndexSearcher(ir);

        TopDocs td = is.search(query, 100);

        ScoreDoc[] docs = td.scoreDocs;

        List<Article> articles = new ArrayList<>();

        for (ScoreDoc doc : docs) {
            Document document = is.doc(doc.doc);
            articles.add(parse(document));
        }

        for (Article article : articles) {
            System.out.println(article);
        }
        ir.close();
        directory.close();
    }

    private Article parse(Document document) {
        return new Article(Long.parseLong(document.getField("id").stringValue()),
                document.getField("title").stringValue(),
                document.getField("content").stringValue());
    }


    public static void main(String[] args) throws Exception {
        LuceneDemo ld = new LuceneDemo();
        ld.writeToDirectory();
        ld.searchByKeywords("title", "content");
    }
}
