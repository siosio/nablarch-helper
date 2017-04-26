import com.intellij.psi.*
import com.intellij.util.xml.*
import siosio.repository.*

fun Property.isHandlerQueue(): Boolean {
    val element = DomUtil.getValueElement(this.name)
    if (element == null) {
        return false
    } else {
        return element.text == "\"handlerQueue\""
    }
}

fun ListComponent.isHandlerQueue(): Boolean {
    return this.name.value == "handlerQueue"
}

fun Property.parameterList():Array<PsiParameter> {
    return name.value?.parameterList?.parameters ?: emptyArray()
}
