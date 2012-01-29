package se.unbound.tapestry.mfautocomplete.services;

import static org.junit.Assert.assertNotNull;

import org.apache.tapestry5.services.LibraryMapping;
import org.junit.Test;

import se.unbound.tapestry.mfautocomplete.mocks.ConfigurationMock;
import se.unbound.tapestry.mfautocomplete.mocks.LibraryMappingChecker;

public class MFAutocompleteModuleTest {
    @Test
    public void instantiation() {
        final MFAutocompleteModule module = new MFAutocompleteModule();
        assertNotNull(module);
    }

    @Test
    public void testContributeComponentClassResolver() {
        final ConfigurationMock<LibraryMapping> configuration = new ConfigurationMock<LibraryMapping>();
        MFAutocompleteModule.contributeComponentClassResolver(configuration);
        configuration.assertConfiguration(new LibraryMappingChecker("mfautocomplete",
                "se.unbound.tapestry.mfautocomplete"));
    }
}
