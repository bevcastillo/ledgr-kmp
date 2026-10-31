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
    HEALTH("health", "Health"),
    ENTERTAINMENT("entertainment", "Entertainment"),
    DEBT_PAYMENTS("debt_payments", "Debt payments"),
    PETS("pets", "Pets"),
    OTHERS("others", "Others");

    companion object {
        fun fromStored(value: String): Category {
            entries.firstOrNull { it.key == value }?.let { return it }
            entries.firstOrNull { it.label.equals(value, ignoreCase = true) }?.let { return it }

            return when (value.lowercase()) {
                "personal" -> PERSONAL_CARE
                "subscriptions" -> BILLS
                "credit card", "loans" -> DEBT_PAYMENTS
                else -> OTHERS
            }
        }
    }
}
