package siosio.validation

import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*

class DomainNameReference(element: PsiLiteralExpression) : PsiReferenceBase<PsiLiteralExpression>(element) {
    override fun resolve(): PsiElement? {
        val text = myElement.text
        val domainName = text.trimStart('"').trimEnd('"')
        return getModule(myElement)?.let { module ->
            findDomainField(
                    project = myElement.project,
                    module = module,
                    field = domainName)
        }
    }

    override fun getVariants(): Array<out Any> {
        return getModule(myElement)?.let { module ->
            getAllDomainFields(myElement.project, module)
                    .map { field ->
                        LookupElementBuilder.create(
                                field,
                                field.nameIdentifier.text)
                                .withIcon(field.getIcon(0))
                                .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                    }.toTypedArray()
        } ?: emptyArray()
    }

}
