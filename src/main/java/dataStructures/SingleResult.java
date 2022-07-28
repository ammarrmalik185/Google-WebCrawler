package dataStructures;
import java.util.List;

public class SingleResult {

    public String link;
    public String displayLink;
    public String title;
    public String snippet;
    public List<SubLink> siteLinks;

    @Override
    public String toString() {
        return "{" +
                "link:\"" + link + '\"' +
                ", displayLink:\"" + displayLink + '\"' +
                ", title:\"" + title + '\"' +
                ", snippet:\"" + snippet.replaceAll("\n", "") + '\"' +
                ", siteLinks:" + siteLinks +
                '}';
    }
}
