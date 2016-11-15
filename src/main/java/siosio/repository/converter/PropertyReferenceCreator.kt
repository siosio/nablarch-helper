package siosio.repository.converter

import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import siosio.repository.*

class PropertyReferenceCreator(
    private val xmlAttributeValue: XmlAttributeValue
) {

  fun findAll(context: ConvertContext): Array<PropertyRefReference> {

    val namedElements = findNamedElement(context)
        .filterIsInstance(Component::class.java)
        .map { component ->
          component.componentClass.value?.let {
            NamedElementHolder(component, it)
          }
        }.filterNotNull()

    val propertyTag = DomUtil.getDomElement(PsiTreeUtil.getParentOfType(xmlAttributeValue, XmlTag::class.java)) as? Property ?: return emptyArray()

    val parameterList = propertyTag.name.value?.parameterList
    
    return if (parameterList?.parametersCount == 1) {
      parameterList!!.parameters.firstOrNull()?.type
    } else {
      null
    }?.let { type ->
      namedElements.asSequence().filter {
        isAssignableFrom(type, it.componentType)
      }.map {
        PropertyRefReference(xmlAttributeValue, it.component, it.componentClass)
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

