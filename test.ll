@.formatting.string = private constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 = icmp slt i32 1, 3

  ;IF STATEMENT
  br i1 %tmp1, label %_then0, label %_else1

  ;THEN STATEMENT
_then0:
  %tmp2 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp3 = call i32 (i8 *, ...)* @printf(i8 * %tmp2, i32 1)
  %tmp4 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp5 = call i32 (i8 *, ...)* @printf(i8 * %tmp4, i32 1)
  br label %_endif2

  ;ELSE STATEMENT
_else1:
  %tmp6 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp7 = call i32 (i8 *, ...)* @printf(i8 * %tmp6, i32 2)
  br label %_endif2

  ;END POINT
_endif2:
  %tmp8 = load i32 * %tmp0
  ret i32 %tmp8
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
