/**
 * 카이사르 암호의 암호화 및 복호화를 처리하는 유틸리티 클래스입니다.
 */
public class CaesarCipher {

    /**
     * 카이사르 암호를 사용하여 텍스트를 암호화합니다.
     * @param text 암호화할 원본 텍스트
     * @param shift 이동할 값 (키)
     * @return 암호화된 텍스트
     */
    public static String encrypt(String text, int shift) {
        return transform(text, shift);
    }

    /**
     * 카이사르 암호로 암호화된 텍스트를 복호화합니다.
     * @param text 복호화할 암호문
     * @param shift 암호화에 사용된 이동 값 (키)
     * @return 복호화된 텍스트
     */
    public static String decrypt(String text, int shift) {
        return transform(text, -shift);
    }

    private static String transform(String text, int shift) {
        if (text == null) {
            return null;
        }

        StringBuilder result = new StringBuilder();
        for (char character : text.toCharArray()) {
            if (character >= 'a' && character <= 'z') {
                result.append(shiftChar(character, 'a', 26, shift));
            } else if (character >= 'A' && character <= 'Z') {
                result.append(shiftChar(character, 'A', 26, shift));
            } else if (character >= '0' && character <= '9') {
                result.append(shiftChar(character, '0', 10, shift));
            } else if (character >= '가' && character <= '힣') {
                result.append(shiftChar(character, '가', 11172, shift));
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }

    private static char shiftChar(char c, char base, int range, int shift) {
        int newPosition = (c - base + shift) % range;
        if (newPosition < 0) newPosition += range;
        return (char) (base + newPosition);
    }
}