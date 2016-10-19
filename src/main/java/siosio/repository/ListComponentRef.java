package siosio.repository;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;
import siosio.repository.converter.RepositoryListRefConverter;

/**
 * listタグ内に置かれるcomponent-refタグ
 */
public interface ListComponentRef extends DomElement {
    
    @NotNull
    @Attribute("name")
    @Convert(RepositoryListRefConverter.class)
    GenericAttributeValue<Component> getComponentClass();

}
