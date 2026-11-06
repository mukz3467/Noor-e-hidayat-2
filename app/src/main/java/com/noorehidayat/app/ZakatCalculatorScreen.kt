package com.noorehidayat.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
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

private data class ZakatAsset(val id: Int, val kind: String, val amount: String = "", val rate: String = "")
private data class ZakatDeduction(val id: Int, val kind: String, val amount: String = "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ZakatCalculatorScreen(appLanguage: AppLanguage = AppLanguage.ENGLISH) {
    val context = LocalContext.current
    val urdu = appLanguage == AppLanguage.URDU
    val kinds = listOf("Cash & bank balance" to "نقد رقم اور بینک بیلنس", "Gold" to "سونا", "Silver" to "چاندی", "Business stock / inventory" to "مالِ تجارت / کاروباری اسٹاک", "Shares & investments (zakatable value)" to "قابلِ زکوٰۃ شیئرز اور سرمایہ کاری", "Money lent / receivable" to "واپس ملنے والی دی گئی رقم", "Other zakatable savings" to "دیگر قابلِ زکوٰۃ بچت")
    val currencies = listOf("PKR (Rs)" to "Rs", "USD ($)" to "$", "SAR" to "SAR", "AED" to "AED", "GBP (£)" to "£", "EUR (€)" to "€", "INR (₹)" to "₹")
    var currencyIndex by remember { mutableIntStateOf(0) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var assets by remember { mutableStateOf(emptyList<ZakatAsset>()) }
    var nextId by remember { mutableIntStateOf(2) }
    var addExpanded by remember { mutableStateOf(false) }
    val deductionKinds = listOf("Short-term debts due" to "قریب الادا قرض", "Overdue bills / essential dues" to "واجب الادا بل اور ضروری واجبات", "Other eligible liabilities" to "دیگر قابلِ کٹوتی واجبات")
    var deductionItems by remember { mutableStateOf(emptyList<ZakatDeduction>()) }
    var nextDeductionId by remember { mutableIntStateOf(1) }
    var deductionExpanded by remember { mutableStateOf(false) }
    var nisabType by remember { mutableStateOf("Silver") }
    var silverRate by remember { mutableStateOf("") }
    var goldRate by remember { mutableStateOf("") }
    val symbol = currencies[currencyIndex].second
    fun n(s: String) = s.replace(",", "").toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
    fun metal(kind: String) = kind == "Gold" || kind == "Silver"
    val assetTotal = assets.sumOf { if (metal(it.kind)) n(it.amount) * n(it.rate) else n(it.amount) }
    val totalDeductions = deductionItems.sumOf { n(it.amount) }
    val net = (assetTotal - totalDeductions).coerceAtLeast(0.0)
    val nisab = if (nisabType == "Silver") 612.36 * n(silverRate) else 87.48 * n(goldRate)
    val hasNisab = nisab > 0
    val eligible = hasNisab && net >= nisab
    val zakat = if (eligible) net * 0.025 else 0.0

    Column(Modifier.fillMaxSize().background(Emerald).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(10.dp))
        Text(I18n.t("zakat_calculator", appLanguage).uppercase(), color = Gold, fontSize = 11.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        Text(if (urdu) "آسان زکوٰۃ کیلکولیٹر" else "Zakat Calculator", color = Cream, fontSize = 23.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
        Text(if (urdu) "اثاثے درج کریں، حساب خود بخود ہوگا" else "Add your assets. The calculation updates automatically.", color = Muted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp, bottom = 8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                ZakatPanel(if (urdu) "1۔ کرنسی منتخب کریں" else "1. Select currency") {
                    ExposedDropdownMenuBox(expanded = currencyExpanded, onExpandedChange = { currencyExpanded = it }) {
                        OutlinedTextField(value = currencies[currencyIndex].first, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), colors = zakatFieldColors())
                        ExposedDropdownMenu(expanded = currencyExpanded, onDismissRequest = { currencyExpanded = false }) {
                            currencies.forEachIndexed { index, item -> DropdownMenuItem(text = { Text(item.first) }, onClick = { currencyIndex = index; currencyExpanded = false }) }
                        }
                    }
                    Text(if (urdu) "تمام رقم اسی کرنسی میں درج کریں۔ کرنسی بدلنے سے رقم تبدیل نہیں ہوگی۔" else "Enter all amounts in this currency. Changing currency does not convert existing amounts.", color = Muted, fontSize = 10.sp)
                }
            }
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Panel), border = BorderStroke(1.dp, if (eligible) EmeraldGlow else Gold)) {
                    Column(Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Text(when { !hasNisab -> if (urdu) "نصاب کا ریٹ درج کریں" else "Enter the selected Nisab metal rate"; eligible -> if (urdu) "نصاب پورا ہے — زکوٰۃ کا تخمینہ" else "Nisab reached — estimated Zakat"; else -> if (urdu) "مال منتخب نصاب سے کم ہے" else "Below selected Nisab" }, color = Gold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        Text("$symbol \${"%,.2f".format(zakat)}", color = Cream, fontSize = 30.sp, fontWeight = FontWeight.Bold)
                        Text(if (urdu) "2.5% زکوٰۃ (تخمینہ)" else "Estimated Zakat at 2.5%", color = Muted, fontSize = 11.sp)
                        HorizontalDivider(color = Border)
                        ZakatResultRow(if (urdu) "کل اثاثے" else "Total assets", "$symbol \${"%,.2f".format(assetTotal)}")
                        ZakatResultRow(if (urdu) "اختیاری کٹوتیاں" else "Optional deductions", "$symbol ${"%,.2f".format(totalDeductions)}")
                        ZakatResultRow(if (urdu) "خالص مال" else "Net wealth", "$symbol \${"%,.2f".format(net)}")
                        ZakatResultRow(if (urdu) "نصاب" else "$nisabType Nisab", if (hasNisab) "$symbol \${"%,.2f".format(nisab)}" else "—")
                        OutlinedButton(onClick = {
                            val summary = "Noor-e-Hidayat Zakat estimate\nCurrency: \${currencies[currencyIndex].first}\nAssets: \${"%.2f".format(assetTotal)}\nOptional deductions: ${"%.2f".format(totalDeductions)}\nNet wealth: \${"%.2f".format(net)}\nNisab: \${"%.2f".format(nisab)}\nEstimated Zakat (2.5%): \${"%.2f".format(zakat)}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Zakat estimate", summary))
                            Toast.makeText(context, if (urdu) "حساب کاپی ہوگیا" else "Calculation copied", Toast.LENGTH_SHORT).show()
                        }, modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold), border = BorderStroke(1.dp, Gold)) {
                            Icon(Icons.Default.ContentCopy, null); Spacer(Modifier.width(5.dp)); Text(if (urdu) "کاپی کریں" else "Copy result")
                        }
                    }
                }
            }
            item {
                ZakatPanel(if (urdu) "2۔ اپنے اثاثے شامل کریں" else "2. Add your zakatable assets") {
                    Text(if (urdu) "جس قسم کا مال موجود ہو اسے منتخب کرکے رقم درج کریں۔ سونے اور چاندی کے لیے وزن اور فی گرام قیمت درج کریں۔" else "Select the assets you own. For gold and silver, enter weight in grams and current price per gram.", color = Muted, fontSize = 11.sp)
                    assets.forEach { asset ->
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Deep), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(12.dp)) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (urdu) kinds.firstOrNull { it.first == asset.kind }?.second ?: asset.kind else asset.kind, color = Cream, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                    TextButton(onClick = { assets = assets.filterNot { it.id == asset.id } }) { Text(if (urdu) "ہٹائیں" else "Remove", color = Color(0xFFE57373), fontSize = 10.sp) }
                                }
                                if (metal(asset.kind)) {
                                    ZakatNumberField(if (urdu) "وزن (گرام)" else "Weight (grams)", asset.amount) { v -> assets = assets.map { if (it.id == asset.id) it.copy(amount = v) else it } }
                                    ZakatNumberField("$symbol / gram — \${if (urdu) "آج کی قیمت" else "Today's price per gram"}", asset.rate) { v -> assets = assets.map { if (it.id == asset.id) it.copy(rate = v) else it } }
                                    Text("$symbol \${"%,.2f".format(n(asset.amount) * n(asset.rate))}", color = Gold, fontSize = 11.sp)
                                } else {
                                    ZakatNumberField(if (urdu) "قابلِ زکوٰۃ رقم ($symbol)" else "Zakatable value ($symbol)", asset.amount) { v -> assets = assets.map { if (it.id == asset.id) it.copy(amount = v) else it } }
                                }
                            }
                        }
                    }
                    Box {
                        OutlinedButton(onClick = { addExpanded = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold), border = BorderStroke(1.dp, Gold)) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(5.dp)); Text(if (urdu) "اثاثہ منتخب کریں" else "Add / select an asset") }
                        DropdownMenu(expanded = addExpanded, onDismissRequest = { addExpanded = false }) {
                            kinds.forEach { kind -> DropdownMenuItem(text = { Text(if (urdu) kind.second else kind.first) }, onClick = { assets = assets + ZakatAsset(nextId++, kind.first); addExpanded = false }) }
                        }
                    }
                }
            }
            item {
                ZakatPanel(if (urdu) "3۔ نصاب منتخب کریں" else "3. Choose Nisab basis") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = nisabType == "Silver", onClick = { nisabType = "Silver" }, label = { Text(if (urdu) "چاندی" else "Silver") }, colors = zakatChipColors())
                        FilterChip(selected = nisabType == "Gold", onClick = { nisabType = "Gold" }, label = { Text(if (urdu) "سونا" else "Gold") }, colors = zakatChipColors())
                    }
                    if (nisabType == "Silver") {
                        ZakatNumberField(if (urdu) "چاندی فی گرام قیمت ($symbol)" else "Silver price per gram ($symbol)", silverRate) { silverRate = it }
                        Text(if (urdu) "نصاب: 612.36 گرام چاندی" else "Reference: 612.36 g silver", color = Muted, fontSize = 10.sp)
                    } else {
                        ZakatNumberField(if (urdu) "سونا فی گرام قیمت ($symbol)" else "Gold price per gram ($symbol)", goldRate) { goldRate = it }
                        Text(if (urdu) "نصاب: 87.48 گرام سونا" else "Reference: 87.48 g gold", color = Muted, fontSize = 10.sp)
                    }
                    Text(if (urdu) "مقامی مارکیٹ ریٹ خود درج کریں؛ لائیو ریٹ دستیاب ہونے کا دعویٰ نہیں۔ نصاب کے انتخاب میں فقہی اختلاف ہوسکتا ہے۔" else "Enter the local market rate yourself; no live rate is claimed. Nisab practice can differ by school of jurisprudence.", color = Muted, fontSize = 10.sp)
                }
            }
            item {
                ZakatPanel(if (urdu) "4۔ اختیاری کٹوتیاں" else "4. Optional deductions") {
                    Text(if (urdu) "اگر آپ پر کوئی قابلِ کٹوتی قرض یا واجب الادا رقم ہے تو شامل کریں؛ ورنہ اس حصے کو خالی چھوڑ دیں۔" else "Add a deduction only if it applies to you. If you have none, leave this section empty.", color = Muted, fontSize = 11.sp)
                    deductionItems.forEach { item ->
                        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Deep), border = BorderStroke(1.dp, Border), shape = RoundedCornerShape(10.dp)) {
                            Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (urdu) deductionKinds.firstOrNull { it.first == item.kind }?.second ?: item.kind else item.kind, color = Cream, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                    TextButton(onClick = { deductionItems = deductionItems.filterNot { it.id == item.id } }) { Text(if (urdu) "ہٹائیں" else "Remove", color = Color(0xFFE57373), fontSize = 10.sp) }
                                }
                                ZakatNumberField(if (urdu) "رقم ($symbol)" else "Amount ($symbol)", item.amount) { value ->
                                    deductionItems = deductionItems.map { if (it.id == item.id) it.copy(amount = value) else it }
                                }
                            }
                        }
                    }
                    Box {
                        OutlinedButton(onClick = { deductionExpanded = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold), border = BorderStroke(1.dp, Gold)) {
                            Icon(Icons.Default.Add, null); Spacer(Modifier.width(5.dp)); Text(if (urdu) "کٹوتی شامل کریں" else "Add a deduction")
                        }
                        DropdownMenu(expanded = deductionExpanded, onDismissRequest = { deductionExpanded = false }) {
                            deductionKinds.forEach { kind ->
                                DropdownMenuItem(text = { Text(if (urdu) kind.second else kind.first) }, onClick = {
                                    deductionItems = deductionItems + ZakatDeduction(nextDeductionId++, kind.first)
                                    deductionExpanded = false
                                })
                            }
                        }
                    }
                    Text(if (urdu) "کل کٹوتی: $symbol ${"%,.2f".format(totalDeductions)}" else "Total deductions: $symbol ${"%,.2f".format(totalDeductions)}", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            item {
                ZakatPanel(if (urdu) "ضروری شرعی وضاحت" else "Important guidance") {
                    Text(if (urdu) "یہ عمومی تخمینہ ہے، فتویٰ نہیں۔ زکوٰۃ عموماً نصاب کے مالک ہونے اور ایک قمری سال گزرنے کی شرط سے متعلق ہے؛ صرف سالانہ تنخواہ یا کل آمدنی پر خودکار طور پر زکوٰۃ نہیں لگتی۔ ذاتی استعمال کا گھر، گاڑی اور عام ذاتی سامان عموماً شامل نہیں۔ زرعی پیداوار، مویشی، پنشن، پیچیدہ سرمایہ کاری اور قرض کی کٹوتی کے احکام الگ ہوسکتے ہیں؛ مستند عالم سے تصدیق کریں۔" else "This is a general estimate, not a fatwa. Zakat generally requires qualifying ownership at Nisab and completion of a lunar year; it is not simply 2.5% of annual salary or gross income. A personal residence, car and ordinary personal-use items are generally excluded. Agricultural produce, livestock, pensions, complex investments and debt deductions follow separate rulings; confirm individual cases with a qualified scholar.", color = Muted, fontSize = 11.sp, lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun ZakatPanel(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Panel), border = BorderStroke(1.dp, Border)) {
        Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { Text(title, color = Gold, fontWeight = FontWeight.Bold, fontSize = 13.sp); content() }
    }
}
@Composable
private fun ZakatResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Muted, fontSize = 11.sp, modifier = Modifier.weight(1f)); Text(value, color = Cream, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
}
@Composable
private fun ZakatNumberField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = { input -> onChange(input.filter { it.isDigit() || it == '.' || it == ',' }) }, label = { Text(label, fontSize = 10.sp) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = zakatFieldColors())
}
@Composable
private fun zakatFieldColors() = OutlinedTextFieldDefaults.colors(focusedBorderColor = Gold, unfocusedBorderColor = Border, focusedTextColor = Cream, unfocusedTextColor = Cream, focusedLabelColor = Gold, unfocusedLabelColor = Muted, cursorColor = Gold, focusedContainerColor = Deep, unfocusedContainerColor = Deep)
@Composable
private fun zakatChipColors() = FilterChipDefaults.filterChipColors(selectedContainerColor = Gold, selectedLabelColor = Emerald, containerColor = Deep, labelColor = Cream)
