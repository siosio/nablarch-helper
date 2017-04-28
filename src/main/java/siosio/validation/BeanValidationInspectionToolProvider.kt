package siosio.validation

import com.intellij.codeHighlighting.*
import com.intellij.codeInsight.*
import com.intellij.codeInsight.daemon.impl.quickfix.*
import com.intellij.codeInspection.*
import com.intellij.psi.*

/**
 * ドメインに関するチェック
 */
class BeanValidationInspectionToolProvider : InspectionToolProvider {

    @Suppress("UNCHECKED_CAST")
    override fun getInspectionClasses(): Array<Class<Any>> = arrayOf(InspectionTool::class.java) as Array<Class<Any>>

    class InspectionTool : BaseJavaLocalInspectionTool() {

        override fun getDisplayName(): String = "ドメインバリデーションに関するチェック"

        override fun isEnabledByDefault(): Boolean = true

        override fun getGroupDisplayName(): String = "nablarch"

        override fun getDefaultLevel(): HighlightDisplayLevel = HighlightDisplayLevel.ERROR

        override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
            return object : JavaElementVisitor() {
                override fun visitField(field: PsiField?) {
                    super.visitField(field ?: return)
                    verifyDomainName(field)
                    verifyDomain(field)
                }

                private fun verifyDomain(field: PsiField) {
                    val module = getModule(field) ?: return
                    if (getDomainBeanClasses(field.project, module).contains(field.containingClass ?: return)) {
                        if (!field.hasModifierProperty("public")) {
                            holder.registerProblem(field.nameIdentifier, "ドメインはpublicフィールドで定義してください。", ModifierFix(field, "public", true, false))
                        }
                    }
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
                            holder.registerProblem(it, "ドメイン名が指定されていません。")
                        } else {
                            findDomainField(project, module, domainName!!) ?:
                            holder.registerProblem(it, "ドメインがDomainBeanに存在しません。")
                        }
                    }

                }
            }
        }
    }
}
