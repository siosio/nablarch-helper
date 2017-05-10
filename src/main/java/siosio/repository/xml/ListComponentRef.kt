package siosio.repository.xml

/**
 * listタグ内に置かれるcomponent-refタグ
 */
interface ListComponentRef : com.intellij.util.xml.DomElement {

    @get:com.intellij.util.xml.Attribute("name")
    @get:com.intellij.util.xml.Convert(siosio.repository.converter.RepositoryListRefConverter::class)
    val componentClass: com.intellij.util.xml.GenericAttributeValue<Component>

}
