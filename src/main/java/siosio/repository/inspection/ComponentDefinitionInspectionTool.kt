package siosio.repository.inspection

import com.intellij.util.xml.*
import com.intellij.util.xml.highlighting.*
import siosio.repository.xml.*

/**
 * dom構造をチェックするクラス。
 */
class ComponentDefinitionInspectionTool : BasicDomElementsInspection<ComponentDefinition>(ComponentDefinition::class.java) {

  override fun getGroupDisplayName(): String = "nablarch"

  override fun getDisplayName(): String = "コンポーネント定義ファイルの内容をチェックする"

  override fun isEnabledByDefault(): Boolean = true


  override fun checkDomElement(element: DomElement?, holder: DomElementAnnotationHolder?, helper: DomHighlightingHelper?) {
    super.checkDomElement(element, holder, helper)
  }
}
