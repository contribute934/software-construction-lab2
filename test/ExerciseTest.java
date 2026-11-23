import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * 核心功能单元测试（JUnit 5）。
 *
 * <p>覆盖：四则运算各题型的约束（运算数、中间结果、结果均为 1..99 正整数）、
 * 除法整除、运算优先级求值、批量生成与去重、CSV 保存与网格排版。
 * 其中 parseAndEvaluate 是测试里独立实现的解析求值（不调用被测类的求值方法），
 * 用来交叉验证题目文本与参考答案是否一致。</p>
 */
public class ExerciseTest {

    /** 独立实现：把题目文本解析回来，按先乘除后加减求值，并检查每一步。 */
    private int parseAndEvaluate(String questionText) {
        String text = questionText.replace(" =", "").replace("×", "*").replace("÷", "/");
        String[] tokens = text.split(" ");
        List<Integer> values = new ArrayList<>();
        List<Character> ops = new ArrayList<>();
        values.add(Integer.parseInt(tokens[0]));
        for (int i = 1; i < tokens.length; i += 2) {
            ops.add(tokens[i].charAt(0));
            values.add(Integer.parseInt(tokens[i + 1]));
        }
        // 先乘除
        List<Integer> v2 = new ArrayList<>();
        List<Character> o2 = new ArrayList<>();
        v2.add(values.get(0));
        for (int i = 0; i < ops.size(); i++) {
            char op = ops.get(i);
            int right = values.get(i + 1);
            if (op == '*' || op == '/') {
                int left = v2.remove(v2.size() - 1);
                if (op == '/') {
                    assertTrue(right != 0 && left % right == 0,
                            "除法必须整除：" + questionText);
                }
                int r = (op == '*') ? left * right : left / right;
                assertTrue(r > 0 && r < 100, "中间结果越界：" + questionText + " -> " + r);
                v2.add(r);
            } else {
                o2.add(op);
                v2.add(right);
            }
        }
        int acc = v2.get(0);
        for (int i = 0; i < o2.size(); i++) {
            acc = (o2.get(i) == '+') ? acc + v2.get(i + 1) : acc - v2.get(i + 1);
            assertTrue(acc > 0 && acc < 100, "中间结果越界：" + questionText + " -> " + acc);
        }
        return acc;
    }

    /** 对一道生成的题目做全面检查：运算数范围、约束、文本回解析答案一致。 */
    private void checkGenerated(Exercise e) {
        for (int operand : e.getOperands()) {
            assertTrue(operand > 0 && operand < 100, "运算数越界：" + e.getQuestionText());
        }
        assertTrue(e.checkConstraints(), "约束检查未通过：" + e.getQuestionText());
        assertEquals(parseAndEvaluate(e.getQuestionText()), e.getAnswer(),
                "回解析答案不一致：" + e.getQuestionText());
    }

    @Test
    void twoOperandAddConstraints() {
        ExerciseGenerator generator = new ExerciseGenerator();
        for (int i = 0; i < 1000; i++) {
            checkGenerated(generator.createOne(1));
        }
    }

    @Test
    void twoOperandSubPositive() {
        ExerciseGenerator generator = new ExerciseGenerator();
        for (int i = 0; i < 1000; i++) {
            Exercise e = generator.createOne(2);
            checkGenerated(e);
            assertTrue(e.getAnswer() > 0, "减法结果必须为正：" + e.getQuestionText());
        }
    }

    @Test
    void twoOperandMulConstraints() {
        ExerciseGenerator generator = new ExerciseGenerator();
        for (int i = 0; i < 1000; i++) {
            Exercise e = generator.createOne(3);
            checkGenerated(e);
            int[] v = e.getOperands();
            assertEquals(v[0] * v[1], e.getAnswer());
        }
    }

    @Test
    void twoOperandDivExact() {
        ExerciseGenerator generator = new ExerciseGenerator();
        for (int i = 0; i < 1000; i++) {
            Exercise e = generator.createOne(4);
            checkGenerated(e);
            int[] v = e.getOperands();
            assertEquals(0, v[0] % v[1], "除法必须整除：" + e.getQuestionText());
            assertEquals(v[0] / v[1], e.getAnswer());
        }
    }

    @Test
    void threeOperandPureTypesConstraints() {
        ExerciseGenerator generator = new ExerciseGenerator();
        for (int type = 5; type <= 8; type++) {
            for (int i = 0; i < 1000; i++) {
                checkGenerated(generator.createOne(type));
            }
        }
    }

    @Test
    void threeOperandMixedIntermediateInRange() {
        ExerciseGenerator generator = new ExerciseGenerator();
        for (int i = 0; i < 3000; i++) {
            Exercise e = generator.createOne(9);
            checkGenerated(e);
            for (int step : e.stepResults()) {
                assertTrue(step > 0 && step < 100,
                        "中间结果越界：" + e.getQuestionText() + " -> " + step);
            }
        }
    }

    @Test
    void evaluateRespectsPrecedence() {
        // 3 + 4 * 5 应先算 4*5=20，再算 3+20=23，而不是从左到右的 35
        Exercise e1 = new ThreeOperandExercise(new int[]{3, 4, 5}, new char[]{'+', '*'});
        assertEquals(23, e1.getAnswer());
        // 6 / 3 * 2 = 4
        Exercise e2 = new ThreeOperandExercise(new int[]{6, 3, 2}, new char[]{'/', '*'});
        assertEquals(4, e2.getAnswer());
        // 9 - 3 * 2 = 3
        Exercise e3 = new ThreeOperandExercise(new int[]{9, 3, 2}, new char[]{'-', '*'});
        assertEquals(3, e3.getAnswer());
        // 8 / 2 / 2 = 2（同级从左到右）
        Exercise e4 = new ThreeOperandExercise(new int[]{8, 2, 2}, new char[]{'/', '/'});
        assertEquals(2, e4.getAnswer());
    }

    @Test
    void batchGenerationHasNoDuplicates() {
        ExerciseGenerator generator = new ExerciseGenerator();
        for (int type = 1; type <= 10; type++) {
            List<Exercise> batch = generator.generateBatch(type, 100);
            assertEquals(100, batch.size(), "题型 " + type + " 数量不足");
            long distinct = batch.stream().map(Exercise::getQuestionText).distinct().count();
            assertEquals(100, distinct, "题型 " + type + " 出现重复题目");
            int expectedOperands = (type <= 4) ? 2 : -1;
            if (expectedOperands > 0) {
                for (Exercise e : batch) {
                    assertEquals(expectedOperands, e.getOperands().length);
                }
            }
        }
    }

    @Test
    void csvExportWritesHeaderAndRows() throws IOException {
        ExerciseGenerator generator = new ExerciseGenerator();
        List<Exercise> batch = generator.generateBatch(10, 30);
        Path file = Files.createTempFile("exercises-test", ".csv");
        try {
            CsvExporter.save(file, batch);
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            // 首行含 BOM，读回后第一行以 BOM 开头
            assertEquals(31, lines.size(), "应为 1 行表头 + 30 行题目");
            assertTrue(lines.get(0).contains("序号,题目,答案"), "表头不正确：" + lines.get(0));
            for (int i = 1; i < lines.size(); i++) {
                String[] parts = lines.get(i).split(",");
                assertEquals(3, parts.length, "每行应有 3 列：" + lines.get(i));
                assertEquals(String.valueOf(i), parts[0]);
                assertEquals(parseAndEvaluate(parts[1]), Integer.parseInt(parts[2]),
                        "CSV 中答案不一致：" + lines.get(i));
            }
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void gridFormatArrangesColumns() {
        ExerciseGenerator generator = new ExerciseGenerator();
        List<Exercise> batch = generator.generateBatch(10, 30);
        String grid = CsvExporter.formatGrid(batch, 5, false).strip();
        String[] rows = grid.split("\n");
        assertEquals(6, rows.length, "30 道题每行 5 列应排成 6 行");
        for (String row : rows) {
            int count = row.split("=", -1).length - 1;
            assertEquals(5, count, "每行应有 5 道题：" + row);
        }
    }
}
