package se.unbound.tapestry.mfautocomplete.services;

import org.apache.tapestry5.ioc.Configuration;
import org.apache.tapestry5.services.LibraryMapping;

/**
 * Module class for the bread crumb module.
 */
public class MFAutocompleteModule {
    /**
     * Contributes a new {@link LibraryMapping} to the component class resolver.
     * 
     * @param configuration The {@link LibraryMapping} configuration to add the new mapping to.
     */
    public static void contributeComponentClassResolver(final Configuration<LibraryMapping> configuration) {
        configuration.add(new LibraryMapping("mfautocomplete", "se.unbound.tapestry.mfautocomplete"));
    }
}
