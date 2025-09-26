import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 텍스트와 모스 부호 간의 변환을 처리하는 유틸리티 클래스입니다.
 */
public class MorseCode {

    private static final Map<Character, String> CHAR_TO_MORSE = new HashMap<>();
    private static final Map<String, Character> MORSE_TO_CHAR;

    static {
        CHAR_TO_MORSE.put('A', ".-");
        CHAR_TO_MORSE.put('B', "-...");
        CHAR_TO_MORSE.put('C', "-.-.");
        CHAR_TO_MORSE.put('D', "-..");
        CHAR_TO_MORSE.put('E', ".");
        CHAR_TO_MORSE.put('F', "..-.");
        CHAR_TO_MORSE.put('G', "--.");
        CHAR_TO_MORSE.put('H', "....");
        CHAR_TO_MORSE.put('I', "..");
        CHAR_TO_MORSE.put('J', ".---");
        CHAR_TO_MORSE.put('K', "-.-");
        CHAR_TO_MORSE.put('L', ".-..");
        CHAR_TO_MORSE.put('M', "--");
        CHAR_TO_MORSE.put('N', "-.");
        CHAR_TO_MORSE.put('O', "---");
        CHAR_TO_MORSE.put('P', ".--.");
        CHAR_TO_MORSE.put('Q', "--.-");
        CHAR_TO_MORSE.put('R', ".-.");
        CHAR_TO_MORSE.put('S', "...");
        CHAR_TO_MORSE.put('T', "-");
        CHAR_TO_MORSE.put('U', "..-");
        CHAR_TO_MORSE.put('V', "...-");
        CHAR_TO_MORSE.put('W', ".--");
        CHAR_TO_MORSE.put('X', "-..-");
        CHAR_TO_MORSE.put('Y', "-.--");
        CHAR_TO_MORSE.put('Z', "--..");
        CHAR_TO_MORSE.put('1', ".----");
        CHAR_TO_MORSE.put('2', "..---");
        CHAR_TO_MORSE.put('3', "...--");
        CHAR_TO_MORSE.put('4', "....-");
        CHAR_TO_MORSE.put('5', ".....");
        CHAR_TO_MORSE.put('6', "-....");
        CHAR_TO_MORSE.put('7', "--...");
        CHAR_TO_MORSE.put('8', "---..");
        CHAR_TO_MORSE.put('9', "----.");
        CHAR_TO_MORSE.put('0', "-----");
        CHAR_TO_MORSE.put('.', ".-.-.-");
        CHAR_TO_MORSE.put(',', "--..--");
        CHAR_TO_MORSE.put('?', "..--..");

        // 역방향 맵 생성
        MORSE_TO_CHAR = CHAR_TO_MORSE.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    /**
     * 텍스트를 모스 부호로 변환합니다. 단어 사이는 " / "로 구분됩니다.
     * @param text 변환할 원본 텍스트
     * @return 모스 부호로 변환된 문자열
     */
    public static String toMorse(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        // 1. 텍스트를 공백 기준으로 단어로 분리
        return java.util.Arrays.stream(text.toUpperCase().split("\\s+"))
            .map(word -> word.chars() // 2. 각 단어를 문자 스트림으로 변환
                    .mapToObj(c -> (char) c)
                    .map(CHAR_TO_MORSE::get) // 3. 각 문자를 모스 부호로 매핑
                    .filter(java.util.Objects::nonNull) // 4. 매핑되지 않은 문자(null)는 무시
                    .collect(Collectors.joining(" "))) // 5. 글자들을 공백으로 연결
            .collect(Collectors.joining(" / ")); // 6. 단어들을 " / "로 연결
    }

    /**
     * 모스 부호를 텍스트로 변환합니다. 글자 사이는 공백, 단어 사이는 " / "로 구분됩니다.
     * @param morseText 변환할 모스 부호 문자열
     * @return 텍스트로 변환된 문자열
     */
    public static String fromMorse(String morseText) {
        if (morseText == null || morseText.isEmpty()) {
            return "";
        }
        // 1. 모스 부호를 " / " 기준으로 단어로 분리
        return java.util.Arrays.stream(morseText.trim().split(" / "))
            .map(word -> java.util.Arrays.stream(word.trim().split(" ")) // 2. 각 단어를 공백 기준으로 글자로 분리
                    .map(MORSE_TO_CHAR::get) // 3. 각 모스 부호를 문자로 매핑
                    .map(c -> c == null ? "?" : c.toString()) // 4. 매핑되지 않으면 "?"로 처리
                    .collect(Collectors.joining(""))) // 5. 글자들을 연결
            .collect(Collectors.joining(" ")); // 6. 단어들을 공백으로 연결
    }

    /**
     * 입력된 문자열이 모스 부호 형식인지 간단하게 확인합니다.
     * '.', '-', '/', 공백 문자만 포함된 경우 모스 부호로 간주합니다.
     * @param text 확인할 문자열
     * @return 모스 부호 형식이면 true, 아니면 false
     */
    public static boolean isMorseCode(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }
        // 정규 표현식을 사용하여 문자열이 모스 부호 문자로만 구성되었는지 확인합니다.
        // ^: 문자열 시작, $: 문자열 끝, [ ... ]+: 괄호 안의 문자가 1번 이상 반복
        return text.trim().matches("^[\\.\\-\\/\\s]+$");
    }
}