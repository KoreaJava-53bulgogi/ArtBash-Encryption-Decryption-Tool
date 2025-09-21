import java.nio.charset.StandardCharsets;

/**
 * 텍스트와 이진수 문자열 간의 변환을 처리하는 유틸리티 클래스입니다.
 */
public class BinaryCipher {

    /**
     * 텍스트를 이진수 문자열로 변환합니다. 각 문자는 UTF-8 바이트로 변환된 후,
     * 8비트 이진수 문자열로 표현되고 공백으로 구분됩니다.
     * @param text 변환할 원본 텍스트
     * @return 이진수 문자열
     */
    public static String toBinary(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            // 바이트를 8자리 이진수 문자열로 변환 (앞에 0 채움)
            String binStr = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            binary.append(binStr).append(" ");
        }
        return binary.toString().trim();
    }

    /**
     * 이진수 문자열을 텍스트로 변환합니다.
     * @param binaryText 변환할 이진수 문자열 (공백으로 구분)
     * @return 텍스트로 변환된 문자열, 또는 형식 오류 시 에러 메시지
     */
    public static String fromBinary(String binaryText) {
        if (binaryText == null || binaryText.isEmpty()) {
            return "";
        }
        try {
            String[] binaryStrings = binaryText.trim().split("\\s+");
            byte[] bytes = new byte[binaryStrings.length];
            for (int i = 0; i < binaryStrings.length; i++) {
                bytes[i] = (byte) Integer.parseInt(binaryStrings[i], 2);
            }
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (NumberFormatException e) {
            return "INVALID BINARY FORMAT";
        }
    }

    public static boolean isBinary(String text) {
        if (text == null || text.isBlank()) return false;
        return text.trim().matches("^[01\\s]+$");
    }
}