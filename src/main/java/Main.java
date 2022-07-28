import core.WebCrawler;
import dataStructures.SearchResult;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler();
        crawler.doSetup();
//        List<SearchResult> result = crawler.doSearches(new String[]{"Java", "Test", "Demo", "Example"});
        SearchResult result = crawler.doSearch("Java");
        System.out.println(result);
//        crawler.tearDown();
    }
}
