package siosio.repository

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.util.*
import com.intellij.psi.xml.*
import com.intellij.util.xml.*

/**
 * このXML要素の親タグを辿って、タグ名が**component**のタグを探す。
 *
 * 見つかった場合は、そのタグを表す[Component]オブジェクトを返す。
 * 見つからなかった場合は**null**
 */
internal fun findComponent(element: XmlElement): Component? {
    val xmlTag = PsiTreeUtil.findFirstParent(element, { element ->
        (element is XmlTag && element.name == "component")
    }) as XmlTag?

    val domElement = DomUtil.getDomElement(xmlTag)
    return if (domElement is Component) {
        domElement
    } else {
        null
    }
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
 * この要素が設定されているクラス([PsiClass])を取得する。
 */
internal fun findComponentClass(element: XmlElement): PsiClass? {
    return findComponent(element)?.componentClass?.value
}

/**
 * 名前付きタグを抽出する。
 */
internal fun findNamedElement(context: ConvertContext?): List<NamedElement> {
    if (context == null) {
        return emptyList()
    }
    return findNablarchXml(context)
        .map {
            val domElement = DomUtil.getDomElement(it.rootTag) as ComponentDefinition
            domElement.components + domElement.lists
        }
        .flatten()
        .filter {
            // name属性に値が設定されている要素だけにする
            !it.name.value.isNullOrBlank()
        }.toList()
}

inline internal fun findNablarchXml(context: ConvertContext): Sequence<XmlFile> {
    val module = context.module
    val contains = GlobalSearchScope.moduleRuntimeScope(module!!, false).contains(context.file.originalFile.virtualFile)
    return FilenameIndex.getAllFilesByExt(context.project, "xml", module.getModuleRuntimeScope(!contains))
        .asSequence()
        .map {
            PsiManager.getInstance(context.project).findFile(it)
        }
        .filter {
            when (it) {
                is XmlFile -> {
                    val domElement = DomUtil.getDomElement(it.rootTag)
                    domElement is ComponentDefinition
                }
                else -> false
            }
        }
        .filterNotNull()
        .filter { it is XmlFile }
        .map { it as XmlFile }
}

internal fun createHandlerInterfaceType(project: Project): PsiType? {
    val psiClass = JavaPsiFacade.getInstance(project).findClasses("nablarch.fw.Handler", GlobalSearchScope.allScope(project))
    return psiClass.firstOrNull()?.let {
        PsiTypesUtil.getClassType(it)
    }
}

