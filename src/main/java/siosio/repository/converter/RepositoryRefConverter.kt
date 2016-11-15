package siosio.repository.converter

import com.intellij.psi.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import siosio.repository.*

/**
 * propertyタグのref属性のコンポーネントを解決するコンバータ
 */
class RepositoryRefConverter : Converter<XmlTag>(), CustomReferenceConverter<XmlTag> {

  override fun createReferences(component: GenericDomValue<XmlTag>?, element: PsiElement?, context: ConvertContext): Array<out PsiReference> {
    val value = component?.stringValue
    if (value.isNullOrEmpty()) {
      return emptyArray()
    }

    return if (element is XmlAttributeValue) {
      return PropertyReferenceCreator(element).findAll(context)
    } else {
      emptyArray()
    }
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
    }?.xmlTag
  }
}

