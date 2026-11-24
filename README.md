# Лаб 8: Mock объектууд — Mockito

Оюутан: Сайнзаяа Код: B252270209

## Орчин

```
openjdk version "25.0.2" 2026-01-20 LTS
OpenJDK Runtime Environment Temurin-25.0.2+10 (build 25.0.2+10-LTS)
OpenJDK 64-Bit Server VM Temurin-25.0.2+10 (build 25.0.2+10-LTS, mixed mode, sharing)
Apache Maven 3.9.9 (8e8579a9e76f7d015ee5ec7bfcdc97d260186937)
Maven home: /Users/tselmuunzaya/tools/apache-maven-3.9.9
Java version: 25.0.2, vendor: Eclipse Adoptium, runtime: /Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home
Default locale: en_MN, platform encoding: UTF-8
OS name: "mac os x", version: "26.5.2", arch: "aarch64", family: "mac"
```

## Ажиллуулах

```bash
mvn test
mkdir -p results && mvn test 2>&1 | tee results/mvn-test.txt
```

Гаралт: `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` (results/mvn-test.txt).
Тестийн тоо **10** = 7 энгийн тест (A1–A7) + A8-ийн 3 мөр.

## Хэсэг A: тестүүд

Тестийн бай: `CourseRegistrationService.register()`. `StudentRepository`, `EmailSender`-ийг `@Mock`-оор орлуулсан.

| № | Тест | Хэрэгсэл |
|---|------|----------|
| A1 | register_activeStudentWithPrerequisites_returnsOkSavesAndSendsEmail | when + verify |
| A2 | register_unknownStudent_returnsNoStudentAndDoesNotSaveOrEmail | verify(..., never()) |
| A3 | register_inactiveStudent_returnsInactiveAndNeverTouchesEmail | verifyNoInteractions |
| A4 | register_missingPrerequisite_returnsPrerequisitesAndDoesNotSave | when + never |
| A5 | register_emailSenderThrows_stillReturnsOkAndSaves | doThrow(...).when(...) |
| A6 | register_nullArgument_throwsIllegalArgumentAndTouchesNothing | assertThrows |
| A7 | register_success_emailBodyContainsCourseCode | ArgumentCaptor |
| A8 | register_errorCases_returnExpectedCodeAndDoNotSave (3 мөр) | @ParameterizedTest + @CsvSource |

### A8-ийн lenient()

A8-д мөр бүр өөр stub-ыг ашигладаг: жишээ нь `noStudent` мөрөнд `courseExists`, `prerequisites` хэзээ ч дуудагддаггүй. MockitoExtension-ий strict stubs горим ашиглагдаагүй stub дээр `UnnecessaryStubbingException` өгдөг тул нийтлэг stub-уудыг `lenient().when(...)`-ээр бичсэн.

## Хэсэг B: мини мутаци

Мутант бүрийн дараа `git checkout -- src/main`-ээр кодыг сэргээсэн.

| Мутант | Эвдрэл | Унасан тест | Үр дүн | Файл |
|--------|--------|-------------|--------|------|
| 1 | `if (!student.active()) ...` комментлосон | A3 `register_inactiveStudent_...` ба A8 `[2]` (inactiveStudent мөр) | Failures: 2, BUILD FAILURE | results/mvn-test-mutant-1.txt |
| 2 | `repository.saveRegistration(...)` комментлосон | A1 `register_activeStudentWithPrerequisites_...` ба A5 `register_emailSenderThrows_...` | Failures: 2, BUILD FAILURE | results/mvn-test-mutant-2.txt |
| 3 (засахаас өмнө) | эхний if-ээс `\|\| courseId == null` устгасан | унасан тест байхгүй | Failures: 0, BUILD SUCCESS (мутант амьд үлдсэн) | results/mvn-test-mutant-3-before-fix.txt |
| 3 (A6 зассаны дараа) | мөн адил | A6 `register_nullArgument_...` | Failures: 1, BUILD FAILURE | results/mvn-test-mutant-3.txt |

Гурван мутантад Errors = 0 байсан, `UnnecessaryStubbingException` гараагүй, бүгд `Failures`-аар унасан.

### Мутант 3-ын цоорхой ба засвар

Анх A6 зөвхөн `register(null, "CS101")`-ийг шалгадаг байсан тул `courseId == null` хэсгийг устгахад ямар ч тест унаагүй (results/mvn-test-mutant-3-before-fix.txt). Тестийн цоорхой илэрсэн. A6-г `register("S1", null)`-ийг мөн шалгадаг болгож засав (нэг л тест хэвээр, тестийн тоо 10). Үүний дараа мутант баригдав (results/mvn-test-mutant-3.txt).

Мутантуудыг дахин ажиллуулах жишээ (мутант 1):

```bash
F=src/main/java/mn/edu/must/sqat/CourseRegistrationService.java
sed -i '' 's|^\(.*student.active().*\)$|//\1|' $F
mvn test 2>&1 | tee results/mvn-test-mutant-1.txt
git checkout -- src/main
```

## Дүгнэлт

Mock нь тестийн дотор Mockito-оор үүсгэсэн, дуудалтыг нь бүртгэж, хариуг нь тест бүрд тохируулдаг орлуулагч бол гараар бичсэн fake нь жинхэнэ логиктой (жишээ нь санах ойд хадгалдаг) өөр нэг хэрэгжүүлэлт бөгөөд тест төлөвийг (state) шалгадаг. Mock-оор "saveRegistration дуудагдсан уу" гэх мэт харьцааг (behavior) шууд шалгаж, fake бичих код хэмнэнэ. Mock хийхээр зохистой зүйл нь өгөгдлийн сан, и-мэйл гэх мэт удаан, гаднын, үр дүн нь тогтворгүй хамаарал юм. Харин `Student` шиг энгийн өгөгдлийн record, тестлэгдэж буй класс өөрөө, `List` зэрэг хямд тогтвортой объектыг mock хийх ёсгүй, бодит объектоор нь л ашиглана. `verify`-г хэтрүүлбэл тест хэрэгжүүлэлтийн дотоод дуудалтын дараалал, тоотой чанга холбогдож, код зөв хэвээр байхад рефакторингийн дараа тест унадаг болно. Мөн дуудалтыг шалгаад үр дүнг (буцаах утга) шалгахгүй орхивол тест хуурамч ногоон болох эрсдэлтэй. Мутаци нь A1–A7 тестүүд ихэнх эвдрэлийг барьж байсныг, гэхдээ A6 нэг аргументыг л шалгаж байсан тул цоорхой үлдсэнийг харуулав. Тийм болохоор зөвхөн ногоон гэж итгэхгүй, зориуд эвдэж шалгах хэрэгтэй.