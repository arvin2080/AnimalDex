package com.example.animaldex.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.example.animaldex.model.Animal


private const val DATABASE_NAME =
    "animaldex.db"


fun databaseHasNewColumns(
    databaseFile: java.io.File
): Boolean {

    if (!databaseFile.exists()) {

        return false
    }


    return try {

        val database =

            SQLiteDatabase.openDatabase(

                databaseFile.absolutePath,

                null,

                SQLiteDatabase.OPEN_READONLY
            )


        val columns =
            mutableSetOf<String>()


        database.rawQuery(
            "PRAGMA table_info(animals)",
            null
        ).use { cursor ->

            val nameIndex =
                cursor.getColumnIndexOrThrow(
                    "name"
                )


            while (cursor.moveToNext()) {

                columns.add(
                    cursor.getString(
                        nameIndex
                    )
                )
            }
        }


        database.close()


        columns.contains(
            "description_fr"
        ) &&
                columns.contains(
                    "fun_fact_fr"
                )

    } catch (
        error: Exception
    ) {

        false
    }
}


fun readExistingDiscoveries(
    databaseFile: java.io.File
): List<Int> {

    if (!databaseFile.exists()) {

        return emptyList()
    }


    return try {

        val database =

            SQLiteDatabase.openDatabase(

                databaseFile.absolutePath,

                null,

                SQLiteDatabase.OPEN_READONLY
            )


        val result =
            mutableListOf<Int>()


        database.rawQuery(

            "SELECT animal_id FROM discoveries",

            null

        ).use { cursor ->

            while (cursor.moveToNext()) {

                result.add(
                    cursor.getInt(0)
                )
            }
        }


        database.close()

        result

    } catch (
        error: Exception
    ) {

        emptyList()
    }
}


fun restoreDiscoveries(

    databaseFile: java.io.File,

    discoveries: List<Int>
) {

    if (discoveries.isEmpty()) {

        return
    }


    try {

        val database =

            SQLiteDatabase.openDatabase(

                databaseFile.absolutePath,

                null,

                SQLiteDatabase.OPEN_READWRITE
            )


        database.beginTransaction()


        try {

            discoveries.forEach { animalId ->

                database.execSQL(

                    """
                    INSERT OR IGNORE INTO discoveries(animal_id)
                    VALUES(?)
                    """.trimIndent(),

                    arrayOf(
                        animalId
                    )
                )
            }


            database.setTransactionSuccessful()

        } finally {

            database.endTransaction()

            database.close()
        }

    } catch (
        error: Exception
    ) {

        error.printStackTrace()
    }
}


// ============================================================
// COMPTEURS DE CAPTURE (nouvelle table, séparée de "discoveries")
// ============================================================
//
// "discoveries" reste inchangée (toujours juste "vu / pas vu").
// Cette table à part stocke combien de fois chaque animal a été
// capturé. Créée nous-mêmes ici — donc entièrement sous contrôle,
// sans dépendre du schéma du fichier animaldex.db livré dans les
// assets.

private fun ensureCaptureCountsTable(
    database: SQLiteDatabase
) {

    database.execSQL(

        """
        CREATE TABLE IF NOT EXISTS capture_counts (
            animal_id INTEGER PRIMARY KEY,
            count INTEGER NOT NULL DEFAULT 0
        )
        """.trimIndent()
    )
}


fun readExistingCaptureCounts(
    databaseFile: java.io.File
): Map<Int, Int> {

    if (!databaseFile.exists()) {

        return emptyMap()
    }


    return try {

        val database =

            SQLiteDatabase.openDatabase(

                databaseFile.absolutePath,

                null,

                SQLiteDatabase.OPEN_READONLY
            )


        val result =
            mutableMapOf<Int, Int>()


        database.rawQuery(

            "SELECT animal_id, count FROM capture_counts",

            null

        ).use { cursor ->

            while (cursor.moveToNext()) {

                result[
                    cursor.getInt(0)
                ] =
                    cursor.getInt(1)
            }
        }


        database.close()

        result

    } catch (
        error: Exception
    ) {

        // La table peut ne pas encore exister (base pas encore
        // migrée) — dans ce cas, pas de compteurs à restaurer.
        emptyMap()
    }
}


fun restoreCaptureCounts(

    databaseFile: java.io.File,

    counts: Map<Int, Int>
) {

    if (counts.isEmpty()) {

        return
    }


    try {

        val database =

            SQLiteDatabase.openDatabase(

                databaseFile.absolutePath,

                null,

                SQLiteDatabase.OPEN_READWRITE
            )


        ensureCaptureCountsTable(
            database
        )


        database.beginTransaction()


        try {

            counts.forEach { (animalId, count) ->

                database.execSQL(

                    """
                    INSERT OR REPLACE INTO capture_counts(animal_id, count)
                    VALUES(?, ?)
                    """.trimIndent(),

                    arrayOf(
                        animalId,
                        count
                    )
                )
            }


            database.setTransactionSuccessful()

        } finally {

            database.endTransaction()

            database.close()
        }

    } catch (
        error: Exception
    ) {

        error.printStackTrace()
    }
}


// Incrémente le compteur de capture d'un animal (crée la ligne à 0
// si elle n'existe pas encore, puis l'augmente de 1). Utilise deux
// étapes simples (INSERT OR IGNORE puis UPDATE) plutôt qu'un UPSERT
// SQLite récent, pour rester compatible avec d'anciennes versions
// d'Android sans mauvaise surprise.
fun incrementAnimalCapture(
    context: Context,
    animalId: Int
) {

    val databaseFile =

        context.getDatabasePath(
            DATABASE_NAME
        )


    try {

        val database =

            SQLiteDatabase.openDatabase(

                databaseFile.absolutePath,

                null,

                SQLiteDatabase.OPEN_READWRITE
            )


        ensureCaptureCountsTable(
            database
        )


        database.beginTransaction()


        try {

            database.execSQL(

                """
                INSERT OR IGNORE INTO capture_counts(animal_id, count)
                VALUES(?, 0)
                """.trimIndent(),

                arrayOf(
                    animalId
                )
            )


            database.execSQL(

                """
                UPDATE capture_counts
                SET count = count + 1
                WHERE animal_id = ?
                """.trimIndent(),

                arrayOf(
                    animalId
                )
            )


            // "discoveries" reste alimentée exactement comme avant,
            // pour ne rien casser de ce qui en dépendait déjà.
            database.execSQL(

                """
                INSERT OR IGNORE INTO discoveries(animal_id)
                VALUES(?)
                """.trimIndent(),

                arrayOf(
                    animalId
                )
            )


            database.setTransactionSuccessful()

        } finally {

            database.endTransaction()

            database.close()
        }

    } catch (
        error: Exception
    ) {

        error.printStackTrace()
    }
}


// Lit le nombre de captures actuel d'un animal (0 si jamais capturé).
// Utile juste après incrementAnimalCapture pour savoir quoi afficher
// (ex. "n trouvé +1") sans recharger toute la base de données.
fun readAnimalCaptureCount(
    context: Context,
    animalId: Int
): Int {

    val databaseFile =

        context.getDatabasePath(
            DATABASE_NAME
        )


    return try {

        val database =

            SQLiteDatabase.openDatabase(

                databaseFile.absolutePath,

                null,

                SQLiteDatabase.OPEN_READONLY
            )


        var result = 0


        database.rawQuery(

            "SELECT count FROM capture_counts WHERE animal_id = ?",

            arrayOf(
                animalId.toString()
            )

        ).use { cursor ->

            if (cursor.moveToFirst()) {

                result =
                    cursor.getInt(0)
            }
        }


        database.close()

        result

    } catch (
        error: Exception
    ) {

        0
    }
}


fun copyDatabaseFromAssets(
    context: Context
) {

    val databaseFile =

        context.getDatabasePath(
            DATABASE_NAME
        )


    /*if (
        databaseHasNewColumns(
            databaseFile
        )
    ) {

        return
    }*/


    val oldDiscoveries =

        readExistingDiscoveries(
            databaseFile
        )


    val oldCaptureCounts =

        readExistingCaptureCounts(
            databaseFile
        )


    if (databaseFile.exists()) {

        SQLiteDatabase.deleteDatabase(
            databaseFile
        )
    }


    databaseFile
        .parentFile
        ?.mkdirs()


    context.assets

        .open(
            DATABASE_NAME
        )

        .use { input ->

            databaseFile

                .outputStream()

                .use { output ->

                    input.copyTo(
                        output
                    )
                }
        }


    restoreDiscoveries(

        databaseFile,

        oldDiscoveries
    )


    restoreCaptureCounts(

        databaseFile,

        oldCaptureCounts
    )
}


fun loadAnimalsFromDatabase(
    context: Context
): List<Animal> {

    copyDatabaseFromAssets(
        context
    )


    val databaseFile =

        context.getDatabasePath(
            DATABASE_NAME
        )


    val database =

        SQLiteDatabase.openDatabase(

            databaseFile.absolutePath,

            null,

            SQLiteDatabase.OPEN_READWRITE
        )


    // Table déjà créée par restoreCaptureCounts en temps normal, mais
    // on s'assure qu'elle existe même sur un tout premier lancement
    // sans aucun compteur à restaurer.
    ensureCaptureCountsTable(
        database
    )


    val animals =
        mutableListOf<Animal>()


    val query = """

        SELECT

            a.id,
            a.name_fr,
            a.common_names_en,
            a.scientific_name,
            a.continents,
            a.phylopic_uuid,
            a.family,
            a.genus,
            a.description_fr,
            a.fun_fact_fr,

            CASE
                WHEN d.animal_id IS NULL THEN 0
                ELSE 1
            END AS discovered,

            COALESCE(cc.count, 0) AS capture_count

        FROM animals a

        LEFT JOIN discoveries d
            ON d.animal_id = a.id

        LEFT JOIN capture_counts cc
            ON cc.animal_id = a.id

        ORDER BY a.id

    """.trimIndent()


    database.rawQuery(
        query,
        null
    ).use { cursor ->

        val idIndex =
            cursor.getColumnIndexOrThrow("id")

        val nameFrIndex =
            cursor.getColumnIndexOrThrow("name_fr")

        val commonENIndex =
            cursor.getColumnIndexOrThrow("common_names_en")

        val scientificIndex =
            cursor.getColumnIndexOrThrow("scientific_name")

        val continentsIndex =
            cursor.getColumnIndexOrThrow("continents")

        val uuidIndex =
            cursor.getColumnIndexOrThrow("phylopic_uuid")

        val familyIndex =
            cursor.getColumnIndexOrThrow("family")

        val genusIndex =
            cursor.getColumnIndexOrThrow("genus")

        val descriptionFrIndex =
            cursor.getColumnIndexOrThrow("description_fr")

        val funFactFrIndex =
            cursor.getColumnIndexOrThrow("fun_fact_fr")

        val discoveredIndex =
            cursor.getColumnIndexOrThrow("discovered")

        val captureCountIndex =
            cursor.getColumnIndexOrThrow("capture_count")


        while (cursor.moveToNext()) {

            val rawContinents =

                if (
                    cursor.isNull(
                        continentsIndex
                    )
                ) {

                    null

                } else {

                    cursor.getString(
                        continentsIndex
                    )
                }


            val parsedContinents =

                if (
                    rawContinents.isNullOrBlank()
                ) {

                    listOf(
                        "UNKNOWN"
                    )

                } else {

                    rawContinents

                        .split("|")

                        .map { continent ->

                            continent
                                .trim()
                                .uppercase()
                        }
                }


            val uuid =

                if (
                    cursor.isNull(
                        uuidIndex
                    )
                ) {

                    null

                } else {

                    cursor.getString(
                        uuidIndex
                    )

                        .trim()

                        .takeIf { value ->

                            value.isNotBlank()
                        }
                }


            animals.add(

                Animal(

                    id =
                        cursor.getInt(
                            idIndex
                        ),

                    nameFr =
                        if (
                            cursor.isNull(
                                nameFrIndex
                            )
                        ) {

                            null

                        } else {

                            cursor.getString(
                                nameFrIndex
                            )
                        },

                    commonNameEN =
                        if (
                            cursor.isNull(
                                commonENIndex
                            )
                        ) {

                            null

                        } else {

                            cursor.getString(
                                commonENIndex
                            )
                        },

                    scientificName =
                        cursor.getString(
                            scientificIndex
                        ),

                    family =
                        if (
                            cursor.isNull(
                                familyIndex
                            )
                        ) {

                            null

                        } else {

                            cursor.getString(
                                familyIndex
                            )
                        },

                    genus =
                        if (
                            cursor.isNull(
                                genusIndex
                            )
                        ) {

                            null

                        } else {

                            cursor.getString(
                                genusIndex
                            )
                        },

                    continents =
                        parsedContinents,

                    phylopicUuid =
                        uuid,

                    discovered =
                        cursor.getInt(
                            discoveredIndex
                        ) == 1,

                    descriptionFr =
                        if (
                            cursor.isNull(
                                descriptionFrIndex
                            )
                        ) {

                            null

                        } else {

                            cursor.getString(
                                descriptionFrIndex
                            )
                        },

                    funFactFr =
                        if (
                            cursor.isNull(
                                funFactFrIndex
                            )
                        ) {

                            null

                        } else {

                            cursor.getString(
                                funFactFrIndex
                            )
                        },

                    captureCount =
                        cursor.getInt(
                            captureCountIndex
                        )
                )
            )
        }
    }


    database.close()


    return animals
}