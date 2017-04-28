package siosio.repository.converter

import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import siosio.repository.*
import siosio.repository.xml.*

/**
 * propertyタグのref属性のコンポーネントを解決するコンバータ
 */
class RepositoryRefConverter : ResolvingConverter<XmlTag>() {
  
  override fun getVariants(context: ConvertContext?): MutableCollection<out XmlTag> {
    val element = context?.referenceXmlElement
    return if (element is XmlAttributeValue) {
      ComponentCreator(element).findAll(context).map { 
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
    // todo 複数あるのか(´・ω・`)
    return XmlHelper.findNamedElement(context).lastOrNull {
      it.name.value == name
    }?.xmlTag
  }

  override fun toString(component: XmlTag?, context: ConvertContext?): String? {
    return component?.getAttributeValue("name")
  }

  class ComponentCreator(private val xmlAttributeValue: XmlAttributeValue) {

    fun findAll(context: ConvertContext): Array<NamedElementHolder> {
      val namedElements = XmlHelper.findNamedElement(context)
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
          XmlHelper.isAssignableFrom(type, it.componentType)
        }.toList().toTypedArray()
      } ?: emptyArray()
    }

    data class NamedElementHolder(
        val component: Component,
        val componentClass: PsiClass
    ) {
      val componentType: PsiClassType = PsiTypesUtil.getClassType(componentClass)
    }
  }
}
