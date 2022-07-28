package dataStructures;

public class SubLink {

    public String title;
    public String link;
    public String snippet;
    public String displayLink;

    @Override
    public String toString() {
        return "{" +
                "title:\"" + title + '\"' +
                ", link:\"" + link + '\"' +
                ", description:\"" + (snippet == null ? null : snippet.replaceAll("\n", "")) + '\"' +
                '}';
    }
}
