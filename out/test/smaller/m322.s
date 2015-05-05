@.formatting.string = private constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0

  ;test LHS and do conditional branch
  br i1 true, label %_true0, label %_false1

  ;test RHS and jump to the closing
_true0:
  br i1 true, label %_closing2, label %_false1
_false1:

  ;tested false, making it so
  %tmp1 = icmp slt i32 1, 0
  br label %_closing2

  ;phi, my lord, phi
_closing2:
  %tmp2 = phi i1 [ %tmp1, %_false1 ], [ true, %_true0 ]

  ;IF STATEMENT
  br i1 %tmp2, label %_then3, label %_endif4

  ;THEN STATEMENT
_then3:
  %tmp3 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp4 = call i32 (i8 *, ...)* @printf(i8 * %tmp3, i32 1)
  br label %_endif4

  ;END POINT
_endif4:
  %tmp5 = load i32 * %tmp0
  ret i32 %tmp5
}
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
