package com.bev.ledgr.ui.components

import androidx.compose.ui.graphics.Color
import com.bev.ledgr.data.Category
import com.bev.ledgr.ui.theme.billsColor
import com.bev.ledgr.ui.theme.debtPaymentColor
import com.bev.ledgr.ui.theme.educationColor
import com.bev.ledgr.ui.theme.entertainmentColor
import com.bev.ledgr.ui.theme.familyColor
import com.bev.ledgr.ui.theme.foodColor
import com.bev.ledgr.ui.theme.groceriesColor
import com.bev.ledgr.ui.theme.healthColor
import com.bev.ledgr.ui.theme.othersColor
import com.bev.ledgr.ui.theme.personalCareColor
import com.bev.ledgr.ui.theme.petsColor
import com.bev.ledgr.ui.theme.shoppingColor
import com.bev.ledgr.ui.theme.transportColor

val Category.color: Color
    get() =
        when (this) {
            Category.FOOD -> foodColor
            Category.GROCERIES -> groceriesColor
            Category.TRANSPORT -> transportColor
            Category.BILLS -> billsColor
            Category.EDUCATION -> educationColor
            Category.SHOPPING -> shoppingColor
            Category.PERSONAL_CARE -> personalCareColor
            Category.FAMILY -> familyColor
            Category.HEALTH -> healthColor
            Category.ENTERTAINMENT -> entertainmentColor
            Category.DEBT_PAYMENTS -> debtPaymentColor
            Category.PETS -> petsColor
            Category.OTHERS -> othersColor
        }
