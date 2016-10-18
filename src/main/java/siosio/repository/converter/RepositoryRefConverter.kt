package siosio.repository.converter

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.Converter
import com.intellij.util.xml.CustomReferenceConverter
import com.intellij.util.xml.DomUtil
import com.intellij.util.xml.GenericDomValue
import siosio.repository.*

/**
 * propertyタグのref属性のコンポーネントを解決するコンバータ
 */
class RepositoryRefConverter : Converter<XmlTag>(), CustomReferenceConverter<XmlTag> {
  override fun createReferences(component: GenericDomValue<XmlTag>?, element: PsiElement?, context: ConvertContext?): Array<out PsiReference> {
    val value = component?.stringValue
    if (value.isNullOrEmpty()) {
      return emptyArray()
    }
    return arrayOf(MyReference(element!!, component, context), MyReference(element!!, component, context))
  }

  override fun toString(component: XmlTag?, context: ConvertContext?): String? {
    return component?.name
  }

  /**
   * refに指定したコンポーネント名に対応するタグに変換する
   */
  override fun fromString(name: String?, context: ConvertContext?): XmlTag? {
    return findNamedElement(context).lastOrNull {
      it.name.value == name
    }?.let {
      it.xmlTag
    } ?: null
  }
}

class MyReference(private val psiElement: PsiElement, val component: GenericDomValue<XmlTag>?, private val context: ConvertContext?) : PsiReferenceBase<PsiElement>(psiElement) {

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
      if (domElement is Property) {
        domElement
      } else {
        null
      }?.let {
        val parameterList = it.name.value?.parameterList
        if (parameterList?.parametersCount == 1) {
          parameterList!!.parameters[0]?.type
        } else {
          null
        }
      }?.let { type ->
        namedElements.asSequence().filter {
          isAssignableFrom(type, it.second)
        }.map {
          LookupElementBuilder.create(it.first.xmlTag, it.first.name.value!!)
              .withIcon(it.third.getIcon(0))
              .withTypeText(it.first.xmlTag.containingFile.name, true)
        }.toList().toTypedArray()
      }
    } ?: emptyArray()
  }

  override fun resolve(): PsiElement? {
    return component?.value
  }
}
