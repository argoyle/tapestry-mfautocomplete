package se.unbound.tapestry.mfautocomplete.mixins;

import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.CSSClassConstants;
import org.apache.tapestry5.ComponentEventCallback;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.Field;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.Translator;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Events;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectContainer;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.internal.util.Holder;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.PropertyAccess;
import org.apache.tapestry5.ioc.services.TypeCoercer;
import org.apache.tapestry5.json.JSONLiteral;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.MarkupWriterFactory;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.ResponseRenderer;
import org.apache.tapestry5.services.TranslatorSource;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.apache.tapestry5.util.TextStreamResponse;

/**
 * A mixin for a text field that allows for autocompletion of multiple fields. This is based on Tapestrys Autocomplete
 * mixin and Prototype's autocompleter control.
 * <p/>
 * 
 * The container is responsible for providing an event handler for event "providecompletions". The context will be the
 * partial input string sent from the client. The return value should be an array or list of completions, in
 * presentation order. I.e.
 * <p/>
 * 
 * <pre>
 * String[] onProvideCompletionsFromMyField(String input)
 * {
 *   return . . .;
 * }
 * </pre>
 */
@Import(library = { "${tapestry.scriptaculous}/controls.js", "MultifieldAutocomplete.js" })
@Events(EventConstants.PROVIDE_COMPLETIONS)
public class MultifieldAutocomplete {
    static final String EVENT_NAME = "autocomplete";
    private static final String PARAM_NAME = "t:input";

    /**
     * The field component to which this mixin is attached.
     */
    @InjectContainer
    private Field field;

    @Inject
    private ComponentResources resources;

    @Inject
    private PropertyAccess access;

    @Environmental
    private JavaScriptSupport javaScriptSupport;

    @Inject
    private Request request;

    @Inject
    private TypeCoercer coercer;

    @Inject
    private MarkupWriterFactory factory;

    @Inject
    private TranslatorSource translatorSource;

    @Inject
    @Path("${tapestry.spacer-image}")
    private Asset spacerImage;

    /**
     * Overwrites the default minimum characters to trigger a server round trip (the default is 1).
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private int minChars;

    @Inject
    private ResponseRenderer responseRenderer;

    /**
     * Overrides the default check frequency for determining whether to send a server request. The default is .4
     * seconds.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private double frequency;

    /**
     * If given, then the autocompleter will support multiple input values, separated by any of the individual
     * characters in the string.
     */
    @Parameter(defaultPrefix = BindingConstants.LITERAL)
    private String tokens;

    @Parameter
    private List<String> properties;

    @Parameter(required = true, allowNull = false)
    private List<String> fields;

    /**
     * Mixin afterRender phrase occurs after the component itself. This is where we write the &lt;div&gt; element and
     * the JavaScript.
     * 
     * @param writer
     */
    void afterRender(final MarkupWriter writer) {
        final String id = this.field.getClientId();

        final String menuId = id + ":menu";
        final String loaderId = id + ":loader";

        // The spacer image is used as a placeholder, allowing CSS to determine what image
        // is actually displayed.

        writer.element("img",

                "src", this.spacerImage.toClientURL(),

                "class", "t-autoloader-icon " + CSSClassConstants.INVISIBLE,

                "alt", "",

                "id", loaderId);
        writer.end();

        writer.element("div",

                "id", menuId,

                "class", "t-autocomplete-menu");
        writer.end();

        final Link link = this.resources.createEventLink(MultifieldAutocomplete.EVENT_NAME);

        final JSONObject config = new JSONObject();
        config.put("paramName", MultifieldAutocomplete.PARAM_NAME);
        config.put("indicator", loaderId);

        if (this.resources.isBound("minChars")) {
            config.put("minChars", this.minChars);
        }

        if (this.resources.isBound("frequency")) {
            config.put("frequency", this.frequency);
        }

        if (this.resources.isBound("tokens")) {
            for (int i = 0; i < this.tokens.length(); i++) {
                config.accumulate("tokens", this.tokens.substring(i, i + 1));
            }
        }

        config.put("afterUpdateElement", new JSONLiteral("function(field, item) { \n" + id
                + ".multifieldautocompleter.updateFields(field, item);\n}"));

        // Let subclasses do more.
        this.configure(config);

        // Initializes scripts
        final String autocompleter = String.format("new Ajax.Autocompleter('%s', '%s', '%s', %s)",
                id, menuId, link.toAbsoluteURI(), config);
        final String fieldNames = this.getFieldNames();
        this.javaScriptSupport
                .addScript(
                        "$('%1$s').multifieldautocompleter = " +
                                "new MultifieldAutocomplete('%1$s', %2$s, '%3$s', '%4$s');",
                        id, autocompleter, fieldNames, this.resources.getId());
    }

    private String getFieldNames() {
        final StringBuilder fieldNames = new StringBuilder();
        for (final String each : this.fields) {
            fieldNames.append(each).append(",");
        }
        fieldNames.setLength(fieldNames.length() - 1);
        return fieldNames.toString();
    }

    Object onAutocomplete() {
        final String input = this.request.getParameter(MultifieldAutocomplete.PARAM_NAME);

        final Holder<List<?>> matchesHolder = Holder.create();

        // Default it to an empty list.

        matchesHolder.put(Collections.emptyList());

        final ComponentEventCallback<?> callback = new CallbackImpl(matchesHolder);

        this.resources.triggerEvent(EventConstants.PROVIDE_COMPLETIONS, new Object[] { input, }, callback);

        final ContentType contentType = this.responseRenderer.findContentType(this);

        final MarkupWriter writer = this.factory.newPartialMarkupWriter(contentType);

        this.generateResponseMarkup(writer, matchesHolder.get());

        return new TextStreamResponse(contentType.toString(), writer.toString());
    }

    /**
     * Invoked to allow subclasses to further configure the parameters passed to the JavaScript Ajax.Autocompleter
     * options. The values minChars, frequency and tokens my be pre-configured. Subclasses may override this method to
     * configure additional features of the Ajax.Autocompleter.
     * <p/>
     * <p/>
     * This implementation does nothing.
     * 
     * @param config parameters object
     */
    protected void configure(final JSONObject config) {
    }

    /**
     * Generates the markup response that will be returned to the client; this should be an &lt;ul&gt; element with
     * nested &lt;li&gt; elements. Subclasses may override this to produce more involved markup (including images and
     * CSS class attributes).
     * 
     * @param writer to write the list to
     * @param matches list of matching objects, each should be converted to a string
     */
    protected void generateResponseMarkup(final MarkupWriter writer, final List<?> matches) {
        writer.element("ul");
        final List<String> props = this.getPropertiesToRead();
        for (final Object o : matches) {
            writer.element("li");
            writer.write(o.toString());
            for (int i = 0; i < props.size(); i++) {
                final Object value = this.access.get(o, props.get(i));
                final String fieldValue = this.translateFieldValue(value);
                writer.attributes(this.fields.get(i), fieldValue);
            }
            writer.end();
        }

        writer.end();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private String translateFieldValue(final Object value) {
        String translatedValue = null;
        if (value != null) {
            final Translator translator = this.translatorSource.findByType(value.getClass());
            if (translator != null) {
                translatedValue = translator.toClient(value);
            } else {
                translatedValue = value.toString();
            }
        }
        return translatedValue;
    }

    private List<String> getPropertiesToRead() {
        List<String> props;
        if (this.properties != null) {
            props = this.properties;
        } else {
            props = this.fields;
        }
        return props;
    }

    /**
     * Implementation of the ComponentEventCallback interface which populates the provided Holder-object with
     * autocompletion results.
     */
    private final class CallbackImpl implements ComponentEventCallback<Object> {
        private final Holder<List<?>> matchesHolder;

        private CallbackImpl(final Holder<List<?>> matchesHolder) {
            this.matchesHolder = matchesHolder;
        }

        @Override
        public boolean handleResult(final Object result) {
            final List<?> matches = MultifieldAutocomplete.this.coercer.coerce(result, List.class);

            this.matchesHolder.put(matches);

            return true;
        }
    }
}
