package com.zamcan.nisaacare.domain.health

import com.zamcan.nisaacare.domain.model.AppLanguage

enum class LearningVisual { CYCLE_RING, UTERUS_FLOW, HORMONE_RHYTHM, CARE_SIGNAL, LIFE_COURSE }

data class MenstrualLesson(
    val id: String,
    val topic: HealthTopic,
    val title: Map<AppLanguage, String>,
    val body: Map<AppLanguage, String>,
    val takeaways: List<Map<AppLanguage, String>>,
    val sourceIds: List<String>,
    val visual: LearningVisual
) {
    fun titleFor(language: AppLanguage): String = title[language] ?: title[AppLanguage.ENGLISH].orEmpty()
    fun bodyFor(language: AppLanguage): String = body[language] ?: body[AppLanguage.ENGLISH].orEmpty()
    fun takeawaysFor(language: AppLanguage): List<String> = takeaways.mapNotNull { it[language] ?: it[AppLanguage.ENGLISH] }
}

object MenstrualLearningCatalog {
    val lessons: List<MenstrualLesson> = listOf(
        MenstrualLesson("cycle_map", HealthTopic.MENSTRUAL_CYCLE,
            mapOf(AppLanguage.ENGLISH to "The cycle is a living rhythm, not a fixed 28-day clock.", AppLanguage.SWAHILI to "Mzunguko ni mpangilio hai wa mwili, si saa iliyofungwa kwenye siku 28.", AppLanguage.ARABIC to "الدورة إيقاع حيوي متغير، وليست ساعة ثابتة من 28 يوماً."),
            mapOf(AppLanguage.ENGLISH to "Day 1 is the first day of menstrual bleeding. The cycle continues until the day before the next period. WHO describes an average range of about 21–35 days. Ovulation does not have to happen on one universal day, so NISAA CARE treats predictions as estimates built from the woman’s own history.",
                AppLanguage.SWAHILI to "Siku ya 1 ni siku ya kwanza ya damu ya hedhi. Mzunguko unaendelea hadi siku kabla ya hedhi inayofuata. WHO inaeleza wastani wa takribani siku 21–35. Ovulation haina siku moja ya lazima kwa kila mwanamke, hivyo NISAA CARE huonyesha makadirio yanayotokana na historia yako.",
                AppLanguage.ARABIC to "اليوم الأول هو أول يوم لنزول دم الحيض، وتنتهي الدورة في اليوم السابق للحيض التالي. تذكر منظمة الصحة العالمية أن المدى المتوسط يقارب 21–35 يوماً. ولا تحدث الإباضة في يوم ثابت عند جميع النساء، لذلك يعرض NISAA CARE تقديراً مبنياً على سجل الدورة."),
            listOf(
                mapOf(AppLanguage.ENGLISH to "Record the first day of bleeding consistently.", AppLanguage.SWAHILI to "Rekodi siku ya kwanza ya damu kwa uthabiti.", AppLanguage.ARABIC to "سجّلي أول يوم للنزف باستمرار."),
                mapOf(AppLanguage.ENGLISH to "More recorded cycles make the personal pattern more useful.", AppLanguage.SWAHILI to "Mizunguko mingi iliyorekodiwa hufanya mwelekeo binafsi uwe na maana zaidi.", AppLanguage.ARABIC to "كلما زادت الدورات المسجلة أصبح النمط الشخصي أكثر فائدة."),
                mapOf(AppLanguage.ENGLISH to "A prediction is not a diagnosis or guaranteed contraception.", AppLanguage.SWAHILI to "Makadirio si utambuzi wa ugonjwa wala kinga ya mimba iliyohakikishwa.", AppLanguage.ARABIC to "التقدير ليس تشخيصاً ولا وسيلة مضمونة لمنع الحمل.")
            ), listOf("WHO_MENSTRUAL_HEALTH_2026", "ACOG_ABNORMAL_UTERINE_BLEEDING"), LearningVisual.CYCLE_RING),
        MenstrualLesson("what_is_period", HealthTopic.MENSTRUATION,
            mapOf(AppLanguage.ENGLISH to "What is menstrual blood?", AppLanguage.SWAHILI to "Damu ya hedhi ni nini?", AppLanguage.ARABIC to "ما هو دم الحيض؟"),
            mapOf(AppLanguage.ENGLISH to "A period is the shedding of the uterine lining when pregnancy has not occurred. Menstrual fluid contains blood, tissue and mucus. It is a normal biological process, not a sign that the body is dirty.",
                AppLanguage.SWAHILI to "Hedhi ni kuondoka kwa tabaka la ndani la mfuko wa uzazi wakati mimba haijatokea. Majimaji ya hedhi yana damu, tishu na ute. Ni mchakato wa kawaida wa kibaolojia, si ishara kwamba mwili ni mchafu.",
                AppLanguage.ARABIC to "الحيض هو تساقط بطانة الرحم عندما لا يحدث حمل. ويحتوي سائل الحيض على دم وأنسجة ومخاط. وهو عملية بيولوجية طبيعية، وليس دليلاً على أن الجسد قذر."),
            listOf(
                mapOf(AppLanguage.ENGLISH to "The uterus prepares a lining and later sheds it.", AppLanguage.SWAHILI to "Mfuko wa uzazi huandaa tabaka kisha huliondoa.", AppLanguage.ARABIC to "يُهيئ الرحم بطانته ثم يطرحها."),
                mapOf(AppLanguage.ENGLISH to "Colour alone does not diagnose a problem.", AppLanguage.SWAHILI to "Rangi pekee haitambui ugonjwa.", AppLanguage.ARABIC to "اللون وحده لا يشخّص مشكلة.")
            ), listOf("WHO_MENSTRUAL_HEALTH_2026"), LearningVisual.UTERUS_FLOW),
        MenstrualLesson("hormone_choreography", HealthTopic.OVULATION,
            mapOf(AppLanguage.ENGLISH to "Hormones choreograph the cycle.", AppLanguage.SWAHILI to "Homoni huongoza mdundo wa mzunguko.", AppLanguage.ARABIC to "الهرمونات تنظم إيقاع الدورة."),
            mapOf(AppLanguage.ENGLISH to "The brain, pituitary gland, ovaries and uterus communicate continuously. FSH and LH help coordinate ovarian activity; estrogen and progesterone help prepare and maintain the uterine lining. Around ovulation, an egg is released from an ovary. The timing varies between people and cycles.",
                AppLanguage.SWAHILI to "Ubongo, tezi ya pituitari, ovari na mfuko wa uzazi huwasiliana kwa mfululizo. FSH na LH husaidia kuratibu shughuli za ovari; estrogen na progesterone husaidia kuandaa na kudumisha tabaka la mfuko wa uzazi. Karibu na ovulation, yai hutolewa kwenye ovari. Muda wake hutofautiana kati ya wanawake na mizunguko.",
                AppLanguage.ARABIC to "يتواصل الدماغ والغدة النخامية والمبايض والرحم باستمرار. يساعد FSH وLH في تنظيم نشاط المبيض، ويساعد الإستروجين والبروجسترون في إعداد بطانة الرحم والمحافظة عليها. وقرب الإباضة تخرج بويضة من أحد المبيضين. ويختلف توقيتها بين النساء وبين الدورات."),
            listOf(
                mapOf(AppLanguage.ENGLISH to "Biology is dynamic; an average pattern is not a rule for every woman.", AppLanguage.SWAHILI to "Biolojia hubadilika; wastani si sheria kwa kila mwanamke.", AppLanguage.ARABIC to "البيولوجيا متغيرة؛ المتوسط ليس قاعدة لكل امرأة."),
                mapOf(AppLanguage.ENGLISH to "Observed dates and estimated fertile days should remain visually distinct.", AppLanguage.SWAHILI to "Tarehe zilizorekodiwa na siku za uzazi zilizokadiriwa zitenganishwe wazi.", AppLanguage.ARABIC to "يجب تمييز التواريخ المسجلة عن أيام الخصوبة المقدرة بصرياً.")
            ), listOf("WHO_MENSTRUAL_HEALTH_2026", "WHO_REPRODUCTIVE_PHYSIOLOGY_2014"), LearningVisual.HORMONE_RHYTHM),
        MenstrualLesson("pain_as_signal", HealthTopic.PAIN,
            mapOf(AppLanguage.ENGLISH to "Pain deserves to be heard.", AppLanguage.SWAHILI to "Maumivu yanastahili kusikilizwa.", AppLanguage.ARABIC to "الألم يستحق أن يُسمع."),
            mapOf(AppLanguage.ENGLISH to "Cramps can happen because the uterus contracts and prostaglandins contribute to those contractions. Mild, short-lived pain is common, but severe pain, pain that worsens over time, pain outside the period, or pain that disrupts life deserves clinical attention.",
                AppLanguage.SWAHILI to "Maumivu ya tumbo yanaweza kutokea kwa sababu mfuko wa uzazi hukaza misuli na prostaglandins huchangia mikazo hiyo. Maumivu madogo ya muda mfupi ni ya kawaida, lakini maumivu makali, yanayoongezeka, yanayotokea nje ya hedhi, au yanayovuruga maisha yanahitaji kuzungumzwa na mtaalamu wa afya.",
                AppLanguage.ARABIC to "قد تحدث التقلصات لأن الرحم ينقبض وتساهم البروستاغلاندينات في هذه الانقباضات. الألم الخفيف القصير شائع، لكن الألم الشديد أو المتزايد أو الذي يحدث خارج الحيض أو يعطل الحياة اليومية يستحق تقييماً صحياً."),
            listOf(
                mapOf(AppLanguage.ENGLISH to "Track pain intensity, timing and impact—not just its presence.", AppLanguage.SWAHILI to "Rekodi kiwango, muda na athari za maumivu—si kuwepo kwake tu.", AppLanguage.ARABIC to "سجّلي شدة الألم ووقته وتأثيره، وليس وجوده فقط."),
                mapOf(AppLanguage.ENGLISH to "Heat, rest, sleep and regular movement may help some people.", AppLanguage.SWAHILI to "Joto, mapumziko, usingizi na mwendo wa kawaida vinaweza kusaidia baadhi ya watu.", AppLanguage.ARABIC to "قد تساعد الحرارة والراحة والنوم والحركة المنتظمة بعض النساء.")
            ), listOf("ACOG_PAINFUL_PERIODS"), LearningVisual.CARE_SIGNAL),
        MenstrualLesson("bleeding_pattern", HealthTopic.FLOW,
            mapOf(AppLanguage.ENGLISH to "Bleeding patterns are useful health information.", AppLanguage.SWAHILI to "Mwelekeo wa damu ni taarifa muhimu ya afya.", AppLanguage.ARABIC to "أنماط النزف معلومات صحية مهمة."),
            mapOf(AppLanguage.ENGLISH to "A period generally lasts up to about 7 days. ACOG lists bleeding that soaks a pad or tampon every hour for several hours, bleeding longer than 7 days, bleeding between periods, or cycles consistently outside expected ranges as reasons to discuss care. Sudden very heavy bleeding with dizziness, shortness of breath or chest pain can require urgent care.",
                AppLanguage.SWAHILI to "Kwa ujumla hedhi inaweza kudumu hadi takribani siku 7. ACOG hutaja damu inayolowesha pedi au tamponi kila saa kwa saa kadhaa, damu inayodumu zaidi ya siku 7, damu kati ya hedhi, au mizunguko iliyo nje ya viwango vinavyotarajiwa kama sababu za kuzungumza na mtaalamu. Damu nyingi ghafla pamoja na kizunguzungu, upungufu wa pumzi au maumivu ya kifua inaweza kuhitaji huduma ya haraka.",
                AppLanguage.ARABIC to "تستمر الدورة الشهرية عموماً حتى نحو 7 أيام. وتذكر ACOG أن النزف الذي يبلل الفوطة أو السدادة كل ساعة لعدة ساعات، أو يستمر أكثر من 7 أيام، أو يحدث بين الدورات، أو تتكرر فيه دورات خارج المدى المتوقع، يستحق مناقشة الرعاية الصحية. أما النزف الشديد المفاجئ مع الدوخة أو ضيق التنفس أو ألم الصدر فقد يحتاج إلى رعاية عاجلة."),
            listOf(
                mapOf(AppLanguage.ENGLISH to "The app records observations; it does not diagnose the cause.", AppLanguage.SWAHILI to "Programu inarekodi unachokiona; haitambui chanzo cha ugonjwa.", AppLanguage.ARABIC to "يسجل التطبيق الملاحظات ولا يشخّص السبب."),
                mapOf(AppLanguage.ENGLISH to "Repeated patterns across cycles are more informative than one isolated day.", AppLanguage.SWAHILI to "Mwelekeo unaojirudia katika mizunguko una taarifa zaidi kuliko siku moja pekee.", AppLanguage.ARABIC to "النمط المتكرر عبر الدورات أكثر فائدة من يوم واحد منفرد.")
            ), listOf("ACOG_ABNORMAL_UTERINE_BLEEDING", "WHO_MENSTRUAL_HEALTH_2026"), LearningVisual.CARE_SIGNAL),
        MenstrualLesson("conditions_awareness", HealthTopic.SEEK_PROFESSIONAL_CARE,
            mapOf(AppLanguage.ENGLISH to "Know the patterns that deserve a closer look.", AppLanguage.SWAHILI to "Tambua mwelekeo unaostahili kuchunguzwa zaidi.", AppLanguage.ARABIC to "اعرفي الأنماط التي تستحق فحصاً أعمق."),
            mapOf(AppLanguage.ENGLISH to "Persistent severe pain, very heavy bleeding, absent or infrequent periods, or major changes from your usual pattern can have many causes. WHO highlights endometriosis and PCOS among possible explanations, but symptoms alone do not establish a diagnosis. NISAA CARE should help you describe your pattern clearly to a qualified clinician.",
                AppLanguage.SWAHILI to "Maumivu makali yanayoendelea, damu nyingi sana, kukosa hedhi au hedhi zisizo za mara kwa mara, au mabadiliko makubwa kutoka kwenye kawaida yako yanaweza kuwa na sababu nyingi. WHO inaangazia endometriosis na PCOS miongoni mwa sababu zinazowezekana, lakini dalili pekee hazithibitishi utambuzi. NISAA CARE inapaswa kukusaidia kueleza mwelekeo wako kwa mtaalamu mwenye sifa.",
                AppLanguage.ARABIC to "قد تكون الآلام الشديدة المستمرة أو النزف الغزير جداً أو غياب الحيض أو عدم انتظامه أو التغيرات الكبيرة عن نمطك المعتاد ذات أسباب متعددة. وتذكر منظمة الصحة العالمية حالات مثل بطانة الرحم المهاجرة وتكيس المبايض ضمن الاحتمالات، لكن الأعراض وحدها لا تثبت التشخيص. يساعدك NISAA CARE على وصف نمطك بوضوح لمختص مؤهل."),
            listOf(
                mapOf(AppLanguage.ENGLISH to "Endometriosis can cause severe period pain, heavy bleeding and chronic pelvic pain.", AppLanguage.SWAHILI to "Endometriosis inaweza kusababisha maumivu makali ya hedhi, damu nyingi na maumivu sugu ya nyonga.", AppLanguage.ARABIC to "قد تسبب بطانة الرحم المهاجرة ألماً شديداً أثناء الحيض ونزفاً غزيراً وألماً مزمناً في الحوض."),
                mapOf(AppLanguage.ENGLISH to "PCOS can involve irregular periods and abnormal ovulation, but diagnosis requires clinical assessment.", AppLanguage.SWAHILI to "PCOS inaweza kuhusisha hedhi zisizo za kawaida na ovulation isiyo ya kawaida, lakini utambuzi unahitaji tathmini ya kitabibu.", AppLanguage.ARABIC to "قد يرتبط تكيس المبايض بعدم انتظام الحيض واضطراب الإباضة، لكن التشخيص يحتاج إلى تقييم طبي.")
            ), listOf("WHO_ENDOMETRIOSIS_2025", "WHO_PCOS_2026"), LearningVisual.CARE_SIGNAL),
        MenstrualLesson("care_routine", HealthTopic.SELF_CARE,
            mapOf(AppLanguage.ENGLISH to "Care is part of menstrual health.", AppLanguage.SWAHILI to "Huduma binafsi ni sehemu ya afya ya hedhi.", AppLanguage.ARABIC to "العناية بالنفس جزء من الصحة أثناء الحيض."),
            mapOf(AppLanguage.ENGLISH to "Good menstrual care combines clean, practical period management with rest, sleep, movement, hydration and access to care. Choose menstrual materials that are safe and affordable for you, keep hands and reusable materials clean, and seek help when symptoms interfere with normal life.",
                AppLanguage.SWAHILI to "Huduma nzuri ya hedhi huunganisha usimamizi safi na wa vitendo wa damu pamoja na mapumziko, usingizi, mwendo, maji na upatikanaji wa huduma. Tumia vifaa vya hedhi vilivyo salama na vinavyoweza kumudu, weka mikono na vifaa vinavyotumika tena katika usafi, na tafuta msaada dalili zinapovuruga maisha ya kawaida.",
                AppLanguage.ARABIC to "تجمع العناية الجيدة بالحيض بين إدارة النزف بطريقة نظيفة وعملية وبين الراحة والنوم والحركة وشرب السوائل والوصول إلى الرعاية. اختاري وسائل حيض آمنة وميسورة، وحافظي على نظافة اليدين والوسائل القابلة لإعادة الاستخدام، واطلبي المساعدة عندما تعطل الأعراض حياتك."),
            listOf(
                mapOf(AppLanguage.ENGLISH to "Menstrual health includes physical, mental and social well-being.", AppLanguage.SWAHILI to "Afya ya hedhi inahusisha ustawi wa mwili, akili na jamii.", AppLanguage.ARABIC to "تشمل صحة الحيض الرفاه الجسدي والنفسي والاجتماعي."),
                mapOf(AppLanguage.ENGLISH to "Support and dignity are health-care actions too.", AppLanguage.SWAHILI to "Msaada na heshima pia ni sehemu ya huduma ya afya.", AppLanguage.ARABIC to "الدعم والكرامة أيضاً من أفعال الرعاية الصحية.")
            ), listOf("WHO_MENSTRUAL_HEALTH_2026", "WHO_SELF_CARE_2026"), LearningVisual.CARE_SIGNAL),
        MenstrualLesson("life_course", HealthTopic.MENOPAUSE,
            mapOf(AppLanguage.ENGLISH to "The menstrual story changes across a lifetime.", AppLanguage.SWAHILI to "Hadithi ya hedhi hubadilika katika maisha yote.", AppLanguage.ARABIC to "تتغير رحلة الحيض عبر مراحل العمر."),
            mapOf(AppLanguage.ENGLISH to "Menstrual patterns can change around the first years after menarche, during the reproductive years and again during perimenopause. WHO describes natural menopause as a point after 12 consecutive months without menstruation when no other obvious cause is present; most women experience it between about 45 and 55. Individual timing varies.",
                AppLanguage.SWAHILI to "Mwelekeo wa hedhi unaweza kubadilika katika miaka ya mwanzo baada ya kuanza hedhi, katika miaka ya uzazi na tena wakati wa perimenopause. WHO inaeleza menopause ya kawaida baada ya miezi 12 mfululizo bila hedhi bila sababu nyingine iliyo wazi; wanawake wengi hupitia kati ya takribani miaka 45–55. Muda hutofautiana kwa mtu.",
                AppLanguage.ARABIC to "قد تتغير أنماط الحيض في السنوات الأولى بعد بدء الحيض وخلال سنوات الإنجاب ثم مرة أخرى في مرحلة ما حول سن اليأس. وتعرّف منظمة الصحة العالمية انقطاع الطمث الطبيعي بأنه مرور 12 شهراً متتالياً دون حيض مع عدم وجود سبب آخر واضح؛ وتحدث هذه المرحلة لدى معظم النساء بين نحو 45 و55 عاماً، مع اختلاف التوقيت فردياً."),
            listOf(
                mapOf(AppLanguage.ENGLISH to "A changing pattern is not automatically a disease.", AppLanguage.SWAHILI to "Mabadiliko ya mwelekeo si lazima yawe ugonjwa.", AppLanguage.ARABIC to "تغير النمط لا يعني تلقائياً وجود مرض."),
                mapOf(AppLanguage.ENGLISH to "Life-stage changes should be interpreted with age and context.", AppLanguage.SWAHILI to "Mabadiliko ya hatua ya maisha yatafsiriwe kwa kuzingatia umri na mazingira.", AppLanguage.ARABIC to "ينبغي تفسير تغيرات مراحل الحياة مع مراعاة العمر والسياق.")
            ), listOf("WHO_MENSTRUAL_HEALTH_2026", "WHO_MENOPAUSE_2024"), LearningVisual.LIFE_COURSE)
    )
}
