import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON 형식의 언어팩 파일을 로드하고 관리하는 클래스입니다.
 */
public class LanguageManager {
    private final Map<String, String> strings = new HashMap<>();

    /**
     * 지정된 언어에 해당하는 JSON 파일을 로드합니다.
     * @param lang 로드할 언어
     */
    public void loadLanguage(AtbashCipherGUI.Language lang) {
        strings.clear();
        String fileName;
        switch (lang) {
            case KOREAN:
                fileName = "ko.json";
                break;
            case CHINESE:
                fileName = "zh.json";
                break;
            case JAPANESE:
                fileName = "ja.json";
                break;
            case ENGLISH:
            default:
                fileName = "en.json";
                break;
            case CUSTOM: // Custom is handled by loadCustomLanguage
                return;
        }
        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            loadLanguageFromStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadLanguageFromStream(InputStream is) {
        strings.clear();
        if (is == null) {
            System.err.println("Language input stream is null.");
            return;
        }
        try {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("\"(.*?)\"\\s*:\\s*\"(.*?)\"");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                strings.put(matcher.group(1), matcher.group(2).replace("\\\"", "\""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadCustomLanguage(Preferences prefs, String langName) {
        strings.clear();
        try {
            String[] keys = prefs.node("custom.lang." + langName).keys();
            for (String key : keys) {
                strings.put(key, prefs.get("custom.lang." + langName + "." + key, ""));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return strings.getOrDefault(key, "!" + key + "!"); // 키가 없는 경우 눈에 띄게 표시
    }

    public TreeMap<String, String> getAllStrings() {
        return new TreeMap<>(this.strings);
    }
}