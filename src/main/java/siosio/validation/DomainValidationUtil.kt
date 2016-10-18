package siosio.validation

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PsiJavaPatterns.psiLiteral
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiJavaToken
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.PsiTypesUtil

/** ファクトリメソッドのメソッド名 */
const private val FACTORY_METHOD_NAME = "getDomainBean"

/** Nablarchが提供するDomainManagerのクラス名（FQCN） */
const private val NABLARCH_DOMAIN_MANAGER_CLASS_NAME = "nablarch.core.validation.ee.DomainManager"

/** Domainアノテーションが設定されている要素を抽出するパターン */
val PATTERN = psiElement(PsiJavaToken::class.java)
    .withSuperParent(4, psiElement(PsiAnnotation::class.java)
        .withChild(psiElement(PsiJavaCodeReferenceElement::class.java).withText("Domain")))

val REFERENCE_PATTERN = psiLiteral()
    .withSuperParent(3, psiElement(PsiAnnotation::class.java)
        .withChild(psiElement(PsiJavaCodeReferenceElement::class.java).withText("Domain")))

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
  return getAllDomainFields(project, module).firstOrNull() {
    it.name == field
  }
}

fun getDomainBeanClasses(project: Project, module: Module): List<PsiClass> {
  return findDomainManagerClass(project, module)?.let {
    return ClassInheritorsSearch.search(
        it, createModuleSearchScope(module), true, false, true).toList()
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
