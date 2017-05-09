package siosio.repository.extension

import com.intellij.openapi.module.*
import com.intellij.psi.*
import com.intellij.psi.search.*

fun PsiFile.inTestScope(module: Module): Boolean {
    return !GlobalSearchScope.moduleRuntimeScope(module, false).contains(this.originalFile.virtualFile)
}

fun PsiElement.getSimpleText() = this.text.trimStart('"').trimEnd('"')

