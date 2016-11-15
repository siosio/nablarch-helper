package siosio.repository.converter

import com.intellij.codeInsight.lookup.*
import com.intellij.psi.*
import siosio.repository.*

class PropertyRefReference(psiElement: PsiElement, val component: Component, val psiClass: PsiClass) : PsiReferenceBase<PsiElement>(psiElement) {
  override fun resolve(): PsiElement? {
    return component.xmlTag
  }

  override fun getValue(): String {
    return component.name.value!!
  }

  override fun getVariants(): Array<out Any> {
    return component.let {
      arrayOf(LookupElementBuilder.create(
          it,
          it.name.value!!
      ).withIcon(psiClass.getIcon(0))
          .withTypeText(it.xmlTag.containingFile.name, false))
    }
  }
}