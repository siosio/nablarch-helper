package siosio.repository.xml

import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import siosio.repository.*

object XmlHelper {

    /**
     * このXML要素の親タグを辿って、タグ名が**component**のタグを探す。
     *
     * 見つかった場合は、そのタグを表す[Component]オブジェクトを返す。
     * 見つからなかった場合は**null**
     */
    fun findComponent(element: XmlElement): Component? {
        return DomUtil.getDomElement(element)?.getParentOfType(Component::class.java, true)
    }
}