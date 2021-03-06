package siosio.repository.xml;

import java.util.List;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * ルート要素
 */
public interface ComponentDefinition extends DomElement {

    @NotNull
    @SubTagList("component")
    List<Component> getComponents();

    @NotNull
    @SubTagList("list")
    List<ListObject> getLists();
    
    @NotNull
    @SubTagList("import")
    List<Import> getImports();
}
