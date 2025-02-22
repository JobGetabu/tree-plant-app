package com.mobiletreeplantingapp.data.model

data class SoilData(
    val type: String,
    val properties: Properties
) {
    data class Properties(
        val layers: List<Layer>
    )

    data class Layer(
        val name: String,
        val unit_measure: UnitMeasure,
        val depths: List<Depth>
    )

    data class UnitMeasure(
        val d_factor: Int,
        val mapped_units: String,
        val target_units: String,
        val uncertainty_unit: String
    )

    data class Depth(
        val range: Range,
        val label: String,
        val values: Values
    )

    data class Range(
        val top_depth: Int,
        val bottom_depth: Int,
        val unit_depth: String
    )

    data class Values(
        val mean: Double,
        val uncertainty: Double,
        val Q0_05: Double,
        val Q0_5: Double,
        val Q0_95: Double
    )
}

data class SoilProperties(
    val clayContent: Double,
    val sandContent: Double,
    val organicMatter: Double,
    val ph: Double
) 