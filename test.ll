@.formatting.string = private constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 =  xor i1 false, true

  ;IF STATEMENT
  br i1 %tmp1, label %_then0, label %_endif1

  ;THEN STATEMENT
_then0:
  %tmp2 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp3 = call i32 (i8 *, ...)* @printf(i8 * %tmp2, i32 1)
  br label %_endif1

  ;END POINT
_endif1:
  %tmp4 = load i32 * %tmp0
  ret i32 %tmp4
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
