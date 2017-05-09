package siosio.repository.psi

import com.intellij.psi.xml.*
import siosio.*
import javax.swing.*

class ComponentXmlTagElement(private val xmlTag: XmlTag) : XmlTag by xmlTag {

    override fun getIcon(flags: Int): Icon = nablarchIcon

    
}

