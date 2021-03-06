package siosio.repository.xml

import com.intellij.openapi.module.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*
import siosio.extension.*
import siosio.repository.extension.*

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


    internal fun isAssignableFrom(left: PsiType, right: PsiType?): Boolean {
        return if (right == null) {
            false
        } else {
            if (left.isAssignableFrom(right)) {
                true
            } else {
                right.superTypes.firstOrNull {
                    isAssignableFrom(left, it)
                } != null
            }
        }
    }

    /**
     * 名前付きタグを抽出する。
     */
    internal fun findNamedElement(context: ConvertContext?): List<NamedElement> {
        if (context == null) {
            return emptyList()
        }
        return findNablarchXml(context.file.originalFile) {
            map {
                val domElement = DomUtil.getDomElement(it.rootTag) as ComponentDefinition
                domElement.components + domElement.lists
            }.flatten().filter {
                // name属性に値が設定されている要素だけにする
                !it.name.value.isNullOrBlank()
            }.toList()
        } ?: emptyList()
    }
    
    internal fun findNamedElement(element: PsiElement): List<NamedElement> {
        return findNablarchXml(element) {
            map {
                val domElement = DomUtil.getDomElement(it.rootTag) as ComponentDefinition
                domElement.components + domElement.lists
            }.flatten().filter {
                // name属性に値が設定されている要素だけにする
                !it.name.value.isNullOrBlank()
            }.toList()
        } ?: emptyList()
    }

    /**
     * コンポーネント定義のXMLファイルを抽出する
     */
    internal fun <T> findNablarchXml(element: PsiElement,
                                     module: Module? = element.getModule(),
                                     block: Sequence<XmlFile>.() -> T): T? {
        module ?: return null
        return FilenameIndex.getAllFilesByExt(element.project, "xml", module.getModuleRuntimeScope(element.containingFile.inTestScope(module)))
            .asSequence()
            .map {
                PsiManager.getInstance(element.project).findFile(it)
            }
            .mapNotNull {
                when (it) {
                    is XmlFile -> if (DomUtil.getDomElement(it.rootTag) is ComponentDefinition) {
                        it
                    } else {
                        null
                    }
                    else -> null
                }
            }.block()
    }

}