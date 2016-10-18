package siosio.repository;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;

/**
 * 名前付き(name属性)タグを表すインタフェース
 */
public interface NamedElement extends DomElement {

    @Attribute("name")
    GenericAttributeValue<String> getName();

}
