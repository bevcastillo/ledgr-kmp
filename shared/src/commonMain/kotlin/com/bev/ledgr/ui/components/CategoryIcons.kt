package com.bev.ledgr.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DirectionsBusFilled
import androidx.compose.material.icons.rounded.FamilyRestroom
import androidx.compose.material.icons.rounded.HealthAndSafety
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.MiscellaneousServices
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.RealEstateAgent
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector
import com.bev.ledgr.data.Category

val Category.icon: ImageVector
    get() =
        when (this) {
            Category.FOOD -> Icons.Rounded.Restaurant
            Category.GROCERIES -> Icons.Rounded.LocalGroceryStore
            Category.TRANSPORT -> Icons.Rounded.DirectionsBusFilled
            Category.BILLS -> Icons.Rounded.Receipt
            Category.EDUCATION -> Icons.Rounded.School
            Category.SHOPPING -> Icons.Rounded.ShoppingBasket
            Category.PERSONAL_CARE -> Icons.Rounded.AutoAwesome
            Category.FAMILY -> Icons.Rounded.FamilyRestroom
            Category.HEALTH -> Icons.Rounded.HealthAndSafety
            Category.ENTERTAINMENT -> Icons.Rounded.SportsEsports
            Category.DEBT_PAYMENTS -> Icons.Rounded.RealEstateAgent
            Category.PETS -> Icons.Rounded.Pets
            Category.OTHERS -> Icons.Rounded.MiscellaneousServices
        }
