public class Metaphone {

    // Constants
    static final int MAXENCODEDLENGTH = 6;
    static final char NULLCHAR = '\0';
    static final String VOWELS = "AEIOU";

    // For tracking position within current String
    static String text;
    static int pos;

    public static void main(String[] args) {
        System.out.println(encode("bromfenac"));
    }

    static void initializeText(String s)
    {
        text = s;
        pos = 0;
    }

    static boolean isOneOf(char c, String s)
    {
        return (s.indexOf(c) != -1);
    }

    static void moveAhead() {
        moveAhead(1);
    }

    static void moveAhead(int count) {
        pos = Math.min(pos + count, text.length());
    }

    static boolean endOfText() {
        return pos >= text.length();
    }

    static char peek() {
        return peek(0);
    }

    static char peek(int ahead) {
        int post = (pos + ahead);
        if (post < 0 || post >= text.length()) {
            return NULLCHAR;
        }
        return text.charAt(post);
    }

    static String normalize(String text) {
        StringBuilder builder = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                builder.append(Character.toUpperCase(c));
            }
        }
        return builder.toString();
    }

    public static String encode(String text)
    {
        // Process normalized text
        initializeText(normalize(text));

        // Write encoded String to StringBuilder
        StringBuilder builder = new StringBuilder();

        // Special handling of some String prefixes:
        //     PN, KN, GN, AE, WR, WH and X
        switch (peek()) {
            case 'P':
            case 'K':
            case 'G':
                if (peek(1) == 'N') {
                    moveAhead();
                }
                break;

            case 'A':
                if (peek(1) == 'E') {
                    moveAhead();
                }
                break;

            case 'W':
                if (peek(1) == 'R') {
                    moveAhead();
                } else if (peek(1) == 'H') {
                    builder.append('W');
                    moveAhead(2);
                }
                break;

            case 'X':
                builder.append('S');
                moveAhead();
                break;
        }

        //
        while (!endOfText() && builder.length() < MAXENCODEDLENGTH)
        {
            // Cache this character
            char c = peek();

            // Ignore duplicates except CC
            if (c == peek(-1) && c != 'C') {
                moveAhead();
                continue;
            }

            // Don't change F, J, L, M, N, R or first-letter vowel
            if (isOneOf(c, "FJLMNR") || (builder.length() == 0 && isOneOf(c, VOWELS))) {
                builder.append(c);
                moveAhead();
            } else {
                int charsConsumed = 1;

                switch (c) {
                    case 'B':
                        // B = 'B' if not -MB
                        if (peek(-1) != 'M' || peek(1) != NULLCHAR) {
                            builder.append('B');
                        }
                        break;

                    case 'C':
                        // C = 'X' if -CIA- or -CH-
                        // Else 'S' if -CE-, -CI- or -CY-
                        // Else 'K' if not -SCE-, -SCI- or -SCY-
                        if (peek(-1) != 'S' || !isOneOf(peek(1), "EIY")) {
                            if (peek(1) == 'I' && peek(2) == 'A') {
                                builder.append('X');
                            } else if (isOneOf(peek(1), "EIY")) {
                                builder.append('S');
                            } else if (peek(1) == 'H') {
                                if ((pos == 0 && !isOneOf(peek(2), VOWELS)) || peek(-1) == 'S') {
                                    builder.append('K');
                                } else {
                                    builder.append('X');
                                }
                                charsConsumed++;    // Eat 'CH'
                            } else {
                                builder.append('K');
                            }
                        }
                        break;

                    case 'D':
                        // D = 'J' if DGE, DGI or DGY
                        // Else 'T'
                        if (peek(1) == 'G' && isOneOf(peek(2), "EIY")) {
                            builder.append('J');
                        } else {
                            builder.append('T');
                        }
                        break;

                    case 'G':
                        // G = 'F' if -GH and not B--GH, D--GH, -H--GH, -H---GH
                        // Else dropped if -GNED, -GN, -DGE-, -DGI-, -DGY-
                        // Else 'J' if -GE-, -GI-, -GY- and not GG
                        // Else K
                        if ((peek(1) != 'H' || isOneOf(peek(2), VOWELS)) &&
                             (peek(1) != 'N' || (peek(1) != NULLCHAR &&
                             (peek(2) != 'E' || peek(3) != 'D'))) &&
                             (peek(-1) != 'D' || !isOneOf(peek(1), "EIY"))) {

                            if (isOneOf(peek(1), "EIY") && peek(2) != 'G') {
                                builder.append('J');
                            } else {
                                builder.append('K');
                            }
                        }
                        // Eat GH
                        if (peek(1) == 'H') {
                            charsConsumed++;
                        }
                        break;

                    case 'H':
                        // H = 'H' if before or not after vowel
                        if (!isOneOf(peek(-1), VOWELS) || isOneOf(peek(1), VOWELS)) {
                            builder.append('H');
                        }
                        break;

                    case 'K':
                        // K = 'C' if not CK
                        if (peek(-1) != 'C') {
                            builder.append('K');
                        }
                        break;

                    case 'P':
                        // P = 'F' if PH
                        // Else 'P'
                        if (peek(1) == 'H') {
                            builder.append('F');
                            charsConsumed++;    // Eat 'PH'
                        } else {
                            builder.append('P');
                        }
                        break;

                    case 'Q':
                        // Q = 'K'
                        builder.append('K');
                        break;

                    case 'S':
                        // S = 'X' if SH, SIO or SIA
                        // Else 'S'
                        if (peek(1) == 'H') {
                            builder.append('X');
                            charsConsumed++;    // Eat 'SH'
                        } else if (peek(1) == 'I' && isOneOf(peek(2), "AO")) {
                            builder.append('X');
                        } else {
                            builder.append('S');
                        }
                        break;

                    case 'T':
                        // T = 'X' if TIO or TIA
                        // Else '0' if TH
                        // Else 'T' if not TCH
                        if (peek(1) == 'I' && isOneOf(peek(2), "AO")) {
                            builder.append('X');
                        } else if (peek(1) == 'H') {
                            builder.append('0');
                            charsConsumed++;    // Eat 'TH'
                        } else if (peek(1) != 'C' || peek(2) != 'H') {
                            builder.append('T');
                        }
                        break;

                    case 'V':
                        // V = 'F'
                        builder.append('F');
                        break;

                    case 'W':
                    case 'Y':
                        // W,Y = Keep if not followed by vowel
                        if (isOneOf(peek(1), VOWELS)) {
                            builder.append(c);
                        }
                        break;

                    case 'X':
                        // X = 'S' if first character (already done)
                        // Else 'KS'
                        builder.append("KS");
                        break;

                    case 'Z':
                        // Z = 'S'
                        builder.append('S');
                        break;
                }
                // Advance over consumed characters
                moveAhead(charsConsumed);
            }
        }
        // Return result
        return builder.toString();
    }
}
