package siosio.repository

import com.intellij.util.xml.*
import siosio.*
import javax.swing.*

class ComponentDefinitionDomDescription :
    DomFileDescription<ComponentDefinition>(ComponentDefinition::class.java, "component-configuration") {

  override fun getFileIcon(flags: Int): Icon {
    return nablarchIcon
  }
}
