package siosio.repository;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.GenericAttributeValue;
import siosio.repository.converter.ListPsiClassConverter;

/**
 * listタグ内に置かれるcomponentタグ
 */
public interface ListComponent extends Component {

    @Attribute("class")
    @Convert(ListPsiClassConverter.class)
    GenericAttributeValue<PsiClass> getComponentClass();

}
