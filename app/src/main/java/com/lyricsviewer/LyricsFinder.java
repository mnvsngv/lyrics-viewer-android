package com.lyricsviewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


@SuppressWarnings("WeakerAccess")
public class LyricsFinder {
    private final static byte[] LYRICS_HEADER = {0x55, 0x53, 0x4c, 0x54};

    public static String findLyrics(String filePath) throws IOException {
        return findLyrics(new File(filePath));
    }

    public static String findLyrics(File file) throws IOException {
        return findLyrics(fullyReadFileToBytes(file));
    }

    public static String findLyrics(byte[] audioBytes) throws IOException {
        String lyrics = "No filePath found!";

        if(audioBytes.length < 22) return lyrics;

        try {
            if (convertByteToChar(audioBytes[0]) == 'I' &&
                    convertByteToChar(audioBytes[1]) == 'D' &&
                    convertByteToChar(audioBytes[2]) == '3') {
                int lyricsHeaderIndex = BoyerMooreSearch.find(audioBytes, LYRICS_HEADER);
                if (lyricsHeaderIndex == -1) {
                    System.out.println("LYRICS NOT FOUND!");
                } else {
                    int length = getLyricsLength(audioBytes, lyricsHeaderIndex);

                    int offset = lyricsHeaderIndex + 15;
                    return convertBytesToLyrics(audioBytes, offset, length);
                }
            } else {
                System.out.println("ID3 TAG NOT FOUND!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lyrics;
    }

    private static int getLyricsLength(byte[] audioBytes, int lyricsHeaderIndex) {
        int frameLength = (convertByteToChar(audioBytes[lyricsHeaderIndex+4]) << 24) |
                (convertByteToChar(audioBytes[lyricsHeaderIndex+5]) << 16) |
                (convertByteToChar(audioBytes[lyricsHeaderIndex+6]) << 8) |
                (convertByteToChar(audioBytes[lyricsHeaderIndex+7]));
        return frameLength - 5;
    }

    private static char convertByteToChar(byte b) {
        return (char) (b & 0xff);
    }

    private static String convertBytesToLyrics(byte[] audioBytes, int offset, int length) {
        StringBuilder builder = new StringBuilder(length);
        char c;
        for(int i = offset; i < offset + length; i++) {
            c = convertByteToChar(audioBytes[i]);
            if(c != '\0') builder.append(c);
        }
        return builder.toString();
    }

    private static byte[] fullyReadFileToBytes(File file) throws IOException {
        int size = (int) file.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        try (FileInputStream fis = new FileInputStream(file)) {

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        } catch (IOException e) {
            throw e;
        }

        return bytes;
    }

    private static class BoyerMooreSearch {

        private static final int SIZE = 256;
        private static final int lastOccurrence[] = new int[SIZE];

        private static void buildIndex(byte[] pattern) {
            int length = pattern.length;

            for (int i = 0; i < SIZE; i++)
                lastOccurrence[i] = -1;

            for (int i = 0; i < length; i++)
                lastOccurrence[convertByteToChar(pattern[i])] = i;
        }

        private static int findLast(byte b) {
            return lastOccurrence[convertByteToChar(b)];
        }

        static int find(byte[] content, byte[] pattern) {
            //validate, null or empty string is not allowed
            if (content == null || content.length == 0)
                return -1;

            if (pattern == null || pattern.length == 0)
                return -1;

            // search pattern
            if (content.length < pattern.length)
                // impossible match
                return -1;

            // build last occurrence index
            buildIndex(pattern);

            // searching
            int start = pattern.length - 1;
            int end = content.length;
            int position, j;

            // search from left to right
            while (start < end) {
                position = start;
                for (j = pattern.length - 1; j >= 0; j--) {
                    // if not match a character
                    if (pattern[j] != content[position]) {
                        // check the last occurrence index
                        if (findLast(content[position]) != -1) {
                            if (j - findLast(content[position]) > 0)
                                // case 1
                                start += j - findLast(content[position]);
                            else
                                // case 2
                                start += 1;
                        } else {
                            // case 3
                            start += j + 1;
                        }
                        break;
                    }
                    if (j == 0) {
                        // found pattern
                        return position;
                    }
                    position--;
                }
            }
            // not found
            return -1;
        }
    }
}