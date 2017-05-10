package siosio.repository.xml;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;
import siosio.repository.converter.PropertyPsiClassConverter;

public interface PropertyComponent extends Component {

    @NotNull
    @Override
    @Attribute("class")
    @Convert(PropertyPsiClassConverter.class)
    GenericAttributeValue<PsiClass> getComponentClass();
}
