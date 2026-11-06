package com.noorehidayat.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DailyReminderSettingsScreen(
    appLanguage: AppLanguage,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("noor_prefs", Context.MODE_PRIVATE) }
    var enabled by remember { mutableStateOf(prefs.getBoolean("daily_reminder_enabled", false)) }
    var reminderHour by remember { mutableIntStateOf(prefs.getInt("daily_reminder_hour", 9)) }
    var reminderMinute by remember { mutableIntStateOf(prefs.getInt("daily_reminder_minute", 0)) }
    var showTimePicker by remember { mutableStateOf(false) }

    fun persistReminder(value: Boolean) {
        enabled = value
        prefs.edit().putBoolean("daily_reminder_enabled", value).apply()
        if (value) {
            DailyReminderScheduler.schedule(context)
        } else {
            DailyReminderScheduler.cancel(context)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            persistReminder(true)
        } else {
            Toast.makeText(
                context,
                if (appLanguage == AppLanguage.URDU) "یاد دہانی کے لیے نوٹیفکیشن کی اجازت ضروری ہے۔"
                else "Notification permission is required for reminders.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val isUrdu = appLanguage == AppLanguage.URDU
    val periodLabel = if (isUrdu) {
        if (reminderHour < 12) "صبح" else "شام"
    } else {
        if (reminderHour < 12) "AM" else "PM"
    }
    val hour12 = if (reminderHour % 12 == 0) 12 else reminderHour % 12
    val timeLabel = String.format(Locale.getDefault(), "%d:%02d %s", hour12, reminderMinute, periodLabel)
    val notificationsAllowed = Build.VERSION.SDK_INT < 33 ||
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isUrdu) "روزانہ یاد دہانی" else "Daily Reminder",
                        color = Cream,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Emerald)
            )
        },
        containerColor = Emerald
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Panel),
                border = BorderStroke(1.dp, Border)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Gold)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (isUrdu) "روزانہ اسلامی یاد دہانی" else "Daily Islamic reminder",
                                color = Cream,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Text(
                                if (isUrdu) "ہر روز $timeLabel" else "Every day at $timeLabel",
                                color = Gold,
                                fontSize = 13.sp
                            )
                        }
                        Switch(
                            checked = enabled,
                            onCheckedChange = { requested ->
                                if (!requested) {
                                    persistReminder(false)
                                } else if (
                                    Build.VERSION.SDK_INT >= 33 &&
                                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    persistReminder(true)
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (isUrdu)
                            "یاد دہانی میں قرآن کی تلاوت، ذکر، دعا اور غور و فکر کی مختصر ترغیب ہوگی۔ وقت آپ کے فون کے مقامی ٹائم زون کے مطابق ہے۔"
                        else
                            "Receive a short prompt for Quran recitation, dhikr, dua or reflection. The reminder uses your phone's local time zone.",
                        color = Cream,
                        fontSize = 13.sp,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (isUrdu) "یاد دہانی کا وقت" else "Reminder time",
                            color = Muted,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { showTimePicker = true }) {
                            Text(
                                if (isUrdu) "وقت تبدیل کریں" else "Change time",
                                color = Gold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            if (!notificationsAllowed) {
                Text(
                    if (isUrdu) "نوٹیفکیشن کی اجازت بند ہے۔ یاد دہانی موصول کرنے کے لیے اجازت دیں۔"
                    else "Notifications are disabled. Allow notification permission to receive the reminder.",
                    color = Gold,
                    fontSize = 12.sp
                )
            }

            Text(
                if (isUrdu)
                    "نوٹ: Android کی بیٹری بچانے والی پابندیاں نوٹیفکیشن میں کچھ تاخیر کر سکتی ہیں۔"
                else
                    "Note: Android battery restrictions may delay a reminder slightly; the app does not request exact-alarm access.",
                color = Muted,
                fontSize = 11.sp,
                lineHeight = 17.sp
            )
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            is24Hour = false
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    if (isUrdu) "یاد دہانی کا وقت منتخب کریں" else "Choose reminder time",
                    color = Cream
                )
            },
            text = {
                TimePicker(state = timeState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        reminderHour = timeState.hour
                        reminderMinute = timeState.minute
                        prefs.edit()
                            .putInt("daily_reminder_hour", reminderHour)
                            .putInt("daily_reminder_minute", reminderMinute)
                            .apply()
                        if (enabled) DailyReminderScheduler.schedule(context)
                        showTimePicker = false
                    }
                ) {
                    Text(if (isUrdu) "محفوظ کریں" else "Save", color = Gold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(if (isUrdu) "منسوخ" else "Cancel", color = Muted)
                }
            },
            containerColor = Panel
        )
    }
}
