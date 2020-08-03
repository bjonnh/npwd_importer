package wd

import org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf

typealias InChIKey = String

/**
 * Access compound information on Wikidata
 */
class WDCompound(override val wdSparql: WDSparql): WDThing<InChIKey> {
    /**
     * Find compounds by InChiKey
     */
    fun findCompoundByInChIKey(key: String): Map<InChIKey, List<WDEntity>> = findByPropertyValue(listOf(key))

    /**
     * Search large quantities of InChIKeys, by default they are chunked by groups of 100
     * this can be changed with the `chunkSize` if you have any performance issue
     */
    fun findCompoundsByInChIKey(keys: List<String>, chunkSize: Int = 100, chunkFeedBack: ()->Unit = {}) =
        findByPropertyValue(keys, chunkSize, chunkFeedBack)

    override val property = "P235"

    override fun stringToT(input: String): InChIKey = input
}