package siosio.validation

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.util.*

class BeanValidationCompletionContributor : CompletionContributor() {

  init {
    extend(CompletionType.BASIC, PATTERN, DomainNameCompletionProvider())
  }

  class DomainNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, p1: ProcessingContext?, resultSet: CompletionResultSet) {
      val originalPosition = parameters.originalPosition
      
      originalPosition?.let {
        val module = getModule(it) ?: return
        val project = it.project

        resultSet.addAllElements(
            getAllDomainFields(project, module).map {
              LookupElementBuilder.create(it, it.nameIdentifier.text)
                  .withIcon(it.getIcon(0))
            }
        )
        resultSet.stopHere()
      }
    }
  }
}

