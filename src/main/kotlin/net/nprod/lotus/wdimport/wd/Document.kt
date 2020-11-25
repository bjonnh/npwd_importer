package net.nprod.lotus.wdimport.wd

import net.nprod.lotus.wdimport.wd.models.ReferencableValueStatement
import net.nprod.lotus.wdimport.wd.models.ReferenceableRemoteItemStatement
import org.wikidata.wdtk.datamodel.helpers.Datamodel
import org.wikidata.wdtk.datamodel.helpers.ItemDocumentBuilder
import org.wikidata.wdtk.datamodel.helpers.ReferenceBuilder
import org.wikidata.wdtk.datamodel.helpers.StatementBuilder
import org.wikidata.wdtk.datamodel.interfaces.*

/**
 * Type safe builder or DSL
 */

fun newReference(f: (ReferenceBuilder) -> Unit): Reference {
    val reference = ReferenceBuilder.newInstance()
    reference.apply(f)
    return reference.build()
}

fun newDocument(name: String, id: ItemIdValue? = null, f: ItemDocumentBuilder.() -> Unit): ItemDocument {
    val builder = ItemDocumentBuilder.forItemId(id ?: ItemIdValue.NULL)

    if (name != "") builder.withLabel(name, "en")
    builder.apply(f)

    return builder.build()
}

fun ReferenceBuilder.propertyValue(property: PropertyIdValue, value: String) =
    this.propertyValue(property, Datamodel.makeStringValue(value))

fun ReferenceBuilder.propertyValue(property: PropertyIdValue, value: Value) {
    this.withPropertyValue(
        property,
        value
    )
}

fun StatementBuilder.reference(reference: Reference) {
    this.reference(reference)
}

fun ItemDocumentBuilder.statement(
    subject: ItemIdValue? = null,
    property: PropertyIdValue,
    value: String,
    f: (StatementBuilder) -> Unit = {}
): ItemDocumentBuilder =
    this.withStatement(newStatement(property, subject, Datamodel.makeStringValue(value), f))

fun ItemDocumentBuilder.statement(
    subject: ItemIdValue? = null,
    property: PropertyIdValue,
    value: Value,
    f: (StatementBuilder) -> Unit = {}
): ItemDocumentBuilder =
    this.withStatement(newStatement(property, subject, value, f))

fun ItemDocumentBuilder.statement(
    subject: ItemIdValue? = null,
    referencableStatement: ReferencableValueStatement,
    instanceItems: InstanceItems
) {
    val statement = newStatement(
        referencableStatement.property.get(instanceItems),
        subject,
        referencableStatement.value
    ) { statementBuilder ->
        referencableStatement.preReferences.forEach {
            statementBuilder.withReference(it.build(instanceItems))
        }
    }

    this.withStatement(statement)
}

fun ItemDocumentBuilder.statement(
    subject: ItemIdValue?,
    referenceableStatement: ReferenceableRemoteItemStatement,
    instanceItems: InstanceItems
) =
    this.statement(
        subject,
        ReferencableValueStatement(
            referenceableStatement.property,
            referenceableStatement.value.get(instanceItems),
            referenceableStatement.preReferences
        ),
        instanceItems
    )

fun newStatement(
    property: PropertyIdValue,
    subject: ItemIdValue? = null,
    value: Value,
    f: (StatementBuilder) -> Unit = {}
): Statement {
    val statement = StatementBuilder.forSubjectAndProperty(subject ?: ItemIdValue.NULL, property)
        .withValue(value)
        .apply(f)
    return statement.build()
}
