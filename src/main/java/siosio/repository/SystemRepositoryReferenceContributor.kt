package siosio.repository

import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.*
import com.intellij.patterns.PsiJavaPatterns.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import siosio.*
import siosio.repository.xml.*

class SystemRepositoryReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(PsiJavaPatterns.psiLiteral()
            .withSuperParent(2, psiElement(PsiMethodCallExpression::class.java)
                .withFirstChild(psiElement(PsiReferenceExpression::class.java)
                    .withText(StandardPatterns.string().endsWith("SystemRepository.get")))
            ), SystemRepositoryReferenceProvider())
    }

    private inner class SystemRepositoryReferenceProvider : PsiReferenceProvider() {

        override fun getReferencesByElement(element: PsiElement,
                                            context: ProcessingContext): Array<PsiReference> {
            return arrayOf(MyReference(element))

        }
    }

    class MyReference(name: PsiElement) : PsiReferenceBase<PsiElement>(name) {

        override fun getVariants(): Array<out Any> {
            val type = PsiTreeUtil.getParentOfType(myElement, PsiDeclarationStatement::class.java)?.let {
                PsiTreeUtil.getChildOfType(it, PsiLocalVariable::class.java)?.type
            }
            return XmlHelper.findNamedElement(myElement)
                .filter {
                    it.name.value?.isNotBlank() ?: false
                }
                .filter {
                    when (it) {
                        is Component ->
                            if (type != null) {
                                it.componentClass.value?.let {
                                    XmlHelper.isAssignableFrom(type, PsiTypesUtil.getClassType(it))
                                } ?: false
                            } else {
                                true
                            }
                        is ListComponent -> false
                        else -> false
                    }
                }
                .map {
                    LookupElementBuilder.create(it, it.name.value!!)
                        .withIcon(nablarchIcon)
                        .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                }.toTypedArray()
        }

        override fun resolve(): PsiElement? {
            return XmlHelper.findNamedElement(myElement)
                .firstOrNull {
                    it.name.value == myElement.text.trimStart('"').trimEnd('"')
                }?.xmlElement
        }
    }
}
