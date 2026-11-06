package com.noorehidayat.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ZakatCalculatorScreen(appLanguage: AppLanguage = AppLanguage.ENGLISH) {
    val ctx = LocalContext.current

    val currencies = remember {
        listOf(
            CurrencyRate("PKR", "Rs", 0.0, 0.0),
            CurrencyRate("INR", "₹", 0.0, 0.0),
            CurrencyRate("USD", "$", 0.0, 0.0),
            CurrencyRate("SAR", "SAR", 0.0, 0.0),
            CurrencyRate("AED", "AED", 0.0, 0.0),
            CurrencyRate("EUR", "€", 0.0, 0.0),
            CurrencyRate("GBP", "£", 0.0, 0.0)
        )
    }

    var selectedCurrency by remember { mutableStateOf(currencies.first()) }
    var goldRateInput by remember { mutableStateOf("") }
    var silverRateInput by remember { mutableStateOf("") }
    val goldRate = goldRateInput.toDoubleOrNull()?.takeIf { it > 0 } ?: 0.0
    val silverRate = silverRateInput.toDoubleOrNull()?.takeIf { it > 0 } ?: 0.0

    // Inputs
    var cashVal by remember { mutableStateOf("") }
    var goldGrams by remember { mutableStateOf("") }
    var silverGrams by remember { mutableStateOf("") }
    var tradeStockVal by remember { mutableStateOf("") }
    var sharesInvestments by remember { mutableStateOf("") }
    var loanedOutVal by remember { mutableStateOf("") }

    // Deductions
    var debtsVal by remember { mutableStateOf("") }
    var unpaidBills by remember { mutableStateOf("") }

    val cash = cashVal.toDoubleOrNull() ?: 0.0
    val goldWeight = goldGrams.toDoubleOrNull() ?: 0.0
    val silverWeight = silverGrams.toDoubleOrNull() ?: 0.0
    val tradeStock = tradeStockVal.toDoubleOrNull() ?: 0.0
    val shares = sharesInvestments.toDoubleOrNull() ?: 0.0
    val loaned = loanedOutVal.toDoubleOrNull() ?: 0.0

    val debts = debtsVal.toDoubleOrNull() ?: 0.0
    val bills = unpaidBills.toDoubleOrNull() ?: 0.0

    // Calculations
    val goldTotalValue = goldWeight * goldRate
    val silverTotalValue = silverWeight * silverRate

    val totalAssets = cash + goldTotalValue + silverTotalValue + tradeStock + shares + loaned
    val totalLiabilities = debts + bills
    val netZakatableWealth = (totalAssets - totalLiabilities).coerceAtLeast(0.0)

    // Silver Nisab (612.36g) is the standard safer threshold for mixed wealth
    val silverNisabValue = 612.36 * silverRate
    val goldNisabValue = 87.48 * goldRate

    val isEligible = silverRate > 0.0 && netZakatableWealth >= silverNisabValue
    val zakatPayable = if (isEligible) netZakatableWealth * 0.025 else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Emerald)
            .padding(horizontal = 18.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text(
            I18n.t("zakat_calculator", appLanguage).uppercase(),
            color = Gold,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            if (appLanguage == AppLanguage.URDU) "جامع شرعی زکوٰۃ کیلکولیٹر" else "Smart Zakat & Nisab Calculator",
            color = Cream,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
        )

        // Currency Selector
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(currencies) { curr ->
                FilterChip(
                    selected = selectedCurrency.code == curr.code,
                    onClick = {
                        selectedCurrency = curr
                        goldRateInput = ""
                        silverRateInput = ""
                    },
                    label = { Text("${curr.code} (${curr.symbol})", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Gold,
                        selectedLabelColor = Emerald,
                        containerColor = Panel,
                        labelColor = Cream
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // Summary Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = if (isEligible) EmeraldGlow.copy(alpha = 0.2f) else Panel),
            border = BorderStroke(1.5.dp, if (isEligible) EmeraldGlow else Gold)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isEligible) "ZAKAT IS PAYABLE (فرض ہے)" else if (silverRate <= 0.0) "ENTER CURRENT SILVER RATE FIRST" else "BELOW NISAB THRESHOLD",
                        color = if (isEligible) EmeraldGlow else Gold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        val summaryText = """
                            --- Noor-e-Hidayat Zakat Calculation ---
                            Net Zakatable Wealth: ${selectedCurrency.symbol} ${"%,.0f".format(netZakatableWealth)}
                            Nisab Threshold (Silver): ${selectedCurrency.symbol} ${"%,.0f".format(silverNisabValue)}
                            Total Zakat Due (2.5%): ${selectedCurrency.symbol} ${"%,.0f".format(zakatPayable)}
                        """.trimIndent()
                        val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        cm.setPrimaryClip(ClipData.newPlainText("Zakat Summary", summaryText))
                        Toast.makeText(ctx, "Zakat summary copied", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.ContentCopy, null, tint = Gold, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    "${selectedCurrency.symbol} ${"%,.0f".format(zakatPayable)}",
                    color = Cream,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Exact 2.5% of net zakatable assets (${selectedCurrency.symbol} ${"%,.0f".format(netZakatableWealth)})",
                    color = Muted,
                    fontSize = 11.sp
                )

                Spacer(Modifier.height(8.dp))
                Text(
                    "Silver Nisab: ${selectedCurrency.symbol} ${"%,.0f".format(silverNisabValue)} (612.36g) · Gold Nisab: ${selectedCurrency.symbol} ${"%,.0f".format(goldNisabValue)} (87.48g)",
                    color = Gold,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Input Fields List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Text(
                    if (appLanguage == AppLanguage.URDU) "حساب سے پہلے آج کے مقامی مارکیٹ ریٹس درج کریں"
                    else "Enter current local market prices per gram. Rates are not fetched live.",
                    color = Gold,
                    fontSize = 12.sp
                )
            }
            item {
                ZakatInputField(
                    label = "Current gold price per gram (${selectedCurrency.code})",
                    urduLabel = "فی گرام سونے کی موجودہ قیمت",
                    value = goldRateInput,
                    onValueChange = { goldRateInput = it },
                    prefix = selectedCurrency.symbol
                )
            }
            item {
                ZakatInputField(
                    label = "Current silver price per gram (${selectedCurrency.code})",
                    urduLabel = "فی گرام چاندی کی موجودہ قیمت",
                    value = silverRateInput,
                    onValueChange = { silverRateInput = it },
                    prefix = selectedCurrency.symbol
                )
            }
            item {
                Text("ZAKATABLE ASSETS (اثاثہ جات)", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            item {
                ZakatInputField(
                    label = "Cash in Hand & Bank Accounts",
                    urduLabel = "نقد رقم اور بینک بیلنس",
                    value = cashVal,
                    onValueChange = { cashVal = it },
                    prefix = selectedCurrency.symbol
                )
            }

            item {
                ZakatInputField(
                    label = "Gold (Grams) · Rate: ${selectedCurrency.symbol}${"%,.2f".format(goldRate)}/g",
                    urduLabel = "سونا (گرام میں)",
                    value = goldGrams,
                    onValueChange = { goldGrams = it },
                    prefix = "g"
                )
            }

            item {
                ZakatInputField(
                    label = "Silver (Grams) · Rate: ${selectedCurrency.symbol}${"%,.2f".format(silverRate)}/g",
                    urduLabel = "چاندی (گرام میں)",
                    value = silverGrams,
                    onValueChange = { silverGrams = it },
                    prefix = "g"
                )
            }

            item {
                ZakatInputField(
                    label = "Business Inventory & Trade Stock",
                    urduLabel = "مالِ تجارت اور اسٹاک",
                    value = tradeStockVal,
                    onValueChange = { tradeStockVal = it },
                    prefix = selectedCurrency.symbol
                )
            }

            item {
                ZakatInputField(
                    label = "Shares, Mutual Funds & Investments",
                    urduLabel = "شیئرز اور سرمایہ کاری",
                    value = sharesInvestments,
                    onValueChange = { sharesInvestments = it },
                    prefix = selectedCurrency.symbol
                )
            }

            item {
                Text("DEDUCTIONS & LIABILITIES (واجب الادا قرضے)", color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
            }

            item {
                ZakatInputField(
                    label = "Immediate Personal Debts Payable",
                    urduLabel = "فوری واجب الادا قرضے",
                    value = debtsVal,
                    onValueChange = { debtsVal = it },
                    prefix = selectedCurrency.symbol
                )
            }

            item {
                ZakatInputField(
                    label = "Overdue Bills, Rent & Wages",
                    urduLabel = "واجب الادا بل، کرایہ اور تنخواہیں",
                    value = unpaidBills,
                    onValueChange = { unpaidBills = it },
                    prefix = selectedCurrency.symbol
                )
            }
        }
    }
}

@Composable
private fun ZakatInputField(
    label: String,
    urduLabel: String,
    value: String,
    onValueChange: (String) -> Unit,
    prefix: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Panel),
        border = BorderStroke(1.dp, Border)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = Cream, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(urduLabel, color = Muted, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Text(prefix, color = Gold, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                placeholder = { Text("0", color = Muted) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = Border,
                    focusedTextColor = Cream,
                    unfocusedTextColor = Cream,
                    focusedContainerColor = Deep,
                    unfocusedContainerColor = Deep
                )
            )
        }
    }
}
