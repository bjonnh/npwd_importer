package net.nprod.lotus.input

import java.io.File
import net.nprod.lotus.helpers.GZIPReader
import net.nprod.lotus.helpers.parseTSVFile
import org.apache.logging.log4j.LogManager

fun loadData(fileName: String, skip: Int = 0, limit: Int? = null): DataTotal {
    val logger = LogManager.getLogger("net.nprod.lotus.chemistry.net.nprod.lotus.tools.wdpropcreator.main")
    val dataTotal = DataTotal()

    logger.info("Started")
    val fileReader = try { GZIPReader(fileName).bufferedReader } catch (e: java.util.zip.ZipException) {
        File(fileName).bufferedReader()
    }
    val file = parseTSVFile(fileReader, limit, skip)

    file?.map {
        val database = it.getString("database")
        val organismCleaned = it.getString("organismCleaned")
        val organismDb = it.getString("organismCleaned_dbTaxo")
        val organismIDs = it.getString("organismCleaned_dbTaxoTaxonIds")
        val organismRanks = it.getString("organismCleaned_dbTaxoTaxonRanks")
        val organismNames = it.getString("organismCleaned_dbTaxoTaxonomy")

        val smiles = it.getString("structureCleanedSmiles")
        val doi = it.getString("referenceCleanedDoi")

        if (organismRanks.contains("genus") || organismRanks.contains("species")) {

            val databaseObj = dataTotal.databaseCache.getOrNew(database) {
                Database(name = database)
            }

            val organismObj = dataTotal.organismCache.getOrNew(organismCleaned) {
                Organism(name = organismCleaned)
            }

            organismObj.textIds[organismDb] = organismIDs
            organismObj.textRanks[organismDb] = organismRanks
            organismObj.textNames[organismDb] = organismNames

            val compoundObj = dataTotal.compoundCache.getOrNew(smiles) {
                Compound(
                    name = it.getString("structureCleaned_nameTraditional"),
                    smiles = smiles, inchi = it.getString("structureCleanedInchi"),
                    inchikey = it.getString("structureCleanedInchikey3D")
                )
            }

            val referenceObj = dataTotal.referenceCache.getOrNew(doi) {
                Reference(
                    doi = doi,
                    title = it.getString("referenceCleanedTitle").ifEqualReplaceByNull("NA")?.ifEqualReplaceByNull(""),
                    pmcid = it.getString("referenceCleanedPmcid").ifEqualReplace("NA", ""),
                    pmid = it.getString("referenceCleanedPmid").ifEqualReplace("NA", "")
                )
            }

            dataTotal.quads.add(
                Quad(
                    databaseObj,
                    organismObj,
                    compoundObj,
                    referenceObj
                )
            )
        } else {
            logger.error("Invalid entry: $it")
        }
    }
    logger.info("Done importing")
    logger.info("Resolving the taxo DB")
    dataTotal.organismCache.store.values.forEach {
        it.resolve(dataTotal.taxonomyDatabaseCache)
    }

    logger.info(dataTotal.taxonomyDatabaseCache.store.values)
    logger.info("Done")
    fileReader.close()
    return dataTotal
}
