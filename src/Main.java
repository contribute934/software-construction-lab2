import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * 程序入口：控制台菜单。
 *
 * <p>流程：选择题型 → 输入题目数量 → 输入每行列数 → 生成并按列展示题目
 * → 展示参考答案 → 按需保存到 CSV 文件 → 返回菜单，直到用户选择退出。
 * 所有输入都先做类型与范围检查，非法输入给出提示并要求重新输入，
 * 程序不会因非法输入崩溃（健壮性要求）。</p>
 */
public class Main {

    /** 题目数量上限，防止误输入天文数字刷屏或长时间运行。 */
    private static final int MAX_COUNT = 500;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ExerciseGenerator generator = new ExerciseGenerator();
        System.out.println("欢迎使用练习题生成器（实验二：加减乘除版）");
        while (true) {
            printMenu();
            int type = readIntInRange(scanner, "请选择题型（0-" + ExerciseGenerator.TYPE_COUNT + "）：",
                    0, ExerciseGenerator.TYPE_COUNT);
            if (type == 0) {
                System.out.println("感谢使用，再见！");
                break;
            }
            int count = readIntInRange(scanner, "请输入题目数量（1-" + MAX_COUNT + "）：", 1, MAX_COUNT);
            int columns = readIntInRange(scanner, "请输入每行显示的列数（1-10）：", 1, 10);

            List<Exercise> exercises = generator.generateBatch(type, count);
            System.out.println("---- " + ExerciseGenerator.TYPE_NAMES[type - 1]
                    + "（共 " + exercises.size() + " 道，每行 " + columns + " 列）----");
            System.out.print(CsvExporter.formatGrid(exercises, columns, false));
            System.out.println("---- 参考答案 ----");
            System.out.print(CsvExporter.formatGrid(exercises, columns, true));

            if (readYesNo(scanner, "是否把本次题目保存到 CSV 文件？(y/n)：")) {
                String fileName = readFileName(scanner);
                try {
                    CsvExporter.save(Paths.get(fileName), exercises);
                    System.out.println("已保存到 " + fileName + "（共 " + exercises.size() + " 道题）。");
                } catch (IOException e) {
                    System.out.println("保存失败：" + e.getMessage() + "，请检查路径后重试。");
                }
            }
        }
        scanner.close();
    }

    /** 打印题型菜单。 */
    private static void printMenu() {
        System.out.println("================ 练习题菜单 ================");
        for (int i = 0; i < ExerciseGenerator.TYPE_COUNT; i++) {
            System.out.println((i + 1) + ". " + ExerciseGenerator.TYPE_NAMES[i]);
        }
        System.out.println("0. 退出");
    }

    /**
     * 读取一个范围内的整数。先用 hasNextInt 预检，避免非数字输入让
     * Scanner.nextInt() 抛 InputMismatchException 使程序崩溃；
     * 越界时提示并重新读取。
     */
    private static int readIntInRange(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            if (!scanner.hasNextInt()) {
                System.out.println("输入不合法：请输入数字。");
                scanner.next();
                continue;
            }
            int value = scanner.nextInt();
            if (value < min || value > max) {
                System.out.println("输入不合法：应在 " + min + " 到 " + max + " 之间。");
                continue;
            }
            return value;
        }
    }

    /** 读取 y/n 回答，只有 y/Y 算是。 */
    private static boolean readYesNo(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String token = scanner.next();
            if (token.equalsIgnoreCase("y")) {
                return true;
            }
            if (token.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("输入不合法：请输入 y 或 n。");
        }
    }

    /** 读取 CSV 文件名，直接回车用默认名 exercises.csv，没写 .csv 后缀时自动补上。 */
    private static String readFileName(Scanner scanner) {
        System.out.print("请输入文件名（直接回车用默认 exercises.csv）：");
        scanner.nextLine(); // 吃掉上一行残留的换行
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            return "exercises.csv";
        }
        if (!name.toLowerCase().endsWith(".csv")) {
            name = name + ".csv";
        }
        return name;
    }
}
