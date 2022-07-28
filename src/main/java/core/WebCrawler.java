package core;

import dataStructures.SingleResult;
import dataStructures.SearchResult;
import dataStructures.SubLink;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WebCrawler {

    // driver that will be used for crawling
    WebDriver driver;
    // maximum number of pages to scan for each search
    int noOfPages;

    private String filterTitle = "";
    private String filterLink = "";
    private String filterSnippet = "";

    public void setFilter(String title, String link, String snippet){
        filterTitle = title;
        filterLink = link;
        filterSnippet = snippet;
    }

    // constructor with noOfPages
    public WebCrawler(int noOfPages) {
        this.noOfPages = noOfPages;
    }

    // default constructor with noOfPages set to 1
    public WebCrawler(){
        noOfPages = 1;
    }

    // initial setup using WebDriverManager (Must be called before using doSearch)
    public void doSetup(){
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
    }

    // closes the driver
    public void tearDown(){
        driver.close();
    }

    // do a single search
    public SearchResult doSearch(String searchQuery){
        long startTime = System.currentTimeMillis();
        driver.get("https://www.google.de/");

        // press the "I Agree" button if available
        try {
            driver.findElement(By.id("L2AGLb")).click();
        }catch (NoSuchElementException | ElementNotInteractableException e) {
            //ignore
        }

        // get the search bar, enter query and press enter
        WebElement searchBar = driver.findElement(By.xpath("//input[@name='q' and @type='text']"));
        searchBar.sendKeys(searchQuery);
        searchBar.sendKeys(Keys.ENTER);

        WebDriverWait wait = new WebDriverWait(driver, 200);

        // setting default values for the completeResult
        SearchResult completeResult = new SearchResult();
        completeResult.query = searchQuery;
        completeResult.status = "running";

        completeResult.adResults = new ArrayList<>();
        completeResult.organicResults = new ArrayList<>();

        // looping for the number of pages
        for (int i = 1; i <= noOfPages; i++){

            wait.until(ExpectedConditions.elementToBeClickable(By.id("rso")));
            completeResult.adResults.addAll(getAdResults());
            completeResult.organicResults.addAll(getOrganicResults());
//            scrollToBottom();

            if(i != noOfPages)
                try{
                    driver.findElement(By.id("pnnext")).click();
                } catch (ElementNotInteractableException | NoSuchElementException e){
                    break;
                }
        }

        completeResult.status = "completed";
        long endTime = System.currentTimeMillis();

        completeResult.runTime = endTime-startTime;
        return completeResult;
    }

    // does multiple searches based on the length of the string array passed
    public List<SearchResult> doSearches(String[] queries){
        List<SearchResult> results = new ArrayList<>();
        for (String query: queries){
            results.add(doSearch(query));
        }
        return results;
    }

    // gets the ad results (if any) on the page
    public List<SingleResult> getAdResults(){
        List<SingleResult> results = new ArrayList<>();

        try {

            SingleResult result = new SingleResult();

            WebElement topAd = driver
                    .findElement(By.id("tads"))
                    .findElement(By.xpath("*/div/div"));

            result.link = topAd
                    .findElement(By.tagName("a"))
                    .getAttribute("href");

            result.title = topAd
                    .findElement(By.tagName("a"))
                    .findElement(By.tagName("span"))
                    .getText();

            result.displayLink = topAd
                    .findElement(By.tagName("a"))
                    .findElement(By.xpath("*//span[@role='text']"))
                    .getText();

            result.snippet = topAd.findElement(By.xpath("//*[@id='tads']/div/div/div/div[2]")).getText();

            results.add(result);

        }catch (NoSuchElementException e){
            // No Ad (to top ad element found)
        }
        return results;
    }

    // get the organic results on the page
    public List<SingleResult> getOrganicResults(){

        WebElement searchResultContainer = driver.findElement(By.id("rso"));

        List<WebElement> searchResults = new ArrayList<>();
        for (WebElement element : searchResultContainer.findElements(By.xpath("*"))) {
            if (Objects.equals(element.getTagName(), "div")){
                searchResults.add(element);
            }
        }

        List<SingleResult> organicResults = new ArrayList<>();
        for (WebElement result : searchResults){
            try {
                SingleResult singleResult = getSingleOrganicResult(result);
                if (singleResult != null)
                    organicResults.add(singleResult);
            }catch (Exception e){
                //
            }
        }
        return organicResults;
    }

    // get the single result from the web element passed into it
    private SingleResult getSingleOrganicResult(WebElement resultContainer){

        // unpack the div if it is packed in another div (usually first organic result)
        if (resultContainer.getAttribute("class").equals("hlcw0c")) {
            resultContainer = resultContainer.findElement(By.tagName("div"));
        }

        // testing for searches that have links in table format
        try
        {
            // testing for table link
            try {
                WebElement headingContainer = resultContainer
                        .findElement(By.tagName("h2"));
                if(Objects.equals(headingContainer.getText(), "Web result with site links")){

                    SingleResult result = new SingleResult();

                    WebElement dataContainer = resultContainer
                            .findElement(By.tagName("div"));

                    WebElement headerContainer = dataContainer
                            .findElement(By.tagName("div"))
                            .findElement(By.tagName("div"));

                    WebElement tableContainer = dataContainer
                            .findElement(By.tagName("table"));

                    result.link = headerContainer
                            .findElement(By.tagName("a"))
                            .getAttribute("href");

                    result.title = headerContainer
                            .findElement(By.tagName("a"))
                            .findElement(By.tagName("h3"))
                            .getText();

                    result.displayLink = headerContainer
                            .findElement(By.tagName("a"))
                            .findElement(By.tagName("cite"))
                            .getText();

                    result.snippet = headerContainer
                            .findElements(By.tagName("span")).get(headerContainer.findElements(By.tagName("span")).size()-1)
                            .getText();

                    result.siteLinks = new ArrayList<>();

                    for (WebElement tableElement : tableContainer.findElements(By.tagName("td"))){
                        SubLink link = new SubLink();

                        link.link = tableElement
                                .findElement(By.tagName("a"))
                                .getAttribute("href");

                        link.title = tableElement.
                                findElement(By.tagName("a"))
                                .getText();

                        try{
                            link.snippet = tableElement
                                    .findElement(By.tagName("div"))
                                    .findElement(By.tagName("div"))
                                    .getText();

                        }catch (NoSuchElementException e){
                            link.snippet = null;
                        }
                        result.siteLinks.add(link);
                    }

                    goToPageAndBack(headerContainer.findElement(By.tagName("a")), result.snippet);
                    return result;
                }
            }catch (NoSuchElementException e){
                //
            }

            SingleResult result = new SingleResult();

            result.link = resultContainer
                    .findElement(By.tagName("a"))
                    .getAttribute("href");

            result.title = resultContainer
                    .findElement(By.tagName("a"))
                    .findElement(By.tagName("h3"))
                    .getText();

            result.displayLink = resultContainer
                    .findElement(By.tagName("a"))
                    .findElement(By.tagName("cite"))
                    .getText();

            result.snippet = resultContainer
                    .findElement(By.xpath("*/div/div[2]"))
                    .findElement(By.tagName("span"))
                    .getText();
            goToPageAndBack(resultContainer.findElement(By.tagName("a")), result.snippet);
            return result;

        }
        catch (Exception e){/**/}

        // testing for searches that have links in list format
        try
        {
            WebElement dataContainer = resultContainer
                    .findElement(By.tagName("div"));

            WebElement headerContainer = dataContainer
                    .findElement(By.tagName("div"))
                    .findElement(By.tagName("div"))
                    .findElement(By.tagName("div"));

            WebElement subLinkContainer = dataContainer.
                    findElements(By.xpath("*")).get(1);

            if (headerContainer.getAttribute("data-sokoban-grid") == null){
                headerContainer = headerContainer.findElement(By.tagName("div"));
            }
            SingleResult result = new SingleResult();
            result.link = headerContainer
                    .findElement(By.tagName("a"))
                    .getAttribute("href");
            result.title = headerContainer
                    .findElement(By.tagName("a"))
                    .findElement(By.tagName("h3"))
                    .getText();
            result.displayLink = headerContainer
                    .findElement(By.tagName("a"))
                    .findElement(By.tagName("cite"))
                    .getText();
            result.snippet = headerContainer
                    .findElements(By.xpath("*")).get(1)
                    .getText();

            result.siteLinks = new ArrayList<>();
            for (WebElement subLink : subLinkContainer.findElements(By.tagName("li"))){

                WebElement subLinkHeaderContainer = subLink
                        .findElement(By.tagName("div"))
                        .findElement(By.tagName("div"))
                        .findElement(By.tagName("div"));

                if (subLinkHeaderContainer.getAttribute("data-sokoban-grid") == null){
                    subLinkHeaderContainer = subLinkHeaderContainer.findElement(By.tagName("div"));
                }

                SubLink link = new SubLink();
                link.link = subLinkHeaderContainer
                        .findElement(By.tagName("a"))
                        .getAttribute("href");

                link.title = subLinkHeaderContainer
                        .findElement(By.tagName("a"))
                        .findElement(By.tagName("h3"))
                        .getText();

                link.displayLink = subLinkHeaderContainer
                        .findElement(By.tagName("a"))
                        .findElement(By.tagName("cite"))
                        .getText();

                link.snippet = subLinkHeaderContainer
                        .findElements(By.xpath("*")).get(1)
                        .getText();

                result.siteLinks.add(link);
            }

            goToPageAndBack(headerContainer.findElement(By.tagName("a")), result.snippet);
            return result;
        }
        catch (Exception e){/**/}

        // testing for generic searches with no sub-links
        try
        {
            SingleResult result = new SingleResult();
            WebElement header = resultContainer
                    .findElement(By.xpath("*//div[@data-header-feature='0']"));

            result.link = header
                    .findElement(By.tagName("a"))
                    .getAttribute("href");

            result.title = header
                    .findElement(By.tagName("a"))
                    .findElement(By.tagName("h3"))
                    .getText();

            result.displayLink = header
                    .findElement(By.tagName("a"))
                    .findElement(By.tagName("cite"))
                    .getText();

            result.snippet = resultContainer
                    .findElement(By.xpath("*//div[@data-content-feature='1']"))
                    .getText();

            goToPageAndBack(header.findElement(By.tagName("a")), result.snippet);
            return result;
        }
        catch (Exception e) {/**/}

        return null;
    }

    // Goes to the page given in the href of the element and then goes back
    public void goToPageAndBack(WebElement element, String snippet){
        try {
            scrollToElement(element);
            String link = element.getAttribute("href");
            String title = element.findElement(By.tagName("h3")).getText();

            // any kind of condition based on the title, snippet and/or link of the WebElement (Search Result)
            if (link.contains(filterLink) && title.contains(filterTitle) && snippet.contains(filterSnippet)) {
                ((JavascriptExecutor)driver).executeScript("arguments[0].click();",element );
                driver.navigate().back();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    // Scrolls the page so that the element e is in view (Called before clicking on any element)
    public void scrollToElement(WebElement e){
        ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();",e );
    }

}
