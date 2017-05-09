package siosio.repository

import com.intellij.codeInsight.lookup.*
import com.intellij.patterns.*
import com.intellij.patterns.PsiJavaPatterns.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import siosio.*
import siosio.repository.psi.*
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

        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference>
            = arrayOf(MyReference(element))
    }

    class MyReference(element: PsiElement) : ComponentReference(element) {

        override fun getVariants(): Array<out Any> {
            val type = PsiTreeUtil.getParentOfType(myElement, PsiDeclarationStatement::class.java)?.let {
                PsiTreeUtil.getChildOfType(it, PsiLocalVariable::class.java)?.type
            }
            return XmlHelper.findNamedElement(myElement)
                .filter {
                    it.name.value?.isNotBlank() ?: false
                }
                .filter {
                    if (type == null) {
                        true
                    } else {
                        when (it) {
                            is Component ->
                                it.componentClass.value?.let {
                                    XmlHelper.isAssignableFrom(type, PsiTypesUtil.getClassType(it))
                                } ?: false
                            is ListObject -> {
                                "java.util.List" in type.canonicalText
                            }
                            else -> false
                        }
                    }
                }
                .map {

                    val xmlTag = it.xmlTag
                    LookupElementBuilder.create(xmlTag, xmlTag.getAttributeValue("name")!!)
                        .withIcon(nablarchIcon)
                        .withTypeText(xmlTag.containingFile.name, true)
                        .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                }.toTypedArray()
        }
    }
}
