package siosio.repository

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import siosio.repository.xml.*

/**
 * この要素が設定されているクラス([PsiClass])を取得する。
 */
internal fun findComponentClass(element: XmlElement): PsiClass? {
    return XmlHelper.findComponent(element)?.componentClass?.value
}

internal fun createHandlerInterfaceType(project: Project): PsiType? {
    val psiClass = JavaPsiFacade.getInstance(project).findClasses("nablarch.fw.Handler", GlobalSearchScope.allScope(project))
    return psiClass.firstOrNull()?.let {
        PsiTypesUtil.getClassType(it)
    }
}

