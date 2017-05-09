package siosio.repository.psi

import com.intellij.ide.util.*
import com.intellij.navigation.*
import com.intellij.psi.*
import com.intellij.psi.xml.*
import siosio.*
import javax.swing.*

class ComponentXmlTagElement(private val xmlTag: XmlTag) : XmlTag by xmlTag, NavigatablePsiElement {
    override fun canNavigate(): Boolean = PsiNavigationSupport.getInstance().canNavigate(this)

    override fun canNavigateToSource(): Boolean = canNavigate()

    override fun getPresentation(): ItemPresentation {
        return object: ItemPresentation {
            override fun getLocationString(): String? = containingFile.name

            override fun getIcon(unused: Boolean): Icon? = nablarchIcon

            override fun getPresentableText(): String? = name
        }
    }

    override fun navigate(requestFocus: Boolean) {
        PsiNavigationSupport.getInstance().getDescriptor(this)?.navigate(requestFocus)
    }
}

