package se.unbound.tapestry.mfautocomplete.mocks;

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.Translator;
import org.apache.tapestry5.services.TranslatorSource;

public class TranslatorSourceMock implements TranslatorSource {
    private final Map<Class, Translator> translators = new HashMap<Class, Translator>();

    public void addTranslator(final Class valueType, final Translator translator) {
        this.translators.put(valueType, translator);
    }

    @Override
    public Translator findByType(final Class valueType) {
        return this.translators.get(valueType);
    }

    // Not implemented

    @Override
    public Translator get(final String name) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public Translator getByType(final Class valueType) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
