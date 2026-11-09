package com.bev.ledgr.data

import kotlinx.serialization.Serializable

@Serializable
enum class Category(val key: String, val label: String) {
    FOOD("food", "Food"),
    GROCERIES("groceries", "Groceries"),
    TRANSPORT("transport", "Transport"),
    BILLS("bills", "Bills"),
    EDUCATION("education", "Education"),
    SHOPPING("shopping", "Shopping"),
    PERSONAL_CARE("personal_care", "Personal care"),
    FAMILY("family", "Family"),
    HEALTHCARE("health", "Healthcare"),
    ENTERTAINMENT("entertainment", "Entertainment"),
    LOANS_GIVEN("loans_given", "Loans Given"),
    PETS("pets", "Pets"),
    HOUSING("housing", "Housing"),
    GIFTS_DONATIONS("gifts", "Gifts and Donation"),
    UTILITIES("utilities", "Utilities"),
    OTHERS("others", "Others");

    companion object {
        fun fromStored(value: String?): Category = entries.firstOrNull { it.key == value } ?: OTHERS
    }
}
