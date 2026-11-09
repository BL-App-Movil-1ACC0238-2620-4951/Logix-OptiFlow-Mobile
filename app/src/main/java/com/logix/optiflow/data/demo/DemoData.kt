package com.logix.optiflow.data.demo

import androidx.annotation.DrawableRes
import com.logix.optiflow.R

/**
 * Datos de ejemplo para las pantallas cuyo dominio todavía no expone el backend
 * (catálogo de monturas, inventario, pacientes del personal, reportes, alertas).
 * Los valores siguen los del prototipo para que la demo coincida con el diseño.
 */
data class Frame(
    val id: String,
    val name: String,
    val category: String,
    val price: Int,
    val fromPrice: String,
    @param:DrawableRes val image: Int,
    val description: String,
    val stock: Int,
    val features: List<String>,
    val colors: List<String>,
    val size: Int,
)

data class StaffPatient(
    val id: String,
    val initials: String,
    val name: String,
    val fullName: String,
    val rut: String,
    val note: String,
    val phone: String,
    val lastVisit: String,
    val ficha: String,
    val age: Int,
    val email: String = "",
)

enum class AgendaStatus { WAITING, CONFIRMED, PENDING, PICKUP, DONE }

data class AgendaItem(
    val time: String,
    val duration: String,
    val patientId: String,
    val name: String,
    val service: String,
    val status: AgendaStatus,
)

data class Product(
    val id: String,
    val name: String,
    val sku: String,
    val code: String,
    val detail: String,
    val price: String,
    val tag: String,
    val tagIcon: Int,
    val available: Int,
    @param:DrawableRes val image: Int,
    val category: String,
    val size: String,
    val location: String,
    val supplier: String,
)

enum class ProductionStage(val label: String) {
    PENDING("Por iniciar"),
    IN_WORKSHOP("En producción"),
    QUALITY_CONTROL("Control calidad"),
    READY_FOR_DELIVERY("Listos"),
    DELIVERED("Entregados"),
}

data class ProductionOrder(
    val code: String,
    val patient: String,
    val detail: String,
    val stage: ProductionStage,
    val due: String,
    val needsAttention: Boolean = false,
)

data class StaffAlert(
    val kind: String,
    val title: String,
    val message: String,
    val action: String,
    val icon: Int,
)

object DemoData {

    val frames =
        listOf(
            Frame(
                "verona", "Verona Classic", "OFTÁLMICO", 120, "Desde $92.90",
                R.drawable.img_frame_verona, "Montura ligera . Atigrado", 4,
                listOf("Acetato premium", "Ligera"), listOf("Azul marino", "Cristal", "Carey"), 52,
            ),
            Frame(
                "havana", "Havana Sun", "SOLAR", 145, "Desde $145.00",
                R.drawable.img_frame_havana, "Montura solar . Dorado", 6,
                listOf("Protección UV400", "Metal"), listOf("Dorado", "Negro"), 54,
            ),
            Frame(
                "sienna", "Sienna Gold", "SOLAR", 180, "Desde $180.00",
                R.drawable.img_frame_sienna, "Montura redonda . Negro y dorado", 3,
                listOf("Polarizado", "Metal"), listOf("Negro", "Dorado"), 50,
            ),
            Frame(
                "roma", "Roma Black", "OFTÁLMICO", 110, "Desde $110.00",
                R.drawable.img_frame_roma, "Montura cuadrada . Negro", 8,
                listOf("Acetato premium", "Resistente"), listOf("Negro", "Carey"), 52,
            ),
            Frame(
                "amber", "Amber Vintage", "OFTÁLMICO", 135, "Desde $135.00",
                R.drawable.img_frame_amber, "Montura vintage . Ámbar", 5,
                listOf("Acetato premium", "Ligera"), listOf("Ámbar", "Carey"), 50,
            ),
            Frame(
                "pacific", "Pacific Blue", "SOLAR", 160, "Desde $160.00",
                R.drawable.img_frame_pacific, "Montura deportiva . Espejado azul", 7,
                listOf("Espejado", "Polarizado"), listOf("Azul", "Plata"), 56,
            ),
        )

    /** Orden visual de la grilla de "Explorar Monturas". */
    val catalogGrid = listOf("sienna", "roma", "amber", "pacific", "roma", "pacific", "sienna")

    fun frame(id: String): Frame = frames.firstOrNull { it.id == id } ?: frames.first()

    val staffPatients =
        listOf(
            StaffPatient("vs", "VS", "Valentina Silva", "Valentina Silva Morales", "18.234.567-8", "Control hoy · 10:30", "+56 9 8412 9043", "24 jun 2024", "OPT-2024-849", 28),
            StaffPatient("mg", "MG", "María González", "María González Pérez", "12.345.678-9", "Último control · 12 jun", "+56 9 7781 2201", "12 jun 2024", "OPT-2024-612", 41),
            StaffPatient("tf", "TF", "Tomás Fuentes", "Tomás Fuentes Ríos", "20.481.292-1", "Nuevo paciente", "+56 9 6620 4419", "—", "OPT-2024-901", 23),
            StaffPatient("cr", "CR", "Camila Rojas", "Camila Rojas Díaz", "16.902.456-4", "Último control · 03 may", "+56 9 5310 7782", "03 may 2024", "OPT-2024-433", 35),
            StaffPatient("cm", "CM", "Carlos Mendoza", "Carlos Mendoza Vera", "15.118.903-2", "Control post-graduación", "+56 9 4471 0098", "18 jun 2024", "OPT-2024-702", 52),
            StaffPatient("mr", "MR", "Mariana Rojas", "Mariana Rojas Soto", "19.554.120-7", "Por confirmar", "+56 9 9012 3345", "—", "OPT-2024-911", 30),
        )

    fun staffPatient(id: String): StaffPatient = staffPatients.firstOrNull { it.id == id } ?: staffPatients.first()

    val agenda =
        listOf(
            AgendaItem("10:30", "45 min", "vs", "Valentina Silva", "Evaluación visual completa", AgendaStatus.WAITING),
            AgendaItem("11:15", "45 min", "cm", "Carlos Mendoza", "Control post-graduación", AgendaStatus.CONFIRMED),
            AgendaItem("12:00", "45 min", "mr", "Mariana Rojas", "Primera consulta", AgendaStatus.PENDING),
            AgendaItem("13:00", "30 min", "mg", "María González", "Retiro de lentes", AgendaStatus.PICKUP),
            AgendaItem("15:00", "45 min", "cr", "Camila Rojas", "Control anual", AgendaStatus.CONFIRMED),
            AgendaItem("16:00", "30 min", "tf", "Tomás Fuentes", "Retiro de lentes", AgendaStatus.PICKUP),
            AgendaItem("16:30", "45 min", "mg", "María González", "Adaptación de lentes", AgendaStatus.CONFIRMED),
            AgendaItem("17:15", "45 min", "cm", "Carlos Mendoza", "Cambio de cristales", AgendaStatus.CONFIRMED),
        )

    val products =
        listOf(
            Product("rb5154", "Ray-Ban Clubmaster", "RB5154", "RB-5154-51-21", "Carey / Dorado · Acetato", "$189.000", "Alta demanda", R.drawable.lucide_ic_check, 4, R.drawable.img_inv_clubmaster, "Armazones", "51-21-145", "Vitrina Centro", "EssilorLuxottica"),
            Product("holbrook", "Holbrook RX", "OX8156", "OX-8156-54-18", "Negro mate · O-Matter", "$165.000", "Deportivo", R.drawable.lucide_ic_check, 2, R.drawable.img_inv_holbrook, "Armazones", "54-18-137", "Vitrina Centro", "Oakley"),
            Product("titan", "Titan Minimal", "TM1520", "TM-1520-52-17", "Sin montura · Titanio", "$290.000", "Titanio", R.drawable.lucide_ic_check, 6, R.drawable.img_inv_titan, "Armazones", "52-17-140", "Vitrina Norte", "Silhouette"),
            Product("vo5282", "VO5282B Cat-Eye", "VO5282B", "VO-5282-53-17", "Borgoña · Acetato fino", "$138.000", "Última unidad", R.drawable.lucide_ic_bell, 1, R.drawable.img_inv_vogue, "Armazones", "53-17-140", "Cajón Taller", "Vogue Eyewear"),
        )

    fun product(id: String): Product = products.firstOrNull { it.id == id } ?: products.first()

    val productionOrders =
        listOf(
            ProductionOrder("OT-2048", "Valentina Silva", "Nova N-24 · Monofocal 1.60", ProductionStage.IN_WORKSHOP, "Entrega 28 jun"),
            ProductionOrder("OT-2042", "María González", "Aira 302 · Monofocal 1.60", ProductionStage.IN_WORKSHOP, "Entrega hoy"),
            ProductionOrder("OT-2039", "Tomás Fuentes", "Milo M-8 · Monofocal 1.60", ProductionStage.IN_WORKSHOP, "Atrasada 1 día", needsAttention = true),
            ProductionOrder("OT-2051", "Camila Rojas", "Verona · Bifocal 1.56", ProductionStage.PENDING, "Entrega 2 jul"),
            ProductionOrder("OT-2052", "Carlos Mendoza", "Roma · Progresivo 1.67", ProductionStage.PENDING, "Entrega 3 jul"),
            ProductionOrder("OT-2053", "Mariana Rojas", "Amber · Monofocal 1.50", ProductionStage.PENDING, "Entrega 3 jul"),
            ProductionOrder("OT-2031", "Ana Torres", "Holbrook · Monofocal 1.60", ProductionStage.QUALITY_CONTROL, "Entrega mañana"),
            ProductionOrder("OT-2029", "Luis Paredes", "Titan · Progresivo 1.67", ProductionStage.QUALITY_CONTROL, "Entrega mañana"),
            ProductionOrder("OT-2020", "Pedro Ruiz", "Clubmaster · Monofocal", ProductionStage.READY_FOR_DELIVERY, "Listo para retiro"),
        )

    val alerts =
        listOf(
            StaffAlert("Inventario", "Stock bajo", "Milo M-8 tiene solo 3 unidades.", "Revisar stock", R.drawable.lucide_ic_bell),
            StaffAlert("Producción", "Orden con retraso", "OT-2039 lleva 1 día de retraso.", "Ver orden", R.drawable.lucide_ic_clock),
            StaffAlert("Producción", "Pedidos listos", "3 pedidos esperan ser entregados.", "Ver pedidos", R.drawable.lucide_ic_box),
        )

    val reportBars = listOf(0.45f, 0.65f, 0.53f, 0.8f, 0.72f, 1f, 0.87f)
}
