package siosio.repository.converter

import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.reference.impl.providers.*
import com.intellij.psi.search.*
import com.intellij.util.xml.*
import siosio.repository.extension.*

/**
 * class属性か[PsiClass]に変換するクラス
 */
open class RepositoryPsiClassConverter : PsiClassConverter() {

    override fun createClassReferenceProvider(
        value: GenericDomValue<PsiClass>,
        context: ConvertContext,
        extendClass: ExtendClass?): JavaClassReferenceProvider {

        val provider = super.createClassReferenceProvider(value, context, extendClass)

        provider.setOption(JavaClassReferenceProvider.INSTANTIATABLE, true)
        provider.setOption(JavaClassReferenceProvider.JVM_FORMAT, true)
        provider.setOption(JavaClassReferenceProvider.NOT_ENUM, true)
        return provider
    }

    /**
     * 実行時のスコープを検索範囲にする
     */
    override fun getScope(context: ConvertContext): GlobalSearchScope? {
        return context.module?.let {
            GlobalSearchScope.moduleRuntimeScope(it, context.file.inTestScope(it))
        } ?: super.getScope(context)
    }
}
