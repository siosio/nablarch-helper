package siosio.validation

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.util.ProcessingContext

class BeanValidationJavaReflectionReferenceContributor : PsiReferenceContributor() {
  override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
    registrar.registerReferenceProvider(REFERENCE_PATTERN, DomainReferenceProvider());
  }

  class DomainReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<out PsiReference> {
      return if (isDomainNameElement(element)) {
        val literalExpression = element as PsiLiteralExpression
        arrayOf(MyReference(literalExpression, getDomainField(literalExpression)))
      } else {
        emptyArray()
      }
    }

    private fun isDomainNameElement(element: PsiElement): Boolean {
      return element is PsiLiteralExpression && element.containingFile.virtualFile != null
    }

    private fun getDomainField(literalExpression: PsiLiteralExpression): PsiField? {
      val project = literalExpression.project
      val module = getModule(literalExpression) ?: return null
      val text = literalExpression.text?.trimStart('"')?.trimEnd('"') ?: ""
      return findDomainField(project, module, text)
    }
  }

  /**
   * リファレンス実装
   */
  class MyReference(private val domainLiteral: PsiElement,
                    private val field: PsiField?) : PsiReferenceBase<PsiElement>(domainLiteral) {

    override fun getVariants(): Array<out Any> {
      val project = element.project
      val module = getModule(element) ?: return emptyArray()

      return getAllDomainFields(project, module).map {
        LookupElementBuilder.create(it, it.nameIdentifier.text)
            .withIcon(it.getIcon(0))
      }.toTypedArray()
    }

    override fun resolve(): PsiElement? {
      return field
    }
  }
}

