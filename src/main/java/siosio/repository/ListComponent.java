package siosio.repository;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;
import siosio.repository.converter.ListPsiClassConverter;

/**
 * listタグ内に置かれるcomponentタグ
 */
public interface ListComponent extends Component {

    @NotNull
    @Override
    @Attribute("class")
    @Convert(ListPsiClassConverter.class)
    GenericAttributeValue<PsiClass> getComponentClass();
}
