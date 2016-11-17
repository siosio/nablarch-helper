package siosio.repository;

import com.intellij.psi.PsiMethod;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.SubTag;
import org.jetbrains.annotations.NotNull;
import siosio.repository.converter.RepositoryPsiMethodConverter;
import siosio.repository.converter.RepositoryRefConverter;

public interface Property extends DomElement {

    @NotNull
    @Attribute("name")
    @Convert(RepositoryPsiMethodConverter.class)
    GenericAttributeValue<PsiMethod> getName();

    @Attribute("value")
    GenericAttributeValue<String> getValue();

    @Attribute("ref")
    @Convert(RepositoryRefConverter.class)
    GenericAttributeValue<XmlTag> getRef();

    @SubTag("component")
    PropertyComponent getComponent();

    @SubTag("list")
    ListObject getList();
}
