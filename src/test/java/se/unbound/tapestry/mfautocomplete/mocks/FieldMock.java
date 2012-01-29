package se.unbound.tapestry.mfautocomplete.mocks;

import org.apache.tapestry5.Field;

public class FieldMock implements Field {
    private final String clientId;

    public FieldMock(final String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientId() {
        return this.clientId;
    }

    @Override
    public String getControlName() {
        return null;
    }

    @Override
    public String getLabel() {
        return null;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }

    @Override
    public boolean isRequired() {
        return false;
    }
}
