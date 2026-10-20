package com.zamcan.nisaacare.domain.islamic

import com.zamcan.nisaacare.domain.model.AppLanguage

data class MenstruationFaithLesson(
    val id: String,
    val title: Map<AppLanguage, String>,
    val body: Map<AppLanguage, String>,
    val source: String,
    val reference: String
) {
    fun titleFor(language: AppLanguage) = title[language] ?: title[AppLanguage.ENGLISH].orEmpty()
    fun bodyFor(language: AppLanguage) = body[language] ?: body[AppLanguage.ENGLISH].orEmpty()
}

object MenstruationFaithCatalog {
    val lessons = listOf(
        MenstruationFaithLesson(
            "quran_222",
            mapOf(AppLanguage.ENGLISH to "Menstruation in the Qur’an", AppLanguage.SWAHILI to "Hedhi katika Qur’ani", AppLanguage.ARABIC to "الحيض في القرآن"),
            mapOf(
                AppLanguage.ENGLISH to "Qur’an 2:222 addresses menstruation directly. The verse gives a boundary around intercourse during menstruation and then speaks about purification. NISAA CARE presents the verse as a source of guidance, not as a reason to treat a menstruating woman as socially isolated or lesser.",
                AppLanguage.SWAHILI to "Qur’ani 2:222 inazungumzia hedhi moja kwa moja. Aya inaweka mpaka kuhusu kujamiiana wakati wa hedhi na kisha inazungumzia kujitakasa. NISAA CARE huiwasilisha kama chanzo cha mwongozo, si sababu ya kumtenga mwanamke kijamii au kumdhalilisha.",
                AppLanguage.ARABIC to "تتناول الآية 222 من سورة البقرة الحيض مباشرة. وتضع الآية حكماً يتعلق بالجماع أثناء الحيض ثم تذكر التطهر. ويعرض NISAA CARE الآية كمصدر للهداية، لا سبباً لعزل المرأة أو الانتقاص منها اجتماعياً."
            ),
            "QURAN",
            "Al-Baqarah 2:222"
        ),
        MenstruationFaithLesson(
            "mercy_not_isolation",
            mapOf(AppLanguage.ENGLISH to "Care does not mean isolation", AppLanguage.SWAHILI to "Huduma si kumtenga", AppLanguage.ARABIC to "الرعاية لا تعني العزل"),
            mapOf(
                AppLanguage.ENGLISH to "Sahih Muslim 302 records that the Prophet ﷺ instructed husbands to continue ordinary association with menstruating wives while observing the boundary concerning intercourse. The app turns this into a practical principle: dignity, companionship and kindness remain part of family life.",
                AppLanguage.SWAHILI to "Sahih Muslim 302 inasimulia kuwa Mtume ﷺ aliwaelekeza waume kuendelea na mahusiano ya kawaida na wake walio kwenye hedhi huku wakizingatia mpaka wa kujamiiana. Programu hugeuza hili kuwa kanuni ya vitendo: heshima, urafiki na wema vinaendelea kuwa sehemu ya maisha ya familia.",
                AppLanguage.ARABIC to "يروي صحيح مسلم 302 أن النبي ﷺ وجّه الأزواج إلى استمرار المعاشرة العادية مع الزوجة الحائض مع الالتزام بحد الجماع. ويحوّل التطبيق ذلك إلى مبدأ عملي: تبقى الكرامة والرفقة والرحمة جزءاً من الحياة الأسرية."
            ),
            "HADITH",
            "Sahih Muslim 302"
        ),
        MenstruationFaithLesson(
            "fasting_prayer",
            mapOf(AppLanguage.ENGLISH to "Fasting and prayer: a clear reported distinction", AppLanguage.SWAHILI to "Saumu na swala: tofauti iliyoripotiwa wazi", AppLanguage.ARABIC to "الصيام والصلاة: فرق ثابت في الرواية"),
            mapOf(
                AppLanguage.ENGLISH to "Aisha reported that menstruating women during the Prophet’s time were instructed to make up missed fasts but were not instructed to make up missed prayers. This is a foundational fiqh teaching; detailed questions can still require madhhab-aware scholarly guidance.",
                AppLanguage.SWAHILI to "Aisha aliripoti kuwa wanawake waliokuwa kwenye hedhi wakati wa Mtume waliagizwa kulipa saumu walizokosa lakini hawakuagizwa kulipa swala walizokosa. Huu ni msingi wa fiqhi; maswali ya kina yanaweza bado kuhitaji mwongozo wa mwanazuoni unaozingatia madhehebu.",
                AppLanguage.ARABIC to "روت عائشة أن النساء في زمن النبي ﷺ كن يؤمرن بقضاء الصيام ولا يؤمرن بقضاء الصلاة. وهذا أصل فقهي مهم، بينما قد تحتاج التفاصيل الدقيقة إلى سؤال عالم يراعي المذهب."
            ),
            "HADITH",
            "Sahih Muslim 335a; Jami‘ at-Tirmidhi 787"
        ),
        MenstruationFaithLesson(
            "purification",
            mapOf(AppLanguage.ENGLISH to "Purification is a practical transition", AppLanguage.SWAHILI to "Kutwaharika ni hatua ya vitendo", AppLanguage.ARABIC to "الطُّهر انتقال عملي"),
            mapOf(
                AppLanguage.ENGLISH to "Reports about menstrual blood on clothing teach practical cleaning rather than shame. Sahih al-Bukhari 307 records guidance to remove and wash menstrual blood from clothing before prayer. NISAA CARE keeps bodily care, cleanliness and dignity together.",
                AppLanguage.SWAHILI to "Riwaya kuhusu damu ya hedhi kwenye nguo zinafundisha usafi wa vitendo, si aibu. Sahih al-Bukhari 307 inasimulia mwongozo wa kuondoa na kuosha damu ya hedhi kwenye nguo kabla ya swala. NISAA CARE huunganisha usafi wa mwili na heshima.",
                AppLanguage.ARABIC to "تعلم الروايات المتعلقة بدم الحيض على الثوب التنظيف العملي لا الخجل. ويروي صحيح البخاري 307 توجيهاً لإزالة دم الحيض وغسل الثوب قبل الصلاة. ويجمع NISAA CARE بين العناية والنظافة والكرامة."
            ),
            "HADITH",
            "Sahih al-Bukhari 307"
        ),
        MenstruationFaithLesson(
            "fiqh_boundaries",
            mapOf(AppLanguage.ENGLISH to "Biology and fiqh are related, but not identical", AppLanguage.SWAHILI to "Biolojia na fiqhi vina uhusiano, lakini si kitu kimoja", AppLanguage.ARABIC to "البيولوجيا والفقه مرتبطان وليسا شيئاً واحداً"),
            mapOf(
                AppLanguage.ENGLISH to "A biological estimate cannot by itself decide a woman’s ritual status. Questions about the beginning or end of hayd, nifas, istihadah, ghusl, or unusual bleeding can depend on detailed fiqh rules and scholarly methodology. The app should show recorded biology separately and invite qualified religious guidance when a ruling is unclear.",
                AppLanguage.SWAHILI to "Makadirio ya kibaolojia hayawezi peke yake kuamua hali ya ibada ya mwanamke. Maswali kuhusu mwanzo au mwisho wa hayd, nifas, istihadah, ghusl au damu isiyo ya kawaida yanaweza kutegemea kanuni za kina za fiqhi na mfumo wa mwanazuoni. Programu itenganishe biolojia iliyorekodiwa na mwongozo wa kidini pale hukumu haiko wazi.",
                AppLanguage.ARABIC to "لا يمكن للتقدير البيولوجي وحده أن يحدد الحالة الشرعية للمرأة. وقد تعتمد مسائل بداية الحيض ونهايته والنفاس والاستحاضة والغسل والنزف غير المعتاد على قواعد فقهية تفصيلية ومنهج العلماء. لذلك يفصل التطبيق بين السجل البيولوجي والإرشاد الشرعي عند عدم وضوح الحكم."
            ),
            "FIQH",
            "Editorial boundary; consult qualified scholarship for individual rulings"
        )
    )
}
