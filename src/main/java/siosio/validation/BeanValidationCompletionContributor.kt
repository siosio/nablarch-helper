package siosio.validation

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.util.ProcessingContext

class BeanValidationCompletionContributor : CompletionContributor() {

  init {
    extend(CompletionType.BASIC, PATTERN, DomainNameCompletionProvider())
  }

  class DomainNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, p1: ProcessingContext?, resultSet: CompletionResultSet) {
      val originalPosition = parameters.originalPosition
      originalPosition?.let {
        val project = it.project
        val module = getModule(it) ?: return
        getAllDomainFields(project, module).forEach {
          resultSet.addElement(
              LookupElementBuilder.create(it, it.nameIdentifier.text)
                  .withIcon(it.getIcon(0)))
        }
      }
      resultSet.stopHere()
    }
  }
}

