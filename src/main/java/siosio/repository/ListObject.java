package siosio.repository;

import java.util.List;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;

/**
 * listタグ
 */
public interface ListObject extends DomElement, NamedElement {

    @SubTagList("component")
    List<ListComponent> getComponent();
    
    @SubTagList("component-ref")
    List<ListComponentRef> getComponentRef();

}
