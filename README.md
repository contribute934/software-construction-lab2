# 软件构造实验二：加减乘除练习题生成器

在实验一（加减法）的基础上扩展的 Java 控制台程序：支持两个数纯加 / 纯减 / 纯乘 / 纯除、
三个数纯加 / 纯减 / 纯乘 / 纯除 / 加减乘除混合、随机混合共 10 种题型；
运算数、每一步中间结果与最终结果均为大于 0、小于 100 的正整数，除法必须整除，
混合运算按“先乘除、后加减”的优先级求值。生成数量与每行列数可设定，结果可保存为 CSV 文件。

## 目录结构

- `src/` —— 全部源程序（Exercise 抽象基类、TwoOperandExercise / ThreeOperandExercise 子类、
  ExerciseGenerator 生成器、CsvExporter 输出工具、Main 菜单入口）与 UML 类图源文件 `uml.puml`
- `test/ExerciseTest.java` —— JUnit 5 单元测试（10 个测试方法）
- `exercises.csv` —— 程序运行生成的示例输出（随机混合 30 道）

## 编译与运行（JDK 17）

```bash
javac -encoding UTF-8 -d out src/*.java
java -Dfile.encoding=UTF-8 -cp out Main
```

## 运行单元测试

需要 JUnit Platform Console Standalone 1.10.2（放入 `lib/` 目录）：

```bash
javac -encoding UTF-8 -cp out:lib/junit-platform-console-standalone-1.10.2.jar -d out test/ExerciseTest.java
java -jar lib/junit-platform-console-standalone-1.10.2.jar execute --class-path out --select-class ExerciseTest
```

## 设计要点

- 基类 `Exercise` 统一管理约束检查与求值：`stepResults()` 两遍扫描（先乘除、后加减）
  得到每一步中间结果，`checkConstraints()` 逐步检查范围与整除。
- 除法用构造法生成（先定被除数再选因数 / 反推被除数），避免随机除法绝大多数不整除的问题。
- `CsvExporter` 只负责排版与 CSV 保存（UTF-8 + BOM，Excel 打开不乱码），功能单一；
  上层依赖抽象基类 `Exercise` 而非具体子类，符合依赖倒转原则。
