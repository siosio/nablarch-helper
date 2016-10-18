package siosio.validation

import com.intellij.codeInsight.AnnotationUtil
import com.intellij.codeInspection.BaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionToolProvider
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiMethod

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
        override fun visitMethod(method: PsiMethod?) {
          super.visitMethod(method ?: return)

          val project = method.project
          val module = getModule(method) ?: return

          AnnotationUtil.findAnnotation(method, "nablarch.core.validation.ee.Domain")?.let {
            val domainName = AnnotationUtil.getStringAttributeValue(it, "value")
            if (domainName.isNullOrEmpty()) {
              holder.registerProblem(it, "ドメイン名が指定されていません。", ProblemHighlightType.ERROR)
            } else {
              if (findDomainField(project, module, domainName!!) == null) {
                holder.registerProblem(it, "ドメインがDomainBeanに存在しません。", ProblemHighlightType.ERROR)
              }
            }
          }
        }
      }
    }
  }
}
