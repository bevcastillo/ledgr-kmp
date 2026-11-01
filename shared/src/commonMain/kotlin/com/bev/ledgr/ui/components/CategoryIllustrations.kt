package com.bev.ledgr.ui.components

import com.bev.ledgr.data.Category
import ledgr.shared.generated.resources.Res
import ledgr.shared.generated.resources.category_bills_badge
import ledgr.shared.generated.resources.category_debt_payments_badge
import ledgr.shared.generated.resources.category_education_badge
import ledgr.shared.generated.resources.category_entertainment_badge
import ledgr.shared.generated.resources.category_family_badge
import ledgr.shared.generated.resources.category_food_badge
import ledgr.shared.generated.resources.category_groceries_badge
import ledgr.shared.generated.resources.category_health_badge
import ledgr.shared.generated.resources.category_others_badge
import ledgr.shared.generated.resources.category_personal_care_badge
import ledgr.shared.generated.resources.category_pets_badge
import ledgr.shared.generated.resources.category_shopping_badge
import ledgr.shared.generated.resources.category_transport_badge
import org.jetbrains.compose.resources.DrawableResource

val Category.illustrations: DrawableResource
    get() = when (this) {
        Category.FOOD -> Res.drawable.category_food_badge
        Category.GROCERIES -> Res.drawable.category_groceries_badge
        Category.TRANSPORT -> Res.drawable.category_transport_badge
        Category.BILLS -> Res.drawable.category_bills_badge
        Category.EDUCATION -> Res.drawable.category_education_badge
        Category.SHOPPING -> Res.drawable.category_shopping_badge
        Category.PERSONAL_CARE -> Res.drawable.category_personal_care_badge
        Category.FAMILY -> Res.drawable.category_family_badge
        Category.HEALTH -> Res.drawable.category_health_badge
        Category.ENTERTAINMENT -> Res.drawable.category_entertainment_badge
        Category.DEBT_PAYMENTS -> Res.drawable.category_debt_payments_badge
        Category.PETS -> Res.drawable.category_pets_badge
        Category.OTHERS -> Res.drawable.category_others_badge
    }
