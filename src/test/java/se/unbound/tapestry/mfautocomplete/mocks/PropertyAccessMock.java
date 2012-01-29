package se.unbound.tapestry.mfautocomplete.mocks;

import java.lang.reflect.Field;

import org.apache.tapestry5.ioc.services.ClassPropertyAdapter;
import org.apache.tapestry5.ioc.services.PropertyAccess;

public class PropertyAccessMock implements PropertyAccess {
    @Override
    public Object get(final Object instance, final String propertyName) {
        return this.getPropertyValue(instance, propertyName);
    }

    private Object getPropertyValue(final Object instance, final String propertyName) {
        final Field field = this.findField(instance.getClass(), propertyName);
        field.setAccessible(true);
        try {
            return field.get(instance);
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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

    // Not implemented

    @Override
    public void set(final Object instance, final String propertyName, final Object value) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public ClassPropertyAdapter getAdapter(final Object instance) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public ClassPropertyAdapter getAdapter(final Class forClass) {
        throw new UnsupportedOperationException("Not yet implemented!");
    }

    @Override
    public void clearCache() {
        throw new UnsupportedOperationException("Not yet implemented!");
    }
}
