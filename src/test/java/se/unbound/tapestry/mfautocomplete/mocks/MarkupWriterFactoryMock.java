package se.unbound.tapestry.mfautocomplete.mocks;

import org.apache.tapestry5.ContentType;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.internal.services.MarkupWriterImpl;
import org.apache.tapestry5.services.MarkupWriterFactory;

public class MarkupWriterFactoryMock implements MarkupWriterFactory {
    @Override
    public MarkupWriter newMarkupWriter(final ContentType contentType) {
        return null;
    }

    @Override
    public MarkupWriter newPartialMarkupWriter(final ContentType contentType) {
        return new MarkupWriterImpl();
    }

    @Override
    public MarkupWriter newMarkupWriter(final String pageName) {
        return null;
    }
}
