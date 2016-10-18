package siosio.repository.converter

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.*
import com.intellij.psi.util.PropertyUtil
import com.intellij.psi.util.PsiFormatUtil
import com.intellij.psi.util.PsiFormatUtilBase
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.Converter
import com.intellij.util.xml.CustomReferenceConverter
import com.intellij.util.xml.GenericDomValue
import siosio.repository.*

/**
 * propertyタグのnameタグを[PsiMethod]に変換するクラス
 */
class RepositoryPsiMethodConverter : Converter<PsiMethod>(), CustomReferenceConverter<PsiMethod> {

  override fun createReferences(method: GenericDomValue<PsiMethod>?, element: PsiElement?, context: ConvertContext?): Array<out PsiReference> {
    return arrayOf(MyReference(element!!, method))
  }

  override fun toString(method: PsiMethod?, context: ConvertContext?): String? {
    return method?.name;
  }

  override fun fromString(method: String?, context: ConvertContext?): PsiMethod? {
    if (context?.xmlElement !is XmlAttribute || method == null) {
      return null
    }
    return findComponentClass(context!!.xmlElement!!)?.let {
      PropertyUtil.findPropertySetter(it, method, false, true)
    } ?: null
  }

  class MyReference(private val psiElement: PsiElement, val propertyName: GenericDomValue<PsiMethod>?) : PsiReferenceBase<PsiElement>(psiElement) {

    override fun handleElementRename(newElementName: String): PsiElement? {
      return super.handleElementRename(PropertyUtil.getPropertyName(newElementName))
    }

    override fun bindToElement(element: PsiElement): PsiElement? {
      if (element !is PsiMethod) {
        return null
      }

      propertyName?.stringValue = PropertyUtil.getPropertyNameBySetter(element)
      return element
    }

    override fun getVariants(): Array<out Any> {
      if (element !is XmlAttributeValue) {
        return emptyArray()
      }

      val component = findComponent(element as XmlAttributeValue)
      val usedProperties = component?.properties?.map {
        it.name.value
      }?.filterNotNull() ?: emptyList()

      return findComponentClass(element as XmlAttributeValue)?.let {
        PropertyUtil.getAllProperties(it, true, false, true)
      }?.asSequence()?.filterNot { props ->
        // propertyタグで未定義のプロパティだけを対象にする。
        usedProperties.contains(props.value)
      }?.map { method ->
        createLookupElementBuilder(method.value)
      }?.toList()?.toTypedArray() ?: emptyArray()
    }

    /**
     * 候補リストに表示するための[LookupElementBuilder]を構築する
     */
    private fun createLookupElementBuilder(method: PsiMethod): LookupElementBuilder {
      val tail = PsiFormatUtil.formatMethod(method,
          PsiSubstitutor.EMPTY,
          PsiFormatUtilBase.SHOW_PARAMETERS,
          PsiFormatUtilBase.SHOW_NAME or PsiFormatUtilBase.SHOW_TYPE)

      return LookupElementBuilder.create(method, PropertyUtil.getPropertyNameBySetter(method))
          .withIcon(method.getIcon(0))
          .withStrikeoutness(method.isDeprecated)
          .withTailText(tail)
    }

    override fun resolve(): PsiElement? {
      return propertyName?.value
    }
  }
}
