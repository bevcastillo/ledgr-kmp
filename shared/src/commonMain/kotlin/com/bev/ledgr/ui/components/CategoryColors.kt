package com.bev.ledgr.ui.components

import androidx.compose.ui.graphics.Color
import com.bev.ledgr.data.Category
import com.bev.ledgr.ui.theme.billsColor
import com.bev.ledgr.ui.theme.debtPaymentColor
import com.bev.ledgr.ui.theme.educationColor
import com.bev.ledgr.ui.theme.entertainmentColor
import com.bev.ledgr.ui.theme.familyColor
import com.bev.ledgr.ui.theme.foodColor
import com.bev.ledgr.ui.theme.giftsDonationsColor
import com.bev.ledgr.ui.theme.groceriesColor
import com.bev.ledgr.ui.theme.healthColor
import com.bev.ledgr.ui.theme.housingColor
import com.bev.ledgr.ui.theme.othersColor
import com.bev.ledgr.ui.theme.personalCareColor
import com.bev.ledgr.ui.theme.petsColor
import com.bev.ledgr.ui.theme.shoppingColor
import com.bev.ledgr.ui.theme.transportColor
import com.bev.ledgr.ui.theme.utilitiesColor

val Category.color: Color
    get() =
        when (this) {
            Category.FOOD -> foodColor
            Category.GROCERIES -> groceriesColor
            Category.TRANSPORTATION -> transportColor
            Category.SHOPPING -> shoppingColor
            Category.BILLS -> billsColor
            Category.UTILITIES -> utilitiesColor
            Category.PERSONAL_CARE -> personalCareColor
            Category.ENTERTAINMENT -> entertainmentColor
            Category.FAMILY -> familyColor
            Category.PETS -> petsColor
            Category.HEALTHCARE -> healthColor
            Category.HOUSING -> housingColor
            Category.LOANS_GIVEN -> debtPaymentColor
            Category.EDUCATION -> educationColor
            Category.GIFTS_DONATIONS -> giftsDonationsColor
            Category.OTHERS -> othersColor
        }
