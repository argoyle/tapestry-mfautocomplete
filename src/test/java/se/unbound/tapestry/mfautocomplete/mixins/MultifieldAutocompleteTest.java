package se.unbound.tapestry.mfautocomplete.mixins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.services.FormSupport;
import org.apache.tapestry5.util.TextStreamResponse;
import org.junit.Before;
import org.junit.Test;

import se.unbound.tapestry.mfautocomplete.mocks.AssetMock;
import se.unbound.tapestry.mfautocomplete.mocks.ComponentResourcesMock;
import se.unbound.tapestry.mfautocomplete.mocks.FieldMock;
import se.unbound.tapestry.mfautocomplete.mocks.JavaScriptSupportMock;
import se.unbound.tapestry.mfautocomplete.mocks.MarkupWriterFactoryMock;
import se.unbound.tapestry.mfautocomplete.mocks.MarkupWriterMock;
import se.unbound.tapestry.mfautocomplete.mocks.PropertyAccessMock;
import se.unbound.tapestry.mfautocomplete.mocks.RequestMock;
import se.unbound.tapestry.mfautocomplete.mocks.ResponseRendererMock;
import se.unbound.tapestry.mfautocomplete.mocks.TranslatorSourceMock;
import se.unbound.tapestry.mfautocomplete.mocks.TypeCoercerMock;

public class MultifieldAutocompleteTest {
    private final MultifieldAutocomplete autocomplete = new MultifieldAutocomplete();
    private final ComponentResourcesMock resources = new ComponentResourcesMock();
    private final MarkupWriter writer = new MarkupWriterMock();
    private final JavaScriptSupportMock javaScriptSupport = new JavaScriptSupportMock();
    private final RequestMock request = new RequestMock();
    private final ResponseRendererMock responseRenderer = new ResponseRendererMock();
    private final MarkupWriterFactoryMock factory = new MarkupWriterFactoryMock();
    private final TypeCoercerMock coercer = new TypeCoercerMock();
    private final PropertyAccessMock access = new PropertyAccessMock();
    private final TranslatorSourceMock translatorSource = new TranslatorSourceMock();

    @Before
    public void setUp() {
        this.setPropertyValue(this.autocomplete, "field", new FieldMock("field"));
        this.setPropertyValue(this.autocomplete, "spacerImage", new AssetMock());
        this.setPropertyValue(this.autocomplete, "resources", this.resources);
        this.setPropertyValue(this.autocomplete, "javaScriptSupport", this.javaScriptSupport);
        this.setPropertyValue(this.autocomplete, "fields", Arrays.asList("field1", "field2"));
        this.setPropertyValue(this.autocomplete, "request", this.request);
        this.setPropertyValue(this.autocomplete, "responseRenderer", this.responseRenderer);
        this.setPropertyValue(this.autocomplete, "factory", this.factory);
        this.setPropertyValue(this.autocomplete, "coercer", this.coercer);
        this.setPropertyValue(this.autocomplete, "access", this.access);
        this.setPropertyValue(this.autocomplete, "translatorSource", this.translatorSource);
    }

    @Test
    public void afterRenderWritesAdditionalMarkupForAutocompleter() {
        this.autocomplete.afterRender(this.writer);

        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter);
        this.writer.toMarkup(printWriter);
        assertEquals(
                "generated content",
                "<html><img id=\"field:loader\" alt=\"\" class=\"t-autoloader-icon t-invisible\"/><div class=\"t-autocomplete-menu\" id=\"field:menu\"></div></html>",
                stringWriter.toString());
    }

    @Test
    public void afterRenderAddsJavascriptInitializerForAutocompleter() {
        this.autocomplete.afterRender(this.writer);

        this.javaScriptSupport
                .assertScriptAdded("$('field').multifieldautocompleter = "
                        + "new MultifieldAutocomplete('field', new Ajax.Autocompleter('field', 'field:menu', 'autocomplete', {\n"
                        + "  \"indicator\" : \"field:loader\",\n"
                        + "  \"afterUpdateElement\" : function(field, item) { \n"
                        + "field.multifieldautocompleter.updateFields(field, item);\n"
                        + "},\n"
                        + "  \"paramName\" : \"t:input\"\n"
                        + "}), 'field1,field2', 'null');");
    }

    @Test
    public void onAutocompleteReturnEmptyUlIfNoMatchesAreFound() throws Exception {
        this.request.addParameter("t:input", "abc");

        final TextStreamResponse result = (TextStreamResponse) this.autocomplete.onAutocomplete();
        assertNotNull("result", result);
        final byte[] buff = new byte[1024];
        final int readBytes = result.getStream().read(buff);
        assertEquals("response", "<ul></ul>", new String(buff, 0, readBytes));
    }

    @Test
    public void onAutocompleteReturnOneLiForEachFoundMatch() throws Exception {
        this.request.addParameter("t:input", "abc");

        this.resources.setEventResult(Arrays.asList(new ResultPOJO("aaa", 0.0, 2.2),
                new ResultPOJO("ccc", 7.5, 1.1)));

        final TextStreamResponse result = (TextStreamResponse) this.autocomplete.onAutocomplete();
        final byte[] buff = new byte[1024];
        final int readBytes = result.getStream().read(buff);
        assertEquals(
                "response",
                "<ul><li field2=\"0.0\" field1=\"aaa\">aaa-0.0</li><li field2=\"7.5\" field1=\"ccc\">ccc-7.5</li></ul>",
                new String(buff, 0, readBytes));
    }

    @Test
    public void onAutocompleteUsesTranslatorIfFound() throws Exception {
        this.request.addParameter("t:input", "abc");
        this.translatorSource.addTranslator(Double.class, new DoubleTranslator(new Locale("sv", "SE")));
        this.resources.setEventResult(Arrays.asList(new ResultPOJO("aaa", 0.0, 2.2),
                new ResultPOJO("ccc", 7.5, 1.1)));

        final TextStreamResponse result = (TextStreamResponse) this.autocomplete.onAutocomplete();
        final byte[] buff = new byte[1024];
        final int readBytes = result.getStream().read(buff);
        assertEquals(
                "response",
                "<ul><li field2=\"0\" field1=\"aaa\">aaa-0.0</li><li field2=\"7,5\" field1=\"ccc\">ccc-7.5</li></ul>",
                new String(buff, 0, readBytes));
    }

    @Test
    public void onAutocompleteUsesPropertiesParameterIfSet() throws Exception {
        this.setPropertyValue(this.autocomplete, "properties", Arrays.asList("field1", "field3"));
        this.request.addParameter("t:input", "abc");
        this.resources.setEventResult(Arrays.asList(new ResultPOJO("aaa", 0.0, 2.2),
                new ResultPOJO("ccc", 7.5, 1.1)));

        final TextStreamResponse result = (TextStreamResponse) this.autocomplete.onAutocomplete();
        final byte[] buff = new byte[1024];
        final int readBytes = result.getStream().read(buff);
        assertEquals(
                "response",
                "<ul><li field2=\"2.2\" field1=\"aaa\">aaa-0.0</li><li field2=\"1.1\" field1=\"ccc\">ccc-7.5</li></ul>",
                new String(buff, 0, readBytes));
    }

    @Test
    public void minCharsParameterIsAddedToConfig() {
        this.setPropertyValue(this.autocomplete, "minChars", 3);
        this.resources.setBound("minChars");
        this.autocomplete.afterRender(this.writer);
        this.javaScriptSupport
                .assertScriptAdded("$('field').multifieldautocompleter = "
                        + "new MultifieldAutocomplete('field', new Ajax.Autocompleter('field', 'field:menu', 'autocomplete', {\n"
                        + "  \"minChars\" : 3,\n"
                        + "  \"indicator\" : \"field:loader\",\n"
                        + "  \"afterUpdateElement\" : function(field, item) { \n"
                        + "field.multifieldautocompleter.updateFields(field, item);\n"
                        + "},\n"
                        + "  \"paramName\" : \"t:input\"\n"
                        + "}), 'field1,field2', 'null');");
    }

    @Test
    public void frequencyParameterIsAddedToConfig() {
        this.setPropertyValue(this.autocomplete, "frequency", 0.25);
        this.resources.setBound("frequency");
        this.autocomplete.afterRender(this.writer);
        this.javaScriptSupport
                .assertScriptAdded("$('field').multifieldautocompleter = "
                        + "new MultifieldAutocomplete('field', new Ajax.Autocompleter('field', 'field:menu', 'autocomplete', {\n"
                        + "  \"indicator\" : \"field:loader\",\n"
                        + "  \"afterUpdateElement\" : function(field, item) { \n"
                        + "field.multifieldautocompleter.updateFields(field, item);\n"
                        + "},\n"
                        + "  \"frequency\" : 0.25,\n"
                        + "  \"paramName\" : \"t:input\"\n"
                        + "}), 'field1,field2', 'null');");
    }

    @Test
    public void tokensParameterIsAddedToConfig() {
        this.setPropertyValue(this.autocomplete, "tokens", "abc");
        this.resources.setBound("tokens");
        this.autocomplete.afterRender(this.writer);
        this.javaScriptSupport
                .assertScriptAdded("$('field').multifieldautocompleter = new MultifieldAutocomplete('field', new Ajax.Autocompleter('field', 'field:menu', 'autocomplete', {\n"
                        +
                        "  \"indicator\" : \"field:loader\",\n" +
                        "  \"tokens\" : [\n" +
                        "    \"a\",\n" +
                        "    \"b\",\n" +
                        "    \"c\"\n" +
                        "  ],\n" +
                        "  \"afterUpdateElement\" : function(field, item) { \n" +
                        "field.multifieldautocompleter.updateFields(field, item);\n" +
                        "},\n" +
                        "  \"paramName\" : \"t:input\"\n" +
                        "}), 'field1,field2', 'null');");
    }

    private void setPropertyValue(final Object target, final String propertyName, final Object value) {
        final Field field = this.findField(target.getClass(), propertyName);

        this.assignValue(field, target, value);
    }

    private Field findField(final Class<? extends Object> targetClass, final String propertyName) {
        Field field = null;
        try {
            field = targetClass.getDeclaredField(propertyName);
        } catch (final SecurityException e) {
            throw new RuntimeException(e);
        } catch (final NoSuchFieldException e) {
            if (Object.class.getName().equals(targetClass.getName())) {
                throw new RuntimeException(e);
            }
            field = this.findField(targetClass.getSuperclass(), propertyName);
        }
        return field;
    }

    private void assignValue(final Field field, final Object target, final Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (final IllegalArgumentException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static class ResultPOJO {
        private final String field1;
        private final Double field2;
        private final Double field3;

        public ResultPOJO(final String field1, final Double field2, final Double field3) {
            this.field1 = field1;
            this.field2 = field2;
            this.field3 = field3;
        }

        @Override
        public String toString() {
            return this.field1 + "-" + this.field2;
        }
    }

    private static class DoubleTranslator implements Translator<Double> {
        private final Locale locale;

        public DoubleTranslator(final Locale locale) {
            this.locale = locale;
        }

        @Override
        public String toClient(final Double value) {
            return NumberFormat.getNumberInstance(this.locale).format(value);
        }

        // Not implemented

        @Override
        public String getName() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public Class<Double> getType() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public String getMessageKey() {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public Double parseClient(final org.apache.tapestry5.Field field, final String clientValue,
                final String message)
                throws ValidationException {
            throw new UnsupportedOperationException("Not yet implemented!");
        }

        @Override
        public void render(final org.apache.tapestry5.Field field, final String message,
                final MarkupWriter writer,
                final FormSupport formSupport) {
            throw new UnsupportedOperationException("Not yet implemented!");
        }
    }
}
