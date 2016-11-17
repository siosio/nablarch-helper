package siosio.repository.converter

import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import com.intellij.util.xml.impl.*
import siosio.repository.*

/**
 * propertyタグのref属性のコンポーネントを解決するコンバータ
 */
class RepositoryRefConverter : ResolvingConverter<XmlTag>() {
  
  override fun getVariants(context: ConvertContext?): MutableCollection<out XmlTag> {
    val element = context?.referenceXmlElement
    return if (element is XmlAttributeValue) {
      PropertyReferenceCreator(element).findAll(context!!).map { 
        it.component.xmlTag
      }.toMutableList()
    } else {
      mutableListOf()
    }
  }

  override fun createLookupElement(t: XmlTag): LookupElement? {
    return LookupElementBuilder.create(t, t.getAttributeValue("name")!!)
        .withIcon(t.getIcon(0))
        .withTypeText(t.containingFile.name)
        .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
  }

  override fun fromString(name: String?, context: ConvertContext?): XmlTag? {
    // todo 複数あった場合どうする？
    return findNamedElement(context).lastOrNull {
      it.name.value == name
    }?.xmlTag
  }

//  override fun createReferences(component: GenericDomValue<XmlTag>?, element: PsiElement?, context: ConvertContext): Array<out PsiReference> {
//    return if (element is XmlAttributeValue) {
//      PropertyReferenceCreator(element).findAll(context)
//    } else {
//      emptyArray()
//    }
//  }

  override fun toString(component: XmlTag?, context: ConvertContext?): String? {
    return component?.getAttributeValue("name")
  }

  class PropertyReferenceCreator(private val xmlAttributeValue: XmlAttributeValue) {

    fun findAll(context: ConvertContext): Array<PropertyRefReference> {
      val namedElements = findNamedElement(context)
          .filterIsInstance(Component::class.java)
          .map { component ->
            component.componentClass.value?.let {
              NamedElementHolder(component, it)
            }
          }.filterNotNull()

      val propertyTag = DomUtil.getDomElement(PsiTreeUtil.getParentOfType(xmlAttributeValue, XmlTag::class.java)) as? Property ?: return emptyArray()

      val parameterList = propertyTag.name.value?.parameterList ?: return emptyArray()

      return if (parameterList.parametersCount == 1) {
        parameterList.parameters.firstOrNull()?.type
      } else {
        null
      }?.let { type ->
        namedElements.asSequence().filter {
          isAssignableFrom(type, it.componentType)
        }.mapNotNull {
          PropertyRefReference(propertyTag.ref, it.component)
        }.toList().toTypedArray()
      } ?: emptyArray()
    }

    data class NamedElementHolder(
        val component: Component,
        val componentClass: PsiClass
    ) {
      val componentType: PsiClassType

      init {
        componentType = PsiTypesUtil.getClassType(componentClass)
      }
    }
  }

  class PropertyRefReference(val ref: GenericDomValue<XmlTag>, val component: Component) : GenericDomValueReference<XmlTag>(ref) {

    override fun getUnresolvedMessagePattern(): String {
      return "${ref.stringValue}が定義されていません。"
    }

    override fun getVariants(): Array<out Any> {
      return arrayOf(LookupElementBuilder
          .create(component.xmlTag, component.name.value!!)
          .withCaseSensitivity(true)
          .withIcon(component.xmlTag.getIcon(0))
          .withTypeText(component.xmlTag.containingFile.name)
          .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
      )
    }
  }
}
