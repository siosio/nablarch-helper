package siosio.extension

import com.intellij.openapi.module.*
import com.intellij.psi.*

fun PsiElement.getModule(): Module? = ModuleUtil.findModuleForPsiElement(this)

