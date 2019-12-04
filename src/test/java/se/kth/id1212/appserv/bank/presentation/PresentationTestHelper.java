package se.kth.id1212.appserv.bank.presentation;

import org.hamcrest.Matcher;
import org.hamcrest.core.AllOf;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

/**
 * Functional library for testing the presentation layer.
 */
public class PresentationTestHelper {
    private PresentationTestHelper() {
    }

    /**
     * Creates a <code>org.springframework.test.web.servlet.ResultMatcher</code>
     * which verifies that the specified elements exist in the page content.
     *
     * @param cssSelectors CSS selectors identifying the HTML elements that shall
     *                     exist.
     * @return The desired matcher.
     */
    public static ResultMatcher containsElements(String... cssSelectors) {
        List<Matcher<? super String>> matchers = new ArrayList<>();
        for (String selector : cssSelectors) {
            matchers.add(HtmlMatchers.containsElement(selector));
        }
        return content().string(AllOf.allOf(matchers));
    }

    /**
     * Creates a <code>org.springframework.test.web.servlet.ResultMatcher</code>
     * which verifies that the specified elements exist in the page content.
     *
     * @param cssSelectors CSS selectors identifying the HTML elements that shall
     *                     exist.
     * @return The desired matcher.
     */
    public static ResultMatcher doesNotContainElements(String... cssSelectors) {
        List<Matcher<? super String>> matchers = new ArrayList<>();
        for (String selector : cssSelectors) {
            matchers.add(HtmlMatchers.doesNotContainElement(selector));
        }
        return content().string(AllOf.allOf(matchers));
    }

    /**
     * Simulates a get request to the specified Url, using the Spring MVC Test
     * Framework. Using this framework means the call is done in-container, but
     * without a server. Therefore, there shall not be any context path in the Url.
     *
     * @param mockMvc The {@link MockMvc} object to use when sending the request.
     * @param Url     The Url to which the request is sent. A slash ("/") is
     *                prepended to the Url if it does not already start with a
     *                slash.
     * @param session The http session in which the call is made.
     * @return A {@link ResultActions} object that can be used to evaluate the
     *         response.
     * @throws Exception If the request fails.
     */
    public static ResultActions sendGetRequest(MockMvc mockMvc, String Url, HttpSession session) throws Exception {
        if (!Url.startsWith("/")) {
            Url = "/" + Url;
        }

        if (session == null) {
            return mockMvc.perform(get(Url));
        }

        return mockMvc.perform(get(Url).session((MockHttpSession) session));
    }

    /**
     * Identical to {@link #sendGetRequest(MockMvc, String, HttpSession)}, except
     * that a new http session is started in each call.
     */
    public static ResultActions sendGetRequest(MockMvc mockMvc, String Url) throws Exception {
        return sendGetRequest(mockMvc, Url, null);
    }

    /**
     * Simulates a post request to the specified Url, using the Spring MVC Test
     * Framework. Using this framework means the call is done in-container, but
     * without a server. Therefore, there shall not be any context path in the Url.
     *
     * @param mockMvc The {@link MockMvc} object to use when sending the request.
     * @param Url     The Url to which the request is sent. A slash ("/") is
     *                prepended to the Url if it does not already start with a
     *                slash.
     * @param session The http session in which the call is made.
     * @param params  The post parameters to include in the request.
     * @return A {@link ResultActions} object that can be used to evaluate the
     *         response.
     * @throws Exception If the request fails.
     */
    public static ResultActions sendPostRequest(MockMvc mockMvc, String Url, HttpSession session,
            MultiValueMap<String, String> params) throws Exception {
        if (!Url.startsWith("/")) {
            Url = "/" + Url;
        }

        if (session == null && params == null) {
            return mockMvc.perform(post(Url));
        }

        if (session == null && params != null) {
            return mockMvc.perform(post(Url).params(params));
        }

        if (session != null && params == null) {
            return mockMvc.perform(post(Url).session((MockHttpSession) session));
        }

        return mockMvc.perform(post(Url).session((MockHttpSession) session).params(params));
    }

    /**
     * Identical to
     * {@link #sendPostRequest(MockMvc, String, HttpSession, MultiValueMap)}, except
     * that no post parameters are included in the request.
     */
    public static ResultActions sendPostRequest(MockMvc mockMvc, String Url, HttpSession session) throws Exception {
        return sendPostRequest(mockMvc, Url, session, null);
    }

    /**
     * Identical to
     * {@link #sendPostRequest(MockMvc, String, HttpSession, MultiValueMap)}, except
     * that a new http session is started in each call. parameters are included in
     * the request.
     */
    public static ResultActions sendPostRequest(MockMvc mockMvc, String Url, MultiValueMap<String, String> params)
            throws Exception {
        return sendPostRequest(mockMvc, Url, null, params);
    }

    /**
     * Identical to
     * {@link #sendPostRequest(MockMvc, String, HttpSession, MultiValueMap)}, except
     * that a new http session is started in each call, and that no post parameters
     * are included in the request.
     */
    public static ResultActions sendPostRequest(MockMvc mockMvc, String Url) throws Exception {
        return sendPostRequest(mockMvc, Url, null, null);
    }

    /**
     * Adds a parameter with the specified name and value to the specified map. The
     * map is supposed to be used as parameters in a http post request.
     *
     * @param params The map with parameters.
     * @param name   The name of the post parameter to add.
     * @param value  The value of the post parameter to add.
     * @return The same map that was passed, with the specified parameter added.
     */
    public static MultiValueMap<String, String> addParam(MultiValueMap<String, String> params, String name,
            String value) {
        params.add(name, value);
        return params;
    }

    /**
     * Creates an empty map and adds a parameter with the specified name and value.
     * The map is supposed to be used as parameters in a http post request.
     *
     * @param name  The name of the post parameter to add.
     * @param value The value of the post parameter to add.
     * @return A map with the specified parameter added.
     */
    public static MultiValueMap<String, String> addParam(String name, String value) {
        return addParam(new LinkedMultiValueMap<>(), name, value);
    }
}
