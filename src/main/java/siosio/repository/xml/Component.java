package siosio.repository.xml;

import java.util.List;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;
import siosio.repository.converter.RepositoryPsiClassConverter;

/**
 * componentタグ
 */
public interface Component extends NamedElement {

    @NotNull
    @Attribute("class")
    @Convert(RepositoryPsiClassConverter.class)
    GenericAttributeValue<PsiClass> getComponentClass();

    @NotNull
    @SubTagList("property")
    List<Property> getProperties();
}
