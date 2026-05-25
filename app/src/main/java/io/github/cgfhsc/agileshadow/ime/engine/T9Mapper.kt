package io.github.cgfhsc.agileshadow.ime.engine

object T9Mapper {

    private val charToT9 = mapOf(
        'a' to 'A', 'b' to 'A', 'c' to 'A',
        'd' to 'D', 'e' to 'D', 'f' to 'D',
        'g' to 'G', 'h' to 'G', 'i' to 'G',
        'j' to 'J', 'k' to 'J', 'l' to 'J',
        'm' to 'M', 'n' to 'M', 'o' to 'M',
        'p' to 'P', 'q' to 'P', 'r' to 'P', 's' to 'P',
        't' to 'T', 'u' to 'T', 'v' to 'T',
        'w' to 'W', 'x' to 'W', 'y' to 'W', 'z' to 'W',
    )

    private val rawPinyinMap = mapOf(
        "A" to "a,b,c",
        "D" to "e,d,f",
        "G" to "g,h,i",
        "J" to "j,k,l",
        "M" to "o,m,n",
        "P" to "p,q,r,s",
        "T" to "t,u,v",
        "W" to "w,x,y,z",
        "AA" to "ba,ca",
        "AD" to "ce",
        "AG" to "ai,bi,ci,ch",
        "AM" to "an,ao,bo",
        "AT" to "bu,cu",
        "DA" to "da,fa",
        "DD" to "de",
        "DG" to "di,ei",
        "DM" to "en,fo",
        "DP" to "er",
        "DT" to "du,fu",
        "GA" to "ga,ha",
        "GD" to "ge,he",
        "GT" to "gu,hu",
        "JA" to "ka,la",
        "JD" to "ke,le",
        "JG" to "ji,li",
        "JM" to "lo",
        "JT" to "ju,ku,lu,lv",
        "MA" to "ma,na",
        "MD" to "me,ne",
        "MG" to "mi,ni",
        "MM" to "mo",
        "MT" to "mu,nu,nv,ou",
        "PA" to "pa,sa",
        "PD" to "re,se",
        "PG" to "pi,qi,ri,si,sh",
        "PM" to "po",
        "PT" to "pu,qu,ru,su",
        "TA" to "ta",
        "TD" to "te",
        "TG" to "ti",
        "TT" to "tu",
        "WA" to "wa,ya,za",
        "WD" to "ye,ze",
        "WG" to "xi,yi,zi",
        "WM" to "wo,yo",
        "WT" to "wu,xu,yu,zu",
        "AAG" to "bai,cai",
        "AAM" to "ban,bao,can,cao",
        "ADG" to "bei",
        "ADM" to "ben,cen",
        "AGA" to "cha",
        "AGD" to "bie,che",
        "AGG" to "chi",
        "AGM" to "bin",
        "AGT" to "chu",
        "AMG" to "ang",
        "AMT" to "cou",
        "ATG" to "cui",
        "ATM" to "cun,cuo",
        "DAG" to "dai",
        "DAM" to "dan,dao,fan",
        "DDG" to "dei,fei",
        "DDM" to "den,fen",
        "DGA" to "dia",
        "DGD" to "die",
        "DGT" to "diu",
        "DMG" to "eng",
        "DMT" to "dou,fou",
        "DTG" to "dui",
        "DTM" to "dun,duo",
        "GAG" to "gai,hai",
        "GAM" to "gan,gao,han,hao",
        "GDG" to "gei,hei",
        "GDM" to "gen,hen",
        "GMT" to "gou,hou",
        "GTA" to "gua,hua",
        "GTG" to "gui,hui",
        "GTM" to "gun,guo,hun,huo",
        "JAG" to "kai,lai",
        "JAM" to "kan,kao,lan,lao",
        "JDG" to "kei,lei",
        "JDM" to "ken",
        "JGA" to "jia,lia",
        "JGD" to "jie,lie",
        "JGM" to "jin,lin",
        "JGT" to "jiu,liu",
        "JMT" to "kou,lou",
        "JTA" to "kua",
        "JTD" to "jue,lve",
        "JTG" to "kui",
        "JTM" to "jun,kun,kuo,lun,luo",
        "MAG" to "mai,nai",
        "MAM" to "man,mao,nan,nao",
        "MDG" to "mei,nei",
        "MDM" to "men,nen",
        "MGD" to "mie,nie",
        "MGM" to "min,nin",
        "MGT" to "miu,niu",
        "MMT" to "mou,nou",
        "MTD" to "nve",
        "MTM" to "nuo",
        "PAG" to "pai,sai",
        "PAM" to "pan,pao,ran,rao,san,sao",
        "PDG" to "pei",
        "PDM" to "pen,ren,sen",
        "PGA" to "qia,sha",
        "PGD" to "pie,qie,she",
        "PGG" to "shi",
        "PGM" to "pin,qin",
        "PGT" to "qiu,shu",
        "PMT" to "pou,rou,sou",
        "PTD" to "que",
        "PTG" to "rui,sui",
        "PTM" to "qun,run,ruo,sun,suo",
        "TAG" to "tai",
        "TAM" to "tan,tao",
        "TDG" to "tei",
        "TGD" to "tie",
        "TMT" to "tou",
        "TTG" to "tui",
        "TTM" to "tun,tuo",
        "WAG" to "wai,zai",
        "WAM" to "wan,yan,yao,zan,zao",
        "WDG" to "wei,zei",
        "WDM" to "wen,zen",
        "WGA" to "xia,zha",
        "WGD" to "xie,zhe",
        "WGG" to "zhi",
        "WGM" to "xin,yin",
        "WGT" to "xiu,zhu",
        "WMT" to "you,zou",
        "WTD" to "xue,yue",
        "WTG" to "zui",
        "WTM" to "xun,yun,zun,zuo",
        "AAMG" to "bang,cang",
        "ADMG" to "beng,ceng",
        "AGAG" to "chai",
        "AGAM" to "bian,biao,chan,chao",
        "AGDM" to "chen",
        "AGMG" to "bing",
        "AGMT" to "chou",
        "AGTA" to "chua",
        "AGTG" to "chui",
        "AGTM" to "chun,chuo",
        "AMMG" to "cong",
        "ATAM" to "cuan",
        "DAMG" to "dang,fang",
        "DDMG" to "deng,feng",
        "DGAM" to "dian,diao,fiao",
        "DGMG" to "ding",
        "DMMG" to "dong",
        "DTAM" to "duan",
        "GAMG" to "gang,hang",
        "GDMG" to "geng,heng",
        "GMMG" to "gong,hong",
        "GTAG" to "guai,huai",
        "GTAM" to "guan,huan",
        "JAMG" to "kang,lang",
        "JDMG" to "keng,leng",
        "JGAM" to "jian,jiao,lian,liao",
        "JGMG" to "jing,ling",
        "JMMG" to "kong,long",
        "JTAG" to "kuai",
        "JTAM" to "juan,kuan,luan",
        "MAMG" to "mang,nang",
        "MDMG" to "meng,neng",
        "MGAM" to "mian,miao,nian,niao",
        "MGMG" to "ming,ning",
        "MMMG" to "nong",
        "MTAM" to "nuan",
        "PAMG" to "pang,rang,sang",
        "PDMG" to "peng,reng,seng",
        "PGAG" to "shai",
        "PGAM" to "pian,piao,qian,qiao,shan,shao",
        "PGDG" to "shei",
        "PGDM" to "shen",
        "PGMG" to "ping,qing",
        "PGMT" to "shou",
        "PGTA" to "shua",
        "PGTG" to "shui",
        "PGTM" to "shun,shuo",
        "PMMG" to "rong,song",
        "PTAM" to "quan,ruan,suan",
        "TAMG" to "tang",
        "TDMG" to "teng",
        "TGAM" to "tian,tiao",
        "TGMG" to "ting",
        "TMMG" to "tong",
        "TTAM" to "tuan",
        "WAMG" to "wang,yang,zang",
        "WDMG" to "weng,zeng",
        "WGAG" to "zhai",
        "WGAM" to "xian,xiao,zhan,zhao",
        "WGDG" to "zhei",
        "WGDM" to "zhen",
        "WGMG" to "xing,ying",
        "WGMT" to "zhou",
        "WGTA" to "zhua",
        "WGTG" to "zhui",
        "WGTM" to "zhun,zhuo",
        "WMMG" to "yong,zong",
        "WTAM" to "xuan,yuan,zuan",
        "AGAMG" to "chang,biang",
        "AGDMG" to "cheng",
        "AGMMG" to "chong",
        "AGTAG" to "chuai",
        "AGTAM" to "chuan",
        "GTAMG" to "guang,huang",
        "JGAMG" to "jiang,liang",
        "JGMMG" to "jiong",
        "JTAMG" to "kuang",
        "MGAMG" to "niang",
        "PGAMG" to "qiang,shang",
        "PGDMG" to "sheng",
        "PGMMG" to "qiong",
        "PGTAG" to "shuai",
        "PGTAM" to "shuan",
        "WGAMG" to "xiang,zhang",
        "WGDMG" to "zheng",
        "WGMMG" to "xiong,zhong",
        "WGTAG" to "zhuai",
        "WGTAM" to "zhuan",
        "AGTAMG" to "chuang",
        "PGTAMG" to "shuang",
        "WGTAMG" to "zhuang",
    )

    private val numToT9Letter = mapOf(
        '2' to 'A', '3' to 'D', '4' to 'G', '5' to 'J',
        '6' to 'M', '7' to 'P', '8' to 'T', '9' to 'W',
    )

    fun numKeyToT9Letter(keycode: Int): Char? = numToT9Letter[keycode.toChar()]

    private val pinyinMap: Map<String, List<String>> =
        rawPinyinMap.mapValues { it.value.split(",") }

    fun t9ToPinyin(t9Sequence: String): Array<String> {
        if (t9Sequence.isEmpty()) return emptyArray()
        val key = t9Sequence.take(7)
        val result = mutableListOf<String>()
        for (length in key.length downTo 1) {
            pinyinMap[key.substring(0, length)]?.let { value ->
                result.addAll(value)
            }
        }
        return result.toTypedArray()
    }

    fun pinyinToT9(pinyin: String): String =
        pinyin.map { charToT9[it.lowercaseChar()] ?: it }.joinToString("")

    /** 将 T9 字母序列递归分解为拼音字符串 */
    fun decomposeT9(t9Letters: String): String {
        if (t9Letters.isEmpty()) return ""
        for (length in minOf(t9Letters.length, 6) downTo 1) {
            pinyinMap[t9Letters.substring(0, length)]?.firstOrNull()?.let { pinyin ->
                return pinyin + decomposeT9(t9Letters.substring(length))
            }
        }
        return decomposeT9(t9Letters.substring(1))
    }

    /**
     * 根据候选词拼音注释和实际输入组合长度，生成拼音显示文本。
     * 取 comment 的拼音但截断到每段实际输入长度，避免未输完就显示完整拼音。
     */
    fun getT9Composition(composition: String, comment: String): String {
        if (comment.isEmpty()) return composition
        val asciiBuilder = StringBuilder()
        val nonAsciiBuilder = StringBuilder()
        for (ch in composition) if (ch.code <= 0xFF) asciiBuilder.append(ch) else nonAsciiBuilder.append(ch)
        val compositionList = asciiBuilder.split("'")
        val commentParts = comment.split("'").filter { it.isNotEmpty() }
        return if (commentParts.size == compositionList.size) {
            buildString {
                append(nonAsciiBuilder)
                commentParts.zip(compositionList).forEach { (pinyin, compo) ->
                    append(pinyin.take(compo.length))
                    append("'")
                }
            }
        } else composition.lowercase()
    }
}
