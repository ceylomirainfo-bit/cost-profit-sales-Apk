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
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun orderNoteDao(): OrderNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ceyvana_spices.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
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

            // Initial catalog definitions with authentic Sri Lankan packaging variants & LKR pricing
            val initialCatalog = listOf(
                // A. SPICES
                InitialProductDef(
                    name = "Ceyvana Premium Chilli Powder",
                    category = "SPICES",
                    description = "Finely ground sun-dried Ceylon red chillies with bold pungent heat and vibrant crimson colour.",
                    variants = listOf(
                        VariantDef("50g", 120.0, 190.0),
                        VariantDef("100g", 230.0, 360.0),
                        VariantDef("250g", 540.0, 850.0),
                        VariantDef("500g", 1020.0, 1650.0),
                        VariantDef("1kg", 1950.0, 3100.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceyvana Premium Chilli Pieces",
                    category = "SPICES",
                    description = "Coarsely crushed sun-dried hot chillies with seeds for authentic spicy sambols and tempering.",
                    variants = listOf(
                        VariantDef("50g", 110.0, 180.0),
                        VariantDef("100g", 210.0, 340.0),
                        VariantDef("250g", 500.0, 800.0),
                        VariantDef("500g", 980.0, 1550.0),
                        VariantDef("1kg", 1880.0, 2980.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceyvana Premium Traditional Curry Powder",
                    category = "SPICES",
                    description = "Classic raw Ceylon aromatic spice blend of coriander, cumin, sweet cumin, and fenugreek.",
                    variants = listOf(
                        VariantDef("50g", 130.0, 210.0),
                        VariantDef("100g", 240.0, 390.0),
                        VariantDef("250g", 570.0, 920.0),
                        VariantDef("500g", 1080.0, 1750.0),
                        VariantDef("1kg", 2050.0, 3300.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceyvana Premium Roasted Curry Powder (Badapu Thunapaha)",
                    category = "SPICES",
                    description = "Masterfully slow-roasted dark Sri Lankan curry powder delivering deep smoky fragrance and rich curry gravy.",
                    variants = listOf(
                        VariantDef("50g", 150.0, 240.0),
                        VariantDef("100g", 280.0, 450.0),
                        VariantDef("250g", 650.0, 1050.0),
                        VariantDef("500g", 1250.0, 2000.0),
                        VariantDef("1kg", 2400.0, 3800.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceyvana Premium Ceylon Turmeric Powder",
                    category = "SPICES",
                    description = "100% pure organic Sri Lankan turmeric with high natural curcumin content and brilliant golden hue.",
                    variants = listOf(
                        VariantDef("50g", 140.0, 230.0),
                        VariantDef("100g", 260.0, 420.0),
                        VariantDef("250g", 620.0, 990.0),
                        VariantDef("500g", 1180.0, 1890.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceyvana Premium Meat Curry Powder",
                    category = "SPICES",
                    description = "Specialized fiery roasted spice blend enriched with black pepper, cardamom, and clove for meats and poultry.",
                    variants = listOf(
                        VariantDef("50g", 160.0, 260.0),
                        VariantDef("100g", 300.0, 480.0),
                        VariantDef("250g", 700.0, 1120.0),
                        VariantDef("500g", 1350.0, 2150.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceyvana Authentic Ceylon Cinnamon",
                    category = "SPICES",
                    description = "True Ceylon Alba & C5 grade quills delicately layered with sweet woody aroma and low coumarin.",
                    variants = listOf(
                        VariantDef("25g", 160.0, 260.0),
                        VariantDef("50g", 300.0, 490.0),
                        VariantDef("100g", 580.0, 940.0),
                        VariantDef("250g", 1390.0, 2250.0),
                        VariantDef("500g", 2650.0, 4200.0),
                        VariantDef("1kg", 5100.0, 8000.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceylon Black Pepper",
                    category = "SPICES",
                    description = "Sun-ripened Ceylon black peppercorns renowned worldwide for extraordinary piperine pungency and sharp bite.",
                    variants = listOf(
                        VariantDef("50g", 140.0, 230.0),
                        VariantDef("100g", 270.0, 440.0),
                        VariantDef("250g", 640.0, 1020.0),
                        VariantDef("500g", 1220.0, 1950.0),
                        VariantDef("1kg", 2350.0, 3750.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceyvana Premium Roasted Fenugreek Blend",
                    category = "SPICES",
                    description = "Gently roasted aromatic fenugreek (Uluhal) seeds to balance bitterness and infuse warm maple-like depth.",
                    variants = listOf(
                        VariantDef("50g", 90.0, 150.0),
                        VariantDef("100g", 170.0, 280.0),
                        VariantDef("250g", 400.0, 650.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceyvana Cinnamon Pieces",
                    category = "SPICES",
                    description = "Pure Ceylon cinnamon chips and broken quills optimal for spice infusions, biryani, and simmering curries.",
                    variants = listOf(
                        VariantDef("50g", 180.0, 300.0),
                        VariantDef("100g", 340.0, 560.0),
                        VariantDef("250g", 820.0, 1340.0),
                        VariantDef("500g", 1580.0, 2550.0)
                    )
                ),

                // B. TEA
                InitialProductDef(
                    name = "Ceyvana 'Hithe Rasaya' BOPF Tea",
                    category = "TEA",
                    description = "Single-estate Broken Orange Pekoe Fannings Ceylon black tea from misty central highlands with brisk full body.",
                    variants = listOf(
                        VariantDef("100g", 220.0, 380.0),
                        VariantDef("250g", 520.0, 890.0),
                        VariantDef("500g", 990.0, 1690.0),
                        VariantDef("1kg", 1890.0, 3200.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceylomira Traditional Black Tea (Ceylon Brew)",
                    category = "TEA",
                    description = "Heritage loose-leaf Ceylon black tea blend with copper-bright liquor, sweet floral notes, and comforting warmth.",
                    variants = listOf(
                        VariantDef("100g", 240.0, 410.0),
                        VariantDef("250g", 560.0, 950.0),
                        VariantDef("500g", 1060.0, 1800.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceyvana Ceylon Spice Chai",
                    category = "TEA",
                    description = "Robust BOP Ceylon tea hand-blended with organic Ceylon cinnamon, ginger root, cloves, and green cardamom.",
                    variants = listOf(
                        VariantDef("100g", 320.0, 550.0),
                        VariantDef("250g", 750.0, 1280.0),
                        VariantDef("500g", 1420.0, 2400.0)
                    )
                ),

                // C. GROCERY ITEMS
                InitialProductDef(
                    name = "Ceyvana Goraka (Garcinia)",
                    category = "GROCERY ITEMS",
                    description = "Naturally sun-cured smoked Gambooge (Garcinia cambogia) pods for traditional Fish Ambul Thiyal and sour meat marinades.",
                    variants = listOf(
                        VariantDef("100g", 190.0, 320.0),
                        VariantDef("250g", 450.0, 750.0),
                        VariantDef("500g", 860.0, 1420.0)
                    )
                ),
                InitialProductDef(
                    name = "Ceyvana Papadam",
                    category = "GROCERY ITEMS",
                    description = "Crispy sun-dried urad flour wafers crafted according to heirloom recipe, puffing golden in hot coconut oil.",
                    variants = listOf(
                        VariantDef("100g", 110.0, 190.0),
                        VariantDef("200g", 200.0, 350.0),
                        VariantDef("500g", 480.0, 820.0)
                    )
                )
            )

            for (item in initialCatalog) {
                val prodId = productDao.insertProduct(
                    ProductEntity(
                        name = item.name,
                        category = item.category,
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
    val variants: List<VariantDef>
)

private data class VariantDef(
    val name: String,
    val cost: Double,
    val sell: Double
)
