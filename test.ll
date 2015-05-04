@.formatting.string = private constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp2 = call i32 (i8 *, ...)* @printf(i8 * %tmp1, i32 10)
  %tmp3 = load i32 * %tmp0
  ret i32 %tmp3
}
define i32 A() {
  %_a = alloca i32
  %_b = alloca i32
  %_c = alloca i32
  %tmp4 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp5 = call i32 (i8 *, ...)* @printf(i8 * %tmp4, i32 10)
  ret i32 1
}
define i32 i() {
  %_a = alloca i1
  ret i32 0
}
%class.a = type { [2 x i8 *], i32, i32 }
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
