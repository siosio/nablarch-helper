package siosio.repository.converter

import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import siosio.repository.*

class PropertyPsiClassConverter : RepositoryPsiClassConverter() {

  override fun createClassReferenceProvider(value: GenericDomValue<PsiClass>, context: ConvertContext, extendClass: ExtendClass?): JavaClassReferenceProvider {
    val provider = super.createClassReferenceProvider(value, context, extendClass)

    value.xmlElement?.let { element ->
      // 親のpropertyタグを探す
      PsiTreeUtil.findFirstParent(element) { parentTag ->
        (parentTag is XmlTag) && parentTag.name == "property"
      }
    }?.let { propertyTag ->
      val property = DomUtil.getDomElement(propertyTag)
      if (property is Property) {
        property
      } else {
        null
      }
    }?.let { property ->
      property.name.value
    }?.let { setter ->
      val parameters = setter.parameterList.parameters
      if (parameters.isNotEmpty()) {
        val parameter = parameters[0]
        val psiClass = PsiTypesUtil.getPsiClass(parameter.type)
        psiClass?.qualifiedName
      } else {
        null
      }
    }?.let { parameterType ->
      provider.setOption(JavaClassReferenceProvider.EXTEND_CLASS_NAMES, arrayOf(parameterType))
    }
    return provider
  }
}
