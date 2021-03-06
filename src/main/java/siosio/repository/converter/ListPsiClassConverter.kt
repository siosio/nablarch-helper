package siosio.repository.converter

import com.intellij.psi.*
import com.intellij.psi.impl.source.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.psi.util.*
import com.intellij.util.xml.*
import inHandlerQueue
import parameterList
import siosio.repository.*
import siosio.repository.xml.*

class ListPsiClassConverter : RepositoryPsiClassConverter() {

    override fun createClassReferenceProvider(value: GenericDomValue<PsiClass>,
                                              context: ConvertContext,
                                              extendClass: ExtendClass?): JavaClassReferenceProvider {
        val provider = super.createClassReferenceProvider(value, context, extendClass)

        val domElement = DomUtil.getDomElement(value.xmlElement) ?: return provider
        val propertyTag = domElement.getParentOfType(Property::class.java, true)

        val parameterType = propertyTag?.let { property ->
            property.parameterList().firstOrNull()?.type
                ?.let { type ->
                    when (type) {
                        is PsiClassReferenceType -> type.reference.typeParameters.firstOrNull()
                        else -> null
                    }
                }
        } ?: run {
            // プロパティ配下じゃない場合は、handlerQueue定義かどうかを探す
            if (domElement.inHandlerQueue()) {
                createHandlerInterfaceType(value.xmlElement!!.project)
            } else {
                null
            }
        } ?: return provider

        when (parameterType) {
            is PsiWildcardType -> PsiTypesUtil.getPsiClass(parameterType.bound)
            else -> PsiTypesUtil.getPsiClass(parameterType)
        }?.let { 
            provider.setOption(JavaClassReferenceProvider.EXTEND_CLASS_NAMES, arrayOf(it.qualifiedName))
        }
        return provider
    }
}
