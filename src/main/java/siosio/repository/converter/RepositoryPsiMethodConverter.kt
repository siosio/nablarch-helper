package siosio.repository.converter

import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import siosio.repository.*
import siosio.repository.xml.*

/**
 * propertyタグのnameタグを[PsiMethod]に変換するクラス
 */
class RepositoryPsiMethodConverter : Converter<PsiMethod>(), CustomReferenceConverter<PsiMethod> {

    override fun createReferences(method: GenericDomValue<PsiMethod>?,
                                  element: PsiElement?,
                                  context: ConvertContext?): Array<out PsiReference> {
        return arrayOf(MyReference(element!!, method))
    }

    override fun toString(method: PsiMethod?, context: ConvertContext?): String? {
        return method?.name
    }

    override fun fromString(method: String?, context: ConvertContext?): PsiMethod? {
        if (context?.xmlElement !is XmlAttribute || method == null) {
            return null
        }
        return findComponentClass(context.xmlElement!!)?.let {
            PropertyUtil.findPropertySetter(it, method, false, true)
        }
    }

    class MyReference(psiElement: PsiElement,
                      val propertyName: GenericDomValue<PsiMethod>?) : PsiReferenceBase<PsiElement>(psiElement) {

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

            val component = XmlHelper.findComponent(element as XmlAttributeValue)
            val usedProperties = component?.properties?.map {
                it.name.value
            }?.filterNotNull() ?: emptyList()

            val lookupElements = findComponentClass(element as XmlAttributeValue)?.let {
                PropertyUtil.getAllProperties(it, true, false, true)
            }?.asSequence()?.filterNot { props ->
                // propertyタグで未定義のプロパティだけを対象にする。
                usedProperties.contains(props.value)
            }?.map { method ->
                createLookupElementBuilder(method.value)
            }?.toList()?.toTypedArray() ?: emptyArray()
            return lookupElements
        }

        /**
         * 候補リストに表示するための[LookupElement]を構築する
         */
        private fun createLookupElementBuilder(method: PsiMethod): LookupElement {
            val tail = PsiFormatUtil.formatMethod(method,
                PsiSubstitutor.EMPTY,
                PsiFormatUtilBase.SHOW_PARAMETERS,
                PsiFormatUtilBase.SHOW_NAME or PsiFormatUtilBase.SHOW_TYPE)

            return LookupElementBuilder.create(method, PropertyUtil.getPropertyNameBySetter(method))
                .withIcon(method.getIcon(0))
                .withStrikeoutness(method.isDeprecated)
                .withTailText(tail)
                .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
        }

        override fun resolve(): PsiElement? {
            return propertyName?.value
        }
    }
}
