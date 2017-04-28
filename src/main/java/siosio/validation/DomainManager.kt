package siosio.validation

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*

object DomainManager {

    private var domainManagerClass: PsiClass? = null

    private fun findDomainManagerClass(project: Project, module: Module): PsiClass? {
        return if (domainManagerClass == null) {
            val domainManager = JavaPsiFacade.getInstance(project).findClasses(
                "nablarch.core.validation.ee.DomainManager", module.getModuleWithDependenciesAndLibrariesScope(false))
            domainManagerClass = domainManager.firstOrNull()
            domainManagerClass
        } else {
            domainManagerClass
        }
    }

    fun getAllDomainFields(project: Project, module: Module): List<PsiField> {
        return getDomainBeanClasses(project, module).map {
            it.fields.toList()
        }.flatten().filterNotNull()
    }

    fun findDomainField(project: Project, module: Module, domainName: String): PsiField? {
        return getAllDomainFields(project, module).firstOrNull {
            it.name == domainName
        }
    }

    fun containsDomain(project: Project,
                       module: Module,
                       domainName: String): Boolean = findDomainField(project, module, domainName) != null

    fun getDomainBeanClasses(project: Project, module: Module): List<PsiClass> {
        return findDomainManagerClass(project, module)?.let {
            ClassInheritorsSearch.search(
                it, module.getModuleWithDependenciesAndLibrariesScope(false), true, true, true).toList()
        }?.map {
            val method = it.findMethodsByName("getDomainBean", false)
            method.first().returnTypeElement?.let {
                PsiTreeUtil.findChildOfType(it, PsiTypeElement::class.java)?.let {
                    PsiTypesUtil.getPsiClass(it.type)
                }
            }
        }?.filterNotNull() ?: emptyList()
    }
}