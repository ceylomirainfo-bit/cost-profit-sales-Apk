package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.OrderNoteEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ProductVariantEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleLineItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProductEntity::class,
        ProductVariantEntity::class,
        SaleEntity::class,
        SaleLineItemEntity::class,
        OrderNoteEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun orderNoteDao(): OrderNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN productType TEXT NOT NULL DEFAULT 'NORMAL'")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_products_productType ON products(productType)")
                db.execSQL("ALTER TABLE sale_line_items ADD COLUMN productType TEXT NOT NULL DEFAULT 'NORMAL'")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ceyvana_spices.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch {
                        populateInitialCatalogue(database)
                    }
                }
            }
        }

        suspend fun populateInitialCatalogue(database: AppDatabase) {
            val productDao = database.productDao()
            if (productDao.getProductCount() > 0) return

            // Authentic CEYVANA SPICES Catalog with verified Sri Lankan Sinhala/English titles,
            // pack sizes, and prices in Sri Lankan Rupees (LKR).
            val initialCatalog = listOf(
                // 1. SPICE PRODUCTS
                InitialProductDef(
                    name = "මිරිස් කුඩු (Chilli Powder)",
                    category = "SPICES",
                    description = "Finely ground sun-dried red chillies with bold pungent heat and rich crimson color.",
                    productType = com.example.data.model.ProductType.PREMIUM,
                    variants = listOf(
                        VariantDef("50g", 65.0, 110.0),
                        VariantDef("100g", 130.0, 220.0),
                        VariantDef("250g", 330.0, 550.0),
                        VariantDef("500g", 660.0, 1100.0),
                        VariantDef("1kg", 1320.0, 2200.0)
                    )
                ),
                InitialProductDef(
                    name = "කෑලි මිරිස් (Chilli Pieces)",
                    category = "SPICES",
                    description = "Coarsely crushed hot red chillies with seeds for authentic spicy lunu miris, sambols, and tempering.",
                    productType = com.example.data.model.ProductType.PREMIUM,
                    variants = listOf(
                        VariantDef("50g", 65.0, 105.0),
                        VariantDef("100g", 125.0, 210.0),
                        VariantDef("250g", 315.0, 525.0),
                        VariantDef("500g", 630.0, 1050.0),
                        VariantDef("1kg", 1260.0, 2100.0)
                    )
                ),
                InitialProductDef(
                    name = "තුනපහ කුඩු (Curry Powder)",
                    category = "SPICES",
                    description = "Classic raw Ceylon aromatic spice blend of coriander, cumin, fennel, and fragrant spices.",
                    productType = com.example.data.model.ProductType.PREMIUM,
                    variants = listOf(
                        VariantDef("50g", 55.0, 95.0),
                        VariantDef("100g", 115.0, 190.0),
                        VariantDef("250g", 285.0, 475.0),
                        VariantDef("500g", 570.0, 950.0),
                        VariantDef("1kg", 1140.0, 1900.0)
                    )
                ),
                InitialProductDef(
                    name = "කහ කුඩු (Turmeric Powder)",
                    category = "SPICES",
                    description = "100% pure organic Sri Lankan turmeric with high natural curcumin and vivid golden color.",
                    productType = com.example.data.model.ProductType.PREMIUM,
                    variants = listOf(
                        VariantDef("25g", 85.0, 140.0),
                        VariantDef("50g", 170.0, 280.0),
                        VariantDef("100g", 310.0, 510.0),
                        VariantDef("250g", 720.0, 1200.0),
                        VariantDef("500g", 1380.0, 2300.0),
                        VariantDef("1kg", 2760.0, 4600.0)
                    )
                ),
                InitialProductDef(
                    name = "බැදපු තුනපහ (Roasted Curry Powder)",
                    category = "SPICES",
                    description = "Masterfully slow-roasted dark Sri Lankan curry powder delivering deep aroma and savory curry gravies.",
                    productType = com.example.data.model.ProductType.PREMIUM,
                    variants = listOf(
                        VariantDef("50g", 70.0, 115.0),
                        VariantDef("100g", 140.0, 230.0),
                        VariantDef("250g", 345.0, 575.0),
                        VariantDef("500g", 690.0, 1150.0),
                        VariantDef("1kg", 1380.0, 2300.0)
                    )
                ),
                InitialProductDef(
                    name = "මස් කරි කුඩු (Meat Curry Powder)",
                    category = "SPICES",
                    description = "Specialized roasted spice blend enriched with black pepper, cardamom, and clove for savory meat and poultry.",
                    productType = com.example.data.model.ProductType.PREMIUM,
                    variants = listOf(
                        VariantDef("50g", 85.0, 140.0),
                        VariantDef("100g", 170.0, 280.0),
                        VariantDef("250g", 420.0, 700.0),
                        VariantDef("500g", 840.0, 1400.0),
                        VariantDef("1kg", 1680.0, 2800.0)
                    )
                ),
                InitialProductDef(
                    name = "උළුහාල් මිශ්රණය (Fenugreek Blend)",
                    category = "SPICES",
                    description = "Fragrant blend of roasted fenugreek (Uluhal) and complementary aromatic spices.",
                    productType = com.example.data.model.ProductType.NORMAL,
                    variants = listOf(
                        VariantDef("25g", 25.0, 40.0),
                        VariantDef("50g", 50.0, 80.0),
                        VariantDef("100g", 100.0, 160.0),
                        VariantDef("250g", 240.0, 400.0),
                        VariantDef("500g", 480.0, 800.0),
                        VariantDef("1kg", 960.0, 1600.0)
                    )
                ),
                InitialProductDef(
                    name = "ගොරක (Goraka)",
                    category = "GROCERY ITEMS",
                    description = "Naturally cured Gambooge (Garcinia cambogia) pods for fish ambul thiyal and sour curries.",
                    productType = com.example.data.model.ProductType.NORMAL,
                    variants = listOf(
                        VariantDef("50g", 25.0, 40.0),
                        VariantDef("100g", 50.0, 80.0),
                        VariantDef("250g", 120.0, 200.0),
                        VariantDef("500g", 240.0, 400.0),
                        VariantDef("1kg", 480.0, 800.0)
                    )
                ),
                InitialProductDef(
                    name = "ගම්මිරිස් (Black Pepper)",
                    category = "SPICES",
                    description = "Pure sun-ripened Ceylon black peppercorns renowned for high piperine and sharp heat.",
                    productType = com.example.data.model.ProductType.PREMIUM,
                    variants = listOf(
                        VariantDef("25g", 45.0, 70.0),
                        VariantDef("50g", 80.0, 130.0),
                        VariantDef("100g", 160.0, 260.0),
                        VariantDef("250g", 390.0, 650.0),
                        VariantDef("500g", 780.0, 1300.0),
                        VariantDef("1kg", 1560.0, 2600.0)
                    )
                ),
                InitialProductDef(
                    name = "තේ BOPF (BOPF Tea)",
                    category = "TEA",
                    description = "Brisk Ceylon Broken Orange Pekoe Fannings black tea delivering full body and golden brew.",
                    productType = com.example.data.model.ProductType.PREMIUM,
                    variants = listOf(
                        VariantDef("50g", 55.0, 90.0),
                        VariantDef("100g", 110.0, 180.0),
                        VariantDef("250g", 270.0, 450.0),
                        VariantDef("500g", 540.0, 900.0),
                        VariantDef("1kg", 1080.0, 1800.0)
                    )
                ),

                // 2. OTHER PRODUCTS
                InitialProductDef(
                    name = "සෝයා (Soya)",
                    category = "GROCERY ITEMS",
                    description = "High protein textured soya meat chunks for flavorful curries and stir-fries.",
                    productType = com.example.data.model.ProductType.NORMAL,
                    variants = listOf(
                        VariantDef("70g", 50.0, 80.0)
                    )
                ),
                InitialProductDef(
                    name = "පපඩම් (Papadam)",
                    category = "GROCERY ITEMS",
                    description = "Crispy sun-dried urad wafers that puff golden and crunchy in hot oil.",
                    productType = com.example.data.model.ProductType.NORMAL,
                    variants = listOf(
                        VariantDef("100g", 60.0, 100.0)
                    )
                ),
                InitialProductDef(
                    name = "කුරුඳු (Cinnamon)",
                    category = "SPICES",
                    description = "Authentic Ceylon cinnamon with delicate sweet aroma and layered quills.",
                    productType = com.example.data.model.ProductType.PREMIUM,
                    variants = listOf(
                        VariantDef("40g", 60.0, 100.0)
                    )
                )
            )

            for (item in initialCatalog) {
                val prodId = productDao.insertProduct(
                    ProductEntity(
                        name = item.name,
                        category = item.category,
                        productType = item.productType,
                        description = item.description,
                        imageUri = "default:spice",
                        isArchived = false
                    )
                )
                val variants = item.variants.map { v ->
                    ProductVariantEntity(
                        productId = prodId,
                        variantName = v.name,
                        costPrice = v.cost,
                        sellingPrice = v.sell,
                        currency = "LKR",
                        isActive = true
                    )
                }
                productDao.insertVariants(variants)
            }
        }
    }
}

private data class InitialProductDef(
    val name: String,
    val category: String,
    val description: String,
    val variants: List<VariantDef>,
    val productType: String = if (name.contains("Premium", ignoreCase = true) || name.contains("Authentic", ignoreCase = true)) {
        com.example.data.model.ProductType.PREMIUM
    } else {
        com.example.data.model.ProductType.NORMAL
    }
)

private data class VariantDef(
    val name: String,
    val cost: Double,
    val sell: Double
)
