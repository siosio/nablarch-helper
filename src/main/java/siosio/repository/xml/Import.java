package siosio.repository.xml;

import com.intellij.psi.PsiFile;
import com.intellij.util.xml.Convert;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;
import siosio.repository.converter.PsiFileConverter;

public interface Import extends DomElement {

    @NotNull
    @Convert(PsiFileConverter.class)
    GenericAttributeValue<PsiFile> getFile();
}
