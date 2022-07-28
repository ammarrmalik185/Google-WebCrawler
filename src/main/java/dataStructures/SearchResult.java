package dataStructures;
import java.util.List;

public class SearchResult {


    public String query;
    public long runTime;
    public String status;

    public List<SingleResult> organicResults;
    public List<SingleResult> adResults;

    @Override
    public String toString() {
        return "{" +
                "query:\"" + query + '\"' +
                ", runTime:" + runTime +
                ", status:\"" + status + '\"' +
                ", organicResults:" + organicResults +
                ", adResults:" + adResults +
                '}';
    }
}
