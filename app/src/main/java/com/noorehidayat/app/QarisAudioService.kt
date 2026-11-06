package com.noorehidayat.app

object QarisAudioService {
    val qaris = listOf(
        Qari(
            id = "alafasy",
            name = "Mishary Rashid Alafasy",
            nameUrdu = "مشاری راشد العفاسی",
            cdns = listOf(
                "https://cdn.islamic.network/quran/audio-surah/128/ar.alafasy/{num}.mp3",
                "https://cdn.mp3quran.net/audio/mishary-alafasy/r1/{pad3}.mp3",
                "https://server8.mp3quran.net/afs/{pad3}.mp3"
            )
        ),
        Qari(
            id = "sudais",
            name = "Abdur-Rahman As-Sudais",
            nameUrdu = "عبد الرحمن السدیس",
            cdns = listOf(
                "https://cdn.mp3quran.net/audio/abdulrahman-sudais/r1/{pad3}.mp3",
                "https://server11.mp3quran.net/sds/{pad3}.mp3"
            )
        ),
        Qari(
            id = "abdulbasit",
            name = "Abdul Basit Abdul Samad",
            nameUrdu = "عبد الباسط عبد الصمد",
            cdns = listOf(
                "https://cdn.mp3quran.net/audio/abdulbasit-abdulsamad/r3/{pad3}.mp3",
                "https://server7.mp3quran.net/basit/{pad3}.mp3"
            )
        ),
        Qari(
            id = "muaiqly",
            name = "Maher Al-Muaiqly",
            nameUrdu = "ماہر المعيقلي",
            cdns = listOf(
                "https://cdn.mp3quran.net/audio/maher-muaiqly/r1/{pad3}.mp3",
                "https://server12.mp3quran.net/maher/{pad3}.mp3"
            )
        ),
        Qari(
            id = "husary",
            name = "Mahmoud Khalil Al-Husary",
            nameUrdu = "محمود خلیل الحصري",
            cdns = listOf(
                "https://cdn.mp3quran.net/audio/mahmoud-husary/r1/{pad3}.mp3",
                "https://server13.mp3quran.net/husr/{pad3}.mp3"
            )
        ),
        Qari(
            id = "shuraim",
            name = "Saud Al-Shuraim",
            nameUrdu = "سعود الشریم",
            cdns = listOf(
                "https://cdn.mp3quran.net/audio/saud-shuraim/r1/{pad3}.mp3",
                "https://server7.mp3quran.net/shur/{pad3}.mp3"
            )
        )
    )

    fun getAudioUrls(qari: Qari, surahNumber: Int): List<String> {
        val pad3 = surahNumber.toString().padStart(3, '0')
        val num = surahNumber.toString()
        return qari.cdns.map { template ->
            template.replace("{pad3}", pad3).replace("{num}", num)
        }
    }
}
