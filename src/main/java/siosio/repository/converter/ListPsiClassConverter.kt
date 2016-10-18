package siosio.repository.converter

import com.intellij.psi.*
import com.intellij.psi.impl.source.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import siosio.repository.*

class ListPsiClassConverter : RepositoryPsiClassConverter() {

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
      parameters.firstOrNull()?.let { parameter ->
        val type = parameter.type
        if (type is PsiClassReferenceType) {
          type.reference.typeParameters.firstOrNull()
        } else {
          null
        }
      }
    }?.let { parameterType ->
      when(parameterType) {
        is PsiWildcardType -> PsiTypesUtil.getPsiClass(parameterType.bound)
        else -> PsiTypesUtil.getPsiClass(parameterType)
      }
    }?.let { parameterType ->
      provider.setOption(JavaClassReferenceProvider.EXTEND_CLASS_NAMES, arrayOf(parameterType.qualifiedName))
    }
    return provider
  }
}
