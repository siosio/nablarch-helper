package siosio.repository.converter

import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import inHandlerQueue
import parameterList
import siosio.repository.*
import siosio.repository.xml.*

class RepositoryListRefConverter : Converter<XmlTag>(), CustomReferenceConverter<XmlTag> {

    override fun createReferences(component: GenericDomValue<XmlTag>?,
                                  element: PsiElement?,
                                  context: ConvertContext?): Array<out PsiReference> {
        return if (component?.stringValue.isNullOrEmpty()) {
            emptyArray()
        } else {
            arrayOf(ListComponentRefReference(element!!, component, context))
        }
    }

    override fun toString(component: XmlTag?, context: ConvertContext?): String? {
        return component?.name
    }

    override fun fromString(name: String?, context: ConvertContext?): XmlTag? {
        return XmlHelper.findNamedElement(context)
            .lastOrNull {
                it.name.value == name
            }?.xmlTag
    }
}

class ListComponentRefReference(psiElement: PsiElement,
                                val component: GenericDomValue<XmlTag>?,
                                private val context: ConvertContext?) :
    PsiReferenceBase<PsiElement>(psiElement) {

    override fun getVariants(): Array<out Any> {
        if (element !is XmlAttributeValue) {
            return emptyArray()
        }

        val namedElements = XmlHelper.findNamedElement(context)
            .mapNotNull { namedElement ->
                if (namedElement is Component) {
                    namedElement.componentClass.value?.let {
                        Triple(namedElement, PsiTypesUtil.getClassType(it), it)
                    }
                } else {
                    null
                }
            }

        return PsiTreeUtil.getParentOfType(element, XmlTag::class.java)?.let {
            val domElement = DomUtil.getDomElement(it) as? ListComponentRef ?: return PsiReference.EMPTY_ARRAY

            val propertyTag = domElement.getParentOfType(Property::class.java, true)

            val type = if (domElement.inHandlerQueue()) {
                createHandlerInterfaceType(element.project)
            } else if (propertyTag != null) {
                val type = propertyTag.parameterList().firstOrNull()?.type
                when (type) {
                    // todo ちょっとここ雑かな。。
                    is PsiClassReferenceType -> type.parameters.firstOrNull()
                    else -> null
                }
            } else {
                null
            }

            namedElements.asSequence().filter {
                type == null || XmlHelper.isAssignableFrom(type, it.second)
            }.map {
                LookupElementBuilder
                    .create(it.first.xmlTag, it.first.name.value!!)
                    .withIcon(it.third.getIcon(0))
                    .withTypeText(it.first.xmlTag.containingFile.name, true)
            }.toList().toTypedArray()
        } ?: emptyArray()
    }

    override fun resolve(): PsiElement? {
        return component?.value
    }
}
