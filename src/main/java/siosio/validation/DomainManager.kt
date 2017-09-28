package siosio.validation

import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import java.util.*

object DomainManager {

    private val cache: WeakHashMap<Module, List<PsiClass>> = WeakHashMap()

    private val domainManagerClassName = "nablarch.core.validation.ee.DomainManager"

    private fun findDomainManagerClass(project: Project, module: Module): PsiClass? {
        val domainManager = JavaPsiFacade.getInstance(project).findClasses(
            domainManagerClassName, module.getModuleWithDependenciesAndLibrariesScope(false))
        return domainManager.firstOrNull()
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
        if (project.isDisposed || module.isDisposed || !project.isOpen) {
            return emptyList()
        }
        return cache.getOrPut(module) {
            findDomainManagerClass(project, module)?.let {
                ClassInheritorsSearch.search(it, module.getModuleWithDependenciesAndLibrariesScope(false), true, true, true)
                    .toList()
                    .mapNotNull {
                        it.findMethodsByName("getDomainBean", false).first().returnTypeElement?.let {
                            val type = it.type
                            when (type) {
                                is PsiClassReferenceType ->
                                    PsiTypesUtil.getPsiClass(type.parameters.firstOrNull())
                                else -> null
                            }
                        }
                    }
            } ?: emptyList()
        }
    }
}
