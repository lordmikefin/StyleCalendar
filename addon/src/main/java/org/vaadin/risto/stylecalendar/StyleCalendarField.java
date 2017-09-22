package org.vaadin.risto.stylecalendar;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

import org.vaadin.risto.stylecalendar.client.shared.field.StyleCalendarFieldRpc;
import org.vaadin.risto.stylecalendar.client.shared.field.StyleCalendarFieldState;

import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.ui.AbstractField;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;

/**
 * Date selector component that uses a {@link StyleCalendar} as a popup for date
 * selection.
 *
 * @author Risto Yrjänä / Vaadin
 */
public class StyleCalendarField extends AbstractField<Date>
        implements HasComponents {

    private StyleCalendar internalCalendar;

    private String nullRepresentation;

    private Converter<String, Date> dateConverter;

    public StyleCalendarField() {
        registerRpc(new StyleCalendarFieldRpc() {

            @Override
            public void popupVisibilityChanged(boolean visible) {
                setShowPopup(visible);

            }

            @Override
            public void setValue(String value) {
                StyleCalendarField.this.setValue(getDateConverter()
                        .convertToModel(value, Date.class, getLocale()));
            }

        });

        setDateConverter(new DefaultDateConverter(this));

    }

    public StyleCalendarField(String caption) {
        this();
        setCaption(caption);
    }

    public Converter<String, Date> getDateConverter() {
        return dateConverter;
    }

    public void setDateConverter(Converter<String, Date> dateConverter) {
        this.dateConverter = dateConverter;
        markAsDirty();
    }

    @Override
    public StyleCalendarFieldState getState() {
        return (StyleCalendarFieldState) super.getState();
    }

    @Override
    public void beforeClientResponse(boolean initial) {
        super.beforeClientResponse(initial);

        String paintValue = getPaintValue();

        getState().setFieldValue(paintValue);

        getState().setShowPopup(isShowPopup());
    }

    protected String getPaintValue() {
        Object value = getValue();

        if (value == null) {
            if (getNullRepresentation() != null) {
                return getNullRepresentation();

            } else {
                return "null";
            }

        } else {
            DateFormat format = DateFormat
                    .getDateInstance(DateFormat.SHORT, getLocale());
            return format.format(value);
        }
    }

    protected StyleCalendar getNewStyleCalendar() {
        StyleCalendar calendar = new StyleCalendar();
        calendar.setValue(getValue());

        if (getValue() != null) {
            calendar.setShowingDate(getValue());
        }

        calendar.addValueChangeListener(
                (ValueChangeListener) event -> StyleCalendarField.this
                        .setValue((Date) event.getProperty().getValue()));

        calendar.setParent(this);
        calendar.setImmediate(true);

        return calendar;
    }

    protected void removeStyleCalendar(StyleCalendar calendar) {
        calendar.setParent(null);
        calendar.removeValueChangeListener(this);
    }


    @Override
    public Class<Date> getType() {
        return Date.class;
    }

    protected boolean isShowPopup() {
        return getState().isShowPopup();
    }

    protected void setShowPopup(boolean showPopup) {
        getState().setShowPopup(showPopup);
        if (showPopup && internalCalendar == null) {
            internalCalendar = getNewStyleCalendar();
        } else if (!showPopup && internalCalendar != null) {
            removeStyleCalendar(internalCalendar);
            internalCalendar = null;
        }
    }

    @Override
    public Iterator<Component> iterator() {
        return new Iterator<Component>() {

            private boolean first = (internalCalendar == null);

            @Override
            public boolean hasNext() {
                return !first;
            }

            @Override
            public Component next() {
                if (!first) {
                    first = true;
                    return internalCalendar;
                } else {
                    return null;
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public String getNullRepresentation() {
        return nullRepresentation;
    }

    public void setNullRepresentation(String nullRepresentation) {
        this.nullRepresentation = nullRepresentation;
    }

    @Override
    public Date getValue() {
        return super.getValue();
    }

    @Override
    public void setValue(Date newValue) {
        super.setValue(newValue);

        if (internalCalendar != null) {
            internalCalendar.setValue(newValue);
            internalCalendar.setShowingDate(newValue);
        }
    }

    /**
     * Default converter for StyleCalendar values. Uses locale and
     * {@link DateFormat#SHORT} to format/parse.
     *
     * @author Risto Yrjänä / Vaadin
     */
    public static class DefaultDateConverter
            implements Converter<String, Date> {

        private final StyleCalendarField field;

        public DefaultDateConverter(StyleCalendarField field) {
            this.field = field;
        }

        @Override
        public Date convertToModel(String value,
                Class<? extends Date> modelClass, Locale locale) throws
                com.vaadin.data.util.converter.Converter.ConversionException {
            DateFormat format = DateFormat
                    .getDateInstance(DateFormat.SHORT, locale);
            try {
                return format.parse(value);
            } catch (ParseException e) {
                throw new ConversionException(e);
            }
        }

        @Override
        public String convertToPresentation(Date value,
                Class<? extends String> presentationClass, Locale locale) throws
                com.vaadin.data.util.converter.Converter.ConversionException {
            if (value == null) {
                if (field.getNullRepresentation() != null) {
                    return field.getNullRepresentation();

                } else {
                    return "null";
                }

            } else {
                DateFormat format = DateFormat
                        .getDateInstance(DateFormat.SHORT, locale);
                return format.format(value);
            }
        }

        @Override
        public Class<Date> getModelType() {
            return Date.class;
        }

        @Override
        public Class<String> getPresentationType() {
            return String.class;
        }
    }
}
