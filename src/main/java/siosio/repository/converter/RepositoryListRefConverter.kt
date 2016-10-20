package siosio.repository.converter

import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import siosio.repository.*

class RepositoryListRefConverter : Converter<XmlTag>(), CustomReferenceConverter<XmlTag> {

  override fun createReferences(component: GenericDomValue<XmlTag>?, element: PsiElement?, context: ConvertContext?): Array<out PsiReference> {
    val value = component?.stringValue
    if (value.isNullOrEmpty()) {
      return emptyArray()
    }
    return arrayOf(ListComponentRefReference(element!!, component, context), PropertyRefReference(element, component, context))
  }

  override fun toString(component: XmlTag?, context: ConvertContext?): String? {
    return component?.name
  }

  override fun fromString(name: String?, context: ConvertContext?): XmlTag? {
    return findNamedElement(context).lastOrNull {
      it.name.value == name
    }?.let {
      it.xmlTag
    } ?: null
  }
}

class ListComponentRefReference(psiElement: PsiElement, val component: GenericDomValue<XmlTag>?, private val context: ConvertContext?) :
    PsiReferenceBase<PsiElement>(psiElement) {

  override fun getVariants(): Array<out Any> {
    if (element !is XmlAttributeValue) {
      return emptyArray()
    }

    val namedElements = findNamedElement(context).mapNotNull { namedElement ->
      if (namedElement is Component) {
        namedElement.componentClass.value?.let {
          Triple(namedElement, PsiTypesUtil.getClassType(it), it)
        } ?: null
      } else {
        null
      }
    }

    return PsiTreeUtil.getParentOfType(element, XmlTag::class.java)?.let {
      val domElement = DomUtil.getDomElement(it)
      if (domElement !is ListComponentRef) {
        return PsiReference.EMPTY_ARRAY
      }

      val type = if (domElement.getParentOfType(ListObject::class.java, true)?.name?.value == "handlerQueue") {
        createHandlerInterfaceType(element.project)
      } else {
        domElement.getParentOfType(Property::class.java, true)?.let {
          val parameterList = it.name.value?.parameterList
          if (parameterList?.parametersCount == 1) {
            val type = parameterList!!.parameters[0]?.type
            // todo ちょっとここ雑かな。。
            if (type is PsiClassReferenceType) {
              type.parameters.firstOrNull()
            } else {
              null
            }
          } else {
            null
          }
        }
      }
      namedElements.asSequence().filter {
        type == null || isAssignableFrom(type, it.second)
      }.map {
        LookupElementBuilder.create(it.first.xmlTag, it.first.name.value!!)
            .withIcon(it.third.getIcon(0))
            .withTypeText(it.first.xmlTag.containingFile.name, true)
      }.toList().toTypedArray()
    } ?: emptyArray()
  }

  override fun resolve(): PsiElement? {
    return component?.value
  }
}
