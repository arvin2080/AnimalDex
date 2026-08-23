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

            SQLiteDatabase.OPEN_READONLY
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
            END AS discovered

        FROM animals a

        LEFT JOIN discoveries d
            ON d.animal_id = a.id

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
                        }
                )
            )
        }
    }


    database.close()


    return animals
}