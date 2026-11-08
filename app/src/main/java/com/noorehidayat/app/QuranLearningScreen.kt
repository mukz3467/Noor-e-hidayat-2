package com.noorehidayat.app

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class QuranCourseLesson(
    val id: String,
    val module: Int,
    val title: List<String>,
    val body: List<String>,
    val example: String,
    val practice: List<String>,
    val searchQuery: String
)

private data class QuranCourseModule(
    val number: Int,
    val title: List<String>,
    val summary: List<String>
)

private fun ql(values: List<String>, language: AppLanguage): String =
    values.getOrElse(language.ordinal) { values.first() }

private val quranCourseModules = listOf(
    QuranCourseModule(1, listOf("Arabic alphabet","عربی حروف","الحروف العربية","अरबी अक्षर"), listOf("Recognise letters and their sounds.","حروف اور ان کی آوازیں پہچانیں۔","تعرّف على الحروف وأصواتها.","अक्षरों और उनकी आवाज़ों को पहचानें।")),
    QuranCourseModule(2, listOf("Letter forms & joining","حروف کی شکلیں اور جوڑ","أشكال الحروف واتصالها","अक्षरों के रूप और जोड़"), listOf("Read letters in connected words.","جڑے ہوئے الفاظ میں حروف پڑھیں۔","اقرأ الحروف في الكلمات المتصلة.","जुड़े हुए शब्दों में अक्षर पढ़ें।")),
    QuranCourseModule(3, listOf("Harakaat & basic reading","حرکات اور بنیادی قرأت","الحركات والقراءة الأساسية","हरकात और शुरुआती पढ़ाई"), listOf("Learn short vowels, tanween, sukoon and shaddah.","زبر، زیر، پیش، تنوین، سکون اور تشدید سیکھیں۔","تعلّم الحركات والتنوين والسكون والشدة.","छोटी मात्राएँ, तन्वीन, सुकून और शद्दा सीखें।")),
    QuranCourseModule(4, listOf("Madd & word reading","مد اور الفاظ کی قرأت","المد وقراءة الكلمات","मद्द और शब्द पढ़ना"), listOf("Understand long vowels and word flow.","لمبی آوازیں اور الفاظ کی روانی سمجھیں۔","افهم حروف المد وسلاسة قراءة الكلمات.","लंबी ध्वनियाँ और शब्दों का प्रवाह समझें।")),
    QuranCourseModule(5, listOf("Makharij & articulation","مخارج اور ادائیگی","مخارج الحروف","मखारिज और उच्चारण"), listOf("Learn articulation points with a teacher.","استاد کے ساتھ مخارج سیکھیں۔","تعلّم المخارج مع معلّم.","शिक्षक के साथ मखारिज सीखें।")),
    QuranCourseModule(6, listOf("Core Tajweed","بنیادی تجوید","أحكام التجويد الأساسية","तजवीद के मूल नियम"), listOf("Study noon, tanween, meem and ghunnah.","نون، تنوین، میم اور غنہ کے قواعد سیکھیں۔","ادرس أحكام النون والتنوين والميم والغنة.","नून, तन्वीन, मीम और ग़ुन्ना के नियम सीखें।")),
    QuranCourseModule(7, listOf("Further Tajweed & waqf","مزید تجوید اور وقف","أحكام التجويد والوقف","आगे के तजवीद नियम और वक़्फ़"), listOf("Practise qalqalah, madd and stopping signs.","قلقلہ، مد اور وقف کی علامات کی مشق کریں۔","تدرّب على القلقلة والمد وعلامات الوقف.","कलक़ला, मद्द और रुकने के संकेत सीखें।")),
    QuranCourseModule(8, listOf("Fluent recitation","روانی سے تلاوت","طلاقة التلاوة","सहज तिलावत"), listOf("Build a careful daily recitation routine.","درست روزانہ تلاوت کا معمول بنائیں۔","ابنِ برنامجاً يومياً للتلاوة الصحيحة.","सही रोज़ाना तिलावत की आदत बनाएँ।")),
    QuranCourseModule(9, listOf("Hifz & revision","حفظ اور دہرائی","الحفظ والمراجعة","हिफ़्ज़ और दोहराई"), listOf("Memorise in portions and revise regularly.","حصوں میں حفظ اور باقاعدہ دہرائی کریں۔","احفظ مقاطع وراجعها بانتظام.","हिस्सों में याद करें और नियमित दोहराएँ।"))
)

private val quranCourseLessons = listOf(
    QuranCourseLesson("a1",1,listOf("Meet the Arabic letters","عربی حروف کی پہچان","تعرّف على الحروف العربية","अरबी अक्षरों की पहचान"),
        listOf("Arabic is read from right to left. Learn the names and shapes of the 28 commonly taught Arabic letters before joining them. A letter's name and its reading sound are not the same thing.","عربی دائیں سے بائیں پڑھی جاتی ہے۔ حروف ملانے سے پہلے عام طور پر پڑھائے جانے والے ۲۸ عربی حروف کے نام اور شکلیں سیکھیں۔ حرف کا نام اور پڑھنے کی آواز الگ چیزیں ہیں۔","تُقرأ العربية من اليمين إلى اليسار. تعلّم أسماء وأشكال الحروف العربية الثمانية والعشرين قبل وصلها. اسم الحرف ليس هو صوته عند القراءة.","अरबी दाएँ से बाएँ पढ़ी जाती है। जोड़ने से पहले सामान्यतः पढ़ाए जाने वाले 28 अक्षरों के नाम और रूप सीखें। अक्षर का नाम और पढ़ने की ध्वनि अलग हैं।"),
        "ا ب ت ث ج ح خ د ذ ر ز س ش ص ض ط ظ ع غ ف ق ك ل م ن ه و ي",
        listOf("Name each letter aloud from right to left; listen to a qualified teacher for its exact sound.","ہر حرف کا نام دائیں سے بائیں پڑھیں؛ درست آواز کے لیے مستند استاد کو سنیں۔","انطق أسماء الحروف من اليمين إلى اليسار واستمع إلى معلّم مؤهل للنطق الصحيح.","दाएँ से बाएँ अक्षरों के नाम बोलें और सही ध्वनि के लिए योग्य शिक्षक को सुनें।"),"Arabic alphabet pronunciation Quran beginner lesson qualified teacher"),
    QuranCourseLesson("a2",1,listOf("Similar letters and dots","ملتے جلتے حروف اور نقطے","الحروف المتشابهة والنقاط","मिलते-जुलते अक्षर और बिंदु"),
        listOf("Some letters share a base shape but differ by dots: ب ت ث, ج ح خ, د ذ, ر ز, س ش, ص ض, ط ظ, ع غ and ف ق. Dots change the letter, so identify them carefully.","کچھ حروف کی بنیادی شکل ملتی ہے مگر نقطوں سے فرق ہوتا ہے: ب ت ث، ج ح خ، د ذ، ر ز، س ش، ص ض، ط ظ، ع غ اور ف ق۔ نقطے حرف بدل دیتے ہیں، انہیں غور سے پہچانیں۔","تشترك بعض الحروف في الشكل وتختلف بالنقاط: ب ت ث، ج ح خ، د ذ، ر ز، س ش، ص ض، ط ظ، ع غ، ف ق. تغيّر النقاط الحرف، فميّزها جيداً.","कुछ अक्षरों का मूल आकार समान है पर बिंदुओं से अंतर होता है: ب ت ث, ج ح خ, د ذ, ر ز, س ش, ص ض, ط ظ, ع غ, ف ق। बिंदु अक्षर बदल देते हैं।"),
        "ب ت ث   ج ح خ   د ذ   ر ز   س ش",
        listOf("Cover the dots, identify the shape, then uncover them and name the letter.","نقطے چھپا کر شکل پہچانیں، پھر نقطے دیکھ کر حرف بتائیں۔","غطِّ النقاط لتمييز الشكل ثم اكشفها وسمِّ الحرف.","बिंदु छिपाकर आकार पहचानें, फिर खोलकर अक्षर बताएँ।"),"Arabic letters similar shapes dots pronunciation"),
    QuranCourseLesson("b1",2,listOf("Beginning, middle and final forms","شروع، درمیان اور آخر کی شکلیں","أشكال الحروف أول الكلمة ووسطها وآخرها","शुरू, बीच और अंत के रूप"),
        listOf("Many Arabic letters change shape according to their position in a word. For example, ب may appear as ب, بـ, ـبـ or ـب. The sound remains the same; the joining shape changes.","بہت سے عربی حروف لفظ میں جگہ کے مطابق شکل بدلتے ہیں۔ مثلاً ب کی شکل ب، بـ، ـبـ یا ـب ہو سکتی ہے۔ آواز وہی رہتی ہے، جوڑ کی شکل بدلتی ہے۔","يتغير شكل كثير من الحروف حسب موضعها في الكلمة. فمثلاً قد يظهر ب هكذا: ب، بـ، ـبـ، ـب. ويبقى الصوت الأساسي نفسه.","कई अरबी अक्षर शब्द में स्थान के अनुसार रूप बदलते हैं। उदाहरण ب: ب، بـ، ـبـ، ـب। मूल ध्वनि वही रहती है।"),
        "ب   بـ   ـبـ   ـب",
        listOf("Trace the four forms of ب, then practise with ت and ن.","ب کی چاروں شکلیں لکھیں، پھر ت اور ن کے ساتھ مشق کریں۔","اكتب أشكال ب الأربعة ثم تدرّب على ت ون.","ب के चारों रूप लिखें, फिर ت और ن का अभ्यास करें।"),"Arabic initial medial final letter forms joining"),
    QuranCourseLesson("b2",2,listOf("Letters that do not join forward","وہ حروف جو آگے نہیں جڑتے","الحروف التي لا تتصل بما بعدها","वे अक्षर जो आगे नहीं जुड़ते"),
        listOf("The letters ا د ذ ر ز و do not connect to the following letter on their left in ordinary Arabic joining. A written break does not automatically mean you should pause while reading.","ا د ذ ر ز و عام عربی جوڑ میں اپنے بعد بائیں طرف آنے والے حرف سے نہیں جڑتے۔ تحریر میں جوڑ ٹوٹنے کا مطلب یہ نہیں کہ پڑھتے ہوئے رکنا بھی ہے۔","الحروف ا د ذ ر ز و لا تتصل بالحرف التالي على يسارها في الوصل المعتاد. والانفصال الكتابي لا يعني الوقف في القراءة.","ا د ذ ر ز و सामान्य अरबी जोड़ में अपने बाद के अक्षर से नहीं जुड़ते। लिखावट का अलगाव पढ़ते समय रुकना नहीं है।"),
        "بَاب   نُور   زَيْت",
        listOf("Find these letters in printed Arabic words and read each complete word smoothly.","چھپے ہوئے عربی الفاظ میں یہ حروف تلاش کریں اور پورا لفظ روانی سے پڑھیں۔","ابحث عن هذه الحروف في كلمات عربية مطبوعة واقرأ الكلمة كاملة بسلاسة.","छपे अरबी शब्दों में ये अक्षर खोजें और पूरा शब्द सहजता से पढ़ें।"),"Arabic letters that do not connect alif dal dhal ra zay waw"),
    QuranCourseLesson("c1",3,listOf("Fathah, kasrah and dammah","زبر، زیر اور پیش","الفتحة والكسرة والضمة","फ़त्हा, कसरा और दम्हा"),
        listOf("The three short-vowel marks are fathah (َ), kasrah (ِ) and dammah (ُ). They indicate a short a, i or u sound after the consonant. Keep these sounds short.","تین مختصر حرکات زبر (َ)، زیر (ِ) اور پیش (ُ) ہیں۔ یہ حرف کے بعد مختصر اَ، اِ یا اُ کی آواز بتاتی ہیں۔ آواز مختصر رکھیں۔","الحركات القصيرة هي الفتحة (َ) والكسرة (ِ) والضمة (ُ)، وتدل على صوت قصير بعد الحرف. اجعل الصوت قصيراً.","तीन छोटी मात्राएँ फ़त्हा (َ), कसरा (ِ) और दम्हा (ُ) हैं। ये अक्षर के बाद छोटी a, i या u ध्वनि बताती हैं।"),
        "بَ   بِ   بُ",
        listOf("Read ba, bi, bu slowly and ask a teacher to correct the vowel sound.","بَ، بِ، بُ آہستہ پڑھیں اور استاد سے آواز درست کروائیں۔","اقرأ بَ بِ بُ ببطء واطلب من المعلّم تصحيح الحركة.","बَ, بِ, بُ धीरे पढ़ें और शिक्षक से मात्रा ठीक करवाएँ।"),"Fatha kasra damma Arabic harakat lesson"),
    QuranCourseLesson("c2",3,listOf("Tanween","تنوین","التنوين","तन्वीन"),
        listOf("Tanween is written with two marks: fathatan (ً), kasratan (ٍ) or dammatan (ٌ). In connected reading it gives an n-sound; the rule changes according to the next letter.","تنوین دو زبر (ً)، دو زیر (ٍ) یا دو پیش (ٌ) سے لکھی جاتی ہے۔ ملا کر پڑھنے میں ن کی آواز آتی ہے؛ اگلے حرف کے مطابق قاعدہ بدلتا ہے۔","يكتب التنوين بفتحتين (ً) أو كسرتين (ٍ) أو ضمتين (ٌ). ويظهر صوت النون في الوصل، ويتغير الحكم بحسب الحرف التالي.","तन्वीन दो फ़त्हा (ً), दो कसरा (ٍ) या दो दम्हा (ٌ) से लिखी जाती है। जोड़कर पढ़ने में न की ध्वनि आती है और आगे के अक्षर से नियम बदलता है।"),
        "بًا   بٍ   بٌ",
        listOf("Identify the tanween mark in each example. The next module explains its Tajweed rules.","ہر مثال میں تنوین کی علامت پہچانیں۔ اگلے ماڈیول میں اس کے تجویدی قواعد ہیں۔","ميّز علامة التنوين في كل مثال؛ ويشرح القسم التالي أحكامها.","हर उदाहरण में तन्वीन पहचानें। अगले भाग में इसके तजवीद नियम हैं।"),"Tanween fathatan kasratan dammatan Quran"),
    QuranCourseLesson("c3",3,listOf("Sukoon","سکون","السكون","सुकून"),
        listOf("Sukoon (ْ) means the consonant has no short vowel of its own. Join it to the preceding sound. Do not add an extra a, i or u after a sakin letter.","سکون (ْ) کا مطلب ہے کہ حرف پر اپنی کوئی مختصر حرکت نہیں۔ اسے پچھلی آواز سے ملائیں۔ ساکن حرف کے بعد اپنی طرف سے زبر، زیر یا پیش نہ بڑھائیں۔","السكون (ْ) يعني أن الحرف ليس عليه حركة قصيرة. صِلْه بالصوت السابق ولا تضف فتحة أو كسرة أو ضمة.","सुकून (ْ) का अर्थ है कि अक्षर पर अपनी छोटी मात्रा नहीं। इसे पिछली ध्वनि से जोड़ें और अतिरिक्त स्वर न जोड़ें।"),
        "أَبْ   مِنْ   لَمْ",
        listOf("Read each example without adding a vowel after the final sakin letter.","ہر مثال پڑھیں اور آخری ساکن حرف کے بعد اضافی حرکت نہ لگائیں۔","اقرأ الأمثلة دون إضافة حركة بعد الحرف الساكن الأخير.","हर उदाहरण पढ़ें और अंतिम साकिन अक्षर के बाद अतिरिक्त मात्रा न जोड़ें।"),"Arabic sukoon jazm Quran reading"),
    QuranCourseLesson("c4",3,listOf("Shaddah","تشدید","الشدة","शद्दा"),
        listOf("Shaddah (ّ) marks a doubled consonant: the first part is sakin and the second carries its vowel. Noon and meem with shaddah have a special ghunnah studied later.","تشدید (ّ) حرف کے دوہرے ادا ہونے کی علامت ہے: پہلا حصہ ساکن اور دوسرا حرکت والا ہوتا ہے۔ نون اور میم مشدد میں خاص غنہ ہوتا ہے، جو آگے پڑھیں گے۔","تدل الشدة (ّ) على تضعيف الحرف؛ أوله ساكن وثانيه متحرك. وللنون والميم المشددتين غنة خاصة تُدرس لاحقاً.","शद्दा (ّ) अक्षर के दोहरे उच्चारण को दिखाती है। शद्दा वाले नून और मीम में विशेष ग़ुन्ना होता है।"),
        "رَبِّ   إِنَّ   ثُمَّ",
        listOf("Find the shaddah and repeat the word after a qualified reciter.","تشدید تلاش کریں اور لفظ مستند قاری کے بعد دہرائیں۔","حدّد الشدة وكرّر الكلمة بعد قارئ مؤهل.","शद्दा पहचानें और योग्य क़ारी के बाद शब्द दोहराएँ।"),"Arabic shaddah tashdeed Quran lesson"),
    QuranCourseLesson("d1",4,listOf("Long vowels: alif, waaw, yaa","لمبی آوازیں: الف، واو، یا","حروف المد: الألف والواو والياء","लंबी ध्वनियाँ: अलिफ, वाव, या"),
        listOf("The letters ا و ي can function as long-vowel letters under their required conditions. The vowel before them matters. Compare a short vowel with its long form.","حروف ا و ي مخصوص شرائط کے ساتھ لمبی آواز دیتے ہیں۔ ان سے پہلے والی حرکت اہم ہوتی ہے۔ مختصر اور لمبی آواز کا فرق سنیں۔","تكون ا و ي حروف مد بشروطها، والحركة قبلها مهمة. قارن بين الحركة القصيرة والمد.","ا و ي कुछ शर्तों के साथ लंबी ध्वनियाँ देते हैं। उनसे पहले की मात्रा महत्वपूर्ण है। छोटी और लंबी ध्वनि की तुलना करें।"),
        "بَ / بَا     بِ / بِي     بُ / بُو",
        listOf("Listen to short and long sounds. Exact madd counts are taught with the recitation tradition by a teacher.","مختصر اور لمبی آوازیں سنیں۔ مد کی درست مقدار اپنی قرأت کی روایت کے مطابق استاد سے سیکھیں۔","استمع إلى الصوت القصير والمد، وتعلّم مقادير المد من معلّم وفق روايتك.","छोटी और लंबी ध्वनि सुनें। मद्द की सही मात्रा अपनी रिवायत के अनुसार शिक्षक से सीखें।"),"Arabic madd letters alif waw yaa"),
    QuranCourseLesson("e1",5,listOf("Makharij: articulation points","مخارج: ادائیگی کے مقامات","المخارج: مواضع خروج الحروف","मखारिज: उच्चारण के स्थान"),
        listOf("Makharij are the articulation points from which letters are produced. Broad areas include the oral cavity, throat, tongue, lips and nasal passage. Every letter has a more specific description.","مخارج وہ مقامات ہیں جہاں سے حروف ادا ہوتے ہیں۔ بنیادی مقامات میں منہ کی گنجائش، حلق، زبان، ہونٹ اور خیشوم شامل ہیں۔ ہر حرف کا مخصوص مخرج بھی ہے۔","المخارج هي مواضع خروج الحروف. وتشمل المواضع العامة الجوف والحلق واللسان والشفتين والخيشوم، ولكل حرف مخرج أدق.","मखारिज वे स्थान हैं जहाँ से अक्षर निकलते हैं। मुख्य स्थानों में मुँह की गुहा, गला, जीभ, होंठ और नासिका मार्ग शामिल हैं।"),
        "ء هـ ع ح غ خ   ف ب م   ق ك",
        listOf("A qualified teacher must demonstrate the sound and correct your mouth and tongue position. Text alone cannot teach precise makhraj.","مستند استاد آواز کا عملی مظاہرہ کرے اور زبان و منہ کی پوزیشن درست کروائے۔ صرف تحریر درست مخرج نہیں سکھا سکتی۔","يجب أن يبيّن معلّم مؤهل الصوت ويصحح موضع اللسان والفم؛ فالنص وحده لا يعلّم المخرج بدقة.","योग्य शिक्षक ध्वनि दिखाए और जीभ-मुँह की स्थिति ठीक करे। केवल लिखावट सही मखराज नहीं सिखा सकती।"),"Quran makharij articulation points practical teacher"),
    QuranCourseLesson("f1",6,listOf("Noon saakinah and tanween","نون ساکن اور تنوین","النون الساكنة والتنوين","नून साकिन और तन्वीन"),
        listOf("Noon saakinah (نْ) and tanween have four main rules according to the next letter: izhar, idgham, iqlab and ikhfa. The next lesson gives the letter groups.","نون ساکن (نْ) اور تنوین کے اگلے حرف کے لحاظ سے چار بنیادی قواعد ہیں: اظہار، ادغام، اقلاب اور اخفاء۔ اگلے سبق میں حروف کے گروپ ہیں۔","للنون الساكنة والتنوين أربعة أحكام بحسب الحرف التالي: الإظهار والإدغام والإقلاب والإخفاء. يشرح الدرس التالي مجموعات الحروف.","नून साकिन (نْ) और तन्वीन के आगे आने वाले अक्षर के अनुसार चार नियम हैं: इज़हार, इदग़ाम, इक़लाब और इख़फ़ा।"),
        "نْ   ـً   ـٍ   ـٌ",
        listOf("Find noon saakinah or tanween in a Mushaf example and identify the next letter.","مصحف کی مثال میں نون ساکن یا تنوین تلاش کریں اور اگلا حرف پہچانیں۔","حدّد النون الساكنة أو التنوين في مثال من المصحف ثم عيّن الحرف التالي.","मुसहफ़ में नून साकिन या तन्वीन ढूँढें और अगला अक्षर पहचानें।"),"Noon sakinah tanween tajweed four rules"),
    QuranCourseLesson("f2",6,listOf("Izhar, idgham, iqlab and ikhfa","اظہار، ادغام، اقلاب اور اخفاء","الإظهار والإدغام والإقلاب والإخفاء","इज़हार, इदग़ाम, इक़लाब और इख़फ़ा"),
        listOf("Izhar letters: ء ه ع ح غ خ. Idgham letters: ي ر م ل و ن. Iqlab occurs before ب. The remaining 15 letters form the ikhfa group. Idgham has further detail, including with and without ghunnah.","اظہار کے حروف ء ه ع ح غ خ، ادغام کے حروف ي ر م ل و ن ہیں۔ ب سے پہلے اقلاب اور باقی ۱۵ حروف کے ساتھ اخفاء ہوتا ہے۔ ادغام کی مزید تفصیل بھی ہے، جیسے غنہ کے ساتھ یا بغیر۔","حروف الإظهار: ء ه ع ح غ خ. وحروف الإدغام: ي ر م ل و ن. ويكون الإقلاب قبل ب، والحروف الخمسة عشر الباقية للإخفاء. وللإدغام تفصيل بغنة وبغير غنة.","इज़हार: ء ه ع ح غ خ। इदग़ाम: ي ر م ل و ن। ب से पहले इक़लाब और शेष 15 अक्षरों के साथ इख़फ़ा। इदग़ाम में ग़ुन्ना सहित/बिना की बारीकियाँ हैं।"),
        "مِنْ بَعْدِ   سَمِيعٌ عَلِيمٌ",
        listOf("Listen to examples of each rule and ask a teacher to confirm the rule and sound.","ہر قاعدے کی مثال سنیں اور استاد سے قاعدے اور آواز کی تصدیق کروائیں۔","استمع إلى أمثلة كل حكم واطلب من المعلّم تأكيد الحكم والصوت.","हर नियम के उदाहरण सुनें और शिक्षक से नियम व ध्वनि की पुष्टि करवाएँ।"),"Izhar Idgham Iqlab Ikhfa tajweed examples"),
    QuranCourseLesson("f3",6,listOf("Meem saakinah and ghunnah","میم ساکن اور غنہ","الميم الساكنة والغنة","मीम साकिन और ग़ुन्ना"),
        listOf("Meem saakinah has three well-known rules: ikhfa shafawi before ب, idgham shafawi before م, and izhar shafawi before other letters. Ghunnah is a nasal quality associated with noon and meem; shaddah on them receives a clear ghunnah.","میم ساکن کے تین معروف قواعد ہیں: ب سے پہلے اخفاء شفوی، م سے پہلے ادغام شفوی اور باقی حروف سے پہلے اظہار شفوی۔ غنہ نون اور میم کی ناک سے ادا ہونے والی کیفیت ہے؛ ان پر تشدید ہو تو واضح غنہ ہوتا ہے۔","للميم الساكنة ثلاثة أحكام: الإخفاء الشفوي قبل ب، والإدغام الشفوي قبل م، والإظهار الشفوي قبل بقية الحروف. والغنة صفة أنفية للنون والميم، وتظهر بوضوح مع الشدة.","मीम साकिन के तीन नियम हैं: ب से पहले इख़फ़ा शफ़वी, م से पहले इदग़ाम शफ़वी और अन्य अक्षरों से पहले इज़हार शफ़वी। ग़ुन्ना नून-मीम की नासिक्य ध्वनि है।"),
        "تَرْمِيهِمْ بِحِجَارَةٍ   إِنَّ   ثُمَّ",
        listOf("Listen to a qualified reciter and practise the lip position and nasal sound with correction.","مستند قاری کو سنیں اور ہونٹوں کی پوزیشن اور غنہ کی مشق اصلاح کے ساتھ کریں۔","استمع إلى قارئ مؤهل وتدرّب على وضع الشفتين والغنة مع التصحيح.","योग्य क़ारी को सुनें और होंठों की स्थिति व ग़ुन्ना का अभ्यास सुधार के साथ करें।"),"Meem sakinah ikhfa shafawi idgham ghunnah"),
    QuranCourseLesson("g1",7,listOf("Qalqalah","قلقلہ","القلقلة","कलक़ला"),
        listOf("The qalqalah letters are ق ط ب ج د. When one is sakin, it has a distinct echoing release. Do not add a full vowel after it. Its strength can vary by position.","قلقلہ کے حروف ق ط ب ج د ہیں۔ ان میں سے کوئی حرف ساکن ہو تو خاص گونج کے ساتھ ادا ہوتا ہے۔ اس کے بعد پوری حرکت نہ بڑھائیں۔ کیفیت جگہ کے لحاظ سے بدل سکتی ہے۔","حروف القلقلة ق ط ب ج د. إذا سكن أحدها ظهرت له صفة القلقلة. لا تضف حركة كاملة بعده، وتختلف درجتها بحسب الموضع.","कलक़ला के अक्षर ق ط ب ج د हैं। इनमें से कोई साकिन हो तो विशेष प्रतिध्वनि आती है। उसके बाद पूरा स्वर न जोड़ें।"),
        "أَحَدْ   الْفَلَقْ   يَجْعَلْ",
        listOf("Listen for a clean release without an added vowel and practise under supervision.","اضافی حرکت کے بغیر صاف ادائیگی سنیں اور نگرانی میں مشق کریں۔","استمع إلى خروج واضح دون حركة زائدة وتدرّب بإشراف المعلّم.","अतिरिक्त स्वर के बिना स्पष्ट ध्वनि सुनें और निगरानी में अभ्यास करें।"),"Qalqalah letters qutbu jadd tajweed"),
    QuranCourseLesson("g2",7,listOf("Madd, laam and raa rules","مد، لام اور را کے قواعد","أحكام المد واللام والراء","मद्द, लाम और रा के नियम"),
        listOf("Madd includes natural, connected, separated and necessary categories. Laam and raa also have context-dependent rules. Exact lengths and details can differ by riwayah and teaching method; learn them from your teacher using one consistent recitation tradition.","مد میں طبیعی، متصل، منفصل اور لازم کی اقسام شامل ہیں۔ لام اور را کے قواعد بھی سیاق کے مطابق بدلتے ہیں۔ مقدار اور تفصیلات روایت اور طریقۂ تعلیم کے مطابق مختلف ہو سکتی ہیں؛ استاد سے ایک ہی روایت کے مطابق سیکھیں۔","من أنواع المد الطبيعي والمتصل والمنفصل واللازم، وللام والراء أحكام بحسب السياق. قد تختلف المقادير والتفاصيل باختلاف الرواية؛ تعلّمها من معلّم وفق رواية واحدة.","मद्द में प्राकृतिक, मुत्तसिल, मुनफ़सिल और लाज़िम शामिल हैं। लाम और रा के नियम संदर्भ पर निर्भर हैं। मात्राएँ रिवायत के अनुसार बदल सकती हैं; शिक्षक से एक रिवायत में सीखें।"),
        "الشَّمْس   الْقَمَر   قَالُوا",
        listOf("Ask your teacher to identify the madd type and laam/raa rule in a short passage.","استاد سے مختصر عبارت میں مد کی قسم اور لام/را کا قاعدہ شناخت کروائیں۔","اطلب من المعلّم تحديد نوع المد وحكم اللام أو الراء في مقطع قصير.","शिक्षक से छोटे अंश में मद्द का प्रकार और लाम/रा का नियम पहचानवाएँ।"),"Madd types laam shamsiyyah qamariyyah raa tajweed"),
    QuranCourseLesson("g3",7,listOf("Waqf and stopping signs","وقف اور رکنے کی علامات","الوقف وعلاماته","वक़्फ़ और रुकने के निशान"),
        listOf("Mushaf editions use signs to guide stopping and continuing. Their presentation can vary, so read the symbol guide in the Mushaf you use. Learn suitable stopping and restarting places with a teacher; stopping can affect meaning.","مصحف میں رکنے اور ملانے کی رہنمائی کے لیے علامات ہوتی ہیں۔ مختلف نسخوں میں صورت بدل سکتی ہے، اس لیے اپنے مصحف کی علامات پڑھیں۔ مناسب وقف اور دوبارہ شروع کرنا استاد سے سیکھیں؛ وقف معنی پر اثر ڈال سکتا ہے۔","تستخدم المصاحف علامات للوقف والوصل وقد يختلف عرضها؛ فاقرأ دليل العلامات في مصحفك. تعلّم مواضع الوقف والابتداء مع معلّم لأن الوقف قد يؤثر في المعنى.","मुसहफ़ में रुकने और जारी रखने के संकेत होते हैं। संस्करणों में रूप बदल सकता है। अपने मुसहफ़ की संकेत-सूची पढ़ें और शिक्षक से सही विराम सीखें।"),
        "م   لا   ج   صلى   قلى",
        listOf("Ask a teacher to explain the signs printed in your Mushaf and practise where to stop and resume.","اپنے مصحف کی علامات استاد سے سمجھیں اور رکنے اور دوبارہ شروع کرنے کی مشق کریں۔","اطلب من المعلّم شرح علامات مصحفك وتدرّب على الوقف والابتداء.","अपने मुसहफ़ के संकेत शिक्षक से समझें और रुकने व फिर शुरू करने का अभ्यास करें।"),"Quran waqf signs stop pause symbols lesson"),
    QuranCourseLesson("h1",8,listOf("Build fluent daily recitation","روزانہ رواں تلاوت کی عادت","ابنِ تلاوة يومية متقنة","रोज़ाना सहज तिलावत"),
        listOf("A steady short daily session is more useful than occasional long sessions. Read a small portion accurately, review corrections, then gradually increase. Use the same Mushaf and recitation tradition while learning.","کبھی کبھار طویل نشست کے بجائے روزانہ مختصر اور مستقل مشق مفید ہے۔ تھوڑا حصہ درست پڑھیں، اصلاح دہرائیں، پھر آہستہ آہستہ مقدار بڑھائیں۔ سیکھتے وقت ایک ہی مصحف اور روایت اپنائیں۔","الجلسة اليومية القصيرة المنتظمة أنفع من جلسات طويلة متباعدة. اقرأ مقداراً قليلاً بدقة وراجع التصحيحات ثم زد تدريجياً، مع الالتزام بمصحف ورواية واحدة.","कभी-कभार लंबे अभ्यास से बेहतर है रोज़ थोड़ा अभ्यास। सही पढ़ें, सुधार दोहराएँ और धीरे-धीरे मात्रा बढ़ाएँ। सीखते समय एक मुसहफ़ और रिवायत अपनाएँ।"),
        "اقْرَأْ بِتَأَنٍّ وَتَدَبُّرٍ",
        listOf("Practise for 5–10 minutes, record a short passage and receive feedback from a qualified teacher.","۵ سے ۱۰ منٹ مشق کریں، مختصر عبارت ریکارڈ کریں اور مستند استاد سے اصلاح لیں۔","تدرّب من ٥ إلى ١٠ دقائق وسجّل مقطعاً قصيراً واعرضه على معلّم مؤهل.","5–10 मिनट अभ्यास करें, छोटा अंश रिकॉर्ड करें और योग्य शिक्षक से प्रतिक्रिया लें।"),"Quran recitation daily fluency practice teacher"),
    QuranCourseLesson("h2",8,listOf("Practise short Surahs carefully","چھوٹی سورتوں کی درست مشق","تدرّب على السور القصيرة","छोटी सूरतों का सावधानी से अभ्यास"),
        listOf("Choose a short Surah you know. Follow every letter, vowel, madd and stopping place in the Mushaf. Memorisation does not guarantee correct recitation, so have it checked by a qualified teacher.","ایسی مختصر سورت منتخب کریں جو یاد ہو۔ مصحف میں ہر حرف، حرکت، مد اور وقف دیکھتے ہوئے پڑھیں۔ یاد ہونا درست تلفظ کی ضمانت نہیں، اس لیے مستند استاد سے جانچ کروائیں۔","اختر سورة قصيرة تحفظها، واتبع الحروف والحركات والمد والوقف في المصحف. الحفظ لا يضمن صحة التلاوة؛ اعرضها على معلّم مؤهل.","ऐसी छोटी सूरत चुनें जो याद हो। मुसहफ़ में अक्षर, मात्रा, मद्द और विराम देखें। याद होना सही तिलावत की गारंटी नहीं; शिक्षक से जँचवाएँ।"),
        "الْفَاتِحَة   الْإِخْلَاص   الْفَلَق   النَّاس",
        listOf("Recite one short Surah to your teacher and write down the corrections.","ایک مختصر سورت استاد کو سنائیں اور اصلاحات لکھیں۔","اقرأ سورة قصيرة على معلّمك وسجّل التصحيحات.","एक छोटी सूरत शिक्षक को सुनाएँ और सुधार लिखें।"),"Surah Fatiha Ikhlas Falaq Nas tajweed recitation"),
    QuranCourseLesson("i1",9,listOf("Memorise in small portions","چھوٹے حصوں میں حفظ","الحفظ على مقاطع قصيرة","छोटे हिस्सों में याद करना"),
        listOf("Choose a manageable passage. Listen to a reliable reciter, read while looking, repeat several times, then recite from memory only when the words are stable. Let a teacher listen before moving to the next portion.","قابلِ برداشت حصہ منتخب کریں۔ معتبر قاری کو سنیں، دیکھ کر پڑھیں، کئی بار دہرائیں، پھر الفاظ مضبوط ہونے پر زبانی پڑھیں۔ اگلا حصہ شروع کرنے سے پہلے استاد کو سنائیں۔","اختر مقطعاً مناسباً. استمع إلى قارئ موثوق، واقرأ بالنظر وكرّر، ثم اقرأ غيباً بعد ثبات الكلمات. اعرضه على المعلّم قبل الانتقال.","छोटा हिस्सा चुनें। विश्वसनीय क़ारी को सुनें, देखकर पढ़ें, दोहराएँ और शब्द पक्के होने पर याद से पढ़ें। आगे बढ़ने से पहले शिक्षक को सुनाएँ।"),
        "استماع  ←  نظر سے پڑھنا  ←  دہرائی  ←  استاد کو سنانا",
        listOf("Memorise a few lines and recite them to your teacher without looking.","چند سطریں یاد کریں اور استاد کو دیکھے بغیر سنائیں۔","احفظ بضعة أسطر ثم اقرأها على المعلّم دون نظر.","कुछ पंक्तियाँ याद करें और शिक्षक को बिना देखे सुनाएँ۔"),"Quran hifz memorization method teacher"),
    QuranCourseLesson("i2",9,listOf("Revision and retention","دہرائی اور یادداشت مضبوط کرنا","المراجعة وتثبيت الحفظ","दोहराई और याद पक्का करना"),
        listOf("Review new memorisation daily and older portions on a planned cycle. Keep the workload realistic. A simple routine is new portion, recent revision and older revision; adjust it with your teacher.","نئے حفظ کو روزانہ دہرائیں اور پرانے حصوں کی مقررہ ترتیب سے دہرائی کریں۔ مقدار قابلِ عمل رکھیں۔ سادہ معمول: نیا سبق، قریبی دہرائی اور پرانا سبق؛ استاد سے مشورہ کریں۔","راجع الحفظ الجديد يومياً والمقاطع القديمة وفق جدول. اجعل المقدار واقعياً. برنامج بسيط: جديد، مراجعة قريبة، ومراجعة قديمة؛ واضبطه مع معلّمك.","नया याद किया रोज़ दोहराएँ और पुराने हिस्सों की योजना बनाएँ। मात्रा व्यावहारिक रखें। क्रम: नया हिस्सा, हाल की दोहराई, पुराना हिस्सा; शिक्षक से सलाह लें।"),
        "جديد  +  قريب  +  قديم",
        listOf("Create a weekly revision plan and recite an older portion to a teacher or trusted listener.","ہفتہ وار دہرائی کا منصوبہ بنائیں اور پرانا حصہ استاد یا قابلِ اعتماد سامع کو سنائیں۔","ضع خطة مراجعة أسبوعية واعرض مقطعاً قديماً على معلّم أو مستمع موثوق.","साप्ताहिक दोहराई योजना बनाएँ और पुराना हिस्सा शिक्षक या विश्वसनीय श्रोता को सुनाएँ।"),"Quran hifz revision schedule"),
    QuranCourseLesson("i3",9,listOf("Completion and your next step","کورس کی تکمیل اور اگلا قدم","إتمام الدورة والخطوة التالية","कोर्स पूरा और अगला कदम"),
        listOf("You have studied the foundations of reading and key Tajweed topics. This is a structured study aid, not a certificate, ijazah or substitute for a qualified Quran teacher. Continue with a teacher who can assess your recitation.","آپ نے قرأت کی بنیادیں اور تجوید کے اہم موضوعات پڑھے ہیں۔ یہ مطالعے کی رہنمائی ہے، سند، اجازت یا مستند قرآن استاد کا متبادل نہیں۔ ایسے استاد کے ساتھ جاری رکھیں جو آپ کی قرأت کی جانچ کر سکے۔","درست أساسيات القراءة وموضوعات مهمة في التجويد. هذه وسيلة تعليمية وليست شهادة أو إجازة ولا بديلاً عن معلّم مؤهل. واصل مع معلّم يقيّم تلاوتك.","आपने पढ़ने की बुनियाद और तजवीद के मुख्य विषय पढ़े हैं। यह अध्ययन सहायता है, प्रमाणपत्र, इजाज़त या योग्य शिक्षक का विकल्प नहीं। ऐसे शिक्षक के साथ जारी रखें जो तिलावत जाँच सके।"),
        "تعلّم  •  استمع  •  اقرأ  •  صحّح  •  راجع",
        listOf("Arrange a regular recitation check with a qualified Quran teacher.","مستند قرآن استاد سے باقاعدہ قرأت کی جانچ طے کریں۔","رتّب تقييماً منتظماً لتلاوتك مع معلّم قرآن مؤهل.","योग्य कुरआन शिक्षक से नियमित तिलावत जाँच तय करें।"),"qualified Quran teacher tajweed recitation learning")
)

@Composable
internal fun QuranLearningScreen(appLanguage: AppLanguage) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("noor_quran_learning", android.content.Context.MODE_PRIVATE) }
    var completed by remember {
        mutableStateOf(prefs.getStringSet("completed_lessons_v1", emptySet())?.toSet() ?: emptySet())
    }
    var selectedId by remember { mutableStateOf<String?>(null) }
    val selected = quranCourseLessons.firstOrNull { it.id == selectedId }
    val bg = LocalAppTheme.current.background
    val surface = LocalAppTheme.current.surface
    val primary = LocalAppTheme.current.primary
    val foreground = LocalAppTheme.current.textPrimary
    val muted = LocalAppTheme.current.textSecondary
    val total = quranCourseLessons.size
    val done = completed.size.coerceAtMost(total)

    Column(Modifier.fillMaxSize().background(bg).padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        if (selected == null) {
            Text(ql(listOf("QURAN LEARNING COURSE","قرآن سیکھنے کا کورس","دورة تعلم القرآن","क़ुरआन सीखने का कोर्स"), appLanguage), color = primary, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold)
            Text(ql(listOf("Learn Quran step by step","قرآن مرحلہ وار سیکھیں","تعلّم القرآن خطوة بخطوة","क़ुरआन चरणबद्ध सीखें"), appLanguage), color = foreground, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            Text(ql(listOf("From Arabic letters to Tajweed, fluent recitation and Hifz revision.","عربی حروف سے تجوید، روانی اور حفظ کی دہرائی تک۔","من الحروف العربية إلى التجويد وطلاقة التلاوة ومراجعة الحفظ.","अरबी अक्षरों से तजवीद, सहज तिलावत और हिफ़्ज़ की दोहराई तक।"), appLanguage), color = muted, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 5.dp, bottom = 12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = surface), border = BorderStroke(1.dp, primary.copy(alpha = .4f)), shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(ql(listOf("Your progress","آپ کی پیش رفت","تقدمك","आपकी प्रगति"), appLanguage), color = foreground, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        Text(done.toString() + " / " + total, color = primary, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(progress = done.toFloat() / total, modifier = Modifier.fillMaxWidth().padding(top = 10.dp), color = primary)
                    Text(ql(listOf("Progress is saved on this device.","پیش رفت اسی ڈیوائس میں محفوظ ہوتی ہے۔","يُحفظ تقدمك على هذا الجهاز.","प्रगति इसी डिवाइस पर सेव होती है।"), appLanguage), color = muted, fontSize = 10.sp, modifier = Modifier.padding(top = 7.dp))
                }
            }
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(top = 12.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(quranCourseModules) { module ->
                    val lessons = quranCourseLessons.filter { it.module == module.number }
                    val moduleDone = lessons.count { completed.contains(it.id) }
                    Card(colors = CardDefaults.cardColors(containerColor = surface), border = BorderStroke(1.dp, LocalAppTheme.current.border), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.padding(13.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(module.number.toString().padStart(2, '0'), color = primary, fontWeight = FontWeight.Bold)
                                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                                    Text(ql(module.title, appLanguage), color = foreground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(ql(module.summary, appLanguage), color = muted, fontSize = 11.sp)
                                }
                                Text(moduleDone.toString() + "/" + lessons.size, color = primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            lessons.forEach { lesson ->
                                Row(Modifier.fillMaxWidth().clickable { selectedId = lesson.id }.padding(vertical = 9.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(if (completed.contains(lesson.id)) Icons.Default.CheckCircle else Icons.Default.PlayCircle, contentDescription = null, tint = if (completed.contains(lesson.id)) primary else muted, modifier = Modifier.size(20.dp))
                                    Text(ql(lesson.title, appLanguage), color = foreground, fontSize = 13.sp, modifier = Modifier.weight(1f).padding(horizontal = 10.dp))
                                    Text("›", color = muted, fontSize = 20.sp)
                                }
                                if (lesson != lessons.last()) HorizontalDivider(color = LocalAppTheme.current.border.copy(alpha = .5f))
                            }
                        }
                    }
                }
                item {
                    Text(ql(listOf("Important: Text supports study, but correct Quran pronunciation requires listening to and reciting with a qualified teacher. This course does not issue an ijazah or certify mastery.","اہم: تحریری اسباق مطالعے میں مدد دیتے ہیں، مگر درست قرآنی تلفظ کے لیے مستند استاد سے سننا اور پڑھنا ضروری ہے۔ یہ کورس اجازت یا مہارت کی سند نہیں دیتا۔","تنبيه: الدروس النصية تعين على الدراسة، لكن صحة النطق تحتاج إلى التلقي عن معلّم مؤهل. هذه الدورة لا تمنح إجازة ولا شهادة إتقان.","महत्वपूर्ण: लिखित पाठ पढ़ाई में मदद करते हैं, लेकिन सही उच्चारण के लिए योग्य शिक्षक से सुनना और पढ़ना ज़रूरी है। यह कोर्स इजाज़त या महारत का प्रमाणपत्र नहीं देता।"), appLanguage), color = muted, fontSize = 10.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedId = null }) { Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = primary) }
                Column(Modifier.weight(1f)) {
                    Text(ql(quranCourseModules.first { it.number == selected.module }.title, appLanguage), color = primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text(ql(selected.title, appLanguage), color = foreground, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                if (completed.contains(selected.id)) Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = primary)
            }
            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(top = 8.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text(ql(selected.body, appLanguage), color = foreground, fontSize = 14.sp, lineHeight = 22.sp) }
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = surface), border = BorderStroke(1.dp, primary.copy(alpha = .35f)), shape = RoundedCornerShape(14.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(ql(listOf("ARABIC EXAMPLE","عربی مثال","مثال عربي","अरबी उदाहरण"), appLanguage), color = primary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(selected.example, color = foreground, fontSize = 24.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                        }
                    }
                }
                item {
                    Text(ql(listOf("PRACTISE","مشق کریں","تدرّب","अभ्यास करें"), appLanguage), color = primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(ql(selected.practice, appLanguage), color = foreground, fontSize = 13.sp, lineHeight = 21.sp, modifier = Modifier.padding(top = 4.dp))
                }
                item {
                    OutlinedButton(onClick = {
                        val url = "https://www.youtube.com/results?search_query=" + Uri.encode(selected.searchQuery)
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors(contentColor = primary), border = BorderStroke(1.dp, primary)) {
                        Icon(Icons.Default.PlayCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(ql(listOf("Find a teacher demonstration","استاد کا عملی سبق تلاش کریں","ابحث عن شرح من معلّم","शिक्षक का प्रदर्शन खोजें"), appLanguage))
                    }
                    Text(ql(listOf("Choose a qualified Quran teacher/reciter and verify the lesson before practising.","مستند قرآن استاد یا قاری منتخب کریں اور مشق سے پہلے سبق کی تصدیق کریں۔","اختر معلّماً أو قارئاً مؤهلاً وتحقق من الدرس قبل التطبيق.","योग्य कुरआन शिक्षक/क़ारी चुनें और अभ्यास से पहले पाठ की पुष्टि करें।"), appLanguage), color = muted, fontSize = 10.sp, lineHeight = 15.sp)
                }
            }
            Button(onClick = {
                val updated = if (completed.contains(selected.id)) completed - selected.id else completed + selected.id
                completed = updated
                prefs.edit().putStringSet("completed_lessons_v1", updated.toMutableSet()).apply()
                if (updated.contains(selected.id)) {
                    val next = quranCourseLessons.dropWhile { it.id != selected.id }.drop(1).firstOrNull { !updated.contains(it.id) }
                    selectedId = next?.id
                }
            }, modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), colors = ButtonDefaults.buttonColors(containerColor = primary, contentColor = bg)) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (completed.contains(selected.id)) ql(listOf("Mark as not completed","نامکمل نشان زد کریں","إلغاء الإكمال","अधूरा चिह्नित करें"), appLanguage) else ql(listOf("Mark lesson complete","سبق مکمل کریں","أكملت الدرس","पाठ पूरा करें"), appLanguage), fontWeight = FontWeight.Bold)
            }
        }
    }
}
