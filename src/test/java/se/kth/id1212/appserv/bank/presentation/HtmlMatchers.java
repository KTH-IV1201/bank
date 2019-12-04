package se.kth.id1212.appserv.bank.presentation;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Creates hamcrest matchers for an html document.
 */
public class HtmlMatchers {
    private static final String HTML_START = "<html";
    private static final String HTML_END = "</html>";

    /**
     * Creates a matcher that matches when the examined html document contains one
     * or more elements with the specified css selector.
     *
     * @param cssSelector What to search for in the examined html document.
     * @return The desired matcher.
     */
    static ElementExists containsElement(String cssSelector) {
        return new ElementExists(cssSelector);
    }

    /**
     * Creates a matcher that matches when the examined html document contains no
     * elements with the specified css selector.
     *
     * @param cssSelector What to search for in the examined html document.
     * @return The desired matcher.
     */
    static ElementDoesNotExist doesNotContainElement(String cssSelector) {
        return new ElementDoesNotExist(cssSelector);
    }

    public static class ElementExists extends TypeSafeMatcher<String> {
        private final String cssSelector;

        ElementExists(String cssSelector) {
            this.cssSelector = cssSelector;
        }

        @Override
        protected boolean matchesSafely(String httpResponse) {
            String html = extractHtmlDoc(httpResponse);
            Document htmlDoc = Jsoup.parse(html);
            Elements elements = htmlDoc.select(cssSelector);
            return !elements.isEmpty();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("document contains element matching \"" + cssSelector + "\"");
        }
    }

    public static class ElementDoesNotExist extends TypeSafeMatcher<String> {
        private final String cssSelector;

        ElementDoesNotExist(String cssSelector) {
            this.cssSelector = cssSelector;
        }

        @Override
        protected boolean matchesSafely(String httpResponse) {
            String html = extractHtmlDoc(httpResponse);
            Document htmlDoc = Jsoup.parse(html);
            Elements elements = htmlDoc.select(cssSelector);
            return elements.isEmpty();
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("document does not contain element matching \"" + cssSelector + "\"");
        }
    }

    private static String extractHtmlDoc(String httpResponse) {
        int htmlStartPos = httpResponse.indexOf(HTML_START);
        int htmlEndPos = httpResponse.indexOf(HTML_END) + HTML_END.length();
        return httpResponse.substring(htmlStartPos, htmlEndPos);
    }
}
