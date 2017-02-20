package siosio.validation

import com.intellij.patterns.*
import com.intellij.patterns.StandardPatterns.*
import com.intellij.psi.*
import com.intellij.util.*

class DomainNameReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(PsiLiteralExpression::class.java)
                        .inside(PlatformPatterns.psiElement(PsiAnnotation::class.java)
                                .withText(string().contains("@Domain"))),
                DomainNameReferenceProvider()
        )
    }

    class DomainNameReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
            return if (element is PsiLiteralExpression) {
                arrayOf(DomainNameReference(element))
            } else {
                emptyArray()
            }
        }
    }
}

