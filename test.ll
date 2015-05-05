@.formatting.string = private constant [4 x i8] c"%d\0A\00"
define i32 @main() {
entry:
  %tmp0 = alloca i32
  store i32 0, i32 * %tmp0
  %tmp1 = getelementptr [4 x i8] * @.formatting.string, i32 0, i32 0
  %tmp2 = call i32 (i8 *, ...)* @printf(i8 * %tmp1, i32 0)
  %tmp3 = load i32 * %tmp0
  ret i32 %tmp3
}
define i32 @i() {
  %tmp4 = getelementptr %class.a %this, i32 0, i32 1
  %tmp6 = mul i32 1, 10
  %tmp7 = call i8* @malloc ( i32 %tmp6)
  %tmp5 = bitcast i8* %tmp7 to i32 **
  store [0 x i32] %tmp4, [10 x i32] %tmp5
  %tmp9 = getelementptr %class.a %this, i32 0, i32 1
  %tmp8 = getelementptr [0 x i32] %tmp9, i32 2
  store i32 1, i32 * %tmp8
  ret i32 0
}
%class.a = type { [1 x i8 *], [0 x i32] }
declare i32 @printf (i8 *, ...)
declare i8 * @malloc (i32)
