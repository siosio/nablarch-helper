package siosio.repository.xml;

import java.util.List;

import com.intellij.util.xml.SubTagList;

/**
 * listタグ
 */
public interface ListObject extends NamedElement {

    @SubTagList("component")
    List<ListComponent> getComponent();
    
    @SubTagList("component-ref")
    List<ListComponentRef> getComponentRef();

}
