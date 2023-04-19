package com.vaadin.flow.component.shared;

import com.vaadin.flow.component.*;

import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

public interface InputField extends HasElement, HasEnabled, HasLabel, HasSize, HasStyle, HasTooltip {

    public <T> T findAncestor(Class<T> componentType);

/*    public <T extends Component> T from(Element element,
                                        Class<T> componentType);*/

    public Stream<Component> getChildren();

    public Optional<String> getId();

    public Optional<Component> getParent();

    public String getTranslation(Locale locale, Object key, Object... params);

    public String getTranslation(Locale locale, String key, Object... params);

    @Deprecated
    public String getTranslation(Object key, Locale locale, Object... params);

    public String getTranslation(Object key, Object... params);

    @Deprecated
    public String getTranslation(String key, Locale locale, Object... params);

    public String getTranslation(String key, Object... params);

    public Optional<UI> getUI();

    public boolean isAttached();

    public boolean isVisible();

    public void onEnabledStateChanged(boolean enabled);

    public void removeFromParent();

    public void scrollIntoView();

    public void scrollIntoView(ScrollOptions scrollOptions);

    public void setId(String id);

    public void setVisible(boolean visible);

}
