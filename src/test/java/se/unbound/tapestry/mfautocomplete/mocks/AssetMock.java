package se.unbound.tapestry.mfautocomplete.mocks;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ioc.Resource;

public class AssetMock implements Asset {
    @Override
    public String toClientURL() {
        return null;
    }

    @Override
    public Resource getResource() {
        return null;
    }
}
