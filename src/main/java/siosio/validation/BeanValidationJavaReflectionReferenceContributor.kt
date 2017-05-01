package siosio.validation

import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.PsiJavaPatterns.*
import com.intellij.psi.*
import com.intellij.util.*
import siosio.extension.*

class BeanValidationJavaReflectionReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(psiLiteral()
            .withSuperParent(3, psiElement(PsiAnnotation::class.java)
                .withChild(psiElement(PsiJavaCodeReferenceElement::class.java).withText("Domain")))!!, DomainReferenceProvider())
    }

    class DomainReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
            return arrayOf(MyReference(element))
        }
    }

    class MyReference(domainLiteral: PsiElement) : PsiReferenceBase<PsiElement>(domainLiteral) {

        override fun getVariants(): Array<out Any> {
            val project = element.project
            val module = element.getModule() ?: return emptyArray()

            return DomainManager.getAllDomainFields(project, module).map {
                LookupElementBuilder
                    .create(it, it.nameIdentifier.text)
                    .withIcon(it.getIcon(0))
                    .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
            }.toTypedArray()
        }

        override fun resolve(): PsiElement? {
            val domainName = myElement.text?.trimStart('"')?.trimEnd('"')
            if (domainName.isNullOrBlank()) {
                return null
            }
            val module = myElement.getModule() ?: return null
            return DomainManager.findDomainField(myElement.project, module, domainName!!)
        }
    }
}

