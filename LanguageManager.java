import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
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
        String fileName = (lang == AtbashCipherGUI.Language.KOREAN ? "ko" : "en") + ".json";
        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is == null) {
                System.err.println("Language file not found: " + fileName);
                return;
            }
            // 간단한 JSON 파서 (플랫한 key-value 구조만 지원)
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("\"(.*?)\"\\s*:\\s*\"(.*?)\"");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                strings.put(matcher.group(1), matcher.group(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return strings.getOrDefault(key, "!" + key + "!"); // 키가 없는 경우 눈에 띄게 표시
    }
}