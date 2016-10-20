package siosio.validation

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInspection.BaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.*

/**
 * ドメイン名の存在チェックを行うインスペクション
 */
class BeanValidationInspectionToolProvider : InspectionToolProvider {

  override fun getInspectionClasses(): Array<Class<Any>> = arrayOf(InspectionTool::class.java) as Array<Class<Any>>

  class InspectionTool : BaseJavaLocalInspectionTool() {

    override fun getDisplayName(): String = "ドメインがDomainBeanに存在しているかチェックする"

    override fun isEnabledByDefault(): Boolean = true

    override fun getGroupDisplayName(): String = "nablarch"

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
      return object: JavaElementVisitor() {
        override fun visitField(field: PsiField?) {
          super.visitField(field ?: return)
          verifyDomainName(field)
        }

        override fun visitMethod(method: PsiMethod?) {
          super.visitMethod(method ?: return)
          verifyDomainName(method)
        }

        private fun verifyDomainName(element: PsiModifierListOwner) {
          val module = getModule(element) ?: return
          val project = element.project

          AnnotationUtil.findAnnotation(element, "nablarch.core.validation.ee.Domain")?.let {
            val domainName = AnnotationUtil.getStringAttributeValue(it, "value")
            if (domainName.isNullOrEmpty()) {
              holder.registerProblem(it, "ドメイン名が指定されていません。", ProblemHighlightType.ERROR)
            } else {
              findDomainField(project, module, domainName!!) ?:
                  holder.registerProblem(it, "ドメインがDomainBeanに存在しません。", ProblemHighlightType.ERROR)
            }
          }

        }
      }
    }
  }
}
