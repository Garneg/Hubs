package com.garnegsoft.hubs.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.garnegsoft.hubs.R
import com.garnegsoft.hubs.api.company.list.CompanySnippet
import com.garnegsoft.hubs.api.utils.placeholderColor
import com.garnegsoft.hubs.ui.screens.company.CompanyProfile


@Composable
private fun defaultCompanyCardStyle(): CompanyCardStyle {
    return CompanyCardStyle(
        backgroundColor = MaterialTheme.colors.surface,
        descriptionTextStyle = TextStyle.Default.copy(
            color = MaterialTheme.colors.onSurface.copy(
                ContentAlpha.disabled
            )
        )
    )
}

@Composable
fun CompanyCard(
    company: CompanySnippet,
    style: CompanyCardStyle = defaultCompanyCardStyle(),
    indicator: @Composable () -> Unit = {
        Text(
            company.statistics.rating.toString(),
            fontWeight = FontWeight.W400,
            color = Color(0xFFF555D7)
        )
    },
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(style.shape)
            .background(style.backgroundColor)
            .clickable { onClick() }
            .padding(style.innerPadding),
        verticalAlignment = remember {
            if (style.showDescription && company.description != null)
                Alignment.Top
            else
                Alignment.CenterVertically
        }) {
        if (company.avatarUrl != null) {
            AsyncImage(
                model = company.avatarUrl,
                contentDescription = "",
                modifier = Modifier
                    .size(style.avatarSize)
                    .clip(style.avatarShape)
                    .background(Color.White)
            )

        } else {
            Icon(
                modifier = Modifier
                    .size(style.avatarSize)
                    .border(
                        width = 2.5.dp,
                        color = placeholderColor(company.alias),
                        shape = style.avatarShape
                    )
                    .background(Color.White, shape = style.avatarShape)
                    .padding(3.dp),
                painter = painterResource(id = R.drawable.company_avatar_placeholder),
                contentDescription = "",
                tint = placeholderColor(company.alias)
            )
        }
        
        Spacer(modifier = Modifier.width(style.innerPadding))
        Column(
            modifier = Modifier.weight(1f),
            ) {
            Text(text = company.title, style = style.titleTextStyle)
            if (style.showDescription)
                company.description?.let {
                    Text(
                        text = it,
                        style = style.descriptionTextStyle,
                        maxLines = style.descriptionMaxLines,
                        overflow = TextOverflow.Ellipsis
                    )
                }
        }
        Box(modifier = Modifier.padding(start = 4.dp), contentAlignment = Alignment.Center) {
            indicator()
        }
    }
}

data class CompanyCardStyle(
    val backgroundColor: Color = Color.White,
    val shape: Shape = RoundedCornerShape(26.dp),
    val innerPadding: Dp = 16.dp,
    val avatarSize: Dp = 40.dp,
    val avatarShape: Shape = RoundedCornerShape(10.dp),
    val titleTextStyle: TextStyle = TextStyle.Default.copy(
        fontWeight = FontWeight.W700,
        fontSize = 20.sp
    ),
    val descriptionTextStyle: TextStyle = TextStyle.Default.copy(color = Color.Gray),
    val indicatorValueTextStyle: TextStyle = TextStyle.Default.copy(color = Color.DarkGray),
    val showDescription: Boolean = false,
    val descriptionMaxLines: Int = 1
)