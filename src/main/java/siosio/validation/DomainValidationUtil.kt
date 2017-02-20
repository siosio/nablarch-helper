package siosio.validation

import com.intellij.codeInsight.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*

/** ファクトリメソッドのメソッド名 */
const private val FACTORY_METHOD_NAME = "getDomainBean"

/** Nablarchが提供するDomainManagerのクラス名（FQCN） */
const private val NABLARCH_DOMAIN_MANAGER_CLASS_NAME = "nablarch.core.validation.ee.DomainManager"

fun getAllDomainFields(project: Project, module: Module): List<PsiField> {
  return getDomainBeanClasses(project, module).map {
    val method = it.findMethodsByName(FACTORY_METHOD_NAME, false)
    method.first().returnTypeElement?.let {
      PsiTreeUtil.findChildOfType(it, PsiTypeElement::class.java)?.let {
        PsiTypesUtil.getPsiClass(it.type)
      }
    }?.let {
      it.fields.filter {
        !AnnotationUtil.getAllAnnotations(it, false, emptySet()).isEmpty()
      }
    } ?: emptyList<PsiField>()
  }.flatten()
}

fun findDomainField(project: Project, module: Module, field: String): PsiField? {
  return getAllDomainFields(project, module).firstOrNull {
    it.name == field
  }
}

fun getDomainBeanClasses(project: Project, module: Module): List<PsiClass> {
  return findDomainManagerClass(project, module)?.let {
    ClassInheritorsSearch.search(
        it, createModuleSearchScope(module), true, true, true).toList()
  } ?: emptyList()
}

fun getModule(element: PsiElement): Module? {
  return ModuleUtil.findModuleForPsiElement(element)
}

fun createModuleSearchScope(module: Module): GlobalSearchScope {
  return GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
}

private fun findDomainManagerClass(project: Project, module: Module): PsiClass? {
  val domainManager = JavaPsiFacade.getInstance(project).findClasses(
      NABLARCH_DOMAIN_MANAGER_CLASS_NAME, createModuleSearchScope(module))
  return domainManager.firstOrNull()
}
