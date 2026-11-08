package com.noorehidayat.app

object IslamicContentData {

    val liveStreams = listOf(
        LiveStream(
            id = "makkah_official",
            title = "Makkah · Saudi Quran TV (Official)",
            titleUrdu = "مکہ مکرمہ · سعودی قرآن چینل",
            location = "Masjid al-Haram, Makkah",
            youtubeId = "channel:UCos52azQNBgW63_9uDJoPDA",
            description = "Official Saudi Quran TV broadcast from Masjid al-Haram. Availability is controlled by the broadcaster."
        ),
        LiveStream(
            id = "madinah_official",
            title = "Madinah · Saudi Sunnah TV (Official)",
            titleUrdu = "مدینہ منورہ · سعودی سنہ چینل",
            location = "Al-Masjid an-Nabawi, Madinah",
            youtubeId = "channel:UCROKYPep-UuODNwyipe6JMw",
            description = "Official Saudi Sunnah TV broadcast from Al-Masjid an-Nabawi. Availability is controlled by the broadcaster."
        )
    )

    val lectureCategories = listOf(
        "All Topics",
        "Worship & Namaz",
        "Seerat un Nabi ﷺ",
        "Prophets Stories",
        "Quran & Tafseer",
        "Tazkiyah & Ethics",
        "History & Kaaba",
        "Family & Parables"
    )

    val scholars = listOf(
        "All Scholars",
        "Dr. Israr Ahmed",
        "Mufti Tariq Masood",
        "Maulana Tariq Jameel",
        "Mufti Menk"
    )

    val curatedLectures = listOf(
        LectureVideo(
            id = "lec_1",
            title = "Surah Al-Fatiha Deep Tafseer",
            speaker = "Dr. Israr Ahmed",
            category = "Quran & Tafseer",
            youtubeId = "2WAFIAfL7nM",
            duration = "45:20"
        ),
        LectureVideo(
            id = "lec_2",
            title = "Surah Al-Baqarah Key Lessons & Guidance",
            speaker = "Dr. Israr Ahmed",
            category = "Quran & Tafseer",
            youtubeId = "h130FLc5f7w",
            duration = "52:10"
        ),
        LectureVideo(
            id = "lec_3",
            title = "Namaz Ko Halke Me Mat Lo - The Weight of Salah",
            speaker = "Mufti Tariq Masood",
            category = "Worship & Namaz",
            youtubeId = "c8Dz_wgbZ1E",
            duration = "34:15"
        ),
        LectureVideo(
            id = "lec_4",
            title = "Namaz Ki Ahmiyat & Attaining Khushu",
            speaker = "Mufti Tariq Masood",
            category = "Worship & Namaz",
            youtubeId = "opcfB1_4TsA",
            duration = "29:40"
        ),
        LectureVideo(
            id = "lec_5",
            title = "Fikar Karna Chhor Do - Trusting in Allah's Plan",
            speaker = "Maulana Tariq Jameel",
            category = "Tazkiyah & Ethics",
            youtubeId = "i_qx9MBt-b4",
            duration = "41:30"
        ),
        LectureVideo(
            id = "lec_6",
            title = "The Incident of Land and Ocean - Lessons of Faith",
            speaker = "Maulana Tariq Jameel",
            category = "Seerat un Nabi ﷺ",
            youtubeId = "-T-CKxKD0i8",
            duration = "38:50"
        ),
        LectureVideo(
            id = "lec_7",
            title = "Lessons from the Prophetic Household",
            speaker = "Mufti Menk",
            category = "Family & Parables",
            youtubeId = "Ivb_Nnop4xs",
            duration = "36:20"
        ),
        LectureVideo(
            id = "lec_8",
            title = "Solve Your Problems by Going Back to Basics",
            speaker = "Mufti Menk",
            category = "Tazkiyah & Ethics",
            youtubeId = "Zqg_ft2CSk4",
            duration = "33:45"
        ),
        LectureVideo(
            id = "lec_9",
            title = "Surah Ar-Rahman Beautiful Heart-Touching Recitation",
            speaker = "Mishary Rashid Alafasy",
            category = "Quran & Tafseer",
            youtubeId = "H4N5eFbLl9A",
            duration = "16:45"
        )
    )

    val dailyDuas = listOf(
        IslamicDua(
            title = "Waking Up in the Morning",
            titleUrdu = "صبح بیدار ہونے کی دعا",
            arabic = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            transliteration = "Alhamdu lillahil-lathee ahyana ba'da ma amatana wa-ilayhin-nushoor",
            english = "All praise is for Allah who gave us life after having taken it from us, and unto Him is the resurrection.",
            urdu = "تمام تعریفیں اللہ کے لیے ہیں جس نے ہمیں مارنے کے بعد زندہ کیا اور اسی کی طرف لوٹ کر جانا ہے۔",
            benefit = "Expresses gratitude for the blessing of a new day and life."
        ),
        IslamicDua(
            title = "Before Sleeping",
            titleUrdu = "سوتے وقت کی دعا",
            arabic = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            transliteration = "Bismika Allahumma amootu wa-ahya",
            english = "In Your name, O Allah, I die and I live.",
            urdu = "اے اللہ! میں تیرے ہی نام کے ساتھ مرتا (سوتا) ہوں اور جیتا (جاگتا) ہوں۔",
            benefit = "Places one's soul under Allah's supreme protection through the night."
        ),
        IslamicDua(
            title = "Entering the Mosque",
            titleUrdu = "مسجد میں داخل ہونے کی دعا",
            arabic = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            transliteration = "Allahummaftah lee abwaba rahmatik",
            english = "O Allah, open for me the doors of Your mercy.",
            urdu = "اے اللہ! میرے لیے اپنی رحمت کے دروازے کھول دے۔",
            benefit = "Invokes Allah's immense mercy upon entering His sanctuary."
        ),
        IslamicDua(
            title = "Leaving the Mosque",
            titleUrdu = "مسجد سے نکلنے کی دعا",
            arabic = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            transliteration = "Allahumma innee as-aluka min fadlik",
            english = "O Allah, I ask You from Your bounty.",
            urdu = "اے اللہ! میں تجھ سے تیرے فضل کا سوال کرتا ہوں۔",
            benefit = "Seeks lawful sustenance and divine bounty in daily affairs."
        ),
        IslamicDua(
            title = "Seeking Divine Forgiveness (Sayyidul Istighfar)",
            titleUrdu = "سید الاستغفار",
            arabic = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ",
            transliteration = "Allahumma Anta Rabbi la ilaha illa Ant, khalaqtani wa ana 'abduk...",
            english = "O Allah, You are my Lord; there is no deity worthy of worship except You. You created me and I am Your servant.",
            urdu = "اے اللہ تو میرا رب ہے، تیرے سوا کوئی معبود نہیں، تو نے مجھے پیدا کیا اور میں تیرا بندہ ہوں۔",
            benefit = "Whoever recites this with sincere conviction and passes away that day enters Paradise."
        ),
        IslamicDua(
            title = "Protection Against Evil & Harm",
            titleUrdu = "ہر قسم کے شر اور نقصان سے حفاظت کی دعا",
            arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahil-lathee la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i wa huwas-Samee'ul-'Aleem",
            english = "In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, the All-Knowing.",
            urdu = "اللہ کے نام کے ساتھ جس کے نام کی برکت سے زمین اور آسمان کی کوئی چیز نقصان نہیں پہنچا سکتی اور وہ خوب سننے والا جاننے والا ہے۔",
            benefit = "Recited 3 times in morning and evening to protect from all unexpected harm."
        )
    )

    val quiz20Questions = listOf(
        QuizItem(
            "How many daily obligatory prayers (Farz) are ordained in Islam?",
            listOf("3", "5", "7", "4"),
            1,
            "Islam ordains five daily obligatory prayers: Fajr, Dhuhr, Asr, Maghrib, and Isha."
        ),
        QuizItem(
            "What was the first word of the Quran revealed to Prophet Muhammad ﷺ in Cave Hira?",
            listOf("Qul", "Iqra", "Bismillah", "Al-Hamd"),
            1,
            "The first revealed word was 'Iqra' (Read / Proclaim) in Surah Al-'Alaq (96:1)."
        ),
        QuizItem(
            "How many Surahs are there in the Holy Quran?",
            listOf("110", "112", "114", "116"),
            2,
            "The Holy Quran contains exactly 114 Surahs."
        ),
        QuizItem(
            "Which Surah is considered the 'Heart of the Quran'?",
            listOf("Surah Al-Mulk", "Surah Ya-Sin", "Surah Ar-Rahman", "Surah Al-Baqarah"),
            1,
            "Prophet Muhammad ﷺ described Surah Ya-Sin as the heart of the Quran."
        ),
        QuizItem(
            "Which Surah is equivalent to one-third of the Quran in reward?",
            listOf("Surah Al-Ikhlas", "Surah Al-Fatihah", "Surah Al-Falaq", "Surah An-Nas"),
            0,
            "Surah Al-Ikhlas confirms the pure Oneness of Allah and equals one-third of the Quran in virtue."
        ),
        QuizItem(
            "What is the longest Ayah in the Holy Quran?",
            listOf("Ayat al-Kursi", "Ayat ad-Dayn (The Verse of Loan)", "Ayah of Nur", "Surah Al-Kawthar"),
            1,
            "Ayat ad-Dayn (Surah Al-Baqarah 2:282), dealing with loan contracts, is the longest Ayah."
        ),
        QuizItem(
            "In which Islamic month was the Holy Quran first revealed?",
            listOf("Rajab", "Sha'ban", "Ramadan", "Dhul-Hijjah"),
            2,
            "The Quran was revealed in the blessed month of Ramadan on Laylat al-Qadr (The Night of Decree)."
        ),
        QuizItem(
            "Which Angel was tasked with bringing the divine revelation to the Prophets?",
            listOf("Mika'il (Michael)", "Jibril (Gabriel)", "Israfil", "Malik"),
            1,
            "Angel Jibril (Gabriel) (AS) was the emissary of divine revelation to all the Prophets."
        ),
        QuizItem(
            "Which Prophet built the Holy Kaaba with his son Isma'il (AS)?",
            listOf("Prophet Adam (AS)", "Prophet Nuh (AS)", "Prophet Ibrahim (AS)", "Prophet Musa (AS)"),
            2,
            "Prophet Ibrahim (Abraham) (AS) and his son Isma'il (Ishmael) (AS) raised the foundations of the Kaaba in Makkah."
        ),
        QuizItem(
            "What is the first pillar of Islam?",
            listOf("Salah (Prayer)", "Shahadah (Declaration of Faith)", "Zakah (Charity)", "Sawm (Fasting)"),
            1,
            "Shahadah (bearing witness that there is no god but Allah and Muhammad is His messenger) is the foundational pillar."
        ),
        QuizItem(
            "Which Surah does not begin with 'Bismillah-ir-Rahman-ir-Rahim'?",
            listOf("Surah At-Tawbah", "Surah Al-Anfal", "Surah Al-Fatihah", "Surah Yunus"),
            0,
            "Surah At-Tawbah (Surah 9) does not begin with Bismillah."
        ),
        QuizItem(
            "In which Surah does 'Bismillah-ir-Rahman-ir-Rahim' appear twice?",
            listOf("Surah An-Naml", "Surah Al-Baqarah", "Surah Maryam", "Surah Al-Kahf"),
            0,
            "In Surah An-Naml (27:30), Bismillah appears at the start and inside the letter from Prophet Sulaiman (AS) to the Queen of Sheba."
        ),
        QuizItem(
            "Which Prophet is mentioned by name most frequently in the Holy Quran?",
            listOf("Prophet Ibrahim (AS)", "Prophet Musa (AS)", "Prophet Isa (AS)", "Prophet Yusuf (AS)"),
            1,
            "Prophet Musa (Moses) (AS) is mentioned by name 136 times in the Quran."
        ),
        QuizItem(
            "What was the first Qibla (direction of prayer) before the Kaaba?",
            listOf("Masjid al-Aqsa in Jerusalem", "Masjid Quba", "Mount Sinai", "Masjid an-Nabawi"),
            0,
            "Muslims initially prayed towards Masjid al-Aqsa in Jerusalem before Allah commanded the turn towards the Kaaba."
        ),
        QuizItem(
            "What is the greatest Ayah in the Quran according to Prophet Muhammad ﷺ?",
            listOf("Ayat al-Kursi (2:255)", "Surah Al-Fatihah 1:1", "Surah Al-Baqarah 2:285", "Surah Al-Hashr 59:24"),
            0,
            "Ayat al-Kursi (2:255) is the greatest verse due to its comprehensive description of Allah's divine attributes."
        ),
        QuizItem(
            "Which companion is the only one mentioned by name in the Quran?",
            listOf("Abu Bakr as-Siddiq (RA)", "Umar ibn al-Khattab (RA)", "Zayd ibn Harithah (RA)", "Ali ibn Abi Talib (RA)"),
            2,
            "Zayd ibn Harithah (RA) is explicitly mentioned by name in Surah Al-Ahzab (33:37)."
        ),
        QuizItem(
            "How many years did the revelation of the Quran span?",
            listOf("10 years", "23 years", "30 years", "40 years"),
            1,
            "The Quran was revealed gradually over approximately 23 years (13 in Makkah and 10 in Madinah)."
        ),
        QuizItem(
            "Which Surah protects from the trials of the Dajjal when its first 10 verses are memorized?",
            listOf("Surah Al-Kahf", "Surah Al-Mulk", "Surah Al-Baqarah", "Surah Al-Hadid"),
            0,
            "Prophet Muhammad ﷺ taught that memorizing the first ten verses of Surah Al-Kahf safeguards from the Dajjal."
        ),
        QuizItem(
            "What is the meaning of the word 'Zakat' in Arabic?",
            listOf("Tax", "Purification and Growth", "Sacrifice", "Obligation"),
            1,
            "Zakat linguistically signifies purification (Taharah) and blessed growth (Namaa)."
        ),
        QuizItem(
            "Which night is described as better than a thousand months in the Quran?",
            listOf("Laylat al-Qadr", "Laylat al-Bara'at", "Night of Isra and Mi'raj", "Night of Eid"),
            0,
            "Surah Al-Qadr states: 'The Night of Decree is better than a thousand months' (97:3)."
        )
    )
}
