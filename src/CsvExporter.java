import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 题目输出工具类，功能单一：只负责把题目列表排版成网格文本，
 * 以及保存为 CSV（逗号分隔值）文件，不参与题目生成。
 */
public class CsvExporter {

    /**
     * 把题目按指定列数排成网格文本（实验任务示例：30 道题、每行 5 列）。
     * 每道题等宽左对齐，列与列之间留两个空格。
     *
     * @param exercises 题目列表
     * @param columns   每行列数
     * @param withAnswer true 时每题后带参考答案（答案版排版）
     * @return 排版好的多行文本
     */
    public static String formatGrid(List<Exercise> exercises, int columns, boolean withAnswer) {
        int width = 0;
        for (Exercise e : exercises) {
            String text = withAnswer ? e.getTextWithAnswer() : e.getQuestionText();
            width = Math.max(width, text.length());
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < exercises.size(); i++) {
            String text = withAnswer
                    ? exercises.get(i).getTextWithAnswer()
                    : exercises.get(i).getQuestionText();
            sb.append(text);
            boolean rowEnd = (i + 1) % columns == 0 || i == exercises.size() - 1;
            if (rowEnd) {
                sb.append('\n');
            } else {
                for (int k = text.length(); k < width; k++) {
                    sb.append(' ');
                }
                sb.append("  ");
            }
        }
        return sb.toString();
    }

    /**
     * 把题目保存为 CSV 文件：首行是表头“序号,题目,答案”，之后每行一道题。
     * 采用 UTF-8 编码并写入 BOM 头，保证用 Excel 打开时中文不乱码。
     *
     * @param path      目标文件路径
     * @param exercises 题目列表
     * @throws IOException 写入失败时抛出，由调用方提示用户
     */
    public static void save(Path path, List<Exercise> exercises) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write('﻿'); // BOM 头（U+FEFF）
            writer.write("序号,题目,答案\r\n");
            for (int i = 0; i < exercises.size(); i++) {
                Exercise e = exercises.get(i);
                writer.write((i + 1) + "," + e.getQuestionText() + "," + e.getAnswer() + "\r\n");
            }
        }
    }
}
