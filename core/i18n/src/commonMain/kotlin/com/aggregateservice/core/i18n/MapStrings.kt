package com.aggregateservice.core.i18n

/**
 * Map string resources.
 */
public object MapStrings {
    public val EN: Map<String, String> =
        mapOf(
            "map_nearby_providers" to "Nearby providers",
            "map_your_location" to "Your location",
            "map_search_this_area" to "Search this area",
            "map_km_away" to "{km} km away",
            "map_show_list" to "Show list",
            "map_show_map" to "Show map",
        )

    public val RU: Map<String, String> =
        mapOf(
            "map_nearby_providers" to "Мастера рядом",
            "map_your_location" to "Ваше местоположение",
            "map_search_this_area" to "Искать здесь",
            "map_km_away" to "{km} км",
            "map_show_list" to "Показать список",
            "map_show_map" to "Показать карту",
        )

    public val HE: Map<String, String> =
        mapOf(
            "map_nearby_providers" to "בעלי מקצוע בקרבת מקום",
            "map_your_location" to "המיקום שלך",
            "map_search_this_area" to "חפש באזור זו",
            "map_km_away" to "{km} ק\"מ ממך",
            "map_show_list" to "הצג רשימה",
            "map_show_map" to "הצג מפה",
        )
}
