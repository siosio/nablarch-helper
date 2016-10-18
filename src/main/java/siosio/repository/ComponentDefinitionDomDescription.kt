package siosio.repository

import com.intellij.openapi.util.IconLoader
import com.intellij.util.xml.DomFileDescription
import javax.swing.Icon

class ComponentDefinitionDomDescription :
    DomFileDescription<ComponentDefinition>(ComponentDefinition::class.java, "component-configuration") {

  override fun getFileIcon(flags: Int): Icon {
    return IconLoader.getIcon("/nablarch.png")
  }
}
