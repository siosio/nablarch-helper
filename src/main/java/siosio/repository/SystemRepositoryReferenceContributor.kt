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
                .withSuperParent(2, psiExpression().methodCall(
                        psiMethod().withName("get")
                                .definedInClass("nablarch.core.repository.SystemRepository"))),
                SystemRepositoryReferenceProvider())
    }

    private inner class SystemRepositoryReferenceProvider : PsiReferenceProvider() {
        override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference>
                = arrayOf(MyReference(element))
    }

    class MyReference(element: PsiElement) : ComponentReference(element) {
        companion object {
            private val listPattern = PsiJavaPatterns.psiClass().withQualifiedName("java.util.List")
        }

        override fun getVariants(): Array<out Any> {
            val type = PsiTreeUtil.getParentOfType(myElement, PsiDeclarationStatement::class.java)?.let {
                PsiTreeUtil.getChildOfType(it, PsiLocalVariable::class.java)?.type
            }

            val filter = createFilter(type)
            return XmlHelper.findNamedElement(myElement)
                    .filter {
                        it.name.value?.isNotBlank() ?: false
                    }
                    .filter(filter)
                    .map {
                        val xmlTag = it.xmlTag
                        LookupElementBuilder.create(xmlTag, xmlTag.getAttributeValue("name")!!)
                                .withIcon(nablarchIcon)
                                .withTypeText(xmlTag.containingFile.name, true)
                                .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                    }.toTypedArray()
        }

        /**
         * 候補に表示する要素を絞り込むフィルターを作る
         */
        fun createFilter(type: PsiType?): (NamedElement) -> Boolean {
            val alwaysTrue = fun(_: NamedElement) = true
            val objectFilter = fun(dom: NamedElement): Boolean {
                return if (dom is Component) {
                    dom.componentClass.value?.let {
                        XmlHelper.isAssignableFrom(type!!, PsiTypesUtil.getClassType(it))
                    } ?: false
                } else {
                    false
                }
            }
            val listFilter = fun(dom: NamedElement): Boolean = dom is ListObject

            return if (type == null) {
                alwaysTrue
            } else {
                if (listPattern.accepts(PsiTypesUtil.getPsiClass(type))) {
                    listFilter
                } else {
                    objectFilter
                }
            }
        }
    }
}
