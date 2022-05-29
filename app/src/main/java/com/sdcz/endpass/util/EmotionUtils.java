package com.sdcz.endpass.util;



import com.sdcz.endpass.R;

import java.util.LinkedHashMap;

/**
 * 功能：自定义表情编码 、图标
 */
public class EmotionUtils {

    private EmotionUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final LinkedHashMap<String, Integer> EMOTION_MAP;

    static {
        EMOTION_MAP = new LinkedHashMap<>();

        EMOTION_MAP.put("/:E6(weix)", R.mipmap.emoji_weix);
        EMOTION_MAP.put("/:E6(wosh)", R.mipmap.emoji_wosh);
        EMOTION_MAP.put("/:E7(qiang)", R.mipmap.emoji_qiang);
        EMOTION_MAP.put("/:E5(jus)", R.mipmap.emoji_jus);
        EMOTION_MAP.put("/:E5(guz)", R.mipmap.emoji_guz);
        EMOTION_MAP.put("/:E5(hua)", R.mipmap.emoji_hua);
        EMOTION_MAP.put("/:E4(ok)", R.mipmap.emoji_ok);
        EMOTION_MAP.put("/:E6(xind)", R.mipmap.emoji_xind);
        EMOTION_MAP.put("/:E6(xins)", R.mipmap.emoji_xins);
        EMOTION_MAP.put("/:E7(tiaop)", R.mipmap.emoji_tiaop);
        EMOTION_MAP.put("/:E5(dax)", R.mipmap.emoji_dax);
        EMOTION_MAP.put("/:E7(yiwen)", R.mipmap.emoji_yiwen);
        EMOTION_MAP.put("/:E4(no)", R.mipmap.emoji_no);
        EMOTION_MAP.put("/:E6(toux)", R.mipmap.emoji_toux);
        EMOTION_MAP.put("/:E4(se)", R.mipmap.emoji_se);
        EMOTION_MAP.put("/:E7(huaix)", R.mipmap.emoji_huaix);
        EMOTION_MAP.put("/:E5(dey)", R.mipmap.emoji_dey);
        EMOTION_MAP.put("/:E5(han)", R.mipmap.emoji_han);
        EMOTION_MAP.put("/:E4(ku)", R.mipmap.emoji_ku);
        EMOTION_MAP.put("/:E5(kum)", R.mipmap.emoji_kum);
        EMOTION_MAP.put("/:E6(zaij)", R.mipmap.emoji_zaij);
        EMOTION_MAP.put("/:E6(shqi)", R.mipmap.emoji_shqi);
        EMOTION_MAP.put("/:E6(nang)", R.mipmap.emoji_nang);
        EMOTION_MAP.put("/:E6(wuyu)", R.mipmap.emoji_wuyu);
        EMOTION_MAP.put("/:E7(shuai)", R.mipmap.emoji_shuai);
        EMOTION_MAP.put("/:E5(fad)", R.mipmap.emoji_fad);
        EMOTION_MAP.put("/:E5(kaf)", R.mipmap.emoji_kaf);
        EMOTION_MAP.put("/:E5(fan)", R.mipmap.emoji_fan);
        EMOTION_MAP.put("/:E6(jiub)", R.mipmap.emoji_jiub);
        EMOTION_MAP.put("/:E5(yun)", R.mipmap.emoji_yun);
        EMOTION_MAP.put("/:E6(dand)", R.mipmap.emoji_dand);
        EMOTION_MAP.put("/:E5(jio)", R.mipmap.emoji_jio);
        EMOTION_MAP.put("/:E5(biz)", R.mipmap.emoji_biz);
        EMOTION_MAP.put("/:E5(kul)", R.mipmap.emoji_kul);
        EMOTION_MAP.put("/:E6(zhut)", R.mipmap.emoji_zhut);
    }

    public static LinkedHashMap<String, Integer> getEmotionMap() {
        return EMOTION_MAP;
    }

}
