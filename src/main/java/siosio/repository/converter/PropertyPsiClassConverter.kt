package siosio.repository.converter

import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.psi.util.*
import com.intellij.util.xml.*
import parameterList
import siosio.repository.*

class PropertyPsiClassConverter : RepositoryPsiClassConverter() {

    override fun createClassReferenceProvider(value: GenericDomValue<PsiClass>,
                                              context: ConvertContext,
                                              extendClass: ExtendClass?): JavaClassReferenceProvider {
        val provider = super.createClassReferenceProvider(value, context, extendClass)

        value.xmlElement?.let(DomUtil::getDomElement)
            ?.getParentOfType(Property::class.java, true)
            ?.let { property ->
                property.parameterList().firstOrNull()
                    ?.let { firstParameter ->
                        val psiClass = PsiTypesUtil.getPsiClass(firstParameter.type)
                        psiClass?.qualifiedName
                    }
            }
            ?.let { parameterType ->
                provider.setOption(JavaClassReferenceProvider.EXTEND_CLASS_NAMES, arrayOf(parameterType))
            }
        return provider
    }
}
