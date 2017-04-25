package siosio.repository.inspection

import com.intellij.codeInspection.InspectionToolProvider

class ComponentDefinitionInspectionToolProvider : InspectionToolProvider {

  override fun getInspectionClasses(): Array<Class<Any>> {
      @Suppress("UNCHECKED_CAST")
      return arrayOf(ComponentDefinitionInspectionTool::class.java) as Array<Class<Any>>
  }
}
