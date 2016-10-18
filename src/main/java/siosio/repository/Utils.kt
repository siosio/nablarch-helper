package siosio.repository

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiType
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag
import com.intellij.util.xml.ConvertContext
import com.intellij.util.xml.DomUtil

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
      right.superTypes.firstOrNull() {
        isAssignableFrom(left, it)
      } != null
    }
  }
}

/**
 * この要素が設定されているクラス([PsiClass])を取得する。
 */
internal fun findComponentClass(element: XmlElement): PsiClass? {
  return findComponent(element)?.let {
    it.componentClass.value
  } ?: null
}

/**
 * 名前付きタグを抽出する。
 */
internal fun findNamedElement(context: ConvertContext?): List<NamedElement> {
  return context?.let {
    val module = it.module
    val contains = GlobalSearchScope.moduleRuntimeScope(module!!, false).contains(context.file.originalFile.virtualFile)

    FilenameIndex.getAllFilesByExt(it.project, "xml", module.getModuleRuntimeScope(!contains))
        .map {
          val file = PsiManager.getInstance(context.project).findFile(it)
          // コンポーネント定義ファイル内のcomponent要素とlist要素を探す
          if (file is XmlFile) {
            val domElement = DomUtil.getDomElement(file.rootTag)
            if (domElement is ComponentDefinition) {
              (domElement.components + domElement.lists)
            } else {
              listOf<NamedElement>()
            }
          } else {
            listOf<NamedElement>()
          }
        }.asSequence()
        .flatten()
        .filter {
          // name属性に値が設定されている要素だけにする
          !it.name.value.isNullOrBlank()
        }.toList()
  } ?: emptyList()
}
