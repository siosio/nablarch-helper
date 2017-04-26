package siosio.repository

import com.intellij.util.xml.*
import siosio.repository.converter.*

/**
 * listタグ内に置かれるcomponent-refタグ
 */
interface ListComponentRef : DomElement {

    @get:Attribute("name")
    @get:Convert(RepositoryListRefConverter::class)
    val componentClass: GenericAttributeValue<Component>

}
