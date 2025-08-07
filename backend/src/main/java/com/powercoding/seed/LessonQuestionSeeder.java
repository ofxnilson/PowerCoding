package com.powercoding.seed;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.powercoding.model.Lesson;
import com.powercoding.model.LessonQuestion;
import com.powercoding.repository.LessonQuestionRepository;
import com.powercoding.repository.LessonRepository;

@Component
public class LessonQuestionSeeder implements CommandLineRunner {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private LessonQuestionRepository lessonQuestionRepository;

    @Override
    public void run(String... args) throws Exception {
        if (lessonRepository.count() > 0) return; // Prevent duplicates

        // Create Lessons
        Lesson basics = new Lesson(
            "Java",
            "Basics",
            "Intro to Java basics: Learn about basic program structure, statements, and simple input/output in Java.",
            true
        );

        Lesson variables = new Lesson(
            "Java",
            "Variables",
            "Working with variables in Java: Understand how to declare, initialize, and use variables of different types.",
            true
        );

        Lesson conditionals = new Lesson(
            "Java",
            "Conditionals",
            "Control flow with if/else: Master how to make decisions in Java using if, else if, and else statements.",
            false
        );

        Lesson loops = new Lesson(
            "Java",
            "Loops",
            "Repetition with loops: Learn to repeat actions using for, while, and do-while loops in Java.",
            false
        );

        Lesson functions = new Lesson(
            "Java",
            "Functions",
            "Methods in Java: Explore how to define, call, and pass data to methods (functions) for code reuse.",
            false
        );

        Lesson advanced = new Lesson(
            "Java",
            "Advanced",
            "",  // TO BE IMPLEMENTED LATER
            false
        );

        // Save lessons
        lessonRepository.saveAll(Arrays.asList(
                basics, variables, conditionals, loops, functions, advanced));

        // Basics Questions for now
        List<LessonQuestion> basicsQuestions = Arrays.asList(
            new LessonQuestion("1) What keyword declares an int x?\nA) int x;\nB) var x;\nC) const x;", "int x;", basics),
            new LessonQuestion("2) Print Hello in Java?\nA) System.out.println(\"Hello\");\nB) print(\"Hello\");\nC) echo \"Hello\";", "System.out.println(\"Hello\");", basics),
            new LessonQuestion("3) Single‐line comment?\nA) // comment\nB) # comment\nC) /* comment */", "// comment", basics),
            new LessonQuestion("4) What ends a statement in Java?\nA) ;\nB) .\nC) ,", ";", basics),
            new LessonQuestion("5) Declare foo method:\nA) void foo() {}\nB) def foo():\nC) func foo() {}", "void foo() {}", basics)
        );

        List<LessonQuestion> variableQuestions = Arrays.asList(
            new LessonQuestion("1) Declare x=5:\nA) int x = 5;\nB) var x = 5;\nC) let x = 5;", "int x = 5;", variables),
            new LessonQuestion("2) String s = \"Bob\":\nA) String s = \"Bob\";\nB) str s = \"Bob\";\nC) var s = \"Bob\";", "String s = \"Bob\";", variables),
            new LessonQuestion("3) Reassign x to 10:\nA) x == 10;\nB) x = 10;\nC) let x = 10;", "x = 10;", variables),
            new LessonQuestion("4) Declare float f:\nA) float f = 3.14f;\nB) float f = 3.14;\nC) var f:float = 3.14;", "float f = 3.14f;", variables),
            new LessonQuestion("5) Keyword for constant?\nA) final\nB) const\nC) static", "final", variables)
        );

        List<LessonQuestion> conditionalQuestions = Arrays.asList(
            new LessonQuestion("1) Basic if statement:\nA) if (x > 0) { ... }\nB) if x > 0: ...\nC) if x > 0 then ...", "if (x > 0) { ... }", conditionals),
            new LessonQuestion("2) else block usage?\nA) else {...}\nB) otherwise {...}\nC) otherwise:", "else { ... }", conditionals),
            new LessonQuestion("3) else if syntax?\nA) else if (x==1) {...}\nB) elseif x==1:\nC) else x==1 {...}", "else if (x==1) { ... }", conditionals),
            new LessonQuestion("4) Logical AND in Java?\nA) &&\nB) and\nC) &", "&&", conditionals),
            new LessonQuestion("5) Logical OR in Java?\nA) ||\nB) or\nC) |", "||", conditionals)
        );

        List<LessonQuestion> loopQuestions = Arrays.asList(
            new LessonQuestion("1) for-loop syntax:\nA) for (int i=0; i<5; i++) {...}\nB) for i in range(5): ...\nC) foreach(i:5){...}", "for (int i=0; i<5; i++) { ... }", loops),
            new LessonQuestion("2) while-loop syntax?\nA) while (cond) {...}\nB) while cond: ...\nC) repeat {...}", "while (cond) { ... }", loops),
            new LessonQuestion("3) Loop exit keyword?\nA) break\nB) stop\nC) exit", "break", loops),
            new LessonQuestion("4) Continue to next iteration?\nA) next\nB) continue\nC) skip", "continue", loops),
            new LessonQuestion("5) Do-while keyword?\nA) do {...} while(...);\nB) while(...){...}\nC) loop {...}", "do { ... } while(...);", loops)
        );

        List<LessonQuestion> functionQuestions = Arrays.asList(
            new LessonQuestion("1) Method declaration:\nA) void foo() {}\nB) function foo() {}\nC) def foo():", "void foo() {}", functions),
            new LessonQuestion("2) Return int method:\nA) int sum() { return 0; }\nB) return int sum() {}\nC) func sum(): int", "int sum() { return 0; }", functions),
            new LessonQuestion("3) Pass parameter x:\nA) void foo(int x) {...}\nB) foo(x:int) {...}\nC) def foo(x):", "void foo(int x) { ... }", functions),
            new LessonQuestion("4) Call a method foo():\nA) foo();\nB) call foo;\nC) foo[];", "foo();", functions),
            new LessonQuestion("5) Method with return?\nA) return x;\nB) give x;\nC) yield x;", "return x;", functions)
        );

        // --- Save Questions ---
        lessonQuestionRepository.saveAll(basicsQuestions);
        lessonQuestionRepository.saveAll(variableQuestions);
        lessonQuestionRepository.saveAll(conditionalQuestions);
        lessonQuestionRepository.saveAll(loopQuestions);
        lessonQuestionRepository.saveAll(functionQuestions);

    }
}
