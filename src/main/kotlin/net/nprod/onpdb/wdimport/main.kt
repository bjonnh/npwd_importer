package net.nprod.onpdb.wdimport

import net.nprod.onpdb.goldcleaner.loadGoldData
import net.nprod.onpdb.wdimport.wd.InstanceItems
import net.nprod.onpdb.wdimport.wd.MainInstanceItems
import net.nprod.onpdb.wdimport.wd.TestInstanceItems
import net.nprod.onpdb.wdimport.wd.WDPublisher
import net.nprod.onpdb.wdimport.wd.models.RemoteProperty
import net.nprod.onpdb.wdimport.wd.models.WDArticle
import net.nprod.onpdb.wdimport.wd.models.WDCompound
import net.nprod.onpdb.wdimport.wd.models.WDTaxon
import net.nprod.onpdb.wdimport.wd.sparql.WDSparql
import org.apache.logging.log4j.LogManager

const val GOLD_PATH = "/home/bjo/Store/01_Research/opennaturalproductsdb/data/interim/tables/4_analysed/gold.tsv.gz"


fun main(args: Array<String>) {
    val logger = LogManager.getLogger("net.nprod.onpdb.wdimport.main")
    logger.info("Playing with Wikidata Toolkit")

    val instanceItems = TestInstanceItems

    logger.info("Initializing toolkit")
    /*val wikibaseDataFetcher = WikibaseDataFetcher(
        BasicApiConnection.getTestWikidataApiConnection(), // TODO put that in instance as well
        "http://test.wikidata.org/entity/"
    )*/
/*
    logger.info("Fetching data for something")
    val thing = wikibaseDataFetcher.getEntityDocument("Q212578")
    println(thing)*/

    val wdSparql = WDSparql(MainInstanceItems) // TODO: For tests we use the official…
    val publisher = WDPublisher(instanceItems)

    publisher.connect()

    val dataTotal = loadGoldData(GOLD_PATH, 100)

    logger.info("Producing organisms")

    val organisms = dataTotal.organismCache.store.values.map {organism ->
        val (genus, species) = organism.name.split(" ").subList(0,2)
        val genusWD = WDTaxon(
            name = genus,
            parentTaxon = null,
            taxonName = genus,
            taxonRank = InstanceItems::genus
        ).tryToFind(wdSparql, instanceItems)

        publisher.publish(genusWD, "Created a missing genus")

        val speciesWD = WDTaxon(
            name = organism.name,
            parentTaxon = genusWD.id,
            taxonName = species,
            taxonRank = InstanceItems::species
        )

        organism.textIds.forEach { dbEntry ->
            speciesWD.addTaxoDB(dbEntry.key, dbEntry.value)
        }

        // Todo, add the taxinfo

        publisher.publish(speciesWD, "Created a missing genus")
        organism to speciesWD
    }.toMap()

    logger.info("Producing articles")

    val references = dataTotal.referenceCache.store.map {
        val article = WDArticle(
            name = "No title yet…",
            title = it.value.doi, // TODO: get the titles
            doi = it.value.doi,
        )
        // TODO: Add PMID and PMCID
        publisher.publish(article, "Creating a new article")
        it.value to article
    }.toMap()

    println("Linking")

    dataTotal.compoundCache.store.forEach { (id, compound) ->
        val wdcompound = WDCompound(
            name = compound.inchikey,
            inChIKey = compound.inchikey,
            inChI = compound.inchi,
            isomericSMILES = compound.inchi,
            pcId = "TODO", // TODO: Export PCID
            chemicalFormula = "TODO" // TODO: Calculate chemical formula
        ) {
            dataTotal.quads.filter { it.compound == compound }.distinct().forEach { quad ->
                naturalProductOfTaxon(organisms[quad.organism] ?: throw Exception("That's bad, we talk about an organism we don't have")) {
                    statedIn(references[quad.reference]?.id ?: throw Exception("That's bad we talk about a reference we don't have."))
                }
            }
        }

        publisher.publish(wdcompound, "Creating a new compound")
    }
/*



*/
    publisher.disconnect()
}