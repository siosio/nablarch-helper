package siosio.repository.psi

import com.intellij.psi.*
import siosio.repository.extension.*
import siosio.repository.xml.*

abstract class ComponentReference(element: PsiElement) : PsiReferenceBase<PsiElement>(element), PsiPolyVariantReference {
    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return XmlHelper.findNamedElement(myElement)
            .filter {
                it.name.value == myElement.getSimpleText()
            }
            .map {
                PsiElementResolveResult(ComponentXmlTagElement(it.xmlTag))
            }.toTypedArray()
    }

    override fun resolve(): PsiElement? {
        val resolve = multiResolve(false)
        return if (resolve.size == 1) {
            return resolve.first().element
        } else {
            null
        }
    }
}
