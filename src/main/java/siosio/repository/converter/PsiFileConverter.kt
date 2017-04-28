package siosio.repository.converter

import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.module.*
import com.intellij.openapi.roots.impl.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.util.xml.*
import siosio.repository.*
import siosio.repository.extension.*
import siosio.repository.xml.*

/**

 */
class PsiFileConverter : Converter<PsiFile>(), CustomReferenceConverter<PsiFile> {

    override fun createReferences(value: GenericDomValue<PsiFile>?,
                                  element: PsiElement?,
                                  context: ConvertContext?): Array<PsiReference> {
        return arrayOf(MyReference(element!!, value, context))
    }

    override fun fromString(s: String?, context: ConvertContext?): PsiFile? {
        val project = context?.project ?: return null
        val module = context.module ?: return null

        val scope = GlobalSearchScope.moduleRuntimeScope(module, context.file.inTestScope(module))
        return ResourceFileUtil.findResourceFileInScope(s, project, scope)?.let {
            return PsiManager.getInstance(project).findFile(it)
        }
    }

    override fun toString(t: PsiFile?, context: ConvertContext?): String? {
        val project = context?.project ?: return null
        val directoryIndex = DirectoryIndex.getInstance(project)
        return directoryIndex.toResourceFile(t?.virtualFile ?: return null)
    }

    class MyReference(psiElement: PsiElement,
                      val file: GenericDomValue<PsiFile>?,
                      private val context: ConvertContext?) : PsiReferenceBase<PsiElement>(psiElement) {

        override fun getVariants(): Array<out Any> {
            context ?: return emptyArray()

            val directoryIndex = DirectoryIndex.getInstance(myElement.project)
            return XmlHelper.findNablarchXml(context) {
                map {
                    LookupElementBuilder.create(directoryIndex.toResourceFile(it.virtualFile))
                        .withIcon(IconLoader.getIcon("/nablarch.png"))
                        .withAutoCompletionPolicy(AutoCompletionPolicy.ALWAYS_AUTOCOMPLETE)
                }.toList().toTypedArray()
            } ?: emptyArray()
        }

        override fun resolve(): PsiElement? {
            return file?.value
        }
    }

}

private fun DirectoryIndex.toResourceFile(file: VirtualFile): String {
    val packageName = getPackageName(file.parent)
    return if (packageName.isNullOrEmpty()) {
        file.name
    } else {
        packageName!!.replace('.', '/') + '/' + file.name
    }
}

