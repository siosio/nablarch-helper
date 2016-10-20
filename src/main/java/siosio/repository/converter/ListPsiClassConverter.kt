package siosio.repository.converter

import com.intellij.psi.*
import com.intellij.psi.impl.source.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.psi.util.*
import com.intellij.util.xml.*
import siosio.repository.*

class ListPsiClassConverter : RepositoryPsiClassConverter() {

  override fun createClassReferenceProvider(value: GenericDomValue<PsiClass>, context: ConvertContext, extendClass: ExtendClass?): JavaClassReferenceProvider {
    val provider = super.createClassReferenceProvider(value, context, extendClass)

    val domElement = DomUtil.getDomElement(value.xmlElement)
    
    val parameterType = domElement?.getParentOfType(Property::class.java, true)?.let { property ->
      val parameters = property.name.value?.parameterList?.parameters
      parameters?.firstOrNull()?.let { parameter ->
        val type = parameter.type
        if (type is PsiClassReferenceType) {
          type.reference.typeParameters.firstOrNull()
        } else {
          null
        }
      }
    } ?: run {
      if (domElement?.getParentOfType(ListObject::class.java, true)?.name?.value == "handlerQueue") {
        createHandlerInterfaceType(value.xmlElement!!.project)
      } else {
        null
      }
    } ?: return provider

    when (parameterType) {
      is PsiWildcardType -> PsiTypesUtil.getPsiClass(parameterType.bound)
      else -> PsiTypesUtil.getPsiClass(parameterType)
    }?.let { parameterType ->
      provider.setOption(JavaClassReferenceProvider.EXTEND_CLASS_NAMES, arrayOf(parameterType.qualifiedName))
    }
    return provider
  }
}
