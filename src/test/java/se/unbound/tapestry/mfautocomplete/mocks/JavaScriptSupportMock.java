package se.unbound.tapestry.mfautocomplete.mocks;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldFocusPriority;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.InitializationPriority;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.services.javascript.StylesheetLink;

/**
 * Mock implementation of the JavaScriptSupport interface for unit testing.
 */
public class JavaScriptSupportMock implements JavaScriptSupport {
    private String fieldIdForAutofocus;
    private final Map<String, JSONObject> addedInits = new HashMap<String, JSONObject>();
    private final List<String> addedScripts = new ArrayList<String>();

    @Override
    public void autofocus(final FieldFocusPriority priority, final String fieldId) {
        this.fieldIdForAutofocus = fieldId;
    }

    @Override
    public void addInitializerCall(final String functionName, final JSONObject parameter) {
        this.addedInits.put(functionName, parameter);
    }

    public String getFieldIdForAutofocus() {
        return this.fieldIdForAutofocus;
    }

    @Override
    public String allocateClientId(final String id) {
        return "allocated-" + id;
    }

    @Override
    public void addScript(final String format, final Object... arguments) {
        this.addedScripts.add(String.format(format, arguments));
    }

    @Override
    public void addScript(final InitializationPriority priority, final String format,
            final Object... arguments) {
        this.addedScripts.add(String.format(format, arguments));
    }

    /**
     * Retrieves an added init-code by its function name.
     * 
     * @param functionName The function name to retrieve init-code for.
     * @return the init-code for the provided function or null if no init-code has been added.
     */
    public JSONObject getInit(final String functionName) {
        return this.addedInits.get(functionName);
    }

    /**
     * Verifies that the provided script was added to the rendering.
     * 
     * @param expectedScript The expected script.
     */
    public void assertScriptAdded(final String expectedScript) {
        if (!this.addedScripts.contains(expectedScript)) {
            fail("Expected script " + expectedScript + " not added. Added scripts are: "
                    + this.addedScripts.toString());
        }
    }

    // Not implemented

    @Override
    public String allocateClientId(final ComponentResources resources) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void addInitializerCall(final InitializationPriority priority, final String functionName,
            final JSONObject parameter) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void addInitializerCall(final String functionName, final String parameter) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void addInitializerCall(final InitializationPriority priority, final String functionName,
            final String parameter) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void importJavaScriptLibrary(final Asset asset) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void importStylesheet(final Asset stylesheet) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void importStylesheet(final StylesheetLink stylesheetLink) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void importStack(final String stackName) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void importJavaScriptLibrary(final String libraryURL) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void addInitializerCall(final String functionName, final JSONArray parameter) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void addInitializerCall(final InitializationPriority priority, final String functionName,
            final JSONArray parameter) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}