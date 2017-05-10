import com.intellij.psi.*
import com.intellij.util.xml.*
import siosio.repository.xml.*

fun DomElement.inHandlerQueue(): Boolean {
    val list = getParentOfType(ListObject::class.java, true)
    val property = getParentOfType(Property::class.java, true)
    return list?.let {
        it.isHandlerQueue() || property?.isHandlerQueue() ?: false
    } ?: false
}

fun Property.isHandlerQueue(): Boolean {
    val element = DomUtil.getValueElement(this.name)
    if (element == null) {
        return false
    } else {
        return element.text == "\"handlerQueue\""
    }
}

fun ListObject.isHandlerQueue(): Boolean {
    return this.name.value == "handlerQueue"
}

fun Property.parameterList(): Array<PsiParameter> {
    return name.value?.parameterList?.parameters ?: emptyArray()
}
