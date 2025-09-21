/**
 * 스퀴탈레(Scytale) 암호의 암호화 및 복호화를 처리하는 유틸리티 클래스입니다.
 */
public class ScytaleCipher {

    /**
     * 스퀴탈레 암호를 사용하여 텍스트를 암호화합니다.
     * @param text 암호화할 원본 텍스트
     * @param diameter 막대의 지름 (열의 수)
     * @return 암호화된 텍스트
     */
    public static String encrypt(String text, int diameter) {
        if (text == null || text.isEmpty() || diameter <= 0) {
            return text;
        }

        String cleanText = text.replaceAll("\\s", "");
        if (cleanText.isEmpty()) return "";

        int len = cleanText.length();
        int rows = (int) Math.ceil((double) len / diameter);
        char[][] grid = new char[rows][diameter];

        int k = 0;
        for (int col = 0; col < diameter; col++) {
            for (int row = 0; row < rows; row++) {
                if (k < len) {
                    grid[row][col] = cleanText.charAt(k++);
                } else {
                    grid[row][col] = 'X'; // Padding character
                }
            }
        }

        StringBuilder result = new StringBuilder();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < diameter; col++) {
                result.append(grid[row][col]);
            }
        }
        return result.toString();
    }

    /**
     * 스퀴탈레 암호로 암호화된 텍스트를 복호화합니다.
     * @param text 복호화할 암호문
     * @param diameter 암호화에 사용된 막대의 지름
     * @return 복호화된 텍스트 (패딩 문자 'X'가 포함될 수 있음)
     */
    public static String decrypt(String text, int diameter) {
        if (text == null || text.isEmpty() || diameter <= 0) {
            return text;
        }

        int len = text.length();
        int rows = (int) Math.ceil((double) len / diameter);
        char[][] grid = new char[rows][diameter];

        int k = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < diameter; col++) {
                if (k < len) grid[row][col] = text.charAt(k++);
            }
        }

        StringBuilder result = new StringBuilder();
        for (int col = 0; col < diameter; col++) {
            for (int row = 0; row < rows; row++) {
                if (grid[row][col] != '\0') result.append(grid[row][col]);
            }
        }
        return result.toString();
    }
}